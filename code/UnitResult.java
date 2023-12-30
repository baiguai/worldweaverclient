import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitResult
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private List<UnitResultMessage> resultMessages = null;
        public List<UnitResultMessage> GetResultMessages() { if (resultMessages == null) resultMessages = new ArrayList<UnitResultMessage>(); return resultMessages; }
        public void SetResultMessages(List<UnitResultMessage> val) { resultMessages = val; }
        public void AddResultMessage(UnitResultMessage val) { GetResultMessages().add(val); }
        public void ClearResultMessages() { resultMessages = null; }
        public boolean HasResultMessages() { return GetResultMessages().size() > 0; }
    //

    public UnitResult(String guidIn)
    {
        SetGuid(guidIn);
    }

    public void PopulateResult(Statement cmd)
    {
        LoadResultMessages(cmd);
    }


    public void LoadResultMessages(Statement cmd)
    {
        UnitResultMessage msg = null;
        String sql = "";

        int colGuid = 1;
        int colFail = 2;
        int colText = 3;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Fail, ";
            sql += "    Text ";
            sql += "FROM ";
            sql += "    ResultMessage ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                msg = new UnitResultMessage(rs.getString(colGuid), (rs.getInt(colFail) == 1), rs.getString(colText));
                AddResultMessage(msg);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }



    public void RunResult(Statement cmd, Parser parser, boolean passed)
    {
        if (HasResultMessages())
        {
            for (UnitResultMessage msg : GetResultMessages())
            {
                msg.RunResultMessage(cmd, parser, passed);
            }
        }
    }
}
