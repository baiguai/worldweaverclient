import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    EVENT HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Event
{
    public List<String> Parse_OneLevel_Events(Statement cmd, Element parent, String eventType)
    {
        return Parse_Events(cmd, parent, eventType, true);
    }
    public List<String> Parse_Events(Statement cmd, Element parent, String eventType)
    {
        return Parse_Events(cmd, parent, eventType, false);
    }

    public List<String> Parse_Events(Statement cmd, Element parent, String eventType, boolean oneLevel)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        if (parent == null) return output;
        Element game = parent.GetGame(parent);

        if (!game.GetIsPlaying()) return output;

        Element room = parent.GetRoom(parent);
        Element player = parent.GetPlayer(parent);
        Event event = new Event();
        ActionSet actionSet = new ActionSet();
        Action action = new Action();
        MessageSet mset = new MessageSet();
        Object obj = new Object();
        Npc npc = new Npc();
        LogicSet lset = new LogicSet();
        String[] arrTp = eventType.replace("|", "@@@").split("@@@");

        event.LoadEvents(cmd, parent);

        for (Element e : parent.GetEvents())
        {
            if (e.GetGuid().equals("")) continue;

            lset.LoadLogicSets(cmd, e);

            for (String s : arrTp)
            {
                if (Functions.StrictMatch(s, e.GetType()))
                {
                    Response resp = new Response();

                    resp = lset.Parse_LogicSets(cmd, e.GetLogicSets());
                    if (resp.GetOutput() != null)
                    {
                        if (Functions.ListHasData(resp.GetOutput()))
                        {
                            output.addAll(Functions.CleanList(resp.GetOutput()));
                        }
                    }

                    // Ensure the current output is meaningful
                    output = Functions.CleanList(output);

                    if (resp.GetResult().equals(resp._success))
                    {
                        // Parse the messagesets first
                        mset.LoadMessageSets(cmd, e);

                        tmp = new ArrayList<String>();
                        tmp.addAll(Functions.CleanList(mset.Parse_MessageSets(cmd, e)));

                        if (Functions.ListHasData(tmp))
                        {
                            output.addAll(tmp);
                        }

                        // Parse ActionSets
                        tmp = new ArrayList<String>();
                        tmp.addAll(Functions.CleanList(actionSet.Parse_ActionSets(cmd, null, e)));

                        if (Functions.ListHasData(tmp))
                        {
                            output.addAll(tmp);
                        }

                        // If travelling, break
                        for (Element aset : e.GetActionSets())
                        {
                            for (Element act : aset.GetActions())
                            {
                                if (act.GetType().equals("travel"))
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                // Add the room label
                if (parent.GetGuid().equals(room.GetGuid()) && s.equals("look"))
                {
                    if (!room.GetLabel().equals("") && output.size() > 0 && output.get(0).indexOf(room.GetLabel()) < 0)
                    {
                        output.add(0, room.GetLabel() + ":");
                    }
                }
            }
        }

        if (oneLevel)
        {
            return output;
        }

        // If the parent is the Game parse the room
        if (parent.GetElementType().equals("game"))
        {
            tmp = new ArrayList<String>();
            tmp.addAll(event.Parse_Events(cmd, room, eventType));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        // Parse the Objects
        obj.LoadObjects(cmd, parent);
        for (Element o : parent.GetObjects())
        {
            if (!o.GetGuid().equals("") && !o.GetType().equals("connector"))
            {
                tmp = new ArrayList<String>();
                tmp.addAll(event.Parse_OneLevel_Events(cmd, o, eventType));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }
            }
        }

        // Parse the Connectors
        for (Element o : parent.GetObjects())
        {
            if (!o.GetGuid().equals("") && o.GetType().equals("connector"))
            {
                tmp = new ArrayList<String>();
                tmp.addAll(event.Parse_Events(cmd, o, eventType));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }
            }
        }

        return output;
    }

    public List<String> Parse_Npc_Events(Statement cmd, Element game, String eventType)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Element room = game.GetRoom(game);
        Npc npc = new Npc();

        npc.LoadNpcs(cmd, game);

        for (Element n : game.GetRoomNpcs(room.GetGuid()))
        {
            tmp = new ArrayList<String>();
            tmp.addAll(Functions.CleanList(Parse_Events(cmd, n, eventType)));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        return output;
    }

    public List<String> Parse_Player_Events(Statement cmd, Element game, String eventType)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Element player = game.GetPlayer(game);

        tmp = new ArrayList<String>();
        tmp.addAll(Functions.CleanList(Parse_Events(cmd, player, eventType)));

        if (Functions.ListHasData(tmp))
        {
            output.addAll(tmp);
        }

        return output;
    }

    public void LoadEvents(Statement cmd, Element parent)
    {
        Element evt = null;
        String sql = "";

        if (parent.GetEvents().size() > 0) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colType = 4;
        int colInitialTime = 5;
        int colIntervalMinutes = 6;
        int colRepeatCount = 7;
        int colFileName = 8;
        int colSort = 9;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    InitialTime, ";
            sql += "    IntervalMinutes, ";
            sql += "    RepeatCount, ";
            sql += "    FileName, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Event ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!parent.GetGuid().equals("game")) sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                evt = new Element(rs.getString(colGuid), "event");
                evt.SetParent(parent);
                evt.SetType(rs.getString(colType));
                evt.SetParentGuid(rs.getString(colParentGuid));
                evt.SetParentType(rs.getString(colParentType));
                evt.SetInitialTime(rs.getInt(colInitialTime));
                evt.SetIntervalMinutes(rs.getInt(colIntervalMinutes));
                evt.SetRepeatCount(rs.getInt(colRepeatCount));
                evt.SetFileName(rs.getString(colFileName));
                evt.SetSort(rs.getInt(colSort));
                parent.AppendEvent(evt);
            }

            // Create an empty attribute to signal a load has been performed
            if (parent.GetEvents().size() < 1)
            {
                evt = new Element("", "event");
                parent.AppendEvent(evt);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SaveEvent(Statement cmd, Element event)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Event ";
            sql += "SET ";
            sql += "    InitialTime = " + event.GetInitialTime() + ", ";
            sql += "    IntervalMinutes = " + event.GetIntervalMinutes() + ", ";
            sql += "    RepeatCount = " + event.GetRepeatCount() + ", ";
            sql += "    Sort = " + event.GetSort() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(event.GetGuid()) + "' ";
            sql += "    AND ParentGuid = '" + Functions.Encode(event.GetParentGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(event.GetParentType()) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneEvents(Statement cmd, List<Query> queries, int fired, Element oldParent, Element newParent)
    {
        LogicSet logicSet = new LogicSet();
        ActionSet actionSet = new ActionSet();
        MessageSet messageSet = new MessageSet();
        String sFired = fired + "";
        String sql = "";

        LoadEvents(cmd, oldParent);
        if (oldParent.GetEvents().size() > 0)
        {
            for (Element oldEvt : oldParent.GetEvents())
            {
                if (!oldEvt.GetGuid().equals(""))
                {
                    Element newEvt = new Element(Functions.GetGUID(), "event");
                    newEvt.SetParentGuid(newParent.GetGuid());
                    newEvt.SetParent(newParent);
                    newEvt.SetParentType(oldEvt.GetParentType());
                    newEvt.SetType(oldEvt.GetType());
                    newEvt.SetInitialTime(oldEvt.GetInitialTime());
                    newEvt.SetIntervalMinutes(oldEvt.GetIntervalMinutes());
                    newEvt.SetRepeatCount(oldEvt.GetRepeatCount());
                    newEvt.SetSort(oldEvt.GetSort());
                    newParent.AppendEvent(newEvt);

                    logicSet.CloneLogicSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldEvt, newEvt);
                    actionSet.CloneActionSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldEvt, newEvt);
                    messageSet.CloneMessageSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldEvt, newEvt);
                }
            }

            // Insert the queries
            for (Element evt : newParent.GetEvents())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(evt.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(evt.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(evt.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(evt.GetType()) + "', ";
                sql += "    " + evt.GetInitialTime() + ", ";
                sql += "    " + evt.GetIntervalMinutes() + ", ";
                sql += "    " + evt.GetRepeatCount() + ", ";
                sql += "    " + evt.GetSort() + " ";
                queries.add(new Query("Event", sql, false));
            }
        }
    }

    public void InsertEvents(Statement cmd, List<Element> events)
    {
        String sql = "";
        String sqlEvent = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Event ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    Type, ";
        sql += "    InitialTime, ";
        sql += "    IntervalMinutes, ";
        sql += "    RepeatCount, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element evt : events)
        {
            if (!evt.GetGuid().equals(""))
            {
                if (!sqlEvent.equals("")) sqlEvent += " UNION ";
                sqlEvent += "SELECT ";
                sqlEvent += "   '" + Functions.Encode(evt.GetGuid()) + "', ";
                sqlEvent += "   '" + Functions.Encode(evt.GetParentGuid()) + "', ";
                sqlEvent += "   '" + Functions.Encode(evt.GetParentType()) + "', ";
                sqlEvent += "   '" + Functions.Encode(evt.GetType()) + "', ";
                sqlEvent += "   " + evt.GetInitialTime() + ", ";
                sqlEvent += "   " + evt.GetIntervalMinutes() + ", ";
                sqlEvent += "   " + evt.GetRepeatCount() + ", ";
                sqlEvent += "   " + evt.GetSort() + " ";
            }
        }

        if (!sqlEvent.equals(""))
        {
            sql = sql + sqlEvent + ";";
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
