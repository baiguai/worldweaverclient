import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitTest
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private List<UnitStep> steps = null;
        public List<UnitStep> GetSteps() { if (steps == null) steps = new ArrayList<UnitStep>(); return steps; }
        public void SetSteps(List<UnitStep> val) { steps = val; }
        public void AddStep(UnitStep val) { GetSteps().add(val); }
        public void ClearSteps() { steps = null; }

        private Parser parser = null;
        public Parser GetParser() { return parser; }
        public void SetParser(Parser val) { parser = val; }
    //

    public UnitTest(Statement cmd, String guidIn)
    {
        SetGuid(guidIn);
        LoadSteps(cmd);
    }


    public void LoadSteps(Statement cmd)
    {
        UnitStep stp = null;
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
            sql += "    Step ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                stp = new UnitStep(rs.getString(colGuid), rs.getString(colName));
                AddStep(stp);
            }

            rs.close();

            for (UnitStep st : GetSteps())
            {
                st.PopulateStep(cmd);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }



    public void RunTest(Statement cmd)
    {
        if (GetParser() == null) return;

        for (UnitStep s : GetSteps())
        {
            GetParser().ClearTestOutput();
            s.RunStep(cmd, GetParser());
        }
    }
}
