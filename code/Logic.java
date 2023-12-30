import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/**
    Performs Logic block evaluations.
*/

public class Logic
{
    public boolean Parse_Logic(Statement cmd, Element logic, Element parent)
    {
        boolean failed = false;

        if (!logic.GetEval().equals(""))
        {
            failed = ProcessEval(cmd, logic.GetEval(), parent);
        }
        else
        {
            failed = ProcessInlineLogic(cmd, logic);
        }

        return failed;
    }


    public void LoadLogicBlocks(Statement cmd, Element parent)
    {
        Element lgc = null;
        String sql = "";
        String guid = "";

        if (parent.HasLogicBlocks()) return;

        int colLogicSetGuid = 1;
        int colType = 2;
        int colSource = 3;
        int colSourceValue = 4;
        int colNewValue = 5;
        int colOperand = 6;
        int colEval = 7;
        int colSort = 8;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    LogicSetGUID, ";
            sql += "    Type, ";
            sql += "    Source, ";
            sql += "    SourceValue, ";
            sql += "    NewValue, ";
            sql += "    Operand, ";
            sql += "    Eval, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Logic ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND LogicSetGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                guid = Functions.GetGUID();
                lgc = new Element(guid, "logic");
                lgc.SetParent(parent);
                lgc.SetParentGuid(parent.GetGuid());
                lgc.SetParentType("logicset");
                lgc.SetType(rs.getString(colType));
                lgc.SetSource(rs.getString(colSource));
                lgc.SetSourceValue(rs.getString(colSourceValue));
                lgc.SetNewValue(rs.getString(colNewValue));
                lgc.SetOperand(rs.getString(colOperand));
                lgc.SetEval(rs.getString(colEval));
                lgc.SetSort(rs.getInt(colSort));
                parent.AppendLogic(lgc);
            }

            rs.close();

            // Load an empty to show logic has been loaded
            if (parent.GetLogicBlocks().size() < 1)
            {
                lgc = new Element("", "logic");
                parent.AppendLogic(lgc);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }



    // Process the inline logic
    public boolean ProcessInlineLogic(Statement cmd, Element logic)
    {
        boolean output = false;
        String type = logic.GetType().trim();
        String operand = logic.GetOperand().trim();
        String source = logic.GetSource().trim();
        String sourceVal = logic.GetSourceValue().trim();
        Global_Data global = new Global_Data();
        String armed = "";

        source = logic.FixAliases(source, logic);
        sourceVal = logic.FixAliases(sourceVal, logic);

        if (!output && source.equals("{inventory}"))
        {
            output = DoInventoryAttributeLogic(cmd, source, sourceVal, operand, logic);
        }

        if (!output && source.equals("{room}"))
        {
            output = DoRoomAttributeLogic(cmd, source, sourceVal, operand, logic);
        }

        if (!output && type.equals("armed"))
        {
            output = DoArmed(logic, source);
        }

        if (!output && type.equals("attribute"))
        {
            source = global.GetAttributeValue(cmd, source);
            output = DoAttributeLogic(source, sourceVal, operand, "");
        }
        if (!output && type.equals("location"))
        {
            output = DoLocationLogic(cmd, source, sourceVal, operand, logic);
        }
        if (!output && type.equals("random"))
        {
            output = DoRandomCheck(source);
        }

        return output;
    }

