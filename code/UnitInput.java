import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitInput
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private String text = "";
        public String GetText() { return text; }
        public void SetText(String val) { text = val; }

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

    public UnitInput(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitInput(String guidIn, String textIn)
    {
        SetGuid(guidIn);
        SetText(textIn);
    }

    public void PopulateInput(Statement cmd)
    {
        LoadEvals(cmd);
        LoadResults(cmd);
    }

    public void LoadEvals(Statement cmd)
    {
        UnitEval ev = null;
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
                ev = new UnitEval(rs.getString(colGuid));
                AddEval(ev);
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



    public void RunInput(Statement cmd, Parser parser)
    {
        String[] cmdArr = GetText().split("\\n");
        for (String c : cmdArr)
        {
            parser.CallListener(c, true);
        }

        boolean passed = true;
        boolean tmp = true;

        // Parse the Evals - if any
        if (HasEvals())
        {
            for (UnitEval e : GetEvals())
            {
                tmp = e.RunEval(cmd, parser);
                if (!tmp)
                {
                    passed = false;
                    break;
                }
            }
        }

        // Parse the Results
        if (HasResults())
        {
            for (UnitResult r : GetResults())
            {
                r.RunResult(cmd, parser, passed);
            }
        }
    }
}
