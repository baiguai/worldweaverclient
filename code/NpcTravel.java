import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    NPCTRAVEL HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class NpcTravel 
{
    public List<String> Parse_NpcTravels(Statement cmd, Element tset)
    {
        List<String> output = new ArrayList<String>();
        Attribute attribute = new Attribute();
        Npc npcClass = new Npc();
        LoadNpcTravels(cmd, tset);
        String repeatType = tset.GetRepeatType();
        int curIndex = GetNpcTravelSetIndex(cmd, tset);
        boolean goingForward = GetNpcTravelSetGoingForward(cmd, tset);
        int trvCount = tset.GetNpcTravels().size() - 1;
        Room room = new Room();
        Element npc = tset.GetSelf(tset);
        Element player = tset.GetPlayer(tset);
        Element game = tset.GetGame(tset);
        Element curTrv = null;
        Element newRoom = null;
        String newLoc = "";

        if (npc == null) return output;

        // Ensure the TravelSet is Active
        attribute.LoadAttributes(cmd, tset);
        Element actAtt = tset.GetAttribute("active");

        if (actAtt != null && !Functions.BooleanString(actAtt.GetValue())) return output;

        if (repeatType.equals("follow"))
        {
            if (player == null) return output;

            // Perform following
            newRoom = game.GetRoom();
            if (newRoom == null) return output;

            // npc.MoveElement(room.GetGuid(), npc);
            npc.SetLocation(newRoom.GetGuid());
            npc.SetParentGuid(newRoom.GetGuid());
            npcClass.SaveNpc(cmd, npc);
            return output;
        }

        if (repeatType.equals("random"))
        {
            int randStep = Functions.RandomInt(-3, 3);
            if (randStep < 0) randStep = -1;
            if (randStep > 0) randStep = 1;
            curIndex += randStep;
            if (curIndex > trvCount) curIndex = trvCount - 1;
            if (curIndex < 0) curIndex = 0;
            UpdateNpcTravelSetIndex(cmd, tset, curIndex, goingForward);
        }

        // Get the new location
        curTrv = tset.GetNpcTravels().get(curIndex);
        if (curTrv != null)
        {
            Element curRoom = game.GetRoom();
            newLoc = curTrv.GetLocation().trim();
            if (!newLoc.equals(""))
            {
                if (curRoom != null && newLoc.equals(curRoom.GetGuid()))
                {
                    newRoom = game.GetRoom();
                }
                else
                {
                    newRoom = room.LoadRoom(cmd, newLoc);
                }
                if (newRoom == null) return output;
                npc.SetParentGuid(newLoc);
                npc.SetLocation(newLoc);
                npcClass.SaveNpc(cmd, npc);
            }
        }

        // Update the current index
        if (!repeatType.equals("random"))
        {
            if (goingForward)
            {
                curIndex++;

                if (curIndex > trvCount)
                {
                    if (repeatType.equals("repeat"))
                    {
                        curIndex = 0;
                    }
                    else
                    {
                        curIndex = trvCount;
                        if (!repeatType.equals("repeat")) goingForward = false;
                    }
                }
            }
            else
            {
                curIndex--;

                if (curIndex < 0)
                {
                    if (repeatType.equals("repeat"))
                    {
                        curIndex = trvCount;
                    }
                    else
                    {
                        curIndex = 0;
                        if (!repeatType.equals("repeat")) goingForward = true;
                    }
                }
            }

            UpdateNpcTravelSetIndex(cmd, tset, curIndex, goingForward);
        }

        return output;
    }


    public void LoadNpcTravels(Statement cmd, Element parent)
    {
        Element trv = null;
        String sql = "";
        String guid = "";

        if (parent.HasNpcTravels()) return;

        int colNpcTravelSetGuid = 1;
        int colLocation = 2;
        int colSort = 3;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    NpcTravelSetGUID, ";
            sql += "    Location, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    NpcTravel ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND NpcTravelSetGUID = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                guid = Functions.GetGUID();
                trv = new Element(guid, "travelset");
                trv.SetParent(parent);
                trv.SetParentGuid(rs.getString(colNpcTravelSetGuid));
                trv.SetParentType("travelset");
                trv.SetLocation(rs.getString(colLocation));
                trv.SetSort(rs.getInt(colSort));
                parent.AppendNpcTravel(trv);
            }

            rs.close();

            // Load an empty to show that Messages have been loaded
            if (!parent.HasNpcTravels())
            {
                trv = new Element("", "travelset");
                parent.AppendNpcTravel(trv);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void UpdateNpcTravelSetIndex(Statement cmd, Element tset, int newIndex, boolean goingForward)
    {
        String sql = "";
        int goingFor = 0;

        if (goingForward) goingFor = 1;
        else goingFor = 0;

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "NpcTravelSetIndex ( ";
            sql += "    NpcTravelSetGUID, ";
            sql += "    NpcTravelSetIndex, ";
            sql += "    GoingForward ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(tset.GetGuid()) + "', ";
            sql += "    " + tset.GetCurrentIndex() + ", ";
            sql += "    1 ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) FROM NpcTravelSetIndex WHERE 1 = 1 ";
            sql += "        AND Deleted = 0 ";
            sql += "        AND NpcTravelSetGUID = '" + Functions.Encode(tset.GetGuid()) + "' ";
            sql += "    ) < 1 ";
            sql += ";";
            cmd.execute(sql);

            sql =  "";
            sql += "UPDATE ";
            sql += "    NpcTravelSetIndex ";
            sql += "SET ";
            sql += "    NpcTravelSetIndex = " + newIndex + ", ";
            sql += "    GoingForward = " + goingFor + " ";
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

    public Integer GetNpcTravelSetIndex(Statement cmd, Element tset)
    {
        Integer output = 0;
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    NpcTravelSetIndex ";
            sql += "FROM ";
            sql += "    NpcTravelSetIndex ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND NpcTravelSetGUID = '" + Functions.Encode(tset.GetGuid()) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = rs.getInt(1);
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

    public boolean GetNpcTravelSetGoingForward(Statement cmd, Element tset)
    {
        boolean output = true;
        String sql = "";
        int tmp = 0;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GoingForward ";
            sql += "FROM ";
            sql += "    NpcTravelSetIndex ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND NpcTravelSetGUID = '" + Functions.Encode(tset.GetGuid()) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                tmp = rs.getInt(1);
                if (tmp == 1) output = true;
                else output = false;
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

    public void CloneNpcTravels(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        String sFired = fired + "";
        String sql = "";

        LoadNpcTravels(cmd, oldParent);
        if (oldParent.HasNpcTravels())
        {
            for (Element oldTrv : oldParent.GetNpcTravels())
            {
                if (!oldTrv.GetGuid().equals(""))
                {
                    Element newTrv = new Element(Functions.GetGUID(), "travelset");
                    newTrv.SetParentGuid(newParent.GetGuid());
                    newTrv.SetParent(newParent);
                    newTrv.SetLocation(oldTrv.GetLocation().replace(oldParentGuid, newParentGuid));
                    newTrv.SetSort(oldTrv.GetSort());
                    newParent.AppendNpcTravel(newTrv);
                }
            }

            // Insert queries
            for (Element trv : newParent.GetNpcTravels())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(trv.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(trv.GetLocation()) + "', ";
                sql += "    " + trv.GetSort() + " ";
                queries.add(new Query("NpcTravel", sql, false));
            }
        }
    }

    public void InsertNpcTravels(Statement cmd, List<Element> travels)
    {
        String sql = "";
        String sqlTrv = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "NpcTravel ( ";
        sql += "    NpcTravelSetGUID, ";
        sql += "    Location, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element trv : travels)
        {
            if (!trv.GetLocation().equals(""))
            {
                if (!sqlTrv.equals("")) sqlTrv += " UNION ";
                sqlTrv += "SELECT ";
                sqlTrv += "     '" + Functions.Encode(trv.GetParentGuid()) + "', ";
                sqlTrv += "     '" + Functions.Encode(trv.GetLocation()) + "', ";
                sqlTrv += "     " + trv.GetSort() + " ";
            }
        }

        if (!sqlTrv.equals(""))
        {
            sql = sql + sqlTrv + ";";
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
