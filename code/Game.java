import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    GAME
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Game
{
    /* Properties */
    //
        private Global global = null;
        public Global GetGlobal() { if (global == null) global = new Global(); return global; }
        public void SetGlobal(Global val) { global = val; }

        private Game_Data data = null;
        public Game_Data GetData() { if (data == null) data = new Game_Data(); return data; }
        public void SetData(Game_Data val) { data = val; }

        private String inputStep = "";
        public String GetInputStep() { return inputStep; }
        public void SetInputStep(String val) { inputStep = val; }

        private Element game = null;
        public Element GetGame() { return game; }
        public void SetGame(Element val) { game = val; }

        private boolean recording = false;
        public boolean GetRecording() { return recording; }
        public void SetRecording(boolean val) { recording = val; }

        private List<String> record = null;
        public List<String> GetRecord() { if (record == null) record = new ArrayList<String>(); return record; }
        public void SetRecord(List<String> val) { record = val; }
        public void AddRecord(String val) { GetRecord().add(val); }
        public void ClearRecord() { record = null; }
    //


    // Constructor
    public Game(String dbPath)
    {
        GetGlobal().OpenGameConn(dbPath);
    }


    // Parser
    public Response Parse_Game(String playerInput, boolean useGui)
    {
        Response res = new Response();
        Input input = new Input(playerInput);
        Command command = new Command();
        Admin admin = new Admin();
        Statement cmd = GetGlobal().GetGameCmd();
        Attribute attribute = new Attribute();
        Note note = new Note();
        Help help = new Help();
        Player pl = new Player();

        // Handle test record
        if (!res.HasResult() && GetRecording())
        {
            // Handle record stop
            if (!res.HasResult() && Functions.Match(input.GetUserInput(), "rec off"))
            {
                SetRecording(false);
                res.SetResult(res._success);

                res.SetDoTrim(false);
                String rec = "";
                rec += "{test, name=<TEST NAME>\n";
                rec += "    [@commands";
                for (String s : GetRecord())
                {
                    if (!rec.equals("")) rec += "\n";
                    rec += "        " + s;
                }
                rec += "\n    ]\n";
                rec += "}\n";

                res.SetOutput(rec);
            }
            else
            {
                AddRecord(playerInput);
            }
        }

        if (!res.HasResult() && input.CmdMatch("init"))
        {
            res.SetResult(res._success);
            res.SetOutput(InitGame(useGui));

            if (res.GetOutput().get(0).equals("SETNAME"))
            {
                res.SetCmdToRun("SETNAME");
                List<String> output = new ArrayList<String>();
                for (int i = 1; i < res.GetOutput().size(); i++)
                {
                    output.add(res.GetOutput().get(i));
                }
                res.SetOutput(output);
            }
        }

        if (!res.HasResult() && input.CmdMatch("resume"))
        {
            res.SetResult(res._success);
            res.SetOutput(ResumeGame());
        }

        // EVERYTHING BELOW ASSUMES A GAME HAS ALREADY BEEN LOADED

        if (GetGame() == null || !GetGame().GetIsPlaying()) return res;

        Element room = GetGame().GetRoom(GetGame());
        Element player = GetGame().GetPlayer(GetGame());

        attribute.LoadAttributes(cmd, player);

        // Parse the Admin tools
        if (!res.HasResult())
        {
            res.SetOutput(admin.Parse_Admin(cmd, GetGame(), input));
            if (res.GetOutput().size() > 0)
            {
                res.SetResult(res._success);

                if (admin.GetRecording())
                {
                    SetRecording(true);
                    ClearRecord();
                }
            }

            if (!GetGame().GetIsPlaying())
            {
                return res;
            }
        }

        // Notes
        if (!res.HasResult())
        {
            res.SetOutput(note.Parse_Notes(cmd, GetGame(), input));
            if (res.GetOutput().size() > 0)
            {
                res.SetResult(res._success);
            }

            if (!GetGame().GetIsPlaying())
            {
                return res;
            }
        }

        // Parse the Commands
        if (!res.HasResult())
        {
            res.SetOutput(command.Parse_Commands(cmd, GetGame(), input));
            if (res.GetOutput().size() > 0)
            {
                res.SetResult(res._success);
            }

            if (!GetGame().GetIsPlaying())
            {
                return res;
            }
        }

        // Parse the Game Object Commands
        if (!res.HasResult())
        {
            for (Element obj : GetGame().GetObjects())
            {
                res.SetOutput(command.Parse_Commands(cmd, obj, input));
                if (res.GetOutput().size() > 0)
                {
                    res.SetResult(res._success);
                }

                if (!GetGame().GetIsPlaying())
                {
                    return res;
                }
            }
        }

        /* COMMAND PARSING */

        // Parse the Room first
        if (!res.HasResult())
        {
            res.SetOutput(command.Parse_Commands(cmd, room, input));
            if (res.GetOutput().size() > 0)
            {
                res.SetResult(res._success);
            }
        }

        // Parse the Player next
        if (!res.HasResult())
        {
            res.SetOutput(command.Parse_Commands(cmd, player, input));
            if (res.GetOutput().size() > 0)
            {
                res.SetResult(res._success);
            }
        }

        // Parse the Room's NPCs
        if (!res.HasResult())
        {
            for (Element npc : GetGame().GetRoomNpcs(room.GetGuid()))
            {
                res.SetOutput(command.Parse_Commands(cmd, npc, input));
                if (res.GetOutput().size() > 0)
                {
                    res.SetResult(res._success);
                }
                if (res.HasResult()) break;
            }
        }

        // Help
        if (!res.HasResult())
        {
            if (input.GetInputCommand().equals("help") || input.GetInputCommand().equals("--help"))
            {
                res.SetOutput(help.Parse_Help(cmd, GetGame(), input));
                if (res.GetOutput().size() > 0)
                {
                    res.SetResult(res._success);
                }

                if (!GetGame().GetIsPlaying())
                {
                    return res;
                }
            }
        }

        // Credits
        if (!res.HasResult())
        {
            if (input.GetInputCommand().equals("credits"))
            {
                res.SetOutput(UI.Credits(cmd));
                if (res.GetOutput().size() > 0)
                {
                    res.SetResult(res._success);
                }

                if (!GetGame().GetIsPlaying())
                {
                    return res;
                }
            }
        }

        return res;
    }


    // Initialize the game
    private List<String> InitGame(boolean useGui)
    {
        return InitGame(true, useGui);
    }

    // Resume the game
    private List<String> ResumeGame()
    {
        return InitGame(false, false);
    }

    public List<String> InitGame(boolean newGame, boolean useGui)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Response res = new Response();
        SetGame(new Element("game", "game"));
        Player pl = new Player();
        Room rm = new Room();
        Event evt = new Event();
        Object object = new Object();
        Attribute attr = new Attribute();

        GetGame().SetIsPlaying(true);

        // Load the Player
        GetGame().SetPlayer(pl.LoadPlayer(GetGlobal().GetGameCmd()));
        attr.LoadAttributes(GetGlobal().GetGameCmd(), GetGame().GetPlayer());
        String plName = attr.GetAttributeValue(GetGlobal().GetGameCmd(), GetGame().GetPlayer(), "name");
        if (!plName.equals("")) GetGame().GetPlayer().SetLabel(plName);

        // Load the Events
        evt.LoadEvents(GetGlobal().GetGameCmd(), GetGame());

        // Load the Objects
        object.LoadObjects(GetGlobal().GetGameCmd(), GetGame());

        // This is a new game
        if (GetGame().GetPlayer().GetLocation().equals("void"))
        {
            // No player name was defined in the game
            if (plName.equals(""))
            {
                if (!useGui)
                {
                    SetInputStep("name");
                    Listener();
                }
                else
                {
                    output.add("SETNAME");
                }
            }

            GetGame().GetPlayer().SetLocation(GetData().GetInitLocation(GetGlobal().GetGameCmd()));
        }

        // Load the Room
        Element elRm = rm.LoadRoom(GetGlobal().GetGameCmd(), GetGame().GetPlayer().GetLocation());
        elRm.SetParent(GetGame());
        evt.LoadEvents(GetGlobal().GetGameCmd(), elRm);
        GetGame().SetRoom(elRm);

        GetGame().GetPlayer().SetParent(elRm);
        SavePlayer();

        // Display the game_init event
        if (newGame)
        {
            tmp = new ArrayList<String>();
            tmp.addAll(evt.Parse_Events(GetGlobal().GetGameCmd(), GetGame(), "game_init"));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Describe the Room
        tmp = new ArrayList<String>();
        tmp.addAll(rm.DescribeRoom(GetGlobal().GetGameCmd(), GetGame().GetRoom()));

        if (Functions.ListHasData(tmp))
        {
            output.addAll(tmp);
        }

        // Parse the Objects

        return output;
    }

    // Delete the game db
    public void DeleteGameDb()
    {
        File f = new File(GetGlobal().GetGameDbPath());
        f.delete();
    }

    // Save the Player
    public void SavePlayer()
    {
        Player pl = new Player();
        pl.SavePlayer(GetGlobal().GetGameCmd(), GetGame().GetPlayer());
    }



    // Player input loop
    public void Listener()
    {
        String input = "";
        Scanner scan = new Scanner(System.in);
        String prompt = ">> ";

        switch (GetInputStep())
        {
            case "name":
                prompt = "Enter a player name >> ";
                break;
        }

        System.out.print(prompt);

        try
        {
            input = scan.nextLine();
            if (CallListener(input)) Listener();
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }
    public boolean CallListener(String userInput)
    {
        Input input = new Input(userInput);
        Response res = new Response();
        boolean call = true;

        if (GetInputStep().equals("name"))
        {
            if (!res.HasResult())
            {
                res.SetResult(res._success);
                GetGame().GetPlayer().SetLabel(input.GetUserInput());
                Functions.OutputWrapped("Player name set to: " + GetGame().GetPlayer().GetLabel());
                call = false;
            }
        }

        return call;
    }
}
