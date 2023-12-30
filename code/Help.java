import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    HELP METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Help
{
    public List<String> Parse_Help(Statement cmd, Element parent, Input input)
    {
        Response resp = new Response();
        Element game = parent.GetGame(parent);

        if (!game.GetIsPlaying())
        {
            return resp.GetOutput();
        }

        if (!resp.HasResult())
        {
            resp.SetOutput(ShowHelp(cmd, input));
            if (resp.GetOutput().size() > 0)
            {
                resp.SetResult(resp._success);
            }
        }

        return resp.GetOutput();
    }


    public List<String> ShowHelp(Statement cmd, Input input)
    {
        List<String> output = new ArrayList<String>();
        List<List<String>> res = new ArrayList<List<String>>();
        List<String> tmp = new ArrayList<String>();
        String sql = "";
        String comm = input.GetUserInput();
        String title = "";

        if (comm.indexOf("help") < 0) return output;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    Syntax, ";
            sql += "    Title, ";
            sql += "    Topic ";
            sql += "FROM ";
            sql += "    Help ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND Syntax = '" + Functions.Encode(comm) + "' ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                tmp = new ArrayList<String>();
                tmp.add(rs.getString("Syntax"));
                tmp.add(rs.getString("Title"));
                tmp.add(rs.getString("Topic"));
                res.add(tmp);
            }

            rs.close();

            if (res.size() < 1)
            {
                output.add("No help topics found. Use: --help");
                return output;
            }
            else
            {
                if (res.size() > 1)
                {
                    for (List<String> hlp : res)
                    {
                        if (title.equals(""))
                        {
                            title = "Help topic not found. Use:";
                            output.add(title);
                        }
                        output.add(hlp.get(0));
                    }
                }
                else
                {
                    for (List<String> hlp : res)
                    {
                        if (title.equals(""))
                        {
                            title = hlp.get(1);
                            output.add(title);
                        }
                        output.add(hlp.get(2));
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return output;
    }
}
