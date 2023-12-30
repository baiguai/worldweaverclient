import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    QUERY
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Query
{
    /* PROPERTIES */
    //
        private String table = "";
        public String GetTable() { return table; }
        public void SetTable(String val) { table = val; }

        private boolean isHead = false;
        public boolean GetIsHead() { return isHead; }
        public void SetIsHead(boolean val) { isHead = val; }

        private String sql = "";
        public String GetSql() { return sql; }
        public void SetSql(String val) { sql = val; }
    //


    public Query() {}
    public Query(String tableIn, String sqlIn)
    {
        SetTable(tableIn);
        SetSql(sqlIn);
    }
    public Query(String tableIn, String sqlIn, boolean isHeadIn)
    {
        SetTable(tableIn);
        SetSql(sqlIn);
        SetIsHead(isHeadIn);
    }



    // Add Head
    public void AddHeads(List<Query> queries)
    {
        String table = "";
        String head = "";

        table = "Action";
        head = "INSERT INTO Action (GUID, ParentGUID, Type, Source, NewValue, Repeat, Sort) ";
        queries.add(new Query(table, head, true));

        table = "ActionSet";
        head = "INSERT INTO ActionSet (GUID, ParentGUID, ParentType, RepeatCount, Sort) ";
        queries.add(new Query(table, head, true));

        table = "Attribute";
        head = "INSERT INTO Attribute (GUID, Type, ParentGUID, ParentType, Value, Sort) ";
        queries.add(new Query(table, head, true));

        table = "Command";
        head = "INSERT INTO Command (GUID, ParentGUID, ParentType, Syntax, Sort) ";
        queries.add(new Query(table, head, true));

        table = "Event";
        head = "INSERT INTO Event (GUID, ParentGUID, ParentType, Type, InitialTime, IntervalMinutes, RepeatCount, Sort) ";
        queries.add(new Query(table, head, true));

        table = "Logic";
        head = "INSERT INTO Logic (LogicSetGUID, Type, Source, SourceValue, NewValue, Operand, Eval, Sort) ";
        queries.add(new Query(table, head, true));

        table = "LogicSet";
        head = "INSERT INTO LogicSet (GUID, ParentGUID, ParentType, Operand, Sort, FailMessage) ";
        queries.add(new Query(table, head, true));

        table = "Message";
        head = "INSERT INTO Message (MessageSetGUID, Output, Sort) ";
        queries.add(new Query(table, head, true));

        table = "MessageSet";
        head = "INSERT INTO MessageSet (GUID, ParentGUID, ParentType, RepeatType, Sort) ";
        queries.add(new Query(table, head, true));

        table = "Object";
        head = "INSERT INTO Object (GUID, ParentGUID, ParentType, Type, Chance, Label, Meta, Sort, Inherit, Count) ";
        queries.add(new Query(table, head, true));
    }



    // Run Queries
    public void RunQueries(Statement cmd, List<Query> queries)
    {
        String head = "";
        String sql = "";
        String curTable = "";
        List<String> tables = new ArrayList<String>();

        try
        {
            for (Query q : queries)
            {
                // Go through all the head queries, grouping by table
                if (q.GetIsHead())
                {
                    if (!q.GetTable().equals(curTable) && !tables.contains(q.GetTable()))
                    {
                        curTable = q.GetTable();
                        tables.add(curTable);
                    }
                }
            }

            // Get the head and queries for each table
            for (String tbl : tables)
            {
                head = GetHead(queries, tbl);
                sql = "";

                // Only worry about queries with heads
                if (!head.equals(""))
                {
                    for (Query q : queries)
                    {
                        // Get all the non-head queries for the current table
                        if (!q.GetIsHead() && q.GetTable().equals(tbl))
                        {
                            if (!sql.equals("")) sql += " UNION ";
                            sql += q.GetSql();
                        }
                    }

                    if (sql.equals("")) continue;

                    sql += ";";

                    sql = head + sql;

                    // Run the table's queries
                    cmd.execute(sql);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }
    public String GetHead(List<Query> queries, String table)
    {
        String output = "";

        for (Query q : queries)
        {
            if (q.GetTable().equals(table) && q.GetIsHead())
            {
                output = q.GetSql();
                break;
            }
        }

        return output;
    }
}
