import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    ROOM HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Room
{
    /*
        Describe Room:
            Describes a Room to the player.
        Notes:
            This is called when a Room is entered.
            On Objects, it calls the 'look' Event.
    */
    public List<String> DescribeRoom(Statement cmd, Element room)
    {
        String fireEv = "enter|init";
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Event ev = new Event();
        Object obj = new Object();
        Npc npc = new Npc();
        Element game = room.GetGame(room);

        // If the player has died, return nothing
        if (!game.GetIsPlaying()) return output;

        // Parse the Room's Events
        if (!room.GetLabel().equals(""))
        {
            output.add(room.GetLabel() + ":");
        }

        // Parse the Game's Events - so that global changes fire
        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(ev.Parse_OneLevel_Events(cmd, game, fireEv));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Parse the Room's Events - one level to avoid duplicates
        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(ev.Parse_OneLevel_Events(cmd, room, fireEv));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Parse the Room's Objects
        if (game.GetIsPlaying()) 
        {
            obj.LoadObjects(cmd, room);
            tmp = new ArrayList<String>();
            tmp.addAll(obj.Parse_Objects(cmd, room.GetObjects()));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Parse the Room's Object Events
        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(obj.Parse_Objects_Events(cmd, room.GetObjects(), "look"));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Parse the Room's Npc Events
        npc.LoadNpcs(cmd, game);
        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(ev.Parse_Npc_Events(cmd, game, fireEv));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Lastly Parse the Room's Connectors Events
        if (game.GetIsPlaying()) 
        {
            tmp = new ArrayList<String>();
            tmp.addAll(obj.Parse_Connectors_Events(cmd, room.GetObjects(), fireEv));

            if (Functions.ListHasData(tmp))
            {
                output.add("\\n");
                output.addAll(tmp);
            }
        }

        if (!game.GetIsPlaying()) output = new ArrayList<String>();

        return output;
    }

    public Element LoadRoom(Statement cmd, String guid)
    {
        Element output = null;
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Label, ";
            sql += "    FileName ";
            sql += "FROM ";
            sql += "    Room ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(guid) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = new Element(rs.getString(1), "room");
                output.SetLabel(rs.getString(2));
                output.SetFileName(rs.getString(3));
                break;
            }

            rs.close();

            // Load Attributes
            if (output != null)
            {
                Attribute attr = new Attribute();
                attr.LoadAttributes(cmd, output);

                Event evt = new Event();
                evt.LoadEvents(cmd, output);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return output;
    }

    public List<Element> GetRooms(Statement cmd, Element game, String filter)
    {
        List<Element> output = new ArrayList<Element>();
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Label, ";
            sql += "    FileName ";
            sql += "FROM ";
            sql += "    Room ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!filter.equals("")) sql += "  AND GUID LIKE '%" + Functions.Encode(filter) + "%' ";
            sql += "ORDER BY ";
            sql += "    Label ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                Element rm = new Element(rs.getString(1), "room");
                rm.SetLabel(rs.getString("Label"));
                rm.SetFileName(rs.getString("FileName"));
                rm.SetParent(game);
                output.add(rm);
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
