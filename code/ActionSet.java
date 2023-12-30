import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/**
    Actionsets group Actions together. The Actionsets can be given aliases,
    and this can be used with Attributes to limit how many times they are
    called.
*/
public class ActionSet
{
    /**
        Parses through the ActionSets, and calls the parse on each of their
        Actions. First it ensures the parent's ActionSets are loaded from the
        database.
    */
    public List<String> Parse_ActionSets(Statement cmd, Input input, Element parent)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        LoadActionSets(cmd, parent);
        Action act = new Action();

        for (Element aset : parent.GetActionSets())
        {
            if (aset.GetRepeatCount() < 1 || (GetActionSetFired(cmd, aset) < aset.GetRepeatCount()))
            {
                tmp = new ArrayList<String>();

                tmp.addAll(act.Parse_Actions(cmd, input, GetActionSetFired(cmd, aset), aset));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }

                if (aset.GetRepeatCount() > 0 && output.size() > 0)
                {
                    UpdateActionSetFired(cmd, aset);
                }
            }
        }

        return output;
    }

    /**
        Loads the parent's ActionSets. This is only done if the ActionSets haven't
        already been loaded.
    */
    public void LoadActionSets(Statement cmd, Element parent)
    {
        String sql = "";
        Element aset = null;
        Action act = new Action();

        if (parent.GetActionSets().size() > 0) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colRepeatCount = 4;
        int colSort = 5;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    RepeatCount, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    ActionSet ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!parent.GetGuid().equals("game")) sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            if (parent.GetGuid().equals("game")) sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                aset = new Element(rs.getString(colGuid), "actionset");
                aset.SetParent(parent);
                aset.SetParentGuid(rs.getString(colParentGuid));
                aset.SetParentType(rs.getString(colParentType));
                aset.SetRepeatCount(rs.getInt(colRepeatCount));
                aset.SetSort(rs.getInt(colSort));
                parent.AppendActionSet(aset);
            }

            rs.close();

            // Load an empty to show that MessageSets have been loaded
            if (parent.GetActionSets().size() < 1)
            {
                aset = new Element("", "actionset");
                Element tmpAct = new Element("", "action");
                aset.AppendAction(tmpAct);
                parent.AppendActionSet(aset);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    /**
        Depricated:
        <br />
        ActionSet firing should be limited using the ActionSet's alias
        and an Attribute value.
    */
    public void UpdateActionSetFired(Statement cmd, Element actionSet)
    {
        Element player = actionSet.GetPlayer(actionSet);
        String sql = "";

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
            sql += "    '" + Functions.Encode(actionSet.GetGuid()) + "', ";
            sql += "    0 ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) FROM PlayerActionSetFired WHERE 1 = 1 ";
            sql += "        AND Deleted = 0 ";
            sql += "        AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "        AND ActionSetGUID = '" + Functions.Encode(actionSet.GetGuid()) + "' ";
            sql += "    ) < 1 ";
            sql += ";";
            cmd.execute(sql);

            sql =  "";
            sql += "UPDATE ";
            sql += "    PlayerActionSetFired ";
            sql += "SET ";
            sql += "    ActionSetFired = ActionSetFired + 1 ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND ActionSetGUID = '" + Functions.Encode(actionSet.GetGuid()) + "' ";
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
        <br />
        ActionSet firing should be limited using the ActionSet's alias
        and an Attribute value.
    */
    public int GetActionSetFired(Statement cmd, Element actionSet)
    {
        int output = 0;
        Element player = actionSet.GetPlayer(actionSet);
        if (player == null) return output;
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    ActionSetFired ";
            sql += "FROM ";
            sql += "    PlayerActionSetFired ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND ActionSetGUID = '" + Functions.Encode(actionSet.GetGuid()) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output = rs.getInt("ActionSetFired");
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
        Creates a copy of the old parent's ActionSets and associates them
        with the new parent.
    */
    public void CloneActionSets(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        Action action = new Action();
        LogicSet logicSet = new LogicSet();
        String sFired = fired + "";
        String sql = "";

        LoadActionSets(cmd, oldParent);
        if (oldParent.GetActionSets().size() > 0)
        {
            for (Element oldAset : oldParent.GetActionSets())
            {
                if (!oldAset.GetGuid().equals(""))
                {
                    Element newAset = new Element(Functions.GetGUID(), "actionset");
                    newAset.SetParentGuid(newParent.GetGuid());
                    newAset.SetParent(newParent);
                    newAset.SetParentType(oldAset.GetParentType());
                    newAset.SetRepeatCount(oldAset.GetRepeatCount());
                    newAset.SetSort(oldAset.GetSort());
                    newParent.AppendActionSet(newAset);

                    logicSet.CloneLogicSets(cmd, queries, fired, oldParentGuid, newParentGuid, oldAset, newAset);
                    action.CloneActions(cmd, queries, fired, oldParentGuid, newParentGuid, oldAset, newAset);
                }
            }

            // Insert queries
            for (Element aset : newParent.GetActionSets())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(aset.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(aset.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(aset.GetParentType()) + "', ";
                sql += "    " + aset.GetRepeatCount() + ", ";
                sql += "    " + aset.GetSort() + " ";
                queries.add(new Query("ActionSet", sql, false));
            }
        }
    }

    /**
        Inserts the specified ActionSets into the database. This assumes that
        each ActionSet doesn't already exist within the database.
    */
    public void InsertActionSets(Statement cmd, List<Element> actionSets)
    {
        String sql = "";
        String sqlAset = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "ActionSet ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    RepeatCount, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element aset : actionSets)
        {
            if (!aset.GetGuid().equals(""))
            {
                if (!sqlAset.equals("")) sqlAset += " UNION ";
                sqlAset += "SELECT ";
                sqlAset += "    '" + Functions.Encode(aset.GetGuid()) + "', ";
                sqlAset += "    '" + Functions.Encode(aset.GetParentGuid()) + "', ";
                sqlAset += "    '" + Functions.Encode(aset.GetParentType()) + "', ";
                sqlAset += "    " + aset.GetRepeatCount() + ", ";
                sqlAset += "    " + aset.GetSort() + " ";
            }
        }

        if (!sqlAset.equals(""))
        {
            sql = sql + sqlAset + ";";
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