    // Process the Eval block
    public boolean ProcessEval(Statement cmd, String eval, Element parent)
    {
        boolean handled = false;
        boolean passed = true;
        String[] lines = eval.split(System.getProperty("line.separator"));
        List<String> expr = new ArrayList<String>();
        String type = "";
        String source = "";
        String checkValue = "";
        String mod = "";
        String operand = "";
        String result = "";
        Global_Data global = new Global_Data();

        // Iterate through the expressions
        for (String exprLine : lines)
        {
            String tmp = exprLine.trim().toLowerCase();

            if (tmp.equals("and")) tmp = "&&";
            if (tmp.equals("or")) tmp = "||";

            // Do not parse the operands
            if (tmp.equals("&&") ||
                tmp.equals("||")
            )
            {
                expr.add(tmp);
            }
            else
            {
                // Process the line
                handled = false;

                // XML cleanup
                exprLine = exprLine.replace("_lt_", "<");
                exprLine = exprLine.replace("_gt_", ">");
                exprLine = exprLine.replace("_lte_", "<=");
                exprLine = exprLine.replace("_gte_", ">=");

                exprLine = exprLine.replace("@", "_type_");

                String[] arr = exprLine.split("_type_", 2);
                String[] arr2 = null;

                try
                {
                    type = arr[0].trim().toLowerCase();

                    if (arr.length > 1)
                    {
                        // %
                        if (!handled)
                        {
                            arr2 = arr[1].replace("%", "_mod_").split("_mod_", 2);
                            if (arr2.length == 2)
                            {
                                String[] arrMod = arr2[1].split("=", 2);
                                if (arrMod.length == 2)
                                {
                                    mod = arrMod[0];
                                    arr2 = (arr2[0] + "," + arrMod[1]).split(",", 2);
                                    if (arr2.length == 2)
                                    {
                                        handled = true;
                                        operand = "mod";
                                    }
                                }
                            }
                        }

                        // <=
                        if (!handled)
                        {
                            arr2 = arr[1].split("<=", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = "<=";
                            }
                        }

                        // >=
                        if (!handled)
                        {
                            arr2 = arr[1].split(">=", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = ">=";
                            }
                        }

                        // <
                        if (!handled)
                        {
                            arr2 = arr[1].split("<", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = "<";
                            }
                        }

                        // !=
                        if (!handled)
                        {
                            arr2 = arr[1].split("!=", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = "!=";
                            }
                        }

                        // >
                        if (!handled)
                        {
                            arr2 = arr[1].split(">", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = ">";
                            }
                        }

                        // =
                        if (!handled)
                        {
                            arr2 = arr[1].split("=", 2);
                            if (arr2.length == 2)
                            {
                                handled = true;
                                operand = "=";
                            }
                        }

                        // Set the source if there is no operand
                        if (!handled)
                        {
                            source = arr[1];
                        }
                    }


                    // Operand found
                    if (handled)
                    {
                        source = arr2[0].trim();
                        checkValue = arr2[1].trim();
                    }

                    if (handled ||
                        type.equals("light_source_on") ||
                        type.equals("light_source_off") ||
                        type.equals("armed") ||
                        type.equals("random"))
                    {
                        // Fix the Aliases
                        checkValue = parent.FixAliases(checkValue, parent);
                        source = parent.FixAliases(source, parent);

                        // Evaluate by type
                        switch (type)
                        {
                            case "armed":
                                result = DoArmed(parent, source) + "";
                                break;

                            case "attribute":
                                source = global.GetAttributeValue(cmd, source);
                                source = parent.FixAliases(source, parent);
                                result = DoAttributeLogic(source, checkValue, operand, mod) + "";
                                break;

                            case "location":
                                result = DoLocationLogic(cmd, source, checkValue, operand, parent) + "";
                                break;

                            case "random":
                                source = arr2[0].trim();
                                result = DoRandomCheck(source) + "";
                                break;
                        }

                        expr.add(result);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    result = "false";
                }
            }
        }


        // Go through the processed results
        passed = true;
        String prevOp = "&&";

        for (String x : expr)
        {
            // Operand row
            if (!x.toLowerCase().equals("true") &&
                !x.toLowerCase().equals("false"))
            {
                prevOp = x;
            }
            else
            {
                x = x.toLowerCase();

                // Handle ||
                if (prevOp.equals("||"))
                {
                    if (x.equals("true")) passed = true;
                }

                // Handle &&
                if (prevOp.equals("&&"))
                {
                    if (x.equals("false")) passed = false;
                }
            }
        }

        return passed;
    }


    // Perform Logic
    private boolean DoInventoryAttributeLogic(Statement cmd, String source, String sourceVal, String operand, Element parent)
    {
        boolean passed = true;

        String[] sourceValArr = sourceVal.replace("|", "@@@").split("@@@");

        if (sourceValArr.length > 1)
        {
            passed = false;

            for (String s : sourceValArr)
            {
                passed = DoInventoryAttributeLogic(cmd, source, s, operand, parent);
                if (passed) break;
            }
        }
        else
        {
            Element player = parent.GetPlayer(parent);
            Object object = new Object();
            String src = "";

            if (player != null)
            {
                object.LoadObjects(cmd, player);

                for (Element obj : player.GetObjects())
                {
                    src = source.replace("{inventory}", obj.GetGuid());
                    passed = DoAttributeLogic(src, sourceVal, operand, "");
                    if (passed) break;
                }
            }
        }

        return passed;
    }

    private boolean DoRoomAttributeLogic(Statement cmd, String source, String sourceVal, String operand, Element parent)
    {
        boolean passed = true;

        String[] sourceValArr = sourceVal.replace("|", "@@@").split("@@@");

        if (sourceValArr.length > 1)
        {
            passed = false;

            for (String s : sourceValArr)
            {
                passed = DoRoomAttributeLogic(cmd, source, s, operand, parent);
                if (passed) break;
            }
        }
        else
        {
            Element room = parent.GetRoom(parent);
            Object object = new Object();
            String src = "";
            Element player = parent.GetPlayer(parent);
            String armed = "";
            if (player != null)
            {
                armed = player.GetArmedWeapon();
            }

            if (source.equals("{player}")) source = player.GetGuid();
            if (sourceVal.equals("{player}")) sourceVal = player.GetGuid();
            if (source.equals("{armed}")) source = armed;
            if (sourceVal.equals("{armed}")) sourceVal = armed;

            if (room != null)
            {
                object.LoadObjects(cmd, room);

                for (Element obj : room.GetObjects())
                {
                    src = source.replace("{inventory}", obj.GetGuid());
                    passed = DoAttributeLogic(src, sourceVal, operand, "");
                    if (passed) break;
                }
            }
        }

        return passed;
    }

    private boolean DoArmed(Element logic, String source)
    {
        Element player = logic.GetPlayer(logic);
        boolean output = false;

        String[] sourceArr = source.replace("|", "@@@").split("@@@");

        if (sourceArr.length > 1)
        {
            output = false;

            for (String s : sourceArr)
            {
                output = DoArmed(logic, s);
                if (output) break;
            }
        }
        else
        {
            if (player.GetArmedWeapon().equals(source))
            {
                output = true;
            }
        }

        return output;
    }

    private boolean DoAttributeLogic(String source, String checkValue, String operand, String mod)
    {
        boolean passed = true;
        int src = 0;
        int chk = 0;

        // Check multiple values, separated by pipes |
        String[] checkArr = checkValue.replace("|", "@@@").split("@@@");

        if (checkArr.length > 1)
        {
            passed = false;

            for (String s : checkArr)
            {
                passed = DoAttributeLogic(source, s, operand, mod);
                if (passed) break;
            }
        }
        else
        {
            try
            {
                // Try a numeric check
                src = Integer.parseInt(source);
                chk = Integer.parseInt(checkValue);
                passed = NumericComparison(src, chk, operand, mod);
            }
            catch (Exception ex) {
                // Not numeric, try string comperison
                passed = StringComparison(source, checkValue, operand);
            }
        }

        return passed;
    }

    private boolean DoLocationLogic(Statement cmd, String source, String checkLocation, String operand, Element parent)
    {
        boolean passed = true;
        String location = "";
        Global_Data global = new Global_Data();
        Element player = parent.GetPlayer(parent);

        String[] checkLocArr = checkLocation.replace("|", "@@@").split("@@@");

        if (checkLocArr.length > 1)
        {
            passed = false;

            for (String s : checkLocArr)
            {
                passed = DoLocationLogic(cmd, source, s, operand, parent);
                if (passed) break;
            }
        }
        else
        {
            if (checkLocation.equals("{player}")) checkLocation = player.GetGuid();
            if (source.equals("{player}")) source = player.GetGuid();

            if (location.equals(""))
            {
                location = global.GetObjectLocation(cmd, source);
                if (location.equals(""))
                {
                    location = global.GetNpcLocation(cmd, source);
                }
                if (location.equals(""))
                {
                    location = parent.GetPlayerLocation();
                }
            }

            passed = StringComparison(location, checkLocation, operand);
        }

        return passed;
    }

    private boolean DoRandomCheck(String source)
    {
        boolean output = false;
        int max = 50;

        try
        {
            max = Integer.parseInt(source);
        }
        catch (Exception ex)
        {
            max = 50;
        }

        int check = Functions.RandomInt(0, 100);

        if (check <= max) output = true;

        return output;
    }

    // Comparison Methods
    private boolean NumericComparison(int valOne, int valTwo, String operand, String mod)
    {
        boolean passed = true;
        int modInt = 0;

        if (!mod.equals(""))
        {
            try
            {
                modInt = Integer.parseInt(mod);
            }
            catch (Exception ex)
            {}
        }

        switch (operand)
        {
            case "mod":
            case "%":
                if (valOne % modInt != valTwo) passed = false;
                break;

            case "gt":
            case ">":
                if (valOne <= valTwo) passed = false;
                break;

            case "lt":
            case "<":
                if (valOne >= valTwo) passed = false;
                break;

            case "gte":
            case ">=":
                if (valOne < valTwo) passed = false;
                break;

            case "lte":
            case "<=":
                if (valOne > valTwo) passed = false;
                break;

            case "=":
                if (!(valOne + "").equals(valTwo + "")) passed = false;
                break;

            case "!=":
                if ((valOne + "").equals(valTwo + "")) passed = false;
                break;
        }

        return passed;
    }

    private boolean StringComparison(String valOne, String valTwo, String operand)
    {
        boolean passed = true;
        String[] vals = valTwo.replace("|", "@@@").split("@@@");

        if (vals.length > 1)
        {
            passed = false;

            for (String s : vals)
            {
                passed = StringComparison(valOne, s, operand);
                if (passed) break;
            }
        }
        else
        {
            switch (operand)
            {
                case "like":
                    if (!Functions.Match(valOne, valTwo)) passed = false;
                    break;

                case "=":
                    if (!valOne.equals(valTwo)) passed = false;
                    break;

                case "!=":
                    if (valOne.equals(valTwo)) passed = false;
                    break;

                case "in":
                    if (!valOne.equals(valTwo)) passed = false;
                    break;

                case "notin":
                    if (!valOne.equals(valTwo)) passed = false;
                    break;
            }
        }

        return passed;
    }

    public void CloneLogicBlocks(Statement cmd, List<Query> queries, int fired, String oldParentGuid, String newParentGuid, Element oldParent, Element newParent)
    {
        MessageSet messageSet = new MessageSet();
        String sFired = fired + "";
        String sql = "";

        LoadLogicBlocks(cmd, oldParent);
        if (oldParent.GetLogicBlocks().size() > 0)
        {
            for (Element oldLogic : oldParent.GetLogicBlocks())
            {
                if (!oldLogic.GetGuid().equals(""))
                {
                    Element newLogic = new Element(Functions.GetGUID(), "logic");
                    newLogic.SetParentGuid(newParent.GetGuid());
                    newLogic.SetParent(newParent);
                    newLogic.SetType(oldLogic.GetType());
                    newLogic.SetSource(oldLogic.GetSource().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newLogic.SetSourceValue(oldLogic.GetSourceValue().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newLogic.SetNewValue(oldLogic.GetValue().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newLogic.SetOperand(oldLogic.GetOperand());
                    newLogic.SetEval(oldLogic.GetEval().replace("{fired}", sFired).replace(oldParentGuid, newParentGuid));
                    newLogic.SetSort(oldLogic.GetSort());
                    newParent.AppendLogic(newLogic);

                    messageSet.CloneMessageSets(cmd, queries, fired, oldParentGuid, newParentGuid, oldLogic, newLogic);
                }
            }

            // Insert queries
            for (Element lgc : newParent.GetLogicBlocks())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(lgc.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetType()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetSource()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetSourceValue()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetNewValue()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetOperand()) + "', ";
                sql += "    '" + Functions.Encode(lgc.GetEval()) + "', ";
                sql += "    " + lgc.GetSort() + " ";
                queries.add(new Query("Logic", sql, false));
            }
        }
    }

    public void InsertLogicBlocks(Statement cmd, List<Element> logicBlocks)
    {
        String sql = "";
        String sqlLog = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Logic ( ";
        sql += "    LogicSetGUID, ";
        sql += "    Type, ";
        sql += "    Source, ";
        sql += "    SourceValue, ";
        sql += "    NewValue, ";
        sql += "    Operand, ";
        sql += "    Eval, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element log : logicBlocks)
        {
            if (!log.GetGuid().equals(""))
            {
                if (!sqlLog.equals("")) sqlLog += " UNION ";
                sqlLog += "SELECT ";
                sqlLog += "     '" + Functions.Encode(log.GetParentGuid()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetType()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetSource()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetSourceValue()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetNewValue()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetOperand()) + "', ";
                sqlLog += "     '" + Functions.Encode(log.GetEval()) + "', ";
                sqlLog += "     " + log.GetSort() + " ";
            }
        }

        if (!sqlLog.equals(""))
        {
            sql = sql + sqlLog + ";";
        }
        else sql = "";

        try
        {
            if (!sql.equals("")) cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }
}
