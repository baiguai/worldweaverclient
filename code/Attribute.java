import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    ATTRIBUTE HELPER METHODS
    ----------------------------------------------------------------------------
    ----------------------------------------------------------------------------
*/

public class Attribute
{
    public void LoadAttributes(Statement cmd, Element parent)
    {
        Element attr = null;
        String sql = "";

        if (parent.HasAttributes()) return;

        int colGuid = 1;
        int colType = 2;
        int colParentGuid = 3;
        int colParentType = 4;
        int colValue = 5;
        int colSort = 6;

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Type, ";
            sql += "    ParentGUID, ";
            sql += "    ParentType, ";
            sql += "    Value, ";
            sql += "    Sort ";
            sql += "FROM ";
            sql += "    Attribute ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!parent.GetElementType().equals("player") && !parent.GetElementType().equals("game")) sql += "    AND ParentGuid = '" + Functions.Encode(parent.GetGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(parent.GetElementType()) + "' ";
            sql += "ORDER BY ";
            sql += "    Sort ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                attr = new Element(rs.getString(colGuid), "attribute");
                attr.SetParent(parent);
                attr.SetType(rs.getString(colType));
                attr.SetParentGuid(rs.getString(colParentGuid));
                attr.SetParentType(rs.getString(colParentType));
                attr.SetValue(rs.getString(colValue));
                attr.SetSort(rs.getInt(colSort));
                parent.AppendAttribute(attr);
            }

            // Store the one-time Random Values
            for (Element att : parent.GetAttributes())
            {
                if (att.GetType().equals("random"))
                {
                    att.SetValue(parent.FixAttributeValue(cmd, att.GetValue(), att));
                    att.SetType("");
                    SaveAttribute(cmd, att);
                }
            }

            // Fix the Attribute Values
            for (Element att : parent.GetAttributes())
            {
                if (!att.GetType().equals("")) att.SetValue(parent.FixAttributeValue(cmd, att.GetValue(), att));

                if (att.GetType().equals("attribute"))
                {
                    att.SetType("");
                    SaveAttribute(cmd, att);
                }
            }

            // Create an empty attribute to signal a load has been performed
            if (parent.GetAttributes().size() < 1)
            {
                attr = new Element("", "attribute");
                parent.AppendAttribute(attr);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public Element GetAttribute(Statement cmd, Element parent, String alias)
    {
        Element output = null;
        LoadAttributes(cmd, parent);
        for (Element attr : parent.GetAttributes())
        {
            if (attr.GetGuid().equals(alias))
            {
                output = attr;
                break;
            }
        }

        return output;
    }


    public String GetAttributeValue(Statement cmd, Element parent, String alias)
    {
        return GetAttributeValue(cmd, parent, alias, "");
    }
    public String GetAttributeValue(Statement cmd, Element parent, String alias, String defaultValue)
    {
        String output = "";
        LoadAttributes(cmd, parent);
        for (Element attr : parent.GetAttributes())
        {
            if (attr.GetGuid().equals(alias))
            {
                output = parent.FixAttributeGetValue(attr);
                break;
            }
        }

        if (output.equals("")) output = defaultValue;

        return output;
    }

    public void SaveAttribute(Statement cmd, Element attribute)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Attribute ";
            sql += "SET ";
            sql += "    Type = '" + Functions.Encode(attribute.GetType()) + "', ";
            sql += "    Value = '" + Functions.Encode(attribute.GetValue()) + "' ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND GUID = '" + Functions.Encode(attribute.GetGuid()) + "' ";
            if (!Functions.Match(attribute.GetParentType(), "player|game"))sql += "    AND ParentGUID = '" + Functions.Encode(attribute.GetParentGuid()) + "' ";
            sql += "    AND ParentType = '" + Functions.Encode(attribute.GetParentType()) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void SaveAttribute(Statement cmd, String parentAlias, String attributeAlias, String value)
    {
        String sql = "";

        try
        {
            sql =  "";
            sql += "UPDATE ";
            sql += "    Attribute ";
            sql += "SET ";
            sql += "    Value = '" + Functions.Encode(value) + "' ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ParentGUID = '" + Functions.Encode(parentAlias) + "' ";
            sql += "    AND GUID = '" + Functions.Encode(attributeAlias) + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }
    }

    public void CloneAttributes(Statement cmd, List<Query> queries, int fired, Element oldParent, Element newParent)
    {
        String sFired = fired + "";
        String sql = "";

        LoadAttributes(cmd, oldParent);
        if (oldParent.GetAttributes().size() > 0)
        {
            for (Element oldAttr : oldParent.GetAttributes())
            {
                if (!oldAttr.GetGuid().equals(""))
                {
                    Element newAttr = new Element(oldAttr.GetGuid(), "attribute");
                    newAttr.SetParentGuid(newParent.GetGuid());
                    newAttr.SetParent(newParent);
                    newAttr.SetParentType(oldAttr.GetParentType());
                    newAttr.SetType(oldAttr.GetType());
                    newAttr.SetValue(oldParent.FixAttributeValue(cmd, oldAttr.GetValue(), newAttr).replace("{fired}", sFired).replace(oldParent.GetGuid(), newParent.GetGuid()));
                    newAttr.SetSort(oldAttr.GetSort());
                    newParent.AppendAttribute(newAttr);
                }
            }

            // Add the queries
            for (Element attr : newParent.GetAttributes())
            {
                sql =  "SELECT ";
                sql += "    '" + Functions.Encode(attr.GetGuid()) + "', ";
                sql += "    '" + Functions.Encode(attr.GetType()) + "', ";
                sql += "    '" + Functions.Encode(attr.GetParentGuid()) + "', ";
                sql += "    '" + Functions.Encode(attr.GetParentType()) + "', ";
                sql += "    '" + Functions.Encode(attr.GetValue()) + "', ";
                sql += "    " + attr.GetSort() + " ";
                queries.add(new Query("Attribute", sql, false));
            }
        }
    }

    public void InsertAttributes(Statement cmd, List<Element> attributes)
    {
        String sql = "";
        String sqlAttr = "";

        sql =  "";
        sql += "INSERT INTO ";
        sql += "Attribute ( ";
        sql += "    GUID, ";
        sql += "    Type, ";
        sql += "    ParentGUID, ";
        sql += "    ParentType, ";
        sql += "    Value, ";
        sql += "    Sort ";
        sql += ") ";

        for (Element attr : attributes)
        {
            if (!attr.GetGuid().equals(""))
            {
                if (!sqlAttr.equals("")) sqlAttr += " UNION ";
                sqlAttr += "SELECT ";
                sqlAttr += "    '" + attr.GetGuid() + "', ";
                sqlAttr += "    '" + attr.GetType() + "', ";
                sqlAttr += "    '" + attr.GetParentGuid() + "', ";
                sqlAttr += "    '" + attr.GetParentType() + "', ";
                sqlAttr += "    '" + attr.GetValue() + "', ";
                sqlAttr += "    " + attr.GetSort() + " ";
            }
        }

        if (!sqlAttr.equals(""))
        {
            sql = sql + sqlAttr + ";";
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
