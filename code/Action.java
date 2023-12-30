import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/**
    Actions are elements that can be called by Commands or Events. It is Actions
    that are capable of making changes to the game's state.
*/
public class Action 
{
    /**
        Actions are parsed primarily according to their type property. This method
        can be called by Events or Commands.
    */
    public List<String> Parse_Actions(Statement cmd, Input input, int fired, Element parent)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        List<String> outputLogic = new ArrayList<String>();
        List<Element> acts = new ArrayList<Element>();
        LoadActions(cmd, parent);
        boolean parsed = true;
        Element game = parent.GetGame(parent);
        Element room = game.GetRoom(game);
        Element player = parent.GetPlayer(parent);

        if (!game.GetIsPlaying()) return output;

        Room rm = new Room();
        Event event = new Event();
        Object object = new Object();
        LogicSet lset = new LogicSet();
        MessageSet messageSet = new MessageSet();

        lset.LoadLogicSets(cmd, parent);

        // Parse LogicSets
        Response resp = new Response();
        resp = lset.Parse_LogicSets(cmd, parent.GetLogicSets());

        if (resp.GetResult().equals(resp._fail))
        {
            if (Functions.ListHasData(resp.GetOutput()))
            {
                output.addAll(resp.GetOutput());
            }
            return output;
        }

        for (Element act : parent.GetActions())
        {
            parsed = true;
            String val = "";

            if (act.GetGuid().equals("")) continue;


            // Process the LogicSet
            lset.LoadLogicSets(cmd, act);
            outputLogic = new ArrayList<String>();

            // Parse LogicSets
            resp = new Response();
            resp = lset.Parse_LogicSets(cmd, act.GetLogicSets());

            if (resp.GetResult().equals(resp._fail))
            {
                outputLogic.addAll(resp.GetOutput());
                if (Functions.ListHasData(outputLogic))
                {
                    output.addAll(outputLogic);
                }
                continue;
            }

            switch (act.GetType().trim())
            {
                // Display the player's armed weapon
                case "armed":
                    tmp = new ArrayList<String>();
                    tmp.addAll(GetArmedWeaponLabel(parent));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                // Arm - arm the player with the specified weapon
                case "arm":
                    String source = act.GetSource().trim();
                    tmp = new ArrayList<String>();

                    if (source.equals(""))
                    {
                        tmp.addAll(ArmWeapon(cmd, input.GetParamString(), parent));
                    }
                    else
                    {
                        tmp.addAll(ArmWeapon(cmd, source, parent));
                    }

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                // Attribute - changes an attribute
                case "attribute":
                    // Handle {input} replacement
                    if (input != null && act.GetNewValue().indexOf("{input}") >= 0)
                    {
                        act.SetNewValue(act.GetNewValue().replace("{input}", input.GetUserInput()));
                    }
                    UpdateAttribute(cmd, act, input);

                    tmp = new ArrayList<String>();
                    tmp.addAll(messageSet.Parse_MessageSets(cmd, act, input));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                case "die":
                    game.SetIsPlaying(false);
                    game.SetIsDead(true);

                    tmp = new ArrayList<String>();
                    tmp.addAll(messageSet.Parse_MessageSets(cmd, act, input));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    // output.add(UI.GameOver());

                    parsed = true;
                    break;

                case "event":
                    tmp = new ArrayList<String>();

                    tmp.addAll(event.Parse_Events(cmd, game, act.GetNewValue()));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    tmp = new ArrayList<String>();
                    tmp.addAll(event.Parse_Player_Events(cmd, game, act.GetNewValue()));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    tmp = new ArrayList<String>();
                    tmp.addAll(event.Parse_Npc_Events(cmd, game, act.GetNewValue()));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                case "fight":
                    Fight fight = new Fight();
                    game.SetFight(fight);
                    tmp = new ArrayList<String>();
                    tmp.addAll(fight.Parse_Fight(cmd, parent));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    if (!game.GetIsPlaying()) return new ArrayList<String>();
                    parsed = true;
                    break;

                case "inventory":
                    Element tmpSelf = act.GetSelf(act);
                    tmp = new ArrayList<String>();
                    tmp.addAll(tmpSelf.ListInventory(cmd, tmpSelf));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                // Wrapper for a messageset - and possibly logicset
                case "message":
                    if (act.GetType().trim() != "die")
                    {
                        tmp = new ArrayList<String>();
                        tmp.addAll(messageSet.Parse_MessageSets(cmd, act, input));

                        if (Functions.ListHasData(tmp))
                        {
                            output.addAll(tmp);
                        }
                    }

                    parsed = true;
                    break;

                case "move":
                    MoveObject(cmd, act);
                    parsed = false;
                    break;

                case "npc_move":
                    MoveNpc(cmd, act);
                    parsed = false;
                    break;

                case "object_event":
                    String tmpGuid = act.GetSource();
                    Element tmpObj = null;
                    boolean handled = false;

                    if (!handled && tmpGuid.equals(""))
                    {
                        handled = true;
                        tmpObj = act.GetSelf(act);
                    }

                    if (!handled)
                    {
                        if (tmpGuid.equals("{params}"))
                        {
                            handled = true;
                            tmpObj = act.GetObjectByMeta(input.GetParamString(), act);
                        }
                        else
                        {
                            if (tmpGuid.equals("{room}"))
                            {
                                tmpGuid = room.GetGuid();
                            }

                            handled = true;
                            tmpObj = act.GetElement(cmd, tmpGuid, act);
                        }
                    }

                    val = act.GetNewValue().trim();
                    if (val.equals("{params}"))
                    {
                        val = input.GetParamString();
                    }
                    tmp = new ArrayList<String>();
                    tmp.addAll(event.Parse_Events(cmd, tmpObj, val));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    // The Game element also listens to object_level events - but only on the Game level
                    tmp = new ArrayList<String>();
                    tmp.addAll(event.Parse_Events(cmd, game, val, true));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                case "quit":
                    game.SetIsPlaying(false);
                    parsed = true;
                    break;

                case "spawn":
                    fired = GetActionFired(cmd, act);
                    SpawnObject(cmd, fired, act);
                    parsed = false;
                    break;

                case "stats":
                    tmp = new ArrayList<String>();
                    tmp.addAll(GetStats(cmd, act));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                case "travel":
                    tmp = new ArrayList<String>();
                    tmp.addAll(MovePlayer(cmd, act.GetNewValue(), act));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    parsed = true;
                    break;

                case "travel_silent":
                    MovePlayer(cmd, act.GetNewValue(), act);
                    parsed = false;
                    break;
            }

            // Parse Action Messages
            if (!parsed)
            {
                acts.add(act);
            }
        }

