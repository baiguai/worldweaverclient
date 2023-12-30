import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    MESSAGE OUTPUT HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class MessageOutput
{
    public String Parse_Output(Statement cmd, Element msg)
    {
        String output = msg.GetOutput();
        String tmp = "";
        String[] arr = null;
        String[] arrEq = { "" };
        String comparisonType = "equals";
        boolean doUpdate = false;
        int start = 0;
        int end = 0;
        Global_Data global = new Global_Data();

        Element game = msg.GetGame(msg);
        Element player = msg.GetPlayer(msg);
        MessageSet messageSet = new MessageSet();
        Message message = new Message();

        // Attribute Value
        while (output.indexOf("[att]") >= 0)
        {
            doUpdate = false;
            String attribPath = "";
            String attribVal = "";
            start = output.indexOf("[att]");
            tmp = output.substring(start + 5);
            end = tmp.indexOf("[/att]");
            tmp = tmp.substring(0, end);
            attribPath = tmp;

            attribVal = global.GetAttributeValue(cmd, attribPath).trim();

            output = output.replace("[att]" + attribPath + "[/att]", attribVal);
        }

        // Inventory List
        while (output.indexOf("[inv]") >= 0)
        {
            String invList = "";
            doUpdate = false;
            String alias = "";
            start = output.indexOf("[inv]");
            tmp = output.substring(start + 5);
            end = tmp.indexOf("[/inv]");
            tmp = tmp.substring(0, end);
            alias = tmp;

            for (Element obj : player.GetObjects())
            {
                if (invList.length() > 0) invList += "\n";
                invList += "- " + obj.GetLabel() + " (" + obj.GetCount() + ") ";
            }

            output = output.replace("[inv]" + alias + "[/inv]", invList);
        }

        // Attribute IF
        while (output.indexOf("[att?") >= 0)
        {
            doUpdate = false;
            String attribPath = "";
            String attribVal = "";
            start = output.indexOf("[att?");
            end = output.indexOf("[/att]");
            tmp = output.substring(start + 5, end);
            arrEq = new String[] { "" };
            String msgTxt = "";
            String[] msgArr = null;

            arr = tmp.split("]");

            if (arr.length == 2)
            {
                attribPath = arr[0];

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("!=");
                    if (arrEq.length == 2) comparisonType = "!=";
                }

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("_lt_");
                    if (arrEq.length == 2) comparisonType = "<";
                }

                // In Norman < is allowed
                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("<");
                    if (arrEq.length == 2) comparisonType = "<";
                }

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("_gt_");
                    if (arrEq.length == 2) comparisonType = ">";
                }

                // In Norman > is allowed
                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split(">");
                    if (arrEq.length == 2) comparisonType = ">";
                }

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("_lte_");
                    if (arrEq.length == 2) comparisonType = "<=";
                }

                // In Norman <= is allowed
                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("<=");
                    if (arrEq.length == 2) comparisonType = "<=";
                }

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("_gte_");
                    if (arrEq.length == 2) comparisonType = ">=";
                }

                // In Norman >= is allowed
                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split(">=");
                    if (arrEq.length == 2) comparisonType = ">=";
                }

                if (arrEq.length != 2)
                {
                    arrEq = attribPath.split("=");
                    if (arrEq.length == 2) comparisonType = "=";
                }

                if (arrEq.length == 2)
                {
                    attribPath = arrEq[0].trim();
                    attribVal = arrEq[1].trim();
                    attribPath = msg.FixAliases(attribPath, msg);
                    attribVal = msg.FixAliases(attribVal, msg);
                    String tmpValue = global.GetAttributeValue(cmd, attribPath);

                    switch (comparisonType)
                    {
                        case "!=":
                            if (!tmpValue.toLowerCase().equals(attribVal.toLowerCase()))
                            {
                                doUpdate = true;
                            }
                            break;

                        case "=":
                            if (tmpValue.toLowerCase().equals(attribVal.toLowerCase()))
                            {
                                doUpdate = true;
                            }
                            break;

                        case ">":
                            try
                            {
                                if (Integer.parseInt(tmpValue) > Integer.parseInt(attribVal))
                                {
                                    doUpdate = true;
                                }
                            }
                            catch (Exception ex)
                            {
                                doUpdate = false;
                            }
                            break;

                        case "<":
                            try
                            {
                                if (Integer.parseInt(tmpValue) < Integer.parseInt(attribVal))
                                {
                                    doUpdate = true;
                                }
                            }
                            catch (Exception ex)
                            {
                                doUpdate = false;
                            }
                            break;

                        case ">=":
                            try
                            {
                                if (Integer.parseInt(tmpValue) >= Integer.parseInt(attribVal))
                                {
                                    doUpdate = true;
                                }
                            }
                            catch (Exception ex)
                            {
                                doUpdate = false;
                            }
                            break;

                        case "<=":
                            try
                            {
                                if (Integer.parseInt(tmpValue) <= Integer.parseInt(attribVal))
                                {
                                    doUpdate = true;
                                }
                            }
                            catch (Exception ex)
                            {
                                doUpdate = false;
                            }
                            break;
                    }

                    msgTxt = arr[1].trim().replace("_else_", "@@@");
                    msgArr = msgTxt.split("@@@");

                    if (doUpdate)
                    {
                        if (msgTxt.indexOf("@@@") >= 0)
                        {
                            output = output.replace(output.substring(start, end+6), msgArr[0]);
                        }
                        else
                        {
                            output = output.replace(output.substring(start, end+6), msgTxt);
                        }
                    }
                    else
                    {
                        if (msgArr.length > 1)
                        {
                            output = output.replace(output.substring(start, end+6), msgArr[1]);
                        }
                        else
                        {
                            output = output.replace(output.substring(start, end+6), "");
                        }
                    }
                }
                else
                {
                    // Something is wrong with the inline logic
                    break;
                }
            }
        }

        // Referenced Message Objects
        while (output.indexOf("[msg:") >= 0)
        {
            String objectAlias = "";
            start = output.indexOf("[msg:");
            tmp = output.substring(start + 5);
            arr = tmp.split("]");
            objectAlias = arr[0];

            tmp = "";

            Element o = msg.GetElement(cmd, objectAlias, msg);

            if (o != null)
            {
                messageSet.LoadMessageSets(cmd, o);

                List<String> tmpStrings = messageSet.Parse_MessageSets(cmd, o);

                for (String s : tmpStrings)
                {
                    if (!tmp.equals("")) tmp += "\n";
                    tmp += s;
                }
            }

            if (!tmp.equals("")) tmp = "\n" + tmp + "\n";

            output = output.replace("[msg:" + objectAlias + "]", tmp);
        }

        // Enemy Attributes
        while (output.indexOf("{enemy:") >= 0)
        {
            Element enemy = null;
            String attr = "";
            String attrVal = "";
            start = output.indexOf("{enemy:");
            tmp = output.substring(start + 7);
            arr = tmp.split("}");
            attr = arr[0];

            tmp = "";

            enemy = game.GetFight().GetOpponent();

            if (attr.trim().equals("name"))
            {
                attrVal = enemy.GetLabel();
            }
            else
            {
                attrVal = enemy.GetAttributeValue(attr.trim());
            }

            output = output.replace("{enemy:" + attr + "}", attrVal);
        }

        // Player Attributes
        while (output.indexOf("{player:") >= 0)
        {
            String attr = "";
            String attrVal = "";
            start = output.indexOf("{player:");
            tmp = output.substring(start + 8);
            arr = tmp.split("}");
            attr = arr[0];

            tmp = "";

            if (attr.trim().equals("name"))
            {
                attrVal = player.GetLabel();
            }
            else
            {
                for (Element attribute : player.GetAttributes())
                {
                    if (attribute.GetGuid().equals(attr.trim()))
                    {
                        attrVal = attribute.GetValue();
                        break;
                    }
                }
            }

            output = output.replace("{player:" + attr + "}", attrVal);
        }

        // Location - object
        while (output.indexOf("{location:") >= 0)
        {
            String attr = "";
            String attrVal = "";
            start = output.indexOf("{location:");
            tmp = output.substring(start + 10);
            arr = tmp.split("}");
            attr = arr[0];
            String target = "";

            tmp = "";

            target = attr.trim();

            tmp = global.GetObjectLocation(cmd, target);

            if (tmp.equals("")) tmp = global.GetNpcLocation(cmd, target);

            output = output.replace("{location:" + attr + "}", tmp);
        }

        return output;
    }
}
