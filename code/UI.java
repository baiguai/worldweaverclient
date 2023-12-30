import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;

/*
    UI
    ----------------------------------------------------------------------------
    Defines the 'UI' elements of the game.

    TODO: Move these into the Game DB
    ----------------------------------------------------------------------------
*/
public class UI
{
    public static String CalledCmd = "";

    public static String Branding(String game)
    {
        String output = game;
        Global global = new Global();
        global.OpenGameConn(game);
        String splash = "";
        String title = "";

        // Get the splash screen - if any
        String sql = "";
        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    Name, ";
            sql += "    Splash ";
            sql += "FROM ";
            sql += "    Game ";
            sql += ";";

            ResultSet rs = global.GetGameCmd().executeQuery(sql);

            while (rs.next())
            {
                splash = rs.getString("Splash") + "";
                title = rs.getString("Name");
                break;
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (splash.equals(""))
        {
            splash = title + "\n\n\nUse one of the following commands:\n\nplay\n\nresume\n\nexit\n\n\n";
        }

        return splash;
    }

    public static String Credits(Statement cmd)
    {
        String output = "";

        // Get the splash screen - if any
        String sql = "";
        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    Credits ";
            sql += "FROM ";
            sql += "    Game ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = rs.getString("Credits") + "";
                break;
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            // Functions.Output("ERROR!\n" + sql);
            return "ERROR!\n" + sql;
        }

        return output;
    }

    // Fight input loop
    public static void BrandListener()
    {
        String input = "";
        Scanner scan = new Scanner(System.in);
        String prompt = ">> ";

        System.out.print(prompt);

        try
        {
            input = scan.nextLine();
            if (CallBrandListener(input)) BrandListener();
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }
    public static boolean CallBrandListener(String userInput)
    {
        String output = "";
        Input input = new Input(userInput);
        boolean call = true;

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "play"))
        {
            CalledCmd = "play";
            call = false;
        }

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "resume"))
        {
            CalledCmd = "resume";
            call = false;
        }

        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "exit"))
        {
            CalledCmd = "exit";
            call = false;
        }

        return call;
    }

    public static String GameOver()
    {
        String output = "";

        output += "\n\n\n   -- GAME OVER --\n\n";
        output += "games : list available games\n";
        output += "help : view help\n";
        output += "q : exit";
        output += "\n\n\n";

        return output;
    }

    public static String Kill()
    {
        String output = "";

        output += "\n\nYou have killed the opponent.\n\n";

        return output;
    }

    public static String GameWon()
    {
        String output = "";

        output += "\n\n\n -- YOU HAVE WON! --\n\n";
        output += "games : list available games\n";
        output += "help : view help\n";
        output += "q : exit";
        output += "\n\n\n";

        return output;
    }

    public static String HomeScreen()
    {
        String output = "";

        output += "                      __    __                                      \n";
        output += " _      ______  _____/ /___/ /  _      _____  ____ __   _____  _____\n";
        output += "| | /| / / __ \\/ ___/ / __  /  | | /| / / _ \\/ __ `/ | / / _ \\/ ___/\n";
        output += "| |/ |/ / /_/ / /  / / /_/ /   | |/ |/ /  __/ /_/ /| |/ /  __/ /    \n";
        output += "|__/|__/\\____/_/  /_/\\__,_/    |__/|__/\\___/\\__,_/ |___/\\___/_/     \n";
        output += "                                                                    \n";
        output += "\n\n";
        output += "                              Game Builder";
        output += "\n\n";
        output += "                          For help type:  help";
        output += "\n";
        output += "                            Quit type:  exit";

        return output;
    }

    public static String HomeScreenMin()
    {
        String output = "";

        output += "(Powered by World Weaver. For help type: --help. To quit type exit)\n\n";

        return output;
    }

    public static String StartFight()
    {
        String output = "";

        output += "\n\n   -- FIGHT --\n\n";

        return output;
    }


    public static List<String> Help()
    {
        List<String> output = new ArrayList<String>();

        output.add("World Weaver Help");
        output.add("");
        output.add("list games");
        output.add("  To list the available games use:");
        output.add("  games");
        output.add("");
        output.add("load");
        output.add("  To load a game use:");
        output.add("  load <game identifier (its database file name)>.");
        output.add("  Or select a number from the existing games menu.");
        output.add("");
        output.add("--help");
        output.add("  When a game is loaded use the --help command to view World Weaver system help.");
        output.add("");
        output.add("register");
        output.add("  When playing games in World Weaver, at least one player must be defined.");
        output.add("  This can be done whether a game is loaded or not using:");
        output.add("  register <name (one word)>, <display name>, <password (one word)>, <email>");

        return output;
    }
}
