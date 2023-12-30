import java.io.File;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Random;

/*
    RESPONSE HELPER METHODS
    ----------------------------------------------------------------------------
    Used to return information from the various parsers in the client.
    ----------------------------------------------------------------------------
*/
public class Response
{
    /* Properties */
    //
        public String _none = "NONE";
        public String _success = "SUCCESS";
        public String _fail = "FAIL";
        public String _error = "ERROR";
        private boolean _overrideClear = false;
        public boolean GetOverrideClear() { return _overrideClear; }
        public void SetOverrideClear(boolean val) { _overrideClear = val; }

        public boolean HasResult()
        {
            if (GetResult().equals(_none)) return false;
            else return true;
        }

        private boolean doTrim = true;
        public boolean GetDoTrim() { return doTrim; }
        public void SetDoTrim(boolean val) { doTrim = val; }

        private String result = "NONE";
        public String GetResult() { return result; }
        public void SetResult(String val)
        {
            // Ensure the value is a legal response
            if (!val.equals(_success) &&
                !val.equals(_fail) &&
                !val.equals(_error))
            {
                Functions.Error("Bad result value: " + val);
                val = "ERROR";
            }

            result = val;
        }

        private boolean doCmd = false;
        public boolean GetDoCmd() { return doCmd; }
        public void SetDoCmd(boolean val) { doCmd = val; }

        private String cmdLevel = "";
        public String GetCmdLevel() { return cmdLevel; }
        public void SetCmdLevel(String val) { cmdLevel = val; }

        private String cmdToRun = "";
        public String GetCmdToRun() { return cmdToRun; }
        public void SetCmdToRun(String val) { cmdToRun = val; }

        private List<String> output = null;
        public List<String> GetOutput() { if (output == null) output = new ArrayList<String>(); return output; }
        public String GetOutputString()
        {
            return GetOutputString(true);
        }
        public String GetOutputString(boolean trimOutput)
        {
            String output = "";
            String br = System.getProperty("line.separator");
            String[] ln = null;
            int indentLevel = 0;
            String indent = "";

            for (String s : GetOutput())
            {
                ln = s.split(br);

                for (String l : ln)
                {
                    if (!output.equals("")) output += "\n";
                    if (l.trim().equals("---")) l = "--------------------------------------------------------------------------------";
                    if (l.trim().equals(">>"))
                    {
                        indentLevel = indentLevel + 1;
                    }
                    if (l.trim().equals("<<"))
                    {
                        indentLevel = indentLevel - 1;
                    }

                    indent = "";
                    for (int i = 0; i < indentLevel; i++)
                    {
                        indent += "    ";
                    }

                    if (!l.trim().equals(">>") && !l.trim().equals("<<"))
                    {
                        if (trimOutput)
                        {
                            output += indent + l.trim();
                        }
                        else
                        {
                            output += indent + l;
                        }
                    }
                }
            }

            return output;
        }
        public void SetOutput(List<String> val) { output = val; }
        public void SetOutput(String val) { ClearOutput(); GetOutput().add(val); }
        public void AppendOutput(String val) { GetOutput().add(val); }
        public void AppendOutput(List<String> val) { GetOutput().addAll(val); }
        public void ClearOutput() { output = null; }
        public int GetOutputSize() { return GetOutput().size(); }
        public boolean HasOutput()
        {
            boolean output = false;

            if (GetOutput().size() > 1) output = true;
            if (GetOutput().size() == 1 && !GetOutput().get(0).trim().equals("")) output = true;

            return output;
        }
    //
}
