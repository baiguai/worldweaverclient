import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    LOGICSET HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class LogicSet
{
    public Response Parse_LogicSets(Statement cmd, List<Element> lsets)
    {
        Response output = new Response();
        boolean pass = true;
        LogicSet logSet = new LogicSet();
        Logic log = new Logic();
        String curPass = "";
        boolean tmp = true;
        List<String> failMsgs = new ArrayList<String>();
        List<String> failEvents = new ArrayList<String>();
        List<String> passMsgs = new ArrayList<String>();
        MessageSet mset = new MessageSet();

        // No logicsets so pass
        if (lsets.size() < 1 || (
            lsets.size() == 1 && lsets.get(0).GetGuid().equals("")
        ))
        {
            output.SetResult(output._success);
            return output;
        }

        for (Element lset : lsets)
        {
            if (lset.GetGuid().equals("")) continue;

            for (Element lgc : lset.GetLogicBlocks())
            {
                tmp = log.Parse_Logic(cmd, lgc, lset);

                if (!tmp)
                {
                    // A previous set passed, but the current failed
                    if (output.GetResult().equals(output._success))
                    {
                        passMsgs = new ArrayList<String>();
                    }

                    output.SetResult(output._fail);
                    failEvents.add(lset.GetFailEvent());

                    String[] arr = null;
                    String arrTmp = "";
                    failMsgs.add(lset.GetFailMessage().replace(" \\n", "\\n").replace("\\n ", "\\n")); // Trim the line breaks

                    if (lset.GetFailEvent() != null && !lset.GetFailEvent().equals(""))
                    {
                        output.SetOutput(FireFailEvent(cmd, lset));
                    }
                    else
                    {
                        output.SetOutput(failMsgs);
                    }

                    // As soon as a logicset fails - return that
                    // Be sure to define your lsets in the correct order
                    return output;
                }
                else
                {
                    output.SetResult(output._success);
                    mset.LoadMessageSets(cmd, lgc);
                    passMsgs.addAll(Functions.CleanList(mset.Parse_MessageSets(cmd, lgc)));
                    output.SetOutput(passMsgs);
                }
            }
        }

        return output;
    }

    public List<String> FireFailEvent(Statement cmd, Element lset)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        String fireEv = lset.GetFailEvent();
        Element self = lset.GetSelf(lset);
        Event ev = new Event();

        tmp = new ArrayList<String>();
        tmp.addAll(ev.Parse_Events(cmd, self, fireEv));

        if (Functions.ListHasData(tmp))
        {
            output.addAll(tmp);
        }

        return output;
    }

    public void LoadLogicSets(Statement cmd, Element parent)
    {
        String sql = "";
        Element lset = null;
        Logic lgc = new Logic();

        if (parent.HasLogicSets()) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colOperand = 4;
        int colSort = 5;
        int colFailEvent = 6;
        int colFailMessage = 7;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Operand, ";
            sql += "    Sort, ";
            sql += "    FailEvent, ";
            sql += "    FailMessage ";
            sql += "FROM ";
            sql += "    LogicSet ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            parent.ClearLogicSets();

            while (rs.next())
            {
                lset = new Element(rs.getString(colGuid), "logicset");
                lset.SetParent(parent);
                lset.SetParentGuid(rs.getString(colParentGuid));
                lset.SetParentType(rs.getString(colParentType));
                lset.SetOperand(rs.getString(colOperand));
                lset.SetSort(rs.getInt(colSort));
                lset.SetFailEvent(rs.getString(colFailEvent));
                lset.SetFailMessage(rs.getString(colFailMessage));
                lgc.LoadLogicBlocks(cmd, lset);
                parent.AppendLogicSet(lset);
            }

            rs.close();

            // Load an empty to show the logicsets have been loaded
            if (parent.GetLogicSets().size() < 1)
            {
                lset = new Element("", "logicset");
                Element tmpLgc = new Element("", "logic");
                lset.AppendMessage(tmpLgc);
                parent.AppendLogicSet(lset);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneLogicSets(Statement cmd, List<Query> queries,int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        Logic logic = new Logic();
        String sFired = fired + "";
        String sql = "";

        LoadLogicSets(cmd, oldParent);
        if (oldParent.GetLogicSets().size() > 0)
        {
            for (Element oldLset : oldParent.GetLogicSets())
            {
                if (!oldLset.GetGuid().equals(""))
                {
                    Element newLset = new Element(Functions.GetGUID(), "logicset");
                    newLset.SetParentGuid(newParent.GetGuid());
                    newLset.SetParent(newParent);
                    newLset.SetParentType(oldLset.GetParentType());
                    newLset.SetOperand(oldLset.GetOperand());
                    newLset.SetSort(oldLset.GetSort());
                    newLset.SetFailEvent(oldLset.GetFailEvent().replace(oldParentGuid, newParentGuid));
                    newLset.SetFailMessage(oldLset.GetFailMessage().replace(oldParentGuid, newParentGuid));
                    newParent.AppendLogicSet(newLset);

                    logic.CloneLogicBlocks(cmd, queries, fired, oldParentGuid, newParentGuid, oldLset, newLset);
                }
            }

            // Insert queries
            for (Element lset : newParent.GetLogicSets())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(lset.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(lset.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(lset.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(lset.GetOperand()) + "', ";
                sql += "    " + lset.GetSort() + ", ";
                sql += "    '" + Functions.Encode(lset.GetFailEvent()) + "' ";
                sql += "    '" + Functions.Encode(lset.GetFailMessage()) + "' ";
                queries.add(new Query("LogicSet", sql, false));
            }
        }
    }

    public void InsertLogicSets(Statement cmd, List<Element> logicSets)
    {
        String sql = "";
        String sqlLset = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "LogicSet ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    Operand, ";
        sql += "    Sort, ";
        sql += "    FailEvent, ";
        sql += "    FailMessage ";
        sql += ") ";

        for (Element lset : logicSets)
        {
            if (!lset.GetGuid().equals(""))
            {
                if (!sqlLset.equals("")) sqlLset += " UNION ";
                sqlLset += "SELECT ";
                sqlLset += "    '" + Functions.Encode(lset.GetGuid()) + "', ";
                sqlLset += "    '" + Functions.Encode(lset.GetParentGuid()) + "', ";
                sqlLset += "    '" + Functions.Encode(lset.GetParentType()) + "', ";
                sqlLset += "    '" + Functions.Encode(lset.GetOperand()) + "', ";
                sqlLset += "    " + lset.GetSort() + ", ";
                sqlLset += "    '" + Functions.Encode(lset.GetFailEvent()) + "', ";
                sqlLset += "    '" + Functions.Encode(lset.GetFailMessage()) + "' ";
            }
        }

        if (!sqlLset.equals(""))
        {
            sql = sql + sqlLset + ";";
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
