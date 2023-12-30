import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    MESSAGE HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Message
{
    public List<String> Parse_Messages(Statement cmd, Element mset)
    {
        return Parse_Messages(cmd, mset, null);
    }
    public List<String> Parse_Messages(Statement cmd, Element mset, Input input)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        LoadMessages(cmd, mset);
        String repeatType = mset.GetRepeatType();
        int curIndex = GetMessageSetIndex(cmd, mset);
        int msgCount = mset.GetMessages().size() - 1;
        MessageOutput outp = new MessageOutput();
        LogicSet lset = new LogicSet();

        lset.LoadLogicSets(cmd, mset);

        // Parse LogicSets
        Response resp = new Response();
        resp = lset.Parse_LogicSets(cmd, mset.GetLogicSets());

        if (resp.GetResult().equals(resp._fail))
        {
            if (Functions.ListHasData(resp.GetOutput()))
            {
                output.addAll(resp.GetOutput());
            }
            return output;
        }

        if (repeatType.equals("random"))
        {
            curIndex = Functions.RandomInt(0, msgCount);
        }

        tmp = new ArrayList<String>();
        tmp.add(outp.Parse_Output(cmd, mset.GetMessages().get(curIndex)));

        if (Functions.ListHasData(tmp))
        {
            output.addAll(tmp);
        }

        if (!repeatType.equals("random"))
        {
            if (curIndex > msgCount)
            {
                if (repeatType.equals("repeat"))
                {
                    curIndex = 0;
                }
                else
                {
                    curIndex = msgCount;
                }
            }

            UpdateMessageSetIndex(cmd, mset, curIndex);
        }

        // Replace {input} with the player's input
        if (input != null)
        {
            for (String s : output)
            {
                if (s.indexOf("{input}") >= 0) s = s.replace("{input}", input.GetUserInput());
                tmp.add(s);
            }

            output = new ArrayList<String>();

            for (String s : tmp)
            {
                output.add(s);
            }
        }

        if (output.size() > 0 && curIndex < msgCount)
        {
            curIndex++;
            UpdateMessageSetIndex(cmd, mset, curIndex);
        }

        return output;
    }


    public void LoadMessages(Statement cmd, Element parent)
    {
        Element msg = null;
        String sql = "";
        String guid = "";

        if (parent.HasMessages()) return;

        int colMessageSetGuid = 1;
        int colOutput = 2;
        int colSort = 3;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    MessageSetGUID, ";
            sql += "    Output, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Message ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND MessageSetGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                guid = Functions.GetGUID();
                msg = new Element(guid, "message");
                msg.SetParent(parent);
                msg.SetParentGuid(rs.getString(colMessageSetGuid));
                msg.SetParentType("messageset");
                msg.SetOutput(rs.getString(colOutput));
                msg.SetSort(rs.getInt(colSort));
                parent.AppendMessage(msg);
            }

            rs.close();

            // Load an empty to show that Messages have been loaded
            if (parent.GetMessages().size() < 1)
            {
                msg = new Element("", "message");
                parent.AppendMessage(msg);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void UpdateMessageSetIndex(Statement cmd, Element mset, int newIndex)
    {
        Element player = mset.GetPlayer(mset);
        String sql = "";

        if (player == null) return;

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "PlayerMessageSetIndex ( ";
            sql += "    PlayerGUID, ";
            sql += "    MessageSetGUID, ";
            sql += "    MessageSetIndex ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(player.GetGuid()) + "', ";
            sql += "    '" + Functions.Encode(mset.GetGuid()) + "', ";
            sql += "    0 ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND ( ";
            sql += "        SELECT COUNT(*) FROM PlayerMessageSetIndex WHERE 1 = 1 ";
            sql += "        AND Deleted = 0 ";
            sql += "        AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "        AND MessageSetGUID = '" + Functions.Encode(mset.GetGuid()) + "' ";
            sql += "    ) < 1 ";
            sql += ";";
            cmd.execute(sql);

            sql =  "";
            sql += "UPDATE ";
            sql += "    PlayerMessageSetIndex ";
            sql += "SET ";
            sql += "    MessageSetIndex = " + newIndex + " ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND MessageSetGUID = '" + Functions.Encode(mset.GetGuid()) + "' ";
            sql += ";";
            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public Integer GetMessageSetIndex(Statement cmd, Element mset)
    {
        Integer output = 0;
        Element player = mset.GetPlayer(mset);
        String sql = "";

        if (player == null) return output;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    MessageSetIndex ";
            sql += "FROM ";
            sql += "    PlayerMessageSetIndex ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "    AND MessageSetGUID = '" + Functions.Encode(mset.GetGuid()) + "' ";
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

    public void CloneMessages(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        String sFired = fired + "";
        String sql = "";

        LoadMessages(cmd, oldParent);
        if (oldParent.GetMessages().size() > 0)
        {
            for (Element oldMsg : oldParent.GetMessages())
            {
                if (!oldMsg.GetGuid().equals(""))
                {
                    Element newMsg = new Element(Functions.GetGUID(), "message");
                    newMsg.SetParentGuid(newParent.GetGuid());
                    newMsg.SetParent(newParent);
                    newMsg.SetOutput(oldMsg.GetOutput().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newMsg.SetSort(oldMsg.GetSort());
                    newParent.AppendMessage(newMsg);
                }
            }

            // Insert queries
            for (Element msg : newParent.GetMessages())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(msg.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(msg.GetOutput()) + "', ";
                sql += "    " + msg.GetSort() + " ";
                queries.add(new Query("Message", sql, false));
            }
        }
    }

    public void InsertMessages(Statement cmd, List<Element> messages)
    {
        String sql = "";
        String sqlMsg = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Message ( ";
        sql += "    MessageSetGUID, ";
        sql += "    Output, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element msg : messages)
        {
            if (!msg.GetOutput().equals(""))
            {
                if (!sqlMsg.equals("")) sqlMsg += " UNION ";
                sqlMsg += "SELECT ";
                sqlMsg += "     '" + Functions.Encode(msg.GetParentGuid()) + "', ";
                sqlMsg += "     '" + Functions.Encode(msg.GetOutput()) + "', ";
                sqlMsg += "     " + msg.GetSort() + " ";
            }
        }

        if (!sqlMsg.equals(""))
        {
            sql = sql + sqlMsg + ";";
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
