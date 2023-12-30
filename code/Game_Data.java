import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    GAME DATA
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Game_Data
{
    public String GetInitLocation(Statement cmd)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    InitialLocation ";
            sql += "FROM ";
            sql += "    Game ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = rs.getString(1);
                break;
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
