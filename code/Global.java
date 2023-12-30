import java.io.File;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/*
    GLOBAL
    ----------------------------------------------------------------------------
    Holds anything that needs to be passed from class to class.
    The items here should be kept small etc.
    ----------------------------------------------------------------------------
*/

public class Global
{
    /* Properties */
    //
        private String _mode_init = "INIT";
        private String _mode_playing = "PLAYING";
        private String _mode_gameover = "GAMEOVER";

        private String gameMode = "";
        public String GetGameMode() { return gameMode; }
        public void SetGameMode(String val)
        {
            if (!val.equals(_mode_init) &&
                !val.equals(_mode_playing) &&
                !val.equals(_mode_gameover))
            {
                Functions.Error("Bad game mode: " + val);
                val = "INIT";
            }

            gameMode = val;
        }

        private String gameDbPath = "";
        public String GetGameDbPath() { return gameDbPath; }

        private Connection gameConn = null;
        public Connection GetGameConn() { return gameConn; }
        public void SetGameConn(Connection val) { gameConn = val; }

        private Statement gameCmd = null;
        public Statement GetGameCmd() { return gameCmd; }
        public void SetGameCmd(Statement val) { gameCmd = val; }

        private Global_Data data = null;
        public Global_Data GetData() { if (data == null) data = new Global_Data(); return data; }
        public void SetData(Global_Data val) { data = val; }
    //


    // GAME DB METHODS
    public void OpenGameConn(String dbPath)
    {
        dbPath = "Data/Games/" + dbPath;
        boolean isNew = false;

        gameDbPath = dbPath;

        try
        {
            File f = new File(dbPath);
            if (!(f.exists()) || (f.isDirectory()))
            {
                Functions.Error("The specified game does not exist");
                return;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        try
        {
            if (!isNew)
            {
                Class.forName("org.sqlite.JDBC");
                SetGameConn(DriverManager.getConnection("jdbc:sqlite:" + dbPath));
                SetGameCmd(GetGameConn().createStatement());
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
