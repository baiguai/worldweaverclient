import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    COMMAND HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Command
{
    /*
        Top level Elements - Rooms, Player, NPCs should call this initially,
        it will then cascade down.
    */
    public List<String> Parse_Commands(Statement cmd, Element parent, Input input)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Element game = parent.GetGame(parent);
        Command command = new Command();
        LogicSet lset = new LogicSet();
        ActionSet actionSet = new ActionSet();
        MessageSet messageSet = new MessageSet();
        Event evt = new Event();
        Object object = new Object();
        Response resp = new Response();

        command.LoadCommands(cmd, parent);

        for (Element c : parent.GetCommands())
        {
            if (c.GetGuid().equals("")) continue;

            c.SetSyntax(c.FixAliases(c.GetSyntax(), c));

            if (Functions.RegMatch(c.GetSyntax(), input.GetUserInput()))
            {
                lset.LoadLogicSets(cmd, c);
                resp = lset.Parse_LogicSets(cmd, c.GetLogicSets());
                if (!resp.GetResult().equals(resp._success) && resp.HasOutput())
                {
                    if (Functions.ListHasData(resp.GetOutput()))
                    {
                        output.addAll(resp.GetOutput());
                    }
                }

                if (resp.GetResult().equals(resp._success))
                {
                    // Process MessageSets
                    tmp = new ArrayList<String>();
                    tmp.addAll(messageSet.Parse_MessageSets(cmd, c));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    // Process ActionSets
                    tmp = new ArrayList<String>();
                    tmp.addAll(actionSet.Parse_ActionSets(cmd, input, c));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    // Process ticks
                    tmp = new ArrayList<String>();
                    tmp.addAll(evt.Parse_Events(cmd, game, "tick"));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    return output;
                }
            }
        }

        if (game.GetIsPlaying())
        {
            // Process the parent's Objects Commands
            for (Element obj : parent.GetObjects())
            {
                tmp = new ArrayList<String>();
                tmp.addAll(command.Parse_Commands(cmd, obj, input));

                if (Functions.ListHasData(tmp))
                {
                    output.addAll(tmp);
                }

                if (output.size() > 0) break;

                // Parse the child Objects
                object.LoadObjects(cmd, obj);
                for (Element ch : obj.GetObjects())
                {
                    tmp = new ArrayList<String>();
                    tmp.addAll(command.Parse_Commands(cmd, ch, input));

                    if (Functions.ListHasData(tmp))
                    {
                        output.addAll(tmp);
                    }

                    if (output.size() > 0) break;
                }
            }
        }

        return output;
    }


    public void LoadCommands(Statement cmd, Element parent)
    {
        Element command = null;
        String sql = "";

        if (parent.GetCommands().size() > 0) return;

        int colGuid = 1;
        int colParentGuid = 2;
        int colParentType = 3;
        int colSyntax = 4;
        int colFileName = 5;
        int colSort = 6;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Syntax, ";
            sql += "    FileName, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Command ";
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
                command = new Element(rs.getString(colGuid), "command");
                command.SetParent(parent);
                command.SetParentGuid(rs.getString(colParentGuid));
                command.SetParentType(rs.getString(colParentType));
                command.SetSyntax(rs.getString(colSyntax));
                command.SetFileName(rs.getString(colFileName));
                command.SetSort(rs.getInt(colSort));
                parent.AppendCommand(command);
            }

            // Create an empty to show that Commands have been loaded
            if (parent.GetCommands().size() < 1)
            {
                command = new Element("", "command");
                parent.AppendCommand(command);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneCommands(Statement cmd, List<Query> queries, int fired, Element oldParent, Element newParent)
    {
        LogicSet logicSet = new LogicSet();
        ActionSet actionSet = new ActionSet();
        MessageSet messageSet = new MessageSet();
        String sFired = fired + "";
        String sql = "";

        LoadCommands(cmd, oldParent);
        if (oldParent.GetCommands().size() > 0)
        {
            for (Element oldCmd : oldParent.GetCommands())
            {
                if (!oldCmd.GetGuid().equals(""))
                {
                    Element newCmd = new Element(Functions.GetGUID(), "command");
                    newCmd.SetParentGuid(newParent.GetGuid());
                    newCmd.SetParent(newParent);
                    newCmd.SetParentType(oldCmd.GetParentType());
                    newCmd.SetSyntax(oldCmd.GetSyntax().replace("{fired}", sFired));
                    newCmd.SetSort(oldCmd.GetSort());
                    newParent.AppendCommand(newCmd);

                    logicSet.CloneLogicSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldCmd, newCmd);
                    actionSet.CloneActionSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldCmd, newCmd);
                    messageSet.CloneMessageSets(cmd, queries, fired, oldParent.GetGuid(), newParent.GetGuid(), oldCmd, newCmd);
                }
            }

            // Insert the queries
            for (Element comm : newParent.GetCommands())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(comm.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(comm.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(comm.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(comm.GetSyntax()) + "', ";
                sql += "    " + comm.GetSort() + " ";
                queries.add(new Query("Command", sql, false));
            }
        }
    }

    public void InsertCommands(Statement cmd, List<Element> commands)
    {
        String sql = "";
        String sqlCmd = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Command ( ";
        sql += "    GUID, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    Syntax, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element comm : commands)
        {
            if (!comm.GetGuid().equals(""))
            {
                if (!sqlCmd.equals("")) sqlCmd += " UNION ";
                sqlCmd += "SELECT ";
                sqlCmd += "     '" + Functions.Encode(comm.GetGuid()) + "', ";
                sqlCmd += "     '" + Functions.Encode(comm.GetParentGuid()) + "', ";
                sqlCmd += "     '" + Functions.Encode(comm.GetParentType()) + "', ";
                sqlCmd += "     '" + Functions.Encode(comm.GetSyntax()) + "', ";
                sqlCmd += "     " + comm.GetSort() + " ";
            }
        }

        if (!sqlCmd.equals(""))
        {
            sql = sql + sqlCmd + ";";
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
