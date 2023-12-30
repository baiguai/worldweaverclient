import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitEval
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private boolean pass = true;
        public boolean GetPass() { return pass; }
        public void SetPass(boolean val) { pass = val; }

        private List<UnitComparison> comparisons = null;
        public List<UnitComparison> GetComparisons() { if (comparisons == null) comparisons = new ArrayList<UnitComparison>(); return comparisons; }
        public void SetComparisons(List<UnitComparison> val) { comparisons = val; }
        public void AddComparison(UnitComparison val) { GetComparisons().add(val); }
        public void ClearComparisons() { comparisons = null; }
        public boolean HasComparisons() { return GetComparisons().size() > 0; }
    //

    public UnitEval(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitEval(String guidIn, boolean passIn)
    {
        SetGuid(guidIn);
        SetPass(passIn);
    }

    public void PopulateEval(Statement cmd)
    {
        LoadComparisons(cmd);
    }

    public void LoadComparisons(Statement cmd)
    {
        UnitComparison cmp = null;
        String sql = "";

        int colGuid = 1;
        int colOperand = 2;
        int colCheckText = 3;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Operand, ";
            sql += "    CheckText ";
            sql += "FROM ";
            sql += "    Comparison ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                cmp = new UnitComparison(rs.getString(colGuid), rs.getString(colOperand), rs.getString(colCheckText));
                AddComparison(cmp);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }



    public boolean RunEval(Statement cmd, Parser parser)
    {
        boolean passed = true;
        boolean tmp = true;

        if (HasComparisons())
        {
            for (UnitComparison c : GetComparisons())
            {
                tmp = true;
                tmp = c.RunComparison(cmd, parser);
                if (!tmp) passed = false;
            }
        }

        return passed;
    }
}
