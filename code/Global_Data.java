import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    GLOBAL DATA
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Global_Data
{
    // Get the Attribute value directly from the database
    public String GetAttributeValue(Statement cmd, String attributePath)
    {
        String output = "";
        String sql = "";
        String sourceElem = "";
        String sourceAttr = "";
        String parentType = "";
        String type = "";

        try
        {
            // Split the Attribute path and use its specified parts
            String[] arr = attributePath.replace(":", "@@@").split("@@@", 2);

            if (arr.length == 2)
            {
                sourceElem = arr[0].trim();
                sourceAttr = arr[1].trim();
            }

            if (sourceElem.equals("{player}"))
            {
                sourceElem = "";
                parentType = "player";
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        try
        {
            // Get the Attribute value directly from the database,
            // in case the Element isn't currently loaded
            sql =  "";
            sql += "SELECT ";
            sql += "    Value, ";
            sql += "    Type ";
            sql += "FROM ";
            sql += "    Attribute ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!sourceElem.equals("")) sql += "    AND ParentGUID = '" + Functions.Encode(sourceElem) + "' ";
            if (!parentType.equals("")) sql += "    AND ParentType = '" + Functions.Encode(parentType) + "' ";
            sql += "    AND GUID = '" + Functions.Encode(sourceAttr) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = rs.getString(1);
                type = rs.getString(2);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (type.equals("random"))
        {
            output = FixRandom(output);
        }

        return output;
    }

    // Get the specified Object directly from the database
    // in case it isn't currently loaded
    public String GetObjectLocation(Statement cmd, String guid)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    ParentGUID ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(guid) + "' ";
            // sql += "    AND ParentType = 'room' ";
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

    // Get the specified Npc directly from the database
    // in case it isn't currently loaded
    public String GetNpcLocation(Statement cmd, String guid)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    Location ";
            sql += "FROM ";
            sql += "    Npc ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(guid) + "' ";
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

    // Get the player's location directly from the database
    public String GetPlayerLocation(Statement cmd, String guid)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    Location ";
            sql += "FROM ";
            sql += "    Player ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(guid) + "' ";
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

    // Get the stats aliases from the database
    public String GetStatsAliases(Statement cmd)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    StatsGUIDS ";
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

    // Updates the Attribute in the database, it belongs to an Element not
    // currently loaded
    public void UpdateAttribute(Statement cmd, Element parent, String attributePath, String newValue)
    {
        String sql = "";
        String sourceElem = "";
        String sourceAttr = "";
        String parentType = "";

        try
        {
            // Split the Attribute path, and use the specified parts
            String[] arr = attributePath.replace(":", "@@@").split("@@@", 2);

            if (arr.length == 2)
            {
                sourceElem = arr[0].trim();
                sourceAttr = arr[1].trim();
            }

            // The source is the player - get the Attribute by type
            if (sourceElem.equals("{player}"))
            {
                sourceElem = "";
                parentType = "player";
            }
            // The source is the current room, get its Attributes
            if (sourceElem.equals("{room}"))
            {
                Element room = parent.GetRoom(parent);
                if (room != null)
                {
                    sourceElem = room.GetGuid();
                }
            }

            try
            {
                // Update the database directly
                sql =  "";
                sql += "UPDATE ";
                sql += "    Attribute ";
                sql += "SET ";
                sql += "    Value = '" + Functions.Encode(newValue) + "' ";
                sql += "WHERE 1 = 1 ";
                sql += "    AND Deleted = 0 ";
                if (parentType.equals("")) sql += "    AND ParentGUID = '" + Functions.Encode(sourceElem) + "' ";
                if (!parentType.equals("")) sql += "    AND ParentType = '" + Functions.Encode(parentType) + "' ";
                sql += "    AND GUID = '" + Functions.Encode(sourceAttr) + "' ";
                sql += ";";

                cmd.execute(sql);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                Functions.Error(sql);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public String FixRandom(String rawString)
    {
        int min = 0;
        int max = 100;

        try
        {
            if (!rawString.equals(""))
            {
                if (rawString.indexOf(",") >= 0)
                {
                    String[] arr = rawString.split(",", 2);
                    if (arr.length < 2) max = Integer.parseInt(arr[0].trim());
                    else
                    {
                        min = Integer.parseInt(arr[0].trim());
                        max = Integer.parseInt(arr[1].trim());
                    }
                }
            }
            rawString = Functions.RandomInt(min, max) + "";
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return rawString;
    }
}
