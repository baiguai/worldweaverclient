import java.io.File;
import java.io.*;
import java.util.*;

/*
    MAIN CLASS
    ----------------------------------------------------------------------------
    The primary class - creates the instance of the parser.

    ---

    Global.java > 351
    ----------------------------------------------------------------------------
*/
public class Main
{
    /* Properties */
    //
        private Parser parser;
        public Parser GetParser() { if (parser == null) parser = new Parser(); return parser; }
        private MainWindow win;
        public MainWindow GetWin() { if (win == null) win = new MainWindow(); return win; }
    //


    public static void main(String[] args)
    {
        // Ensure all the directories exist
        File f = null;
        f = new File("Config");
        if (!(f.exists()) && !(f.isDirectory()))
        {
            Functions.OutputRaw("Creating Config directory...");
            f.mkdir();

            // Create the config files
            Functions.OutputRaw("Creating the default config files...");
            List<String> fileContents = new ArrayList<String>();
            fileContents.add("verbose=true");
            Functions.WriteToFile("Config/Global.config", fileContents);
            fileContents = new ArrayList<String>();
            fileContents.add("line_width=80");
            Functions.WriteToFile("Config/Theme.config", fileContents);
        }
        f = new File("debug.log");
        if (f.exists() && !(f.isDirectory()))
        {
            f.delete();
        }
        Functions.Log("Beginning log...");
        f = new File("Data");
        if (!(f.exists()) && !(f.isDirectory()))
        {
            Functions.OutputRaw("Creating Data directory...");
            f.mkdir();
        }
        f = new File("Data/Games");
        if (!(f.exists()) && !(f.isDirectory()))
        {
            Functions.OutputRaw("Creating Data/Games directory...");
            f.mkdir();
        }

        Main m = new Main();

        if (args.length < 1)
        {
            m.GetWin().CreateWindow();
        }
        else
        {
            m.GetParser().Listener(args);
        }
    }
}
