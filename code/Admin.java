import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/**
    Tools for game authors, used to test games.
    <br />
    Be sure the game element has an admin password set. Once in admin mode,
    use the command <em>_admin</em> to list all possible commands.
*/
public class Admin 
{
    /* PROPERTIES */
    //
        private boolean recording = false;
        public boolean GetRecording() { return recording; }
        public void SetRecording(boolean val) { recording = val; }
    //



    /**
        Parses the admin input, looking for admin commands - if the game is in admin mode.
    */
    public List<String> Parse_Admin(Statement cmd, Element parent, Input input)
    {
        List<String> output = new ArrayList<String>();
        Element game = parent.GetGame(parent);
        String pass = GetAdminPass(cmd);

        if (pass.equals("")) return output;

        input.SetMatch(false);

        // Debug Mode
        {
            if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), pass))
            {
                input.SetMatch(true);
                game.SetDebug(!game.GetDebug());

                if (game.GetDebug())
                {
                    output.add("DEBUG Mode: ON");
                }
                else
                {
                    output.add("DEBUG Mode: OFF");
                }
            }
        }

        if (!game.GetDebug()) return output;


        // Turn on Recording
        if (!input.GetMatch() && Functions.Match(input.GetUserInput(), "_rec on"))
        {
            input.SetMatch(true);
            SetRecording(true);
            output.add("Recording...");
        }

        // List Admin Commands
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_admin"))
        {
            input.SetMatch(true);
            output.addAll(ListCommands());
        }
        // List Mapping Commands
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_mapping"))
        {
            input.SetMatch(true);
            output.addAll(ListMapping());
        }

        // Add testing notes
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_addtestnote"))
        {
            input.SetMatch(true);
            output.addAll(AddTestingNote(cmd, parent, input.GetParamString().trim()));
        }

        // Delete testing notes
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_deltestnote"))
        {
            input.SetMatch(true);
            output.addAll(DeleteTestingNote(cmd, parent));
        }

        // Testing notes
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_testnote"))
        {
            input.SetMatch(true);
            output.addAll(TestingNote(cmd, parent));
        }

        // Get room aliases
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_rooms"))
        {
            input.SetMatch(true);
            String aliasIn = input.GetParamString().trim();
            output.addAll(SearchRooms(cmd, aliasIn));
        }

        // Get npc aliases
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_npcs"))
        {
            input.SetMatch(true);
            String aliasIn = input.GetParamString().trim();
            output.addAll(SearchNpcs(cmd, aliasIn));
        }

        // Get object aliases
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_objectsbytype"))
        {
            input.SetMatch(true);
            String typeIn = input.GetParamString().trim();
            output.addAll(SearchObjectsByType(cmd, parent, typeIn));
        }

        // Get object aliases
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_objects"))
        {
            input.SetMatch(true);
            String aliasIn = input.GetParamString().trim();
            output.addAll(SearchObjects(cmd, aliasIn));
        }

        // List log types
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_logs"))
        {
            input.SetMatch(true);
            output.addAll(ListLogTypes(cmd));
        }

        // List logs
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_log"))
        {
            input.SetMatch(true);
            output.addAll(ListLog(cmd, input.GetParamString().trim()));
        }

        // Move player
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_travel"))
        {
            input.SetMatch(true);
            String alias = input.GetParamString().trim();
            output.addAll(MovePlayer(cmd, parent, alias));
        }

        // Move object
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_move"))
        {
            input.SetMatch(true);
            String alias = input.GetParams()[0].trim();
            String newloc = input.GetParams()[1].trim();
            output.addAll(MoveObject(cmd, parent, alias, newloc));
        }

        // Take object
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_take"))
        {
            input.SetMatch(true);
            String alias = input.GetParamString().trim();
            output.addAll(TakeObject(cmd, parent, alias));
        }

        // Kill enemy
        // Handled in the Fight class

        // Die
        // Handled in the Fight class

        // Attributes
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_attribs"))
        {
            input.SetMatch(true);
            String parentAliasIn = input.GetParamString().trim();
            output.addAll(SearchAttributes(cmd, parent, parentAliasIn));
        }

        // Player
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_player"))
        {
            input.SetMatch(true);
            output.addAll(SearchAttributes(cmd, game, "{player}"));
        }

        // Set Attribute
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_setattrib"))
        {
            input.SetMatch(true);
            String alias = input.GetParams()[0].trim();
            String attr = input.GetParams()[1].trim();
            String val = input.GetParams()[2].trim();
            output.addAll(SetAttribute(cmd, parent, alias, attr, val));
        }

        // Current Room
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_room"))
        {
            input.SetMatch(true);
            output.addAll(CurrentRoom(cmd, parent));
        }

        // Game Map
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_map"))
        {
            input.SetMatch(true);
            output.addAll(GetMap(cmd, parent, input.GetParamString()));
        }

        // Connectors List
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_connectors"))
        {
            input.SetMatch(true);
            output.addAll(GetObjectList(cmd, parent, "@@connector@@"));
        }

        // Objects List
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_objects"))
        {
            input.SetMatch(true);
            output.addAll(GetObjectList(cmd, parent, input.GetParamString()));
        }

        // Enemy
        // Handled in the Fight class

        // Unit Tests
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_listunittests"))
        {
            input.SetMatch(true);
            output.addAll(ListUnitTests(cmd));
        }

        // Unit Test
        // Handled in the Parser class

        // Tests
        if (!input.GetMatch() && Functions.Match(input.GetInputCommand(), "_tests"))
        {
            input.SetMatch(true);
            output.addAll(ListTests(cmd));
        }

        // Test
        // Handled in the Parser class

        return output;
    }


    /**
        Gets the admin password.
    */
    public String GetAdminPass(Statement cmd)
    {
        String output = "";
        String sql = "";

        try
        {
            sql =  "";
            sql += "SELECT ";
            sql += "    AdminPass ";
            sql += "FROM ";
            sql += "    Game ";
            sql += "WHERE 1 = 1 ";
            sql += ";";

            ResultSet rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                if (!rs.getString(1).equals(""))
                {
                    output = rs.getString(1).trim();
                }
                break;
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Output("ERR:\n" + sql);
        }

        return output;
    }



    /* HELPER METHODS */

    /**
        Adds a testing note. Beware: these will be cleared out if the game is rebuilt.
    */
    public List<String> AddTestingNote(Statement cmd, Element parent, String params)
    {
        List<String> output = new ArrayList<String>();
        String sql = "";
        int passing = 0;
        Element room = parent.GetRoom(parent);

        String[] parms = params.split(",");

        if (parms.length != 2)
        {
            output.add("To add a testing note use: _addtestnote <pass (true|false)>, <note>");
            return output;
        }

        if (parms[0].trim().toLowerCase().equals("true")) passing = 1;
        else passing = 0;

        try
        {
            sql =  "";
            sql += "INSERT INTO ";
            sql += "    Testing ";
            sql += "SELECT '" + room.GetGuid() + "', " + passing + ", '" + parms[1].trim() + "', 0 ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        output.add("Testing note added.");

        return output;
    }

    /**
        Outputs the current Room's information.
    */
    public List<String> CurrentRoom(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        Element room = parent.GetRoom(parent);

        output.addAll(RoomDetails(cmd, room));

        return output;
    }

    /**
        Deletes a testing note.
    */
    public List<String> DeleteTestingNote(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        String sql = "";
        Element room = parent.GetRoom(parent);

        try
        {
            sql =  "";
            sql += "DELETE FROM ";
            sql += "    Testing ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND RoomGUID = '" + room.GetGuid() + "' ";
            sql += ";";

            cmd.execute(sql);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        output.add("Testing note deleted.");

        return output;
    }

    /**
        Gets a 'map' of Rooms and their connectors. The Rooms shown can be filtered.
        The filter looks at their aliases.
    */
    public List<String> GetMap(Statement cmd, Element parent, String filter)
    {
        String fileName = "map.txt";
        List<String> output = new ArrayList<String>();
        Room room = new Room();
        Element game = parent.GetGame(parent);
        List<Element> rooms = room.GetRooms(cmd, game, filter);

        for (Element rm : rooms)
        {
            output.addAll(RoomDetails(cmd, rm));
        }

        Functions.WriteToFile(fileName, output);

        return output;
    }

    /**
        Outputs Object information. The Objects shown can be filtered.
        The filter looks at their aliases.
    */
    public List<String> GetObjectList(Statement cmd, Element parent, String filter)
    {
        List<String> output = new ArrayList<String>();
        Object object = new Object();
        List<Element> objects = object.GetObjects(cmd, filter);

        for (Element obj : objects)
        {
            output.addAll(ObjectDetails(cmd, obj));
        }

        return output;
    }

    /**
        Outputs a list of possible admin commands. Currently this is a hardcoded list,
        so be sure to modify this when admin commands are added or modified.
    */
    public List<String> ListCommands()
    {
        List<String> output = new ArrayList<String>();

        output.add("_mapping");
        output.add("   Lists the possible commands useful for 'mapping' the game..");
        output.add("");
        output.add("_addtestnote <pass (true|false)>, <note>");
        output.add("   Adds testing notes for the current room.");
        output.add("");
        output.add("_deltestnote");
        output.add("   Deletes the testing notes for the current room.");
        output.add("");
        output.add("_testnote");
        output.add("   Displays the testing notes for the current room.");
        output.add("");
        output.add("_log <type>");
        output.add("   Displays the log entries. If a type is specified, only those");
        output.add("   log types are shown.");
        output.add("");
        output.add("_logs");
        output.add("   Lists the available log types");
        output.add("");
        output.add("_travel <room alias>");
        output.add("   Moves the player to the specified room. To get the room alias");
        output.add("   use _roomalias.");
        output.add("");
        output.add("_move <object alias>, <element alias>");
        output.add("   Moves the object to the specified room or element. To get the room alias use");
        output.add("   _roomalias.");
        output.add("");
        output.add("_take <object alias>");
        output.add("   Causes the player to take the specified object. To get object");
        output.add("   aliases use _objectalias.");
        output.add("");
        output.add("_kill");
        output.add("   When fighting, causes the enemy to die.");
        output.add("");
        output.add("_die");
        output.add("   Causes the player to die.");
        output.add("");
        output.add("_attribs <object alias>");
        output.add("   Lists the attributes and their values for the specified object.");
        output.add("   To get the object alias use _objectalias.");
        output.add("");
        output.add("_player");
        output.add("   Lists the player's attributes and their values.");
        output.add("");
        output.add("_setattrib <object alias>, <attribute alias>, <new value>");
        output.add("   Sets the specified attribute to the specified value.");
        output.add("");
        output.add("_enemy");
        output.add("   When fighting, lists all the details of the current enemy.");
        output.add("");
        output.add("_rec on");
        output.add("    Begin recording the commands entered into the game (For use in defining tests etc).");
        output.add("");
        output.add("_rec off");
        output.add("    End the current recording and output the recorded commands.");
        output.add("");
        output.add("_tests");
        output.add("   Lists the available tests (macros).");
        output.add("");
        output.add("_test <test name>");
        output.add("   Runs the specified test (macro).");
        output.add("");
        output.add("_unittests");
        output.add("   Lists the available unit tests (test / assert).");
        output.add("");
        output.add("_unittest <test name>");
        output.add("   Runs the specified unit test (test/ assert).");
        output.add("");
        output.addAll(ListMapping());

        return output;
    }

    /**
        Outputs a list of possible mapping commands. Currently this is a hardcoded list,
        so be sure to modify this when mapping commands are added or modified.
    */
    public List<String> ListMapping()
    {
        List<String> output = new ArrayList<String>();

        output.add("_connectors");
        output.add("   Lists all the connector objects in the game.");
        output.add("");
        output.add("_map <filter>");
        output.add("   Lists the room(s) and their connectors, if a filter is specified");
        output.add("   filters the rooms on it.");
        output.add("   For more help on using maps use: help maps.");
        output.add("");
        output.add("_npcs <filter>");
        output.add("   Lists the aliases of npcs, found using the filter string.");
        output.add("");
        output.add("_objects <filter>");
        output.add("   Lists the objects and their aliases, if a filter is specified it");
        output.add("   limits the objects by that.");
        output.add("");
        output.add("_objectsbytype <filter>");
        output.add("   Lists the objects and their aliases, filtered by their type value.");
        output.add("");
        output.add("_room");
        output.add("   Lists the current room and its connectors.");
        output.add("");
        output.add("_rooms <filter>:");
        output.add("   Lists the room aliases that match the filter, or if none is");
        output.add("   given, lists all room aliases.");
        output.add("");

        return output;
    }

    /**
        Displays all log types
    */
    public List<String> ListLogTypes(Statement cmd)
    {
        List<String> log = new ArrayList<String>();
        String sql = "";

        log.add("\n\nLog Types:\n\n");

        int colLogType = 1;
        int colFilePath = 2;
        int colMessage = 3;

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT DISTINCT ";
            sql += "    LogType ";
            sql += "FROM ";
            sql += "    Log ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                log.add("  " + rs.getString(colLogType) + "\n\n");
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return log;
    }

    /**
        Displays all log entries. This output can be filtered by type.
    */
    public List<String> ListLog(Statement cmd, String type)
    {
        List<String> log = new ArrayList<String>();
        String sql = "";

        type = Functions.Encode(type);

        log.add("\n\nLog:\n\n");

        int colLogType = 1;
        int colFilePath = 2;
        int colMessage = 3;

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    LogType, ";
            sql += "    FilePath, ";
            sql += "    Message ";
            sql += "FROM ";
            sql += "    Log ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            if (!type.equals(""))
            {
                sql += "    AND LogType = '" + type + "' ";
            }
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                log.add("--------------------------------------------------------------------------------");
                log.add("Type: " + rs.getString(colLogType));
                log.add("File: " + rs.getString(colFilePath));
                log.add("Message:\n" + rs.getString(colMessage));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        return log;
    }

    public List<String> ListTests(Statement cmd)
    {
        List<String> output = new ArrayList<String>();
        String cmds = "";
        String[] cmdArr = null;
        String sql = "";
        Input inp = null;

        output.add("Tests:");

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    TestName ";
            sql += "FROM ";
            sql += "    Test ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output.add(rs.getString(1));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return output;
    }

    public List<String> ListUnitTests(Statement cmd)
    {
        List<String> output = new ArrayList<String>();
        String cmds = "";
        String[] cmdArr = null;
        String sql = "";
        Input inp = null;

        output.add("Unit Tests:");

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    GUID ";
            sql += "FROM ";
            sql += "    UnitTest ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                output.add(rs.getString(1));
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return output;
    }

    /**
        Moves the Player to the specified location. The location is specified using its alias.
    */
    public List<String> MovePlayer(Statement cmd, Element parent, String alias)
    {
        List<String> output = new ArrayList<String>();
        Action action = new Action();

        output.addAll(action.MovePlayer(cmd, alias, parent));

        return output;
    }

    /**
        Outputs the specified Object's details.
    */
    public List<String> ObjectDetails(Statement cmd, Element objectIn)
    {
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Object object = new Object();
        Attribute attribute = new Attribute();
        Event event = new Event();
        Command command = new Command();

        output.add("Object " + objectIn.GetGuid() + ":");
        output.add("- File: " + objectIn.GetFileName());
        output.add("- Label: " + objectIn.GetLabel());
        output.add("- Meta: " + objectIn.GetMeta());
        output.add("- Type: " + objectIn.GetType());
        output.add("- Parent: " + objectIn.GetParentGuid());
        output.add("- Parent Type: " + objectIn.GetParentType());
        output.add("- Chance: " + objectIn.GetChance());

        attribute.LoadAttributes(cmd, objectIn);
        if (objectIn.HasAttributes())
        {
            output.add("- Attributes:");
            for (Element attr : objectIn.GetAttributes())
            {
                output.add("   - " + attr.GetGuid() + ", " + attr.GetValue());
            }
        }

        event.LoadEvents(cmd, objectIn);
        if (objectIn.HasEvents())
        {
            output.add("- Events:");
            tmp = new ArrayList<String>();
            for (Element evt : objectIn.GetEvents())
            {
                if (!tmp.contains(evt.GetType()))
                {
                    tmp.add(evt.GetType());
                }
            }
            for (String s : tmp)
            {
                output.add("   - Type: " + s);
            }
        }

        command.LoadCommands(cmd, objectIn);
        if (objectIn.HasCommands())
        {
            output.add("- Commands:");
            tmp = new ArrayList<String>();
            for (Element comm : objectIn.GetCommands())
            {
                if (!tmp.contains(comm.GetSyntax()))
                {
                    tmp.add(comm.GetSyntax());
                }
            }
            for (String s : tmp)
            {
                output.add("   - Syntax: " + s);
            }
        }

        output.add("");

        return output;
    }

    /**
        Returns a list of the passed in Room's details.
    */
    public List<String> RoomDetails(Statement cmd, Element room)
    {
        Element game = room.GetGame(room);
        List<String> output = new ArrayList<String>();
        List<String> tmp = new ArrayList<String>();
        Object object = new Object();
        Attribute attribute = new Attribute();
        Event event = new Event();
        ActionSet actionSet = new ActionSet();
        Action action = new Action();
        Command command = new Command();
        if (room == null)
        {
            output.add("Room not found.");
            return output;
        }

        attribute.LoadAttributes(cmd, room);
        object.LoadObjects(cmd, room);
        command.LoadCommands(cmd, room);

        event.LoadEvents(cmd, room);

        output.add("Room " + room.GetLabel() + ":");
        output.add("Alias: " + room.GetGuid());
        output.add("File: " + room.GetFileName());

        if (room.HasAttributes())
        {
            output.add("|- Attributes:");
            for (Element attr : room.GetAttributes())
            {
                output.add("|__ " + attr.GetGuid() + ", " + attr.GetValue());
            }
        }

        if (room.GetConnectors().size() > 0)
        {
            output.add("|");
            output.add("|- Connectors:");
            for (Element obj : room.GetConnectors())
            {
                output.add("|__ " + obj.GetGuid() + ", " + obj.GetLabel() + ",  File: " + obj.GetFileName());
                for (String dest : obj.GetConnectorDestinations(cmd, obj))
                {
                    output.add("|  |__ Destination: " + dest);
                }
            }
            output.add("|");
        }

        if (room.HasEvents())
        {
            output.add("|");
            output.add("|- Events:");
            for (Element evt : room.GetEvents())
            {
                output.add("|__ " + evt.GetGuid() + ", " + evt.GetType() + ",  File: " + evt.GetFileName());

                actionSet.LoadActionSets(cmd, evt);
                if (evt.HasActionSets())
                {
                    for (Element aset : evt.GetActionSets())
                    {
                        output.add("|  |__ ActionSet: " + aset.GetGuid());
                        action.LoadActions(cmd, aset);

                        if (aset.HasActions())
                        {
                            for (Element act : aset.GetActions())
                            {
                                output.add("|  |  |__ Action: " + act.GetGuid() + ", Type=" + act.GetType());
                            }
                        }
                    }
                }
            }
            output.add("|");
        }

        if (room.HasObjects())
        {
            output.add("|");
            tmp = new ArrayList<String>();
            for (Element obj : room.GetObjects())
            {
                if (!obj.GetType().equals("connector") && !tmp.contains("   - " + obj.GetLabel() + " (" + obj.GetGuid() + ")"))
                {
                    tmp.add("|__ " + obj.GetLabel() + " (" + obj.GetGuid() + "),  File: " + obj.GetFileName());
                }
            }
            if (tmp.size() > 0)
            {
                output.add("|- Objects:");
                for (String s : tmp)
                {
                    if (!s.trim().equals(""))
                    {
                        output.add(s.trim());
                    }
                }
            }
            output.add("|");
        }

        List<Element> npcs = game.GetRoomNpcs(room.GetGuid());
        if (npcs.size() > 0)
        {
            tmp = new ArrayList<String>();
            for (Element obj : npcs)
            {
                output.add("|");
                tmp.add("|__ " + obj.GetLabel() + " (" + obj.GetGuid() + "),  File: " + obj.GetFileName());

                attribute.LoadAttributes(cmd, obj);
                if (obj.HasAttributes())
                {
                    tmp.add("|- Attributes:");
                    for (Element attr : obj.GetAttributes())
                    {
                        tmp.add("|__ " + attr.GetGuid() + ", " + attr.GetValue());
                    }
                }
                output.add("|");
            }

            if (tmp.size() > 0)
            {
                output.add("|");
                output.add("|- NPCs:");
                for (String s : tmp)
                {
                    if (!s.trim().equals(""))
                    {
                        output.add(s.trim());
                    }
                }
                output.add("|");
            }
        }


        if (room.HasCommands())
        {
            output.add("|");
            output.add("|- Commands:");
            for (Element obj : room.GetCommands())
            {
                output.add("|__ Syntax:  " + obj.GetSyntax() + " (" + obj.GetGuid() + ")");
            }
            output.add("|");
        }

        output.add("");

        return output;
    }

    /**
        Returns a list of Attributes and Travelset Attributes for the specified parent element.
    */
    public List<String> SearchAttributes(Statement cmd, Element parent, String parentAlias)
    {
        List<String> output = new ArrayList<String>();
        Attribute attribute = new Attribute();
        NpcTravelSet npcTrv = new NpcTravelSet();
        Element found = parent.GetElement(cmd, parentAlias, parent);
        String itm = "";

        if (found != null)
        {
            attribute.LoadAttributes(cmd, found);
            npcTrv.LoadNpcTravelSets(cmd, found);

            output.add("Location:");
            output.add("- " + found.GetLocation());
            output.add("\n");

            if (found.GetAttributes().size() > 0)
            {
                output.add("Attributes:");
            }

            itm = "";

            for (Element attr : found.GetAttributes())
            {
                itm += "- Alias: " + attr.GetGuid() + "   Value: " + attr.GetValue() + "\n";
            }

            output.add(itm);

            if (found.GetNpcTravelSets().size() > 0)
            {
                output.add("TravelSets:");

                itm = "";

                for (Element trv : found.GetNpcTravelSets())
                {
                    attribute.LoadAttributes(cmd, trv);
                    if (trv.GetAttributes().size() > 0)
                    {
                        output.add("- Attributes");
                        for (Element attr : trv.GetAttributes())
                        {
                            itm += "-- Alias: " + attr.GetGuid() + "   Value: " + attr.GetValue() + "\n";
                        }

                        output.add(itm);
                    }
                }
            }
        }
        else
        {
            output.add("Item: " + parentAlias + " not found.");
        }

        return output;
    }

    /**
        Searches through the game's NPCs, returning the Name and alias of the matches.
    */
    public List<String> SearchNpcs(Statement cmd, String alias)
    {
        List<String> output = new ArrayList<String>();
        String row = "";
        String sql = "";

        alias = Functions.Encode(alias);

        int colGuid = 1;
        int colName = 2;
        int colMeta = 3;
        int colLocation = 4;

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Name, ";
            sql += "    Meta, ";
            sql += "    Location ";
            sql += "FROM ";
            sql += "    Npc ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ";
            sql += "        Name LIKE '%" + alias + "%' ";
            sql += "        OR ";
            sql += "        GUID LIKE '%" + alias + "%' ";
            sql += "        OR ";
            sql += "        Meta LIKE '%" + alias + "%' ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                row = " - " + rs.getString(colName) + " (" + rs.getString(colGuid) + ") - Location: " + rs.getString(colLocation);
                output.add(row);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (output.size() > 0)
        {
            output.add(0, "Found NPCs:");
        }

        return output;
    }

    /**
        Searches Object aliases by the specified string, returning the Label and alias of the matches.
    */
    public List<String> SearchObjects(Statement cmd, String alias)
    {
        List<String> output = new ArrayList<String>();
        String row = "";
        String sql = "";

        alias = Functions.Encode(alias);

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Label ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ";
            sql += "        Label LIKE '%" + alias + "%' ";
            sql += "        OR ";
            sql += "        GUID LIKE '%" + alias + "%' ";
            sql += "        OR ";
            sql += "        Meta LIKE '%" + alias + "%' ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                row = " - " + rs.getString(1) + " (" + rs.getString(2) + ")";
                output.add(row);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (output.size() > 0)
        {
            output.add(0, "Found Objects:");
        }

        return output;
    }

    /**
        Searches Object types by the specified string, returning the label and alias oand file of the matches.
    */
    public List<String> SearchObjectsByType(Statement cmd, Element parent, String type)
    {
        List<String> output = new ArrayList<String>();
        String row = "";
        String sql = "";
        Element room = null;

        type = Functions.Encode(type);

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    GUID, ";
            sql += "    Label, ";
            sql += "    FileName ";
            sql += "FROM ";
            sql += "    Object ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ";
            sql += "        Type LIKE '%" + type + "%' ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                row = rs.getString("GUID") + " (" + rs.getString("Label") + "),  File: " + rs.getString("FileName");
                output.add(row);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (output.size() > 0)
        {
            output.add(0, "Found Objects:");
        }

        return output;
    }

    /**
        Searches Room aliases by the specified string, returning the Label and alias of the matches.
    */
    public List<String> SearchRooms(Statement cmd, String alias)
    {
        List<String> output = new ArrayList<String>();
        String row = "";
        String sql = "";

        alias = Functions.Encode(alias);

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    Label, ";
            sql += "    GUID, ";
            sql += "    FileName ";
            sql += "FROM ";
            sql += "    Room ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND ";
            sql += "        Label LIKE '%" + alias + "%' ";
            sql += "        OR ";
            sql += "        GUID LIKE '%" + alias + "%' ";
            sql += "ORDER BY ";
            sql += "    Label ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                row = " - " + rs.getString(1) + " (" + rs.getString(2) + "),  File: " + rs.getString("FileName");
                output.add(row);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (output.size() > 0)
        {
            output.add(0, "Found Rooms:");
        }

        return output;
    }

    /**
        Updates the specified Attribute to the specified Value
    */
    public List<String> SetAttribute(Statement cmd, Element parent, String alias, String attributeIn, String value)
    {
        List<String> output = new ArrayList<String>();
        Attribute attribute = new Attribute();
        Element elem = parent.GetElement(cmd, alias, parent);
        Element attr = null;

        if (elem == null)
        {
            // Not an Element loaded into memory
            attribute.SaveAttribute(cmd, alias, attributeIn, value);
            output.add("Attribute " + alias + ":" + attributeIn + " set to " + value);
            return output;
        }

        attr = attribute.GetAttribute(cmd, elem, attributeIn);

        if (attr == null)
        {
            output.add("Attribute " + alias + ":" + attributeIn + " not found.");
            return output;
        }

        attr.SetValue(value);
        attribute.SaveAttribute(cmd, attr);

        output.add("Loaded Attribute " + alias + ":" + attr.GetGuid() + " set to " + value);

        return output;
    }

    /**
        Causes the Player to take the specified Object - no matter where that Object is in relation to the Player
        (It can be in far away Rooms etc).
    */
    public List<String> TakeObject(Statement cmd, Element parent, String alias)
    {
        List<String> output = new ArrayList<String>();
        Object object = new Object();
        Element room = parent.GetRoom(parent);
        Element player = parent.GetPlayer(parent);

        String newLoc = player.GetGuid();

        object.MoveObject(cmd, alias, newLoc);

        // Clear the Room Objects so they reload
        room.ClearObjects();

        // Clear the Player Objects so they reload
        player.ClearObjects();

        output.add("You have taken the Object; " + alias);

        return output;
    }

    /**
        Causes the Object to be moved to the specified location
    */
    public List<String> MoveObject(Statement cmd, Element parent, String alias, String newloc)
    {
        List<String> output = new ArrayList<String>();
        Object object = new Object();
        Element room = parent.GetRoom(parent);
        Element player = parent.GetPlayer(parent);

        object.MoveObject(cmd, alias, newloc);

        // Clear the Room Objects so they reload
        room.ClearObjects();

        // Clear the Player Objects so they reload
        player.ClearObjects();

        output.add("The object " + alias + " has been moved to " + newloc);

        return output;
    }

    /**
        Displays the testing notes for the specified Room.
    */
    public List<String> TestingNote(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        String row = "";
        String sql = "";
        String passed = "";
        Element room = parent.GetRoom(parent);

        try
        {
            ResultSet rs;

            sql =  "";
            sql += "SELECT ";
            sql += "    Pass, ";
            sql += "    Notes ";
            sql += "FROM ";
            sql += "    Testing ";
            sql += "WHERE 1 = 1 ";
            sql += "    AND Deleted = 0 ";
            sql += "    AND RoomGUID = '" + room.GetGuid() + "' ";
            sql += ";";

            rs = cmd.executeQuery(sql);

            while (rs.next())
            {
                if (rs.getInt(1) == 0) passed = "false";
                else passed = "true";

                row = "Passed: " + passed + "\nNote:\n" + rs.getString(2);
                output.add(row);
            }

            rs.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error(sql);
        }

        if (output.size() > 0)
        {
            output.add(0, "Testing Notes:");
        }
        else
        {
            output.add("No notes found.");
        }

        return output;
    }
}
