import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    PLAYER HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Player
{
    public Element LoadPlayer(Statement cmd)
    {
        Element output = null;
        String sql = "";

        EnsurePlayerExists(cmd);

        int colGuid = 1;
        int colName = 2;
        int colLocation = 3;
        int colArmedWeapon = 4;
        int colPoints = 5;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Name, ";
            sql += "    Location, ";
            sql += "    ArmedWeapon, ";
            sql += "    Points ";
            sql += "FROM ";
            sql += "    Player ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = new Element(rs.getString(colGuid), "player");
                output.SetLabel(rs.getString(colName));
                output.SetLocation(rs.getString(colLocation));
                output.SetArmedWeapon(rs.getString(colArmedWeapon));
                output.SetPoints(rs.getInt(colPoints));
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

    public void EnsurePlayerExists(Statement cmd)
    {
        String sql = "";
        String guid = Functions.GetGUID();

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "Player ( ";
            sql += "    GUID, ";
            sql += "    Name, ";
            sql += "    Location ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(guid) + "', ";
            sql += "    'New Player', ";
            sql += "    'void' ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) ";
            sql += "        FROM ";
            sql += "            Player ";
            sql += "        WHERE 1 = 1 ";
            sql += "            AND Deleted = 0 ";
            sql += "    ) < 1 ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            // ex.printStackTrace();
            // Functions.Error(sql);
        }
    }

    public void SavePlayer(Statement cmd, Element player)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Player ";
            sql += "SET ";
            sql += "    GUID = '" + Functions.Encode(player.GetGuid()) + "', ";
            sql += "    Name = '" + Functions.Encode(player.GetLabel()) + "', ";
            sql += "    Location = '" + Functions.Encode(player.GetLocation()) + "', ";
            if (player.GetArmedWeapon() != null) sql += "    ArmedWeapon = '" + Functions.Encode(player.GetArmedWeapon()) + "', ";
            sql += "    Points = " + player.GetPoints() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }
}
