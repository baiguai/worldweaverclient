import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    FIGHT HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Fight
{
    /* PROPERTIES */
    //
        private boolean initialAttack = true;
        private int PlayerLife = -1;
        private int OppLife = -1;

        private List<String> output = null;
        public List<String> GetOutput() { if (output == null) output = new ArrayList<String>(); return output; }
        public void SetOutput(List<String> val) { output = val; }
        public void AddOutput(String val) { GetOutput().add(val); }
        public boolean HasOutput() { return GetOutput().size() > 0; }
        public void ClearOutput() { output = null; }

        // These elements are used multiple times - only search for them once.
        private Element opponent = null;
        public Element GetOpponent() { return opponent; }
        public void SetOpponent(Element val) { opponent = val; }

        private Element player = null;
        public Element GetPlayer() { return player; }
        public void SetPlayer(Element val) { player = val; }
        // ----
    //

    public List<String> Parse_Fight(Statement cmd, Element parent)
    {
        initialAttack = true;
        Element opTmp = parent.GetNpc(parent);
        Element plTmp = parent.GetPlayer(parent);

        if (opTmp != null) SetOpponent(opTmp);
        else return GetOutput();

        if (plTmp != null) SetPlayer(plTmp);
        else return GetOutput();

        Global_Data global = new Global_Data();
        // If there is a display_mode attribute 'clear', clear the console
        if (global.GetAttributeValue(cmd, "{game}:display_mode").equals("clear"))
        {
            Functions.ClearConsole();
        }

        // Add messages to the output
        Listener(cmd);

        Functions.Output(GetOutput());

        return GetOutput();
    }


    // Fight input loop
    public void Listener(Statement cmd)
    {
        String input = "";
        Attribute attribute = new Attribute();
        Event event = new Event();
        Scanner scan = new Scanner(System.in);
        String prompt = "";
        Element game = player.GetGame(player);
        Object object = new Object();
        Element playerWeapon = object.GetArmedWeapon(cmd, GetPlayer());

        attribute.LoadAttributes(cmd, GetOpponent());
        attribute.LoadAttributes(cmd, GetPlayer());
        event.LoadEvents(cmd, GetOpponent());

        try
        {
            if (PlayerLife < 0) PlayerLife = Integer.parseInt(GetPlayer().GetAttributeValue("life"));
            if (OppLife < 0) OppLife = Integer.parseInt(GetOpponent().GetAttributeValue("life"));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        if (PlayerLife < 1 || OppLife < 1)
        {
            return;
        }

        if (initialAttack)
        {
            List<String> tmp = new ArrayList<String>();
            tmp.addAll(GetFightMessage(cmd, playerWeapon, false, "attack"));
            Functions.Output(tmp);
            initialAttack = false;
        }

        if (PlayerLife > 0 && OppLife > 0)
        {
            if (HasOutput())
            {
                Functions.Output(GetOutput());
                ClearOutput();
            }

            Functions.Output("1 - attack or 2 - flee\n\n");

            System.out.print("pl: " + PlayerLife + ", en: " + OppLife + ">> ");

            try
            {
                input = scan.nextLine();
                if (CallListener(cmd, input)) Listener(cmd);
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }
    public boolean CallListener(Statement cmd, String userInput)
    {
        Input input = new Input(userInput);
        Element game = GetPlayer().GetGame(GetPlayer());
        Object object = new Object();
        Element playerWeapon = object.GetArmedWeapon(cmd, GetPlayer());
        Response res = new Response();
        boolean call = true;

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "quit|exit"))
        {
            System.exit(0);
        }

        if (game.GetDebug())
        {
            if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_kill"))
            {
                input.SetMatch(true);
                Attribute attribute = new Attribute();
                Element att = attribute.GetAttribute(cmd, GetOpponent(), "life");
                OppLife = 0;
                att.SetValue("0");
                attribute.SaveAttribute(cmd, att);
                input.AppendOutput(UI.Kill());
                call = false;
            }

            if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_die"))
            {
                input.SetMatch(true);
                Attribute attribute = new Attribute();
                Element att = attribute.GetAttribute(cmd, GetPlayer(), "life");
                att.SetValue("0");
                PlayerLife = 0;
                attribute.SaveAttribute(cmd, att);
                input.AppendOutput(UI.GameOver());
                call = false;
            }

            if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_enemy"))
            {
                input.AppendOutput(GetEnemyDetails(cmd));
            }
        }

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "attack|1"))
        {
            input.SetMatch(true);
            input.AppendOutput(DoAttack(cmd));

            // Do die check
            if (PlayerLife == 0)
            {
                call = false;
                game.SetIsPlaying(false);
                input.SetOutput(UI.GameOver());
                return call;
            }

            if (OppLife == 0)
            {
                call = false;
                SetOutput(GetFightMessage(cmd, playerWeapon, true, "kill"));
                return call;
            }
        }

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "flee|2"))
        {
            input.SetMatch(true);
            input.AppendOutput(DoAttack(cmd, true));

            // Do die check
            if (DoDieCheck())
            {
                call = false;
                game.SetIsPlaying(false);
                input.SetOutput(UI.GameOver());
                return call;
            }

            // Do kill check
            if (DoKillCheck())
            {
                call = false;
                SetOutput(GetFightMessage(cmd, playerWeapon, true, "kill"));
                return call;
            }

            call = false;
        }

        if (input.HasOutput())
        {
            SetOutput(input.GetOutput());
        }

        return call;
    }



    private List<String> DoAttack(Statement cmd)
    {
        return DoAttack(cmd, false);
    }

    private List<String> DoAttack(Statement cmd, boolean enemyAttacking)
    {
        List<String> output = new ArrayList<String>();
        Element game = player.GetGame(GetPlayer());
        Object object = new Object();
        Attribute attribute = new Attribute();
        Element playerWeapon = object.GetArmedWeapon(cmd, GetPlayer());
        Element opponentWeapon = object.GetArmedWeapon(cmd, GetOpponent());
        int playerInit = 80; // Initiative change defaults to 80/100
        int opponentInit = 20; // Initiative change defaults to 20/100
        int plInitCheck = 0;
        int opInitCheck = 0;
        String tmp = "";

        tmp = attribute.GetAttributeValue(cmd, GetPlayer(), "initiative");
        if (!tmp.equals(""))
        {
            try
            {
                playerInit = Integer.parseInt(tmp);
            }
            catch (Exception ex)
            {
                playerInit = 80;
            }
        }
        tmp = attribute.GetAttributeValue(cmd, GetOpponent(), "initiative");
        if (!tmp.equals(""))
        {
            try
            {
                opponentInit = Integer.parseInt(tmp);
            }
            catch (Exception ex)
            {
                opponentInit = 20;
            }
        }

        if (playerWeapon == null || opponentWeapon == null) return output;

        if (!enemyAttacking)
        {
            // Get the initiative
            tmp = attribute.GetAttributeValue(cmd, opponentWeapon, "speed");
            if (!tmp.equals(""))
            {
                try
                {
                    opponentInit = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    opponentInit = 20;
                }
            }
            tmp = attribute.GetAttributeValue(cmd, playerWeapon, "speed");
            if (!tmp.equals(""))
            {
                try
                {
                    playerInit = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    playerInit = 80;
                }
            }

            plInitCheck = Functions.RandomInt(playerInit, 100);
            opInitCheck = Functions.RandomInt(opponentInit, 100);
        }
        else
        {
            plInitCheck = 0;
            opInitCheck = 100;
        }

        if (plInitCheck > opInitCheck)
        {
            // Player Attacks
            output.addAll(Functions.CleanList(DoPlayerAttack(cmd, opponentWeapon, playerWeapon)));
        }
        else
        {
            // Opponent Attacks
            output.addAll(Functions.CleanList(DoOpponentAttack(cmd, opponentWeapon, playerWeapon)));
        }

        if (DoKillCheck())
        {
            output.addAll(GetFightMessage(cmd, playerWeapon, true, "kill"));
        }

        if (DoDieCheck())
        {
            game.SetIsPlaying(false);
            output.add(UI.GameOver());
        }

        return output;
    }

    private boolean DoKillCheck()
    {
        boolean output = false;

        if (OppLife < 0) OppLife = 0;

        if (OppLife < 1) output = true;

        return output;
    }
    private boolean DoDieCheck()
    {
        boolean output = false;

        if (PlayerLife < 0) PlayerLife = 0;

        if (PlayerLife < 1) output = true;

        return output;
    }

    private List<String> DoPlayerAttack(Statement cmd, Element opponentWeapon, Element playerWeapon)
    {
        List<String> output = new ArrayList<String>();
        Attribute attribute = new Attribute();
        Element game = player.GetGame(GetPlayer());
        int accuracy = 0; // Default accuracy to 50/50
        int minCheck = 50; // Default min accuracy check to 50/50
        int damage = 2; // Default damage to 2
        int oppLife = 0; // Default the opponent's life to 0
        String tmp = "";

        tmp = attribute.GetAttributeValue(cmd, playerWeapon, "accuracy");
        if (!tmp.equals(""))
        {
            try
            {
                accuracy = Integer.parseInt(tmp);
            }
            catch (Exception ex)
            {
                accuracy = 0;
            }
        }

        if (Functions.RandomInt(accuracy, 100) > minCheck)
        {
            // Do the attack
            tmp = attribute.GetAttributeValue(cmd, playerWeapon, "damage");

            if (!tmp.equals(""))
            {
                try
                {
                    damage = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    damage = 2;
                }
            }

            tmp = attribute.GetAttributeValue(cmd, GetOpponent(), "life");
            if (!tmp.equals(""))
            {
                try
                {
                    oppLife = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    oppLife = 0;
                }
            }

            // Do the damage
            oppLife = oppLife -= damage;

            if (oppLife < 0) oppLife = 0;

            Element att = attribute.GetAttribute(cmd, GetOpponent(), "life");

            att.SetValue(oppLife + "");
            attribute.SaveAttribute(cmd, att);
            GetOpponent().SetAttribute("life", oppLife + "");
            OppLife = oppLife;

            if (oppLife > 0)
            {
                output.addAll(GetFightMessage(cmd, playerWeapon, true, "attack"));
            }
            else
            {
                return output;
            }
        }
        else
        {
            output.addAll(GetFightMessage(cmd, playerWeapon, true, "miss"));
        }

        return output;
    }

    private List<String> DoOpponentAttack(Statement cmd, Element opponentWeapon, Element playerWeapon)
    {
        List<String> output = new ArrayList<String>();
        Attribute attribute = new Attribute();
        Element game = GetPlayer().GetGame(GetPlayer());
        int accuracy = 0; // Default accuracy to 50/50
        int minCheck = 50; // Default min accuracy check to 50/50
        int damage = 2; // Default damage to 2
        int playerLife = 0; // Default the player's life to 0
        String tmp = "";

        tmp = attribute.GetAttributeValue(cmd, opponentWeapon, "accuracy");
        if (!tmp.equals(""))
        {
            try
            {
                accuracy = Integer.parseInt(tmp);
            }
            catch (Exception ex)
            {
                accuracy = 0;
            }
        }

        if (Functions.RandomInt(accuracy, 100) > minCheck)
        {
            // Do the attack
            tmp = attribute.GetAttributeValue(cmd, opponentWeapon, "damage");
            if (!tmp.equals(""))
            {
                try
                {
                    damage = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    damage = 2;
                }
            }

            tmp = attribute.GetAttributeValue(cmd, GetPlayer(), "life");
            if (!tmp.equals(""))
            {
                try
                {
                    playerLife = Integer.parseInt(tmp);
                }
                catch (Exception ex)
                {
                    playerLife = 0;
                }
            }

            // Do the damage
            playerLife = playerLife -= damage;

            if (playerLife < 0) playerLife = 0;

            Element att = attribute.GetAttribute(cmd, GetPlayer(), "life");
            att.SetValue(playerLife + "");
            attribute.SaveAttribute(cmd, att);
            player.SetAttribute("life", playerLife + "");
            PlayerLife = playerLife;

            if (playerLife > 0)
            {
                output.addAll(GetFightMessage(cmd, playerWeapon, false, "attack"));
            }
            else
            {
                output.add("You have been killed.");
            }
        }
        else
        {
            output.addAll(GetFightMessage(cmd, playerWeapon, false, "miss"));
        }

        return output;
    }

    public List<String> GetEnemyDetails(Statement cmd)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Attribute attribute = new Attribute();
        Object object = new Object();
        Event event = new Event();
        Command command = new Command();

        output.add("Enemy " + GetOpponent().GetLabel() + ":");
        output.add("- Alias: " + GetOpponent().GetGuid());
        output.add("- Meta: " + GetOpponent().GetMeta());

        attribute.LoadAttributes(cmd, GetOpponent());
        if (GetOpponent().HasAttributes())
        {
            output.add("- Attributes:");
            for (Element attr : GetOpponent().GetAttributes())
            {
                output.add("   - " + attr.GetGuid() + ", " + attr.GetValue());
            }
        }

        event.LoadEvents(cmd, GetOpponent());
        if (GetOpponent().HasEvents())
        {
            tmp = new ArrayList<String>();
            for (Element evt : GetOpponent().GetEvents())
            {
                if (!tmp.contains("   - Type: " + evt.GetType()))
                {
                    tmp.add("   - Type: " + evt.GetType());
                }
            }
            if (tmp.size() > 0)
            {
                output.add("- Events:");
                for (String s : tmp)
                {
                    output.add(s);
                }
            }
        }

        object.LoadObjects(cmd, GetOpponent());
        if (GetOpponent().HasObjects())
        {
            output.add("- Objects: ");
            for (Element obj : GetOpponent().GetObjects())
            {
                output.add("   - " + obj.GetLabel() + " (" + obj.GetGuid() + ")");
            }
        }

        command.LoadCommands(cmd, GetOpponent());
        if (GetOpponent().HasCommands())
        {
            tmp = new ArrayList<String>();
            for (Element comm : GetOpponent().GetCommands())
            {
                if (!tmp.contains("   - Syntax: " + comm.GetSyntax()))
                {
                    tmp.add("   - Syntax: " + comm.GetSyntax());
                }
            }
            if (tmp.size() > 0)
            {
                output.add("- Commands:");
                for (String s : tmp)
                {
                    output.add(s);
                }
            }
        }

        output.add("");

        return output;
    }


    /* FIRE FIGHT EVENTS, GRABBING THEIR OUTPUT */
    public List<String> GetFightMessage(Statement cmd, Element playerWeapon, boolean playerAttacking, String eventType)
    {
        List<String> output = new ArrayList<String>();
        Event event = new Event();
        String msg = "";

        switch (eventType)
        {
            case "attack":
                if (playerAttacking)
                {
                    msg = "You have attacked enemy:fight_label.";
                }
                else
                {
                    msg = "enemy:fight_proper_label has attacked you.";
                }
                break;

            case "miss":
                if (playerAttacking)
                {
                    msg = "Your attack on enemy:fight_label has failed.";
                }
                else
                {
                    msg = "enemy:fight_proper_label's attack on you has failed.";
                }
                break;

            case "kill":
                if (playerAttacking)
                {
                    msg = "You have enemy:fight_kill enemy:fight_label.";
                }
                else
                {
                    msg = "You have been killed.";
                }
                break;
        }

        if (playerAttacking)
        {
            event.LoadEvents(cmd, playerWeapon);
            output = event.Parse_Events(cmd, playerWeapon, eventType, false);
            if (output.size() < 1)
            {
                output.add(msg);
            }
        }
        else
        {
            event.LoadEvents(cmd, GetOpponent());

            output = event.Parse_Events(cmd, GetOpponent(), eventType, false);

            if (output.size() < 1)
            {
                output.add(msg);
            }
        }

        output = FixFightMessage(cmd, output);

        return output;
    }

    public List<String> FixFightMessage(Statement cmd, List<String> input)
    {
        List<String> output = new ArrayList<String>();
        Attribute attr = new Attribute();
        attr.LoadAttributes(cmd, GetOpponent());

        for (String s : input)
        {
            s = s.replace("{enemy:fight_label}", attr.GetAttributeValue(cmd, GetOpponent(), "fight_label", "the opponent"));
            s = s.replace("{enemy:fight_proper_label}", attr.GetAttributeValue(cmd, GetOpponent(), "fight_proper_label", "The opponent"));
            s = s.replace("{enemy:fight_kill}", attr.GetAttributeValue(cmd, GetOpponent(), "fight_kill", "killed"));

            s = s.replace("enemy:fight_label", attr.GetAttributeValue(cmd, GetOpponent(), "fight_label", "the opponent"));
            s = s.replace("enemy:fight_proper_label", attr.GetAttributeValue(cmd, GetOpponent(), "fight_proper_label", "The opponent"));
            s = s.replace("enemy:fight_kill", attr.GetAttributeValue(cmd, GetOpponent(), "fight_kill", "killed"));
            output.add(s);
        }

        return output;
    }
}
