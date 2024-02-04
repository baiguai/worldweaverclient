import java.io.*;
import java.io.Console;
import java.lang.Object;
import java.sql.*;
import java.util.*;
import java.util.Scanner;
import javax.swing.*;
import java.awt.event.*;

/*
    parser
    ----------------------------------------------------------------------------
    The parse handles player input at the highest level. - Outside of the 
    current game's scope. It handles system wide commands.
    ----------------------------------------------------------------------------
*/
public class Parser
{
    /* Properties */
    //
        public String InitGame = "";
        public boolean IsConsole = false;

        private JFrame window = null;
        public JFrame GetWindow() { return window; }
        public void SetWindow(JFrame val) { window = val; }

        private JTextArea outputField = null;
        public JTextArea GetOutputField() { return outputField; }
        public void SetOutputField(JTextArea val) { outputField = val; }

        private JTextField inputField = null;
        public JTextField GetInputField() { return inputField; }
        public void SetInputField(JTextField val) { inputField = val; }

        private String suffix = "__in_prog";
        public String GetSuffix() { return suffix; }

        private boolean initialized = false;
        public boolean GetInitialized() { return initialized; }
        public void SetInitialized(boolean val) { initialized = val; }

        private List<String> games = Functions.ListFiles("Data/Games/", "");
        public List<String> GetGames() { return games; }
        public void SetGames(List<String> val) { games = val; }

        private Game game = null;
        public Game GetGame() { return game; }
        public void SetGame(Game val) { game = val; }
        public void ClearGame()
        {
            GetGame().DeleteGameDb();
            game = null;
        }

        private int histLimit = 100;
        private List<String> history = null;
        public List<String> GetHistory() { if (history == null) history = new ArrayList<String>(); return history; }
        public void SetHistory(List<String> val) { history = val; }
        public void AppendHistory(String val)
        {
            if (GetHistory().size() > histLimit)
                GetHistory().remove(GetHistory().size() - 1);
            GetHistory().add(0, val);
        }
        public void AppendHistory(List<String> val) { GetHistory().addAll(val); }
        public void ClearHistory() { history = null; }

        public List<String> testOutput = null;
        public List<String> GetTestOutput() { if (testOutput == null) testOutput = new ArrayList<String>(); return testOutput; }
        public void SetTestOutput(List<String> val) { testOutput = val; }
        public void AddTestOutput(String val) { GetTestOutput().add(val); }
        public void ClearTestOutput() { testOutput = null; }
    //


