import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitStep
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private String name = "";
        public String GetName() { return name; }
        public void SetName(String val) { name = val; }

        private List<UnitInput> inputs = null;
        public List<UnitInput> GetInputs() { if (inputs == null) inputs = new ArrayList<UnitInput>(); return inputs; }
        public void SetInputs(List<UnitInput> val) { inputs = val; }
        public void AddInput(UnitInput val) { GetInputs().add(val); }
        public void ClearInputs() { inputs = null; }

        private List<UnitAssert> asserts = null;
        public List<UnitAssert> GetAsserts() { if (asserts == null) asserts = new ArrayList<UnitAssert>(); return asserts; }
        public void SetAsserts(List<UnitAssert> val) { asserts = val; }
        public void AddAssert(UnitAssert val) { GetAsserts().add(val); }
        public void ClearAsserts() { asserts = null; }
    //

    public UnitStep(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitStep(String guidIn, String nameIn)
    {
        SetGuid(guidIn);
        SetName(nameIn);
    }

    public void PopulateStep(Statement cmd)
    {
        LoadInputs(cmd);
        LoadAsserts(cmd);
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

    public void LoadAsserts(Statement cmd)
    {
        UnitAssert ast = null;
        String sql = "";

        int colGuid = 1;
        int colName = 2;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Name ";
            sql += "FROM ";
            sql += "    Assert ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                ast = new UnitAssert(rs.getString(colGuid), rs.getString(colName));
                AddAssert(ast);
            }

            rs.close();

            for (UnitAssert a : GetAsserts())
            {
                a.PopulateAssert(cmd);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }



    public void RunStep(Statement cmd, Parser parser)
    {
        Functions.OutputRaw("\n- Step: " + GetName() + " -");

        // First execute the input
        for (UnitInput i : GetInputs())
        {
            i.RunInput(cmd, parser);
        }

        // Then execute the asserts
        for (UnitAssert a : GetAsserts())
        {
            a.RunAssert(cmd, parser);
        }
    }
}
