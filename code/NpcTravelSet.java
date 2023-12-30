import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    NPCTRAVELSET HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class NpcTravelSet
{
    public List<String> Parse_NpcTravelSets(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        NpcTravel travel = new NpcTravel();

        LoadNpcTravelSets(cmd, parent);

        for (Element tset : parent.GetNpcTravelSets())
        {
            if (tset.GetGuid().equals("")) continue;

            tmp = new ArrayList<String>();
            tmp.addAll(travel.Parse_NpcTravels(cmd, tset));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        return output;
    }

    public void LoadNpcTravelSets(Statement cmd, Element parent)
    {
        String sql = "";
        Element tset = null;
        NpcTravel trv = new NpcTravel();

        if (parent.HasNpcTravelSets()) return;

        int colGuid = 1;
        int colNpcGuid = 2;
        int colWaitMinute = 3;
        int colMode = 4;
        int colGoingForward = 5;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    NpcGUID, ";
            sql += "    WaitMinute, ";
            sql += "    Mode, ";
            sql += "    GoingForward ";
            sql += "FROM ";
            sql += "    NpcTravelSet ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND NpcGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                tset = new Element(rs.getString(colGuid), "travelset");
                tset.SetParent(parent);
                tset.SetParentGuid(rs.getString(colNpcGuid));
                tset.SetWaitMinutes(rs.getInt(colWaitMinute));
                tset.SetRepeatType(rs.getString(colMode));
                tset.SetGoingForward(rs.getInt(colGoingForward) == 1);
                parent.AppendNpcTravelSet(tset);
            }

            rs.close();

            // Load an empty to show that MessageSets have been loaded
            if (!parent.HasNpcTravelSets())
            {
                tset = new Element("", "travelset");
                Element tmpTrv = new Element("", "travelset");
                tset.AppendNpcTravel(tmpTrv);
                parent.AppendNpcTravel(tset);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SaveNpcTravelSetIndex(Statement cmd, Element tset)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    NpcTravelSetIndex ";
            sql += "SET ";
            sql += "    NpcTravelSetIndex = " + tset.GetCurrentIndex() + ", ";
            sql += "    GoingForward = " + tset.GetGoingForward() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND NpcTravelSetGUID = '" + Functions.Encode(tset.GetGuid()) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneTravelSets(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        NpcTravel travel = new NpcTravel();
        String sFired = fired + "";
        String sql = "";

        LoadNpcTravelSets(cmd, oldParent);
        if (oldParent.HasNpcTravelSets())
        {
            for (Element oldTset : oldParent.GetNpcTravelSets())
            {
                if (!oldTset.GetGuid().equals(""))
                {
                    Element newTset = new Element(Functions.GetGUID(), "travelset");
                    newTset.SetParentGuid(newParent.GetGuid());
                    newTset.SetParent(newParent);
                    newTset.SetWaitMinutes(oldTset.GetWaitMinutes());
                    newTset.SetRepeatType(oldTset.GetRepeatType());
                    newTset.SetGoingForward(oldTset.GetGoingForward());
                    newParent.AppendNpcTravelSet(newTset);

                    travel.CloneNpcTravels(cmd, queries, fired, oldParentGuid, newParentGuid, oldTset, newTset);
                }
            }

            // Insert queries
            for (Element tset : newParent.GetNpcTravelSets())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(tset.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(tset.GetParentGuid()) + "', ";
                sql += "    " + tset.GetWaitMinutes() + ", ";
                sql += "    '" + Functions.Encode(tset.GetRepeatType()) + "', ";
                sql += "    " + tset.GetGoingForward() + " ";
                queries.add(new Query("NpcTravelSet", sql, false));
            }
        }
    }

    public void InsertNpcTravelSets(Statement cmd, List<Element> npcTravelSets)
    {
        String sql = "";
        String sqlTset = "";
        int goingFor = 0;

        sql =  "";
        sql += "INSERT INTO ";
        sql += "NpcTravelSet ( ";
        sql += "    GUID, ";
        sql += "    NpcGUID, ";
        sql += "    WaitMinute, ";
        sql += "    Mode, ";
        sql += "    GoingForward ";
        sql += ") ";

        for (Element tset : npcTravelSets)
        {
            if (!tset.GetGuid().equals(""))
            {
                if (tset.GetGoingForward()) goingFor = 1;
                else goingFor = 0;

                if (!sqlTset.equals("")) sqlTset += " UNION ";
                sqlTset += "SELECT ";
                sqlTset += "    '" + Functions.Encode(tset.GetGuid()) + "', ";
                sqlTset += "    '" + Functions.Encode(tset.GetParentGuid()) + "', ";
                sqlTset += "    " + tset.GetWaitMinutes() + ", ";
                sqlTset += "    '" + Functions.Encode(tset.GetRepeatType()) + "', ";
                sqlTset += "    " + goingFor + " ";
            }
        }

        if (!sqlTset.equals(""))
        {
            sql = sql + sqlTset + ";";
        }
        else sql = "";

        try
        {
            if (!sql.equals("")) cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }
}