        // Parse Action Messages
        for (Element a : acts)
        {
            tmp = new ArrayList<String>();
            tmp.addAll(messageSet.Parse_MessageSets(cmd, a));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        return output;
    }


    /**
        Arms the player with the specified weapon. The weapon is matched on the
        meta value.
    */
    public List<String> ArmWeapon(Statement cmd, String meta, Element parent)
    {
        List<String> output = new ArrayList<String>();
        Object object = new Object();
        Element player = parent.GetPlayer(parent);

        if (player != null)
        {
            object.LoadObjects(cmd, player);
            for (Element obj : player.GetWeapons())
            {
                if (Functions.RegMatch(obj.GetMeta(), meta))
                {
                    player.SetArmedWeapon(obj.GetGuid());
                    output.add(obj.GetLabel() + " is now your armed weapon.");
                    break;
                }
            }
        }

        return output;
    }

    /**
        Gets the label for the player's armed weapon.
    */
    public List<String> GetArmedWeaponLabel(Element parent)
    {
        List<String> output = new ArrayList<String>();
        Element player = parent.GetPlayer(parent);
        Element armed = player.GetElementObject(player.GetArmedWeapon(), player);

        if (armed != null)
        {
            output.add("Armed weapon: " + armed.GetLabel() + ".");
        }
        else
        {
            output.add("You are not armed with a weapon.");
        }

        return output;
    }

    /**
        Lists the Player's stats, using the stats_aliases defined in the
        game object to determine which stats to show.
    */
    public List<String> GetStats(Statement cmd, Element action)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Element player = action.GetPlayer(action);
        Global_Data global = new Global_Data();
        String aliases = global.GetStatsAliases(cmd);
        Attribute attribute = new Attribute();

        attribute.LoadAttributes(cmd, player);
        for (Element attr : player.GetAttributes())
        {
            if (Functions.StrictMatch(attr.GetGuid(), aliases))
            {
                tmp.add(attr.GetGuid() + " = " + attr.GetValue());
            }
        }

