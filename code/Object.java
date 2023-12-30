import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    OBJECT HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Object
{
    public List<String> Parse_Objects(Statement cmd, List<Element> objects)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        MessageSet mset = new MessageSet();
        LogicSet lset = new LogicSet();

        for (Element o : objects)
        {
            if (o.GetGuid().equals("")) continue;

            // Parse MessageSets
            mset.LoadMessageSets(cmd, o);
            tmp = new ArrayList<String>();
            tmp.addAll(Functions.CleanList(mset.Parse_MessageSets(cmd, o)));

            if (Functions.ListHasData(tmp))
            {
                output.addAll(tmp);
            }
        }

        return output;
    }

    public List<String> Parse_Objects_Events(Statement cmd, List<Element> objects, String types)
    {
        List<String> output = new ArrayList<String>();
        MessageSet mset = new MessageSet();
        Event evt = new Event();

        for (Element o : objects)
        {
            if (o.GetGuid().equals("")) continue;

            // Only parse non-connectors
            if (!o.GetType().equals("connector"))
            {
                // Parse Events
                evt.LoadEvents(cmd, o);
                output.addAll(Functions.CleanList(evt.Parse_Events(cmd, o, types)));
            }
        }

        return output;
    }

    public List<String> Parse_Connectors_Events(Statement cmd, List<Element> objects, String types)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        MessageSet mset = new MessageSet();
        Event evt = new Event();

        for (Element o : objects)
        {
            if (o.GetGuid().equals("")) continue;

            // Only parse non-connectors
            if (o.GetType().equals("connector"))
            {
                // Parse MessageSets
                mset.LoadMessageSets(cmd, o);
                tmp = new ArrayList<String>();
                tmp.addAll(Functions.CleanList(mset.Parse_MessageSets(cmd, o)));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }

                // Parse Events
                evt.LoadEvents(cmd, o);
                tmp = new ArrayList<String>();
                tmp.addAll(Functions.CleanList(evt.Parse_Events(cmd, o, types)));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }
            }
        }

        return output;
    }

    public void LoadObjects(Statement cmd, Element parent)
    {
        Element obj = null;
        String sql = "";
        List<Element> objs = new ArrayList<Element>();

        if (parent.GetObjects().size() > 0) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colType = 4;
        int colChance = 5;
        int colLabel = 6;
        int colMeta = 7;
        int colSort = 8;
        int colInherit = 9;
        int colFileName = 10;
        int colCount = 11;
        int colHasChildren = 12;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    Chance, ";
            sql += "    Label, ";
            sql += "    Meta, ";
            sql += "    Sort, ";
            sql += "    Inherit, ";
            sql += "    FileName, ";
            sql += "    Count, ";
            sql += "    ( ";
            sql += "        SELECT COUNT(*) FROM Object chld WHERE 1 = 1 ";
            sql += "        AND chld.Deleted = 0 ";
            sql += "        AND chld.ParentGUID = GUID ";
            sql += "    ) HasChildren ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!parent.GetGuid().equals("game") && !parent.GetElementType().equals("player")) sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                obj = new Element(rs.getString(colGuid), "object");
                obj.SetParent(parent);
                obj.SetType(rs.getString(colType));
                obj.SetParentGuid(rs.getString(colParentGuid));
                obj.SetParentType(rs.getString(colParentType));
                obj.SetChance(rs.getInt(colChance));
                obj.SetLabel(rs.getString(colLabel));
                if (rs.getString(colMeta) != null) obj.SetMeta(rs.getString(colMeta));
                obj.SetSort(rs.getInt(colSort));
                obj.SetInherit(rs.getString(colInherit));
                obj.SetFileName(rs.getString(colFileName));
                obj.SetCount(rs.getInt(colCount));

                if (rs.getInt(colHasChildren) > 0)
                {
                    obj.SetHasChildObjects(true);
                }
                else
                {
                    obj.SetHasChildObjects(false);
                }

                objs.add(obj);
            }

            // Create an empty to show that Objects have been loaded
            if (objs.size() < 1)
            {
                obj = new Element("", "object");
                objs.add(obj);
            }

            rs.close();

            parent.SetObjects(objs);

            // Arm with default weapon
            if (parent.GetElementType().equals("player") && (parent.GetArmedWeapon() == null || parent.GetArmedWeapon().equals("")))
            {
                parent.SetDefaultArmed(parent);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public List<Element> GetObjects(Statement cmd, String filter)
    {
        List<Element> output = new ArrayList<Element>();
        Element obj = null;
        String sql = "";
        String type = "";

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colType = 4;
        int colChance = 5;
        int colLabel = 6;
        int colMeta = 7;
        int colFileName = 8;
        int colHasChildren = 9;

        if (filter.equals("@@connector@@"))
        {
            type = "connector";
            filter = "";
        }

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    Chance, ";
            sql += "    Label, ";
            sql += "    Meta, ";
            sql += "    FileName, ";
            sql += "    ( ";
            sql += "        SELECT COUNT(*) FROM Object chld WHERE 1 = 1 ";
            sql += "        AND chld.Deleted = 0 ";
            sql += "        AND chld.ParentGUID = GUID ";
            sql += "    ) HasChildren ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!type.equals("")) sql += "    AND Type = '" + Functions.Encode(type) + "' ";
            if (!filter.equals("")) sql += "    AND GUID LIKE '%" + Functions.Encode(filter) + "%' ";
            sql += "ORDER BY ";
            sql += "    Label ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                obj = new Element(rs.getString(colGuid), "object");
                if (rs.getInt(colHasChildren) > 0)
                {
                    obj.SetHasChildObjects(true);
                }
                else
                {
                    obj.SetHasChildObjects(false);
                }
                obj.SetParentGuid(rs.getString(colParentGuid));
                obj.SetParentType(rs.getString(colParentType));
                obj.SetType(rs.getString(colType));
                obj.SetChance(rs.getInt(colChance));
                obj.SetLabel(rs.getString(colLabel));
                obj.SetMeta(rs.getString(colMeta));
                obj.SetFileName(rs.getString(colFileName));
                output.add(obj);
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

    public Element GetArmedWeapon(Statement cmd, Element parent)
    {
        Element wpn = null;

        LoadObjects(cmd, parent);
        for (Element obj : parent.GetObjects())
        {
            if (obj.GetType().equals("weapon") && parent.GetArmedWeapon().equals(obj.GetGuid()))
            {
                wpn = obj;
                break;
            }
        }

        // If no armed weapon, grab the first weapon
        if (wpn == null)
        {
            for (Element obj : parent.GetObjects())
            {
                if (obj.GetType().equals("weapon"))
                {
                    wpn = obj;
                    break;
                }
            }
        }

        return wpn;
    }

    public void LoadObject(Statement cmd, Element obj)
    {
        String sql = "";

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colType = 4;
        int colChance = 5;
        int colLabel = 6;
        int colMeta = 7;
        int colSort = 8;
        int colInherit = 9;
        int colFileName = 10;
        int colCount = 11;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    Chance, ";
            sql += "    Label, ";
            sql += "    Meta, ";
            sql += "    Sort, ";
            sql += "    Inherit, ";
            sql += "    FileName, ";
            sql += "    Count ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(obj.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                obj.SetType(rs.getString(colType));
                obj.SetParentGuid(rs.getString(colParentGuid));
                obj.SetParentType(rs.getString(colParentType));
                obj.SetChance(rs.getInt(colChance));
                obj.SetLabel(rs.getString(colLabel));
                if (rs.getString(colMeta) != null) obj.SetMeta(rs.getString(colMeta));
                obj.SetSort(rs.getInt(colSort));
                obj.SetFileName(rs.getString(colFileName));
                obj.SetInherit(rs.getString(colInherit));
                obj.SetCount(rs.getInt(colCount));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public Element LoadObjectFromData(Statement cmd, String alias)
    {
        Element obj = null;
        String sql = "";

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colType = 4;
        int colChance = 5;
        int colLabel = 6;
        int colMeta = 7;
        int colSort = 8;
        int colInherit = 9;
        int colFileName = 10;
        int colCount = 11;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    Chance, ";
            sql += "    Label, ";
            sql += "    Meta, ";
            sql += "    Sort, ";
            sql += "    Inherit, ";
            sql += "    FileName, ";
            sql += "    Count ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(alias) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                obj = new Element(rs.getString(colGuid), "object");
                obj.SetType(rs.getString(colType));
                obj.SetParentGuid(rs.getString(colParentGuid));
                obj.SetParentType(rs.getString(colParentType));
                obj.SetChance(rs.getInt(colChance));
                obj.SetLabel(rs.getString(colLabel));
                if (rs.getString(colMeta) != null) obj.SetMeta(rs.getString(colMeta));
                obj.SetSort(rs.getInt(colSort));
                obj.SetFileName(rs.getString(colFileName));
                obj.SetInherit(rs.getString(colInherit));
                obj.SetCount(rs.getInt(colCount));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return obj;
    }

    public void MoveObject(Statement cmd, String objectGuid, String newLocation)
    {
        String sql = "";
        String newLoc = Functions.Encode(newLocation);
        String parentType = GetLocationType(cmd, newLocation);

        if (newLoc.equals("game")) parentType = "game";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Object ";
            sql += "SET ";
            sql += "    ParentGUID = '" + newLoc + "', ";
            sql += "    ParentType = '" + parentType + "' ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(objectGuid) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public String GetLocationType(Statement cmd, String location)
    {
        String output = "";
        String sql = "";

        if (location.equals("{player}"))
        {
            output = "player";
            return output;
        }
        if (location.equals("{room}"))
        {
            output = "room";
            return output;
        }
        if (location.equals("{game}"))
        {
            output = "game";
            return output;
        }

        output = "room";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    'room' Type ";
            sql += "FROM ";
            sql += "    Room ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(location) + "' ";
            sql += "UNION ";
            sql += "SELECT ";
            sql += "    'npc' Type ";
            sql += "FROM ";
            sql += "    Npc ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(location) + "' ";
            sql += "UNION ";
            sql += "SELECT ";
            sql += "    'player' Type ";
            sql += "FROM ";
            sql += "    Player ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(location) + "' ";
            sql += "UNION ";
            sql += "SELECT ";
            sql += "    'object' Type ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(location) + "' ";
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

    public void SaveObject(Statement cmd, Element object)
    {
        EnsureObjectExists(cmd, object);
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Object ";
            sql += "SET ";
            sql += "    ParentGUID = '" + Functions.Encode(object.GetParentGuid()) + "', ";
            sql += "    ParentType = '" + Functions.Encode(object.GetParentType()) + "', ";
            sql += "    Chance = " + object.GetChance() + ", ";
            sql += "    Label = '" + Functions.Encode(object.GetLabel()) + "', ";
            if (object.GetMeta() != null) sql += "    Meta = '" + Functions.Encode(object.GetMeta()) + "', ";
            sql += "    Sort = " + object.GetSort() + ", ";
            if (object.GetInherit() != null) sql += "    Inherit = '" + Functions.Encode(object.GetInherit()) + "', ";
            sql += "    Count = " + object.GetCount() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(object.GetGuid()) + "' ";
            if (!object.GetParentType().equals("game")) sql += "    AND ParentGUID = '" + Functions.Encode(object.GetParentGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(object.GetParentType()) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void EnsureObjectExists(Statement cmd, Element object)
    {
        String sql = "";

        try 
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "Object ( ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Type, ";
            sql += "    Chance, ";
            sql += "    Label, ";
            sql += "    Meta, ";
            sql += "    Sort, ";
            sql += "    Inherit, ";
            sql += "    Count ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(object.GetGuid()) + "', ";
            sql += "    '" + Functions.Encode(object.GetParentGuid()) + "', ";
            sql += "    '" + Functions.Encode(object.GetParentType()) + "', ";
            sql += "    '" + Functions.Encode(object.GetType()) + "', ";
            sql += "    " + object.GetChance() + ", ";
            sql += "    '" + Functions.Encode(object.GetLabel()) + "', ";
            if (object.GetMeta() != null) sql += "    '" + Functions.Encode(object.GetMeta()) + "', ";
            sql += "    " + object.GetSort() + ", ";
            if (object.GetInherit() != null) sql += "    '" + Functions.Encode(object.GetInherit()) + "', ";
            sql += "    " + object.GetCount() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) ";
            sql += "        FROM Object ";
            sql += "        WHERE 1 = 1 ";
            sql += "            AND Deleted = 0 ";
            sql += "            AND GUID = '" + Functions.Encode(object.GetGuid()) + "' ";
            sql += "    ) < 1 ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SpawnObject(Statement cmd, Element source, int fired, Element newLocation, String newLocationType)
    {
        Attribute attribute = new Attribute();
        List<Query> queries = new ArrayList<Query>();
        Query query = new Query();
        Event event = new Event();
        Object object = new Object();
        Command command = new Command();
        List<String> sql = new ArrayList<String>();
        String sFired = fired + "";
        String table = "";
        String head = "";

        // Add the heads
        query.AddHeads(queries);

        // Create the new Object 
        Element newObject = new Element(Functions.GetGUID(), "object");

        // Adjust the GUID
        newObject.SetParent(newLocation);
        newObject.SetParentGuid(newLocation.GetGuid());
        newObject.SetParentType(newLocationType);
        newObject.SetType(source.GetType());
        newObject.SetChance(source.GetChance());
        newObject.SetLabel(source.GetLabel().replace("{fired}", sFired));
        newObject.SetMeta(source.GetMeta().replace("{fired}", sFired));
        newObject.SetSort(source.GetSort());
        newObject.SetCount(source.GetCount());

        SaveObject(cmd, newObject);

        attribute.CloneAttributes(cmd, queries, fired, source, newObject);
        event.CloneEvents(cmd, queries, fired, source, newObject);
        object.CloneObjects(cmd, queries, fired, source, newObject);
        command.CloneCommands(cmd, queries, fired, source, newObject);

        newLocation.ClearObjects();
        object.LoadObjects(cmd, newLocation);

        // Run the Queries
        query.RunQueries(cmd, queries);
    }

    public void CloneObjects(Statement cmd, List<Query> queries, int fired, Element oldParent, Element newParent)
    {
        Attribute attribute = new Attribute();
        Event event = new Event();
        Command command = new Command();
        String sFired = fired + "";
        String parentType = newParent.GetType();
        String sql = "";

        if (newParent.GetElementType().equals("npc")) parentType = "npc";
        if (newParent.GetElementType().equals("player")) parentType = "player";

        LoadObjects(cmd, oldParent);
        if (oldParent.GetObjects().size() > 0)
        {
            for (Element oldObj : oldParent.GetObjects())
            {
                if (!oldObj.GetGuid().equals(""))
                {
                    Element newObj = new Element(Functions.GetGUID(), "object");
                    newObj.SetParentGuid(newParent.GetGuid());
                    newObj.SetParent(newParent);
                    newObj.SetParentType(parentType);
                    newObj.SetType(oldObj.GetType());
                    newObj.SetChance(oldObj.GetChance());
                    newObj.SetLabel(oldObj.GetLabel().replace("{fired}", sFired));
                    newObj.SetMeta(oldObj.GetMeta().replace("{fired}", sFired));
                    newObj.SetSort(oldObj.GetSort());
                    newObj.SetCount(oldObj.GetCount());
                    newParent.AppendObject(newObj);

                    attribute.CloneAttributes(cmd, queries, fired, oldObj, newObj);
                    event.CloneEvents(cmd, queries, fired, oldObj, newObj);
                    command.CloneCommands(cmd, queries, fired, oldObj, newObj);
                    CloneObjects(cmd, queries, fired, oldObj, newObj);
                }
            }

            // Insert the queries
            for (Element obj : newParent.GetObjects())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(obj.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(obj.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(obj.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(obj.GetType()) + "', ";
                sql += "    " + obj.GetChance() + ", ";
                sql += "    '" + Functions.Encode(obj.GetLabel()) + "', ";
                sql += "    '" + Functions.Encode(obj.GetMeta()) + "', ";
                sql += "    " + obj.GetSort() + ", ";
                sql += "    '" + Functions.Encode(obj.GetInherit()) + "', ";
                sql += "    " + obj.GetCount() + " ";
                queries.add(new Query("Object", sql, false));
            }

            oldParent.ClearObjects();
            newParent.ClearObjects();
        }
    }

    public void InsertObjects(Statement cmd, List<Element> objects)
    {
        String sql = "";
        String sqlObj = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Object ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    Type, ";
        sql += "    Chance, ";
        sql += "    Label, ";
        sql += "    Meta, ";
        sql += "    Sort, ";
        sql += "    Inherit, ";
        sql += "    Count ";
        sql += ") ";

        for (Element obj : objects)
        {
            if (!obj.GetGuid().equals(""))
            {
                if (!sqlObj.equals("")) sqlObj += " UNION ";
                sqlObj += "SELECT ";
                sqlObj += "     '" + Functions.Encode(obj.GetGuid()) + "', ";
                sqlObj += "     '" + Functions.Encode(obj.GetParentGuid()) + "', ";
                sqlObj += "     '" + Functions.Encode(obj.GetParentType()) + "', ";
                sqlObj += "     '" + Functions.Encode(obj.GetType()) + "', ";
                sqlObj += "     " + obj.GetChance() + ", ";
                sqlObj += "     '" + Functions.Encode(obj.GetLabel()) + "', ";
                sqlObj += "     '" + Functions.Encode(obj.GetMeta()) + "', ";
                sqlObj += "     " + obj.GetSort() + ", ";
                sqlObj += "     '" + Functions.Encode(obj.GetInherit()) + "', ";
                sqlObj += "     " + obj.GetCount() + " ";
            }
        }

        if (!sqlObj.equals(""))
        {
            sql = sql + sqlObj + ";";
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
