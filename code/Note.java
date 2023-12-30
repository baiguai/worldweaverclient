import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    NOTE HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Note 
{
    public List<String> Parse_Notes(Statement cmd, Element parent, Input input)
    {
        Response resp = new Response();
        Element game = parent.GetGame(parent);

        if (!game.GetIsPlaying())
        {
            return resp.GetOutput();
        }

        if (!resp.HasResult() && Functions.RegMatch("notes", input.GetInputCommand()))
        {
            resp.SetResult(resp._success);
            resp.SetOutput(ListNotes(cmd, parent));
        }

        if (!resp.HasResult() && Functions.RegMatch("addnote", input.GetInputCommand()))
        {
            resp.SetResult(resp._success);
            resp.SetOutput(AddNote(cmd, input.GetParamString(), parent));
        }

        if (!resp.HasResult() && Functions.RegMatch("deletenote", input.GetInputCommand()))
        {
            resp.SetResult(resp._success);
            resp.SetOutput(DeleteNote(cmd, input.GetParams(), parent));
        }

        return resp.GetOutput();
    }


    public List<String> AddNote(Statement cmd, String note, Element parent)
    {
        List<String> output = new ArrayList<String>();
        Element player = parent.GetPlayer(parent);
        String sql = "";

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "PlayerNote ( ";
            sql += "    PlayerGUID, ";
            sql += "    Note ";
            sql += ") ";
            sql += "SELECT ";
            sql += "    '" + Functions.Encode(player.GetGuid()) + "', ";
            sql += "    '" + Functions.Encode(note) + "' ";
            sql += ";";

            cmd.execute(sql);

            output.add("Note added.");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return output;
    }

    public List<String> DeleteNote(Statement cmd, String[] params, Element parent)
    {
        List<String> output = new ArrayList<String>();
        List<String> rowIds = new ArrayList<String>();
        Element player = parent.GetPlayer(parent);
        String sql = "";
        int index = -1;
        String rowId = "";

        if (params == null || params.length < 1)
        {
            output.add("To delete a note use: deletenote: <note number>.");
            return output;
        }

        try
        {
            index = Integer.parseInt(params[0].trim());
            index--;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error("param: " + params[0]);
        }

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    ROWID ID ";
            sql += "FROM ";
            sql += "    PlayerNote ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Note ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                rowIds.add(rs.getString("ID"));
            }

            rs.close();

            for (int i = 0; i < rowIds.size(); i++)
            {
                if (i == index)
                {
                    rowId = rowIds.get(i);
                    break;
                }
            }

            if (!rowId.equals(""))
            {
                sql =  "";
                sql += "DELETE FROM ";
                sql += "    PlayerNote ";
                sql += "WHERE 1 = 1 ";
                sql += "    AND ROWID = '" + rowId + "' ";
                sql += ";";

                cmd.execute(sql);

                output.add("Note deleted.");
            }
            else
            {
                output.add("Be sure to specify an existing note's number.");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return output;
    }

    public List<String> ListNotes(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        Element player = parent.GetPlayer(parent);
        String sql = "";
        String title = "";
        int index = 1;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    PlayerGUID, ";
            sql += "    Note ";
            sql += "FROM ";
            sql += "    PlayerNote ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND PlayerGUID = '" + Functions.Encode(player.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Note ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                if (title.equals(""))
                {
                    title = "Notes:";
                    output.add(title);
                }
                output.add(index + " - " + rs.getString("Note"));
                index++;
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
}
