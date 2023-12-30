import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    MESSAGESET HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class MessageSet
{
    public List<String> Parse_MessageSets(Statement cmd, Element parent)
    {
        return Parse_MessageSets(cmd, parent, null);
    }
    public List<String> Parse_MessageSets(Statement cmd, Element parent, Input input)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Message message = new Message();
        LogicSet lset = new LogicSet();

        LoadMessageSets(cmd, parent);

        // Parse the parent logic sets
        Response resp = new Response();
        resp = lset.Parse_LogicSets(cmd, parent.GetLogicSets());

        if (Functions.ListHasData(resp.GetOutput()))
        {
            output.addAll(Functions.CleanList(resp.GetOutput()));
        }

        if (!resp.GetResult().equals(resp._fail))
        {
            for (Element mset : parent.GetMessageSets())
            {
                if (mset.GetGuid().equals("")) continue;

                tmp = new ArrayList<String>();
                tmp.addAll(message.Parse_Messages(cmd, mset, input));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }
            }
        }

        return output;
    }

    public void LoadMessageSets(Statement cmd, Element parent)
    {
        String sql = "";
        Element mset = null;
        Message msg = new Message();

        if (parent.HasMessageSets()) return;

        int colGuid = 1;
        int colParentGUID = 2;
        int colParentType = 3;
        int colRepeatType = 4;
        int colSort = 5;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    RepeatType, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    MessageSet ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                mset = new Element(rs.getString(colGuid), "messageset");
                mset.SetParent(parent);
                mset.SetParentGuid(rs.getString(colParentGUID));
                mset.SetParentType(rs.getString(colParentType));
                mset.SetRepeatType(rs.getString(colRepeatType));
                mset.SetSort(rs.getInt(colSort));
                parent.AppendMessageSet(mset);
            }

            rs.close();

            // Load an empty to show that MessageSets have been loaded
            if (parent.GetMessageSets().size() < 1)
            {
                mset = new Element("", "messageset");
                Element tmpMsg = new Element("", "message");
                mset.AppendMessage(tmpMsg);
                parent.AppendMessageSet(mset);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SavePlayerMessageSetIndex(Statement cmd, Element mset)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    PlayerMessageSetIndex ";
            sql += "SET ";
            sql += "    MessageSetIndex = " + mset.GetCurrentIndex() + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND MessageSetGUID = '" + Functions.Encode(mset.GetGuid()) + "' ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(mset.GetPlayer().GetGuid()) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneMessageSets(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        Message message = new Message();
        String sFired = fired + "";
        String sql = "";

        LoadMessageSets(cmd, oldParent);
        if (oldParent.GetMessageSets().size() > 0)
        {
            for (Element oldMset : oldParent.GetMessageSets())
            {
                if (!oldMset.GetGuid().equals(""))
                {
                    Element newMset = new Element(Functions.GetGUID(), "messageset");
                    newMset.SetParentGuid(newParent.GetGuid());
                    newMset.SetParent(newParent);
                    newMset.SetParentType(oldMset.GetParentType());
                    newMset.SetRepeatType(oldMset.GetRepeatType());
                    newMset.SetSort(oldMset.GetSort());
                    newParent.AppendMessageSet(newMset);

                    message.CloneMessages(cmd, queries, fired, oldParentGuid, newParentGuid, oldMset, newMset);
                }
            }

            // Insert queries
            for (Element mset : newParent.GetMessageSets())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(mset.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(mset.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(mset.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(mset.GetRepeatType()) + "', ";
                sql += "    " + mset.GetSort() + " ";
                queries.add(new Query("MessageSet", sql, false));
            }
        }
    }

    public void InsertMessageSets(Statement cmd, List<Element> messageSets)
    {
        String sql = "";
        String sqlMset = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "MessageSet ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    RepeatType, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element mset : messageSets)
        {
            if (!mset.GetGuid().equals(""))
            {
                if (!sqlMset.equals("")) sqlMset += " UNION ";
                sqlMset += "SELECT ";
                sqlMset += "    '" + Functions.Encode(mset.GetGuid()) + "', ";
                sqlMset += "    '" + Functions.Encode(mset.GetParentGuid()) + "', ";
                sqlMset += "    '" + Functions.Encode(mset.GetParentType()) + "', ";
                sqlMset += "    '" + Functions.Encode(mset.GetRepeatType()) + "', ";
                sqlMset += "    " + mset.GetSort() + " ";
            }
        }

        if (!sqlMset.equals(""))
        {
            sql = sql + sqlMset + ";";
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
