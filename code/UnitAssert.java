import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitAssert
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
        public boolean HasInputs() { return GetInputs().size() > 0; }

        private List<UnitEval> evals = null;
        public List<UnitEval> GetEvals() { if (evals == null) evals = new ArrayList<UnitEval>(); return evals; }
        public void SetEvals(List<UnitEval> val) { evals = val; }
        public void AddEval(UnitEval val) { GetEvals().add(val); }
        public void ClearEvals() { evals = null; }
        public boolean HasEvals() { return GetEvals().size() > 0; }

        private List<UnitResult> results = null;
        public List<UnitResult> GetResults() { if (results == null) results = new ArrayList<UnitResult>(); return results; }
        public void SetResults(List<UnitResult> val) { results = val; }
        public void AddResult(UnitResult val) { GetResults().add(val); }
        public void ClearResults() { results = null; }
        public boolean HasResults() { return GetResults().size() > 0; }
    //

    public UnitAssert(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitAssert(String guidIn, String nameIn)
    {
        SetGuid(guidIn);
        SetName(nameIn);
    }

    public void PopulateAssert(Statement cmd)
    {
        LoadInputs(cmd);
        LoadEvals(cmd);
        LoadResults(cmd);
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

    public void LoadEvals(Statement cmd)
    {
        UnitEval eva = null;
        String sql = "";

        int colGuid = 1;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID ";
            sql += "FROM ";
            sql += "    Eval ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                eva = new UnitEval(rs.getString(colGuid));
                AddEval(eva);
            }

            rs.close();

            for (UnitEval e : GetEvals())
            {
                e.PopulateEval(cmd);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void LoadResults(Statement cmd)
    {
        UnitResult res = null;
        String sql = "";

        int colGuid = 1;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID ";
            sql += "FROM ";
            sql += "    Result ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                res = new UnitResult(rs.getString(colGuid));
                AddResult(res);
            }

            rs.close();

            for (UnitResult r : GetResults())
            {
                r.PopulateResult(cmd);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }




    public void RunAssert(Statement cmd, Parser parser)
    {
        boolean pass = true;
        if (!HasInputs()) return;

        for (UnitInput i : GetInputs())
        {
            i.RunInput(cmd, parser);
        }

        if (!HasEvals()) return;

        for (UnitEval e : GetEvals())
        {
            pass = e.RunEval(cmd, parser);
            if (!pass) break;
        }

        Functions.OutputRaw("\n-- ASSERT: " + GetName());

        if (pass)
        {
            Functions.OutputRaw("\n[PASSED]");
        }
        else
        {
            Functions.OutputRaw("\n[FAILED]");
        }

        if (!HasResults()) return;

        for (UnitResult r : GetResults())
        {
            r.RunResult(cmd, parser, pass);
        }
    }
}