        if (tmp.size() > 0)
        {
            output.add("Player Stats:");
            output.addAll(tmp);
        }

        return output;
    }

    /**
        Updates the specified attribute.
        <br />
        If its parent isn't currently loaded, update the
        database directly
    */
    public void UpdateAttribute(Statement cmd, Element action, Input input)
    {
        if (action.GetSource().equals("")) return;
        Attribute attribute = new Attribute();
        Element player = action.GetPlayer(action);
        boolean handled = false;
        String tmpAttr = "";

        String[] arr = action.GetSource().replace(":", "@@@").split("@@@", 2);

        if (arr.length != 2) return;
        String parent = arr[0].trim();
        String attrib = arr[1].trim();
        String tmpVal = "";

        Element parentElem = action.GetElement(cmd, parent, action);

        // The Attribute belongs to an Element not currently loaded so use the
        // Global_Data class to update it
        if (parentElem == null)
        {

            handled = true;
            Global_Data global = new Global_Data();
            tmpAttr = action.GetNewValue();
            tmpAttr = action.FixAttributeValue(cmd, tmpAttr, action);
            tmpAttr = action.FixAttributeValue(tmpAttr, action);

            global.UpdateAttribute(cmd, action, action.GetSource(), tmpAttr);
        }

        if (!handled)
        {
            handled = true;
            attribute.LoadAttributes(cmd, parentElem);
            for (Element attr : parentElem.GetAttributes())
            {
                if (attr.GetGuid().equals(attrib))
                {
                    tmpVal = action.FixAttributeValue(attr.GetValue(), action);
                    if (tmpVal.equals("{params}"))
                    {
                        tmpVal = input.GetParamString();
                    }

                    attr.SetValue(tmpVal);
                    attribute.SaveAttribute(cmd, attr);
                }
            }
        }

        player.FixPlayerLife(cmd);
    }

    /**
        Moves the Object, specified in the Action, to the location, also
        specified in the Action.
    */
    public void MoveObject(Statement cmd, Element action)
    {
        Object object = new Object();
        Npc npc = new Npc();
        String source = action.GetSource().trim();
        if (source.equals(""))
        {
            Element self = action.GetSelf(action);
            if (self != null) source = self.GetGuid();
        }
        String objGuid = action.FixAliases(source, action);
        String newLoc = action.GetNewValue().trim();
        Element game = action.GetGame(action);
        Element room = action.GetRoom(action);
        Element player = action.GetPlayer(action);

        if (newLoc.equals("{room}")) newLoc = room.GetGuid();
        if (newLoc.equals("{player}")) newLoc = player.GetGuid();

        object.MoveObject(cmd, objGuid, newLoc);

        if (newLoc.equals("game"))
        {
            Element g = action.GetGame(action);
            g.ClearObjects();
            object.LoadObjects(cmd, g);
        }
        else
        {
            // Clear the Room Objects so they reload
            room.ClearObjects();
            object.LoadObjects(cmd, room);

            // Clear the Player Objects so they reload
            player.ClearObjects();
            object.LoadObjects(cmd, player);

            // Clear the Npc Objects so they reload
            npc.LoadNpcs(cmd, game);
            for (Element n : game.GetNpcs())
            {
                n.ClearObjects();
                object.LoadObjects(cmd, n);
            }
        }
    }

    /**
        Moves the Npc, specified in the Action, to the location, also
        specified in the Action.
    */
    public void MoveNpc(Statement cmd, Element action)
    {
        Npc npc = new Npc();
        String npcGuid = action.GetSource().trim();
        String newLoc = action.GetNewValue().trim();
        Element game = action.GetGame(action);
        Element room = action.GetRoom(action);

        if (newLoc.equals("{room}")) newLoc = room.GetGuid();

        npc.MoveNpc(cmd, npcGuid, newLoc);

        // Clear the Npc Objects so they reload
        npc.LoadNpcs(cmd, game);
        for (Element n : game.GetNpcs())
        {
            if (n.GetGuid().equals(npcGuid))
            {
                n.SetParentGuid(newLoc);
                n.SetLocation(newLoc);
                break;
            }
        }
    }

    /**
        Causes the Player to travel to the location that is passed in.
    */
    public List<String> MovePlayer(Statement cmd, String roomGuid, Element parent)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Room room = new Room();
        Event event = new Event();
        MessageSet messageSet = new MessageSet();
        Npc npc = new Npc();
        Player pl = new Player();
        Element game = parent.GetGame(parent);
        Element curRoom = game.GetRoom(game);
        Element player = parent.GetPlayer(parent);
        Element newRoom = new Element(roomGuid, "room");

        roomGuid = roomGuid.replace("{room}", curRoom.GetGuid());

        // Parse the exit Event
        tmp = new ArrayList<String>();
        tmp.addAll(event.Parse_Events(cmd, game, "exit"));

        if (Functions.ListHasData(tmp))
        {
            output.addAll(tmp);
        }

        newRoom = room.LoadRoom(cmd, roomGuid);
        if (newRoom == null) return output;
        newRoom.SetParent(game);
        game.ClearRoom();
        game.SetRoom(newRoom);

        // Parse the NPC travel
        npc.Parse_Travel(cmd, parent);

        player.SetParent(newRoom);
        player.SetLocation(newRoom.GetGuid());
        pl.SavePlayer(cmd, player);

        event.LoadEvents(cmd, newRoom);

        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(messageSet.Parse_MessageSets(cmd, parent));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        output.add("\\n");

        if (game.GetIsPlaying())
        {
            tmp = new ArrayList<String>();
            tmp.addAll(room.DescribeRoom(cmd, game.GetRoom()));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        return output;
    }

    /**
        Creates a copy of the Object, specified in the Action.
        <br />
        If there is a newvalue specified, the new Object is moved to that
        location, otherwise it is moved to the current Room.
    */
    public void SpawnObject(Statement cmd, int fired, Element action)
    {
        Object object = new Object();
        Npc npc = new Npc();
        String elemGuid = action.GetSource().trim();
        Element game = action.GetGame(action);
        Element newLocEl = action.GetRoom(action);
        String newLoc = newLocEl.GetGuid();
        String newLocType = "room";
        Element sourceElem = action.GetElement(cmd, elemGuid, action);

        if (!action.GetNewValue().equals(""))
        {
            newLocEl = action.GetElement(cmd, action.GetNewValue(), action);

            if (newLocEl != null)
            {
                newLoc = action.GetNewValue();
                if (!newLocEl.GetType().equals("")) newLocType = newLocEl.GetType();

                if (newLocEl.GetElementType().equals("npc")) newLocType = "npc";
            }
        }

        fired = fired + 1;

        UpdateActionFired(cmd, fired, action);

        if (sourceElem.GetElementType().equals("npc"))
        {
            npc.SpawnNpc(cmd, sourceElem, fired, newLocEl);
        }

        if (sourceElem.GetElementType().equals("object"))
        {
            object.SpawnObject(cmd, sourceElem, fired, newLocEl, newLocType);
        }
    }

    /**
        Depricated:
        Action fired limitations should be handled with Attributes, and Actions.
    */
    public void UpdateActionFired(Statement cmd, int fired, Element action)
    {
        String sql = "";
        Element player = action.GetPlayer(action);

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "PlayerActionSetFired ( ";
            sql += "    PlayerGUID, ";
            sql += "    ActionSetGUID, ";
            sql += "    ActionSetFired ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(player.GetGuid()) + "', ";
            sql += "    '" + Functions.Encode(action.GetParentGuid()) + "', ";
            sql += "    0 ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) FROM PlayerActionSetFired WHERE 1 = 1 ";
            sql += "        AND Deleted = 0 ";
            sql += "        AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "        AND ActionSetGUID = '" + Functions.Encode(action.GetParentGuid()) + "' ";
            sql += "    ) < 1 ";
            sql += ";";
            cmd.execute(sql);

            sql =  "";
            sql += "UPDATE ";
            sql += "    PlayerActionSetFired ";
            sql += "SET ";
            sql += "    ActionSetFired = " + fired + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND ActionSetGUID = '" + Functions.Encode(action.GetParentGuid()) + "' ";
            sql += ";";
            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    /**
        Depricated:
        Action fired limitations should be handled with Attributes, and Actions.
    */
    public int GetActionFired(Statement cmd, Element action)
    {
        int output = 0;
        String sql = "";
        Element player = action.GetPlayer(action);

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    ActionSetFired Fired ";
            sql += "FROM ";
            sql += "    PlayerActionSetFired ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND ActionSetGUID = '" + Functions.Encode(action.GetParentGuid()) + "' ";
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




    /**
        Loads the parent's Actions from the database.
        <br />
        This is only done if the Actions aren't already loaded.
    */
    public void LoadActions(Statement cmd, Element parent)
    {
        Element act = null;
        String sql = "";
        String guid = "";

        if (parent.GetActions().size() > 0) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colType = 3;
        int colSource = 4;
        int colNewValue = 5;
        int colRepeat = 6;
        int colSort = 7;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    Type, ";
            sql += "    Source, ";
            sql += "    NewValue, ";
            sql += "    Repeat, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Action ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                act = new Element(rs.getString(colGuid), "action");
                act.SetParent(parent);
                act.SetParentGuid(rs.getString(colParentGuid));
                act.SetParentType("actionset");
                act.SetType(rs.getString(colType));
                act.SetSource(rs.getString(colSource));
                act.SetNewValue(rs.getString(colNewValue));
                act.SetRepeat(rs.getInt(colRepeat));
                act.SetSort(rs.getInt(colSort));
                parent.AppendAction(act);
            }

            rs.close();

            // Load an empty to show that Messages have been loaded
            if (parent.GetActions().size() < 1)
            {
                act = new Element("", "action");
                parent.AppendAction(act);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    /**
        Creates a copy of the old parent's Actions, associates them with the
        new parent.
    */
    public void CloneActions(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        MessageSet messageSet = new MessageSet();
        LogicSet logicSet = new LogicSet();
        String sFired = fired + "";
        String sql = "";

        LoadActions(cmd, oldParent);
        if (oldParent.GetActions().size() > 0)
        {
            for (Element oldAct : oldParent.GetActions())
            {
                if (!oldAct.GetGuid().equals(""))
                {
                    Element newAct = new Element(Functions.GetGUID(), "action");
                    newAct.SetParentGuid(newParent.GetGuid());
                    newAct.SetParent(newParent);
                    newAct.SetParentType(oldParent.GetParentType());
                    newAct.SetType(oldAct.GetType());
                    newAct.SetSource(oldAct.GetSource().replace(oldParentGuid, newParentGuid));
                    newAct.SetNewValue(oldAct.GetNewValue().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newAct.SetRepeatCount(oldAct.GetRepeatCount());
                    newAct.SetSort(oldAct.GetSort());
                    newParent.AppendAction(newAct);

                    // Load the children
                    logicSet.CloneLogicSets(cmd, queries, fired, oldParentGuid, newParentGuid, oldAct, newAct);
                    messageSet.CloneMessageSets(cmd, queries, fired, oldParentGuid, newParentGuid, oldAct, newAct);
                }
            }

            // Insert the queries
            for (Element act : newParent.GetActions())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(act.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(act.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(act.GetType()) + "', ";
                sql += "    '" + Functions.Encode(act.GetSource()) + "', ";
                sql += "    '" + Functions.Encode(act.GetNewValue()) + "', ";
                sql += "    " + act.GetRepeat() + ", ";
                sql += "    " + act.GetSort() + " ";
                queries.add(new Query("Action", sql, false));
            }
        }
    }

    /**
        Inserts new Actions into the database. Assumes that any Actions passed into it do not
        already exist in the database.
    */
    public void InsertActions(Statement cmd, List<Element> actions)
    {
        String sql = "";
        String sqlAct = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Action ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    Type, ";
        sql += "    Source, ";
        sql += "    NewValue, ";
        sql += "    Repeat, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element act : actions)
        {
            if (!act.GetGuid().equals(""))
            {
                if (!sqlAct.equals("")) sqlAct += " UNION ";
                sqlAct += "SELECT ";
                sqlAct += "     '" + Functions.Encode(act.GetGuid()) + "', ";
                sqlAct += "     '" + Functions.Encode(act.GetParentGuid()) + "', ";
                sqlAct += "     '" + Functions.Encode(act.GetType()) + "', ";
                sqlAct += "     '" + Functions.Encode(act.GetSource()) + "', ";
                sqlAct += "     '" + Functions.Encode(act.GetNewValue()) + "', ";
                sqlAct += "     " + act.GetRepeatCount() + ", ";
                sqlAct += "     " + act.GetSort() + " ";
            }
        }

        if (!sqlAct.equals(""))
        {
            sql = sql + sqlAct + ";";
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