    // Main Listener
    //  Accepts the player's input and calls the CallListener
    //  sending it the input, and checking for an escapement.
    public void Listener()
    {
        String[] args = { "" };
        Listener(args);
    }
    // Args
    //  Currently args aren't used. In the future these may be utilized to
    //  extend the client
    public void Listener(String[] args)
    {
        String input = "";
        Scanner scan = new Scanner(System.in);
        String prompt = ">> ";
        // Check the config for an initial game value
        InitGame = Functions.GetSetting("Config/Global.config", "init_game", "").trim();
        String output = "";
        if (args != null && args.length > 0 && args[0].equals("console"))
        {
            IsConsole = true;
        }

        // Initialization
        //  Initialization needs to only happen once. It handles the initial
        //  game load or simply the default WW splash screen.
        if (!GetInitialized())
        {
            SetInitialized(true);

            // There is an initial game - load it.
            if (!InitGame.equals(""))
            {
                String command = "";

                // Splash Commands
                //  The splash commands come from the user's input on the
                //  branded splash screen. See UI class.
                output = UI.Branding(InitGame);

                if (GetOutputField() != null)
                {
                    GetOutputField().setText(output);
                }
                else
                {
                    Functions.OutputRaw(output);
                }
            }
            else
            {
                // Build Welcome
                if (GetOutputField() == null)
                {
                    Functions.Output(UI.HomeScreen());
                }
                else
                {
                    Functions.Output(GetOutputField(), UI.HomeScreen());
                }

                if (GetOutputField() == null)
                {
                    Functions.Output(ListGames());
                }
                else
                {
                    GetOutputField().setText(ListGames());
                }
            }
        }

        prompt =  ">> ";
        if (GetInputField() == null)
        {
            System.out.print(prompt);

            try
            {
                // Scan the player's input
                input = scan.nextLine();
                if (CallListener(input, false)) Listener();
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }
        else
        {
            GetInputField().addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        String[] argsIn = { GetInputField().getText() };

                        if (CallListener(GetInputField().getText(), false))
                        {
                            GetInputField().setText("");
                        }
                    }
                }
            });
        }
    }

    // Listener Caller
    //  Calls the parser, sending in the input and returning
    //  a boolean, allowing for an escapement.
    public boolean CallListener(String userInput, boolean testing)
    {
        boolean call = true;
        boolean useGui = false;
        if (userInput.equals("")) return call;
        Input input = new Input(userInput);
        Response res = new Response();
        boolean newGame = false;
        boolean doTrim = true;

        if (GetOutputField() != null) useGui = true;

        // Exit
        /*
            Kills the game client, does not perform any
            state saving etc - ideally that is done as the
            player issues commands or timed events fire.
        */
        if (!res.HasResult() && Functions.StrictMatch(input.GetUserInput(), "exit"))
        {
            res.SetResult(res._success);
            call = false;

            if (GetWindow() != null)
            {
                GetWindow().setVisible(false); //you can't see me!
                GetWindow().dispose(); //Destroy the JFrame object
                System.exit(0);
            }
        }

        // List Games
        /*
            Lists any games that are in the {root}/Data/Games
            directory. - Currently no validation is performed
            on the databases.
        */
        if (!res.HasResult() && input.CmdMatch("listgames|games"))
        {
            res.SetResult(res._success);
            res.GetOutput().add(ListGames());
            doTrim = false;
        }


        // Selecting a Game to play
        if (!res.HasResult() &&
            GetGame() == null)
        {
            String gameDb = "";
            boolean initHandled = false;

            if (!initHandled && !InitGame.equals("") && Functions.StrictMatch(input.GetUserInput(), "play"))
            {
                initHandled = true;
                newGame = true;
                gameDb = InitGame;

                try
                {
                    Functions.CopyFile("Data/Games/" + gameDb, "Data/Games/" + gameDb + GetSuffix());
                }
                catch (Exception ex) {}

                gameDb = gameDb + GetSuffix();
            }

            if (!initHandled && !InitGame.equals("") && Functions.StrictMatch(input.GetUserInput(), "resume"))
            {
                initHandled = true;
                newGame = false;
                gameDb = InitGame + GetSuffix();
            }

            if (!initHandled)
            {
                try
                {
                    int gameIx = Integer.parseInt(input.GetUserInput());
                    gameDb = GetGamesList().get(gameIx-1);

                    // If this is a new game, make an 'in progress' copy.
                    // When the game ends, delete the 'in progress' copy.
                    if (gameDb.indexOf(GetSuffix()) < 0)
                    {
                        newGame = true;
                        Functions.CopyFile("Data/Games/" + gameDb, "Data/Games/" + gameDb + GetSuffix());
                        gameDb = gameDb + GetSuffix();
                    }

                }
                catch(Exception ex) {}
            }

            if (!gameDb.equals(""))
            {
                SetGame(new Game(gameDb));
                GetGame().InitGame(true, true);

                if (GetGame().GetGame().GetPlayer().GetLabel().equals("New Player"))
                {
                    if (useGui && newGame)
                    {
                        // JFrame frame = new JFrame("");
                        String name = JOptionPane.showInputDialog(GetWindow(), "Enter your player's name");
                        GetGame().GetGame().GetPlayer().SetLabel(name);
                        GetGame().SavePlayer();
                    }
                    else
                    {
                        String newname = "";
                        Scanner scan = new Scanner(System.in);
                        System.out.print("Enter a player name >> ");
                        try
                        {
                            newname = scan.nextLine();
                            GetGame().GetGame().GetPlayer().SetLabel(newname);
                            GetGame().SavePlayer();
                            System.out.println("Player name set to: " + newname);
                        }
                        catch (Exception ex) { ex.printStackTrace(); }
                    }
                }

                if (newGame) res = GetGame().Parse_Game("init", useGui);
                else res = GetGame().Parse_Game("resume", useGui);
            }
            else
            {
                res.SetResult(res._success);
                res.SetOutput("Be sure to select a valid game number.");
            }
        }

        // Up Arrow
        if (!res.HasResult() && input.CmdMatch("[A"))
        {
            // input.SetMatch(true);
            // Display command history
        }



        // Unit Test
        if (!res.HasResult() && GetGame() != null)
        {
            if (input.CmdMatch("_unittest") && GetGame().GetGame().GetDebug())
            {
                Statement cmd = GetGame().GetGlobal().GetGameCmd();
                UnitTest t = new UnitTest(cmd, input.GetParamString());
                t.SetParser(this);
                t.RunTest(cmd);
                return call;
            }
        }



        // Game
        /*
            Pass the input on to the Game object.
        */
        if (GetGame() != null && !res.HasResult())
        {
            res = GetGame().Parse_Game(input.GetUserInput(), useGui);
            if (res.GetResult().equals(res._success))
            {
                if (!res.GetDoTrim()) doTrim = false;
                input.SetMatch(true);
            }
        }

        // Test
        if (!res.HasResult() && GetGame() != null)
        {
            if (input.CmdMatch("_test") && GetGame().GetGame().GetDebug())
            {
                boolean isRec = GetGame().GetRecording();
                Statement cmd = GetGame().GetGlobal().GetGameCmd();
                List<String> cmds = new ArrayList<String>();
                cmds = Parse_Test(cmd, input.GetParamString());

                for (String s : cmds)
                {
                    if (isRec) GetGame().SetRecording(false);
                    String[] cmdArr = s.split("\\n");
                    for (String c : cmdArr)
                    {
                        call = CallListener(c, false);
                    }
                    if (isRec) GetGame().SetRecording(true);
                }

                return call;
            }
            else
            {
                if (!GetGame().GetGame().GetIsPlaying())
                {
                    if (GetOutputField() == null)
                    {
                        Functions.Output(UI.HomeScreen());
                    }
                    else
                    {
                        Functions.Output(GetOutputField(), UI.HomeScreen());
                    }
                    ListGames();
                }
            }
        }



        // FINAL OUTPUT //

        int wrap = 80;
        String noOut = "";
        // Get the wrap_width setting from the Game
        if (GetGame() != null)
        {
            Attribute attribute = new Attribute();
            Element attr = attribute.GetAttribute(GetGame().GetGlobal().GetGameCmd(), GetGame().GetGame(), "wrap_width");
            if (attr != null)
            {
                try
                {
                    wrap = Integer.parseInt(attr.GetValue());
                }
                catch (Exception ex) {}
            }
        }

        // There is output - send it to the console.
        if (res.HasResult() && res.GetOutputSize() > 0)
        {
            Global_Data global = new Global_Data();

            // If there is a display_mode attribute 'clear', clear the console
            if (!testing)
            {
                if (GetGame() != null &&
                    GetGame().GetGame().GetAttribute("display_mode") != null &&
                    GetGame().GetGame().GetAttribute("display_mode").GetValue().equals("clear"))
                {
                    // Functions.ClearConsole();
                }
            }

            noOut = NoOutputLogic(res, userInput);

            if (!testing)
            {
                if (GetGame() != null)
                {
                    if (GetGame().GetGame().GetIsDead())
                    {
                        res.GetOutput().add(UI.GameOver());
                    }
                }

                if (!noOut.equals("")) res.AppendOutput(noOut);

                if (GetOutputField() == null)
                {
                    if (!doTrim)
                    {
                        Functions.Output(res.GetOutput());
                    }
                    else
                    {
                        Functions.OutputWrapped(res.GetOutput(), wrap);
                    }
                }
                else
                {
                    Functions.Output(GetOutputField(), res.GetOutputString(doTrim));
                }
            }
            else
            {
                SetTestOutput(res.GetOutput());
                AddTestOutput("");
                res.ClearOutput();
            }
        }
        if (!res.HasResult())
        {
            noOut = NoOutputLogic(res, userInput);
            if (!noOut.equals("")) res.AppendOutput(noOut);

            if (GetOutputField() == null)
            {
                Functions.OutputWrapped(res.GetOutput(), wrap);
            }
            else
            {
                Functions.Output(GetOutputField(), res.GetOutputString());
            }
        }

        // The game was started but has ended
        if (GetGame() != null && !GetGame().GetGame().GetIsPlaying())
        {
            ClearGame();
        }

        return call;
    }


    private String NoOutputLogic(Response res, String userInput)
    {
        String output = "";
        boolean outputFound = true;
        boolean handled = false;
        String test = "";

        if (!handled && res.GetOutput().size() < 1)
        {
            handled = true;
            outputFound = false;
        }

        if (!handled)
        {
            test = "";

            for (String s : res.GetOutput())
            {
                test += s;
            }

            if (test.trim().equals(""))
            {
                handled = true;
                outputFound = false;
            }
        }

        if (!outputFound)
        {
            handled = false;

            if (!handled && Functions.RegMatch("examine*|x*", userInput))
            {
                handled = true;
                output = "I'm not sure what you are trying to examine.";
            }

            if (!handled && Functions.RegMatch("take *|pick *up* ", userInput))
            {
                handled = true;
                output = "I'm not sure what you are trying to take.";
            }

            if (!handled && Functions.RegMatch("drop *|put * down*", userInput))
            {
                handled = true;
                output = "I'm not sure what you are trying to drop.";
            }

            if (!handled && Functions.RegMatch("use *", userInput))
            {
                handled = true;
                output = "I'm not sure what you are trying to use.";
            }

            if (!handled && GetGame() != null && GetGame().GetGame().GetIsPlaying())
            {
                handled = true;
                output = "I didn't understand that.";
            }
            if (!handled)
            {
                handled = true;
            }
        }

        return output;
    }


    // List Games
    // TODO: Validate the game DBs and alert player of any issues
    private String ListGames()
    {
        String output = "";
        int ix = 1;

        output += UI.HomeScreen();
        output += "\n";

        if (GetGames().size() < 1)
        {
            output += "\n\nTo make games available for play simply copy the game databases into\n<root>/Data/Games\n\n";
            return output;
        }

        output += "\n\nAvailable Games (Enter a number to select a game)\n";
        output += "New Games:\n";

        for (String f : GetGames())
        {
            if (f.indexOf(GetSuffix()) < 0)
            {
                output += ix + " - " + f + "\n";
                ix++;
            }
        }

        output += "\n\nResume Games:\n";

        for (String f : GetGames())
        {
            if (f.indexOf(GetSuffix()) >= 0)
            {
                output += ix + " - " + f + "\n";
                ix++;
            }
        }

        return output;
    }
    private List<String> GetGamesList()
    {
        List<String> output = new ArrayList<String>();

        if (GetGames().size() < 1)
        {
            return output;
        }

        for (String f : GetGames())
        {
            if (f.indexOf(GetSuffix()) < 0)
            {
                output.add(f);
            }
        }
        for (String f : GetGames())
        {
            if (f.indexOf(GetSuffix()) >= 0)
            {
                output.add(f);
            }
        }

        return output;
    }


    // Parse Test
    private List<String> Parse_Test(Statement cmd, String testName)
    {
        List<String> output = new ArrayList<String>();
        String sql = "";

        testName = Functions.Encode(testName);

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    Commands ";
            sql += "FROM ";
            sql += "    Test ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND TestName = '" + testName + "' ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output.add(rs.getString(1));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return output;
    }
}
