import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitResultMessage
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private boolean fail = false;
        public boolean GetFail() { return fail; }
        public void SetFail(boolean val) { fail = val; }

        private String text = "";
        public String GetText() { return text; }
        public void SetText(String val) { text = val; }

        private List<UnitInput> inputs = null;
        public List<UnitInput> GetInputs() { if (inputs == null) inputs = new ArrayList<UnitInput>(); return inputs; }
        public void SetInputs(List<UnitInput> val) { inputs = val; }
        public void AddInput(UnitInput val) { GetInputs().add(val); }
        public void ClearInputs() { inputs = null; }
        public boolean HasInputs() { return GetInputs().size() > 0; }
    //

    public UnitResultMessage(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitResultMessage(String guidIn, boolean failIn)
    {
        SetGuid(guidIn);
        SetFail(failIn);
    }
    public UnitResultMessage(String guidIn, boolean failIn, String textIn)
    {
        SetGuid(guidIn);
        SetFail(failIn);
        SetText(textIn);
    }

    public void PopulateResultMessage(Statement cmd)
    {
        LoadInputs(cmd);
    }


    public void LoadInputs(Statement cmd)
    {
        UnitInput inp = null;
        String sql = "";

        int colGuid = 1;
        int colText = 2;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Text ";
            sql += "FROM ";
            sql += "    Input ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                inp = new UnitInput(rs.getString(colGuid), rs.getString(colText));
                AddInput(inp);
            }

            rs.close();

            for (UnitInput i : GetInputs())
            {
                i.PopulateInput(cmd);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }




    public void RunResultMessage(Statement cmd, Parser parser, boolean passed)
    {
        Parser tmp = parser;
        tmp.ClearTestOutput();
        List<String> output = new ArrayList<String>();
        int wrap = 80;

        // Get the wrap_width setting from the Game
        if (parser.GetGame() != null)
        {
            Attribute attribute = new Attribute();
            Element attr = attribute.GetAttribute(parser.GetGame().GetGlobal().GetGameCmd(), parser.GetGame().GetGame(), "wrap_width");
            if (attr != null)
            {
                try
                {
                    wrap = Integer.parseInt(attr.GetValue());
                }
                catch (Exception ex) {}
            }
        }

        if (!passed == GetFail())
        {
            if (!HasInputs())
            {
                output.add(GetText());
            }
            else
            {
                for (UnitInput i : GetInputs())
                {
                    i.RunInput(cmd, tmp);
                    if (tmp.GetTestOutput().size() > 0)
                    {
                        output.addAll(Functions.CleanList(tmp.GetTestOutput()));
                        tmp.ClearTestOutput();
                    }
                }
            }
        }

        // Output the message
        Functions.Output(output);
    }
}
