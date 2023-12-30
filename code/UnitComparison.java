import java.sql.*;
import java.util.*;
import java.util.Random;

public class UnitComparison
{
    /* PROPERTIES */
    //
        private String guid = "";
        public String GetGuid() { return guid; }
        public void SetGuid(String val) { guid = val; }

        private String operand = "";
        public String GetOperand() { return operand; }
        public void SetOperand(String val) { operand = val; }

        private String checkText = "";
        public String GetCheckText() { return checkText; }
        public void SetCheckText(String val) { checkText = val; }
    //

    public UnitComparison(String guidIn)
    {
        SetGuid(guidIn);
    }
    public UnitComparison(String guidIn, String operandIn)
    {
        SetGuid(guidIn);
        SetOperand(operandIn);
    }
    public UnitComparison(String guidIn, String operandIn, String checkTextIn)
    {
        SetGuid(guidIn);
        SetOperand(operandIn);
        SetCheckText(checkTextIn);
    }



    public boolean RunComparison(Statement cmd, Parser parser)
    {
        boolean passed = true;

        switch (GetOperand().toLowerCase())
        {
            case "contains":
                passed = DoContains(parser.GetTestOutput(), GetCheckText());
                break;

            case "equals":
                passed = DoEquals(parser.GetTestOutput(), GetCheckText());
                break;

            case "notcontains":
                passed = !DoContains(parser.GetTestOutput(), GetCheckText());
                break;

            case "notequals":
                passed = !DoEquals(parser.GetTestOutput(), GetCheckText());
                break;
        }

        return passed;
    }


    public boolean DoContains(List<String> output, String check)
    {
        boolean contains = false;

        if (output.size() < 1) return contains;

        for (String s : output)
        {
            if (s.indexOf(check) >= 0)
            {
                contains = true;
                break;
            }
        }

        return contains;
    }

    public boolean DoEquals(List<String> output, String check)
    {
        boolean equals = false;

        if (output.size() != 1) return equals;

        if (output.get(0).equals(check)) equals = true;

        return equals;
    }
}
