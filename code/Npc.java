import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    NPC HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Npc
{
    public void Parse_Travel(Statement cmd, Element parent)
    {
        Element game = parent.GetGame(parent);
        if (game == null) return;
        NpcTravelSet npcTravelSet = new NpcTravelSet();

        for (Element npc : game.GetNpcs())
        {
            npcTravelSet.Parse_NpcTravelSets(cmd, npc);
        }
    }


    public void MoveNpc(Statement cmd, String npcGuid, String newLocation)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Npc ";
            sql += "SET ";
            sql += "    Location = '" + Functions.Encode(newLocation) + "' ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(npcGuid) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SpawnNpc(Statement cmd, Element source, int fired, Element room)
    {
        Element game = room.GetGame(room);
        Attribute attribute = new Attribute();
        Event event = new Event();
        Object object = new Object();
        Command command = new Command();
        List<Query> queries = new ArrayList<Query>();
        Query query = new Query();

        // Add the heads
        query.AddHeads(queries);

        // Create the new Npc
        Element newNpc = new Element(Functions.GetGUID(), "npc");

        // Adjust the GUID
        newNpc.SetLocation(room.GetGuid());
        newNpc.SetParent(room);
        newNpc.SetLabel(source.GetLabel());
        newNpc.SetMeta(source.GetMeta());
        newNpc.SetInherit(source.GetInherit());

        InsertNpc(cmd, newNpc);

        attribute.CloneAttributes(cmd, queries, fired, source, newNpc);
        event.CloneEvents(cmd, queries, fired, source, newNpc);
        object.CloneObjects(cmd, queries, fired, source, newNpc);
        command.CloneCommands(cmd, queries, fired, source, newNpc);

        game.AppendNpc(newNpc);

        // Run the Queries
        query.RunQueries(cmd, queries);
    }

    public void InsertNpc(Statement cmd, Element npc)
    {
        String sql = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Npc ( ";
        sql += "    GUID, ";
        sql += "    Name, ";
        sql += "    Meta, ";
        sql += "    Location, ";
        sql += "    Inherit ";
        sql += ") ";
        sql += "SELECT ";
        sql += "    '" + Functions.Encode(npc.GetGuid()) + "', ";
        sql += "    '" + Functions.Encode(npc.GetLabel()) + "', ";
        sql += "    '" + Functions.Encode(npc.GetMeta()) + "', ";
        sql += "    '" + Functions.Encode(npc.GetLocation()) + "', ";
        sql += "    '" + Functions.Encode(npc.GetInherit()) + "' ";
        sql += ";";

        try
        {
            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SaveNpc(Statement cmd, Element npc)
    {
        String sql = "";

        sql =  "";
        sql += "UPDATE ";
        sql += "    Npc ";
        sql += "SET ";
        sql += "    Location = '" + Functions.Encode(npc.GetLocation()) + "' ";
        sql += "WHERE 1 = 1 ";
        sql += "    AND Deleted = 0 ";
        sql += "    AND GUID = '" + Functions.Encode(npc.GetGuid()) + "' ";
        sql += ";";

        try
        {
            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void LoadNpcs(Statement cmd, Element game)
    {
        Element npc = null;
        String sql = "";

        if (game.GetNpcs().size() > 0) return;

        int colGuid = 1;
        int colName = 2;
        int colMeta = 3;
        int colLocation = 4;
        int colInherit = 5;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Name, ";
            sql += "    Meta, ";
            sql += "    Location, ";
            sql += "    Inherit ";
            sql += "FROM ";
            sql += "    Npc ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "Order By ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                npc = new Element(rs.getString(colGuid), "npc");
                npc.SetParent(game);
                npc.SetLabel(rs.getString(colName));
                npc.SetMeta(rs.getString(colMeta));
                npc.SetLocation(rs.getString(colLocation));
                npc.SetInherit(rs.getString(colInherit));
                game.AppendNpc(npc);
            }

            // Create an empty attribute to signal a load has been performed
            if (game.GetNpcs().size() < 1)
            {
                npc = new Element("", "npc");
                game.AppendNpc(npc);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }
}
