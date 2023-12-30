import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/*
    ELEMENT
    ----------------------------------------------------------------------------
    Refer to the Database that is built in the Admin utility for the Element
    structures per type.

    TODO:
    Convert the Element Objects list to an array of element lists
    and the element. Wrap the new structure so that it
    works seemlessly - but if more than one is present, output the count.
    If the count is reduced to 0, remove it.
    This will allow for situations like multiple items of the same kind in
    inventory etc.
    ----------------------------------------------------------------------------
*/

public class Element
{
    /* Properties */
    //
        /* PARENT AND GLOBAL ELEMENTS */
        //
            private boolean DEBUG = false;
            public boolean GetDebug() { return DEBUG; }
            public void SetDebug(boolean val) { DEBUG = val; }

            private boolean isPlaying = false;
            public boolean GetIsPlaying() { return isPlaying; }
            public void SetIsPlaying(boolean val) { isPlaying = val; }

            private boolean isDead = false;
            public boolean GetIsDead() { return isDead; }
            public void SetIsDead(boolean val) { isDead = val; }

            private Element parent = null;
            public Element GetParent() { return parent; }
            public void SetParent(Element val) { parent = val; }

            private Element room = null;
            public Element GetRoom() { return room; }
            public void SetRoom(Element val)
            {
                room = val;

                // Fix NPC parents - in case they've moved
                for (Element npc : GetNpcs())
                {
                    if (npc.GetLocation().equals(val.GetGuid()))
                    {
                        npc.SetParent(val);
                    }
                }
            }
            public void ClearRoom()
            {
                room = null;
                System.gc();
            }

            private Element player = null;
            public Element GetPlayer() { return player; }
            public void SetPlayer(Element val) { player = val; }

            private List<Element> npcs = null;
            public List<Element> GetNpcs() { if (npcs == null) npcs = new ArrayList<Element>(); return npcs; }
            public List<Element> GetRoomNpcs(String roomGuid)
            {
                List<Element> output = new ArrayList<Element>();
                for (Element npc : GetNpcs())
                {
                    if (npc.GetLocation().equals(roomGuid))
                    {
                        output.add(npc);
                    }
                }
                return output;
            }
            public void SetNpcs(List<Element> val) { npcs = val; }
            public void AppendNpcs(List<Element> val) { GetNpcs().addAll(val); }
            public void AppendNpc(Element val) { GetNpcs().add(val); }
            public void ClearNpcs()
            {
                npcs = null;
            }
        //

        /* ELEMENT PROPERTIES */
        //
            private String elementType = "";
            public String GetElementType() { return elementType; }
            public void SetElementType(String val) { elementType = val; }

            private String fileName = "";
            public String GetFileName() { return fileName; }
            public void SetFileName(String val) { fileName = val; }

            private String guid = "";
            public String GetGuid() { return guid; }
            public void SetGuid(String val) { guid = val; }

            private String parentGuid = "";
            public String GetParentGuid() { return parentGuid; }
            public void SetParentGuid(String val) { parentGuid = val; }

            private String parentType = "";
            public String GetParentType() { return parentType; }
            public void SetParentType(String val) { parentType = val; }

            private String label = "";
            public String GetLabel() { return label; }
            public void SetLabel(String val) { label = val; }

            private String meta = "";
            public String GetMeta() { return meta; }
            public void SetMeta(String val) { meta = val; }

            private int count = 1;
            public int GetCount() { return count; }
            public void SetCount(int val) { count = val; }

            private String location = "";
            public String GetLocation() { return location; }
            public void SetLocation(String val) { location = val; }

            private String inherit = "";
            public String GetInherit() { return inherit; }
            public void SetInherit(String val) { inherit = val; }

            private String type = "";
            public String GetType() { return type; }
            public void SetType(String val) { type = val; }

            private int chance = 0;
            public int GetChance() { return chance; }
            public void SetChance(int val) { chance = val; }

            private String value = "";
            public String GetValue() { return value; }
            public void SetValue(String val) { value = val; }

            private String syntax = "";
            public String GetSyntax() { return syntax; }
            public void SetSyntax(String val) { syntax = val; }

            private String source = "";
            public String GetSource() { return source; }
            public void SetSource(String val) { source = val; }

            private String sourceValue = "";
            public String GetSourceValue() { return sourceValue; }
            public void SetSourceValue(String val) { sourceValue = val; }

            private String newValue = "";
            public String GetNewValue() { return newValue; }
            public void SetNewValue(String val) { newValue = val; }

            private String operand = "";
            public String GetOperand() { return operand; }
            public void SetOperand(String val) { operand = val; }

            private String failMessage = "";
            public String GetFailMessage() { return failMessage; }
            public void SetFailMessage(String val) { failMessage = val; }

            private String failEvent = "";
            public String GetFailEvent() { return failEvent; }
            public void SetFailEvent(String val) { failEvent = val; }

            private String eval = "";
            public String GetEval() { return eval; }
            public void SetEval(String val) { eval = val; }

            private int initialTime = 0;
            public int GetInitialTime() { return initialTime; }
            public void SetInitialTime(int val) { initialTime = val; }

            private int waitMinutes = 0;
            public int GetWaitMinutes() { return waitMinutes; }
            public void SetWaitMinutes(int val) { waitMinutes = val; }

            private boolean goingForward = false;
            public boolean GetGoingForward() { return goingForward; }
            public void SetGoingForward(boolean val) { goingForward = val; }

            private int intervalMinutes = 0;
            public int GetIntervalMinutes() { return intervalMinutes; }
            public void SetIntervalMinutes(int val) { intervalMinutes = val; }

            private int repeatCount = 0;
            public int GetRepeatCount() { return repeatCount; }
            public void SetRepeatCount(int val) { repeatCount = val; }

            private int repeat = 0;
            public int GetRepeat() { return repeat; }
            public void SetRepeat(int val) { repeat = val; }

            private String repeatType = "";
            public String GetRepeatType() { return repeatType; }
            public void SetRepeatType(String val) { repeatType = val; }

            private int currentIndex = 0;
            public int GetCurrentIndex() { return currentIndex; }
            public void SetCurrentIndex(int val) { currentIndex = val; }

            private String output = "";
            public String GetOutput() { return ProcessOutput(output); }
            public void SetOutput(String val) { output = val; }

            private String armedWeapon = "";
            public String GetArmedWeapon() { return armedWeapon; }
            public void SetArmedWeapon(String val) { armedWeapon = val; }
            public boolean HasArmedWeapon() { if (armedWeapon == null || armedWeapon.equals("")) return false; else return true; }

            private int points = 0;
            public int GetPoints() { return points; }
            public void SetPoints(int val) { points = val; }

            private boolean hasChildObjects = false;
            public boolean GetHasChildObjects() { return hasChildObjects; }
            public void SetHasChildObjects(boolean val) { hasChildObjects = val; }

            private int sort = 0;
            public int GetSort() { return sort; }
            public void SetSort(int val) { sort = val; }

            private Fight fight = null;
            public Fight GetFight() { return fight; }
            public void SetFight(Fight val) { fight = val; }
        //

        /* ELEMENT CHILDREN */
        //
            private List<Element> attributes = null;
            public List<Element> GetAttributes() { if (attributes == null) attributes = new ArrayList<Element>(); return attributes; }
            public Element GetAttribute(String alias)
            {
                Element output = null;
                for (Element att : GetAttributes())
                {
                    if (att.GetGuid().equals(alias))
                    {
                        output = att;
                        break;
                    }
                }

                return output;
            }
            public String GetAttributeValue(String alias)
            {
                String output = "";
                Element att = GetAttribute(alias);
                if (att != null) output = att.GetValue();
                return output;
            }
            public void SetAttributes(List<Element> val) { attributes = val; }
            public void SetAttribute(String alias, String newValue)
            {
                Element att = GetAttribute(alias);
                if (att != null) att.SetValue(newValue);
            }
            public void AppendAttribute(Element val) { GetAttributes().add(val); }
            public boolean HasAttributes()
            {
                boolean output = false;

                if (GetAttributes().size() > 0)
                {
                    if (GetAttributes().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetAttributes().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> events = null;
            public List<Element> GetEvents() { if (events == null) events = new ArrayList<Element>(); return events; }
            public void SetEvents(List<Element> val) { events = val; }
            public void AppendEvent(Element val) { GetEvents().add(val); }
            public boolean HasEvents()
            {
                boolean output = false;

                if (GetEvents().size() > 0)
                {
                    if (GetEvents().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetEvents().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> commands = null;
            public List<Element> GetCommands() { if (commands == null) commands = new ArrayList<Element>(); return commands; }
            public void SetCommands(List<Element> val) { commands = val; }
            public void AppendCommand(Element val) { GetCommands().add(val); }
            public boolean HasCommands()
            {
                boolean output = false;

                if (GetCommands().size() > 0)
                {
                    if (GetCommands().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetCommands().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<List<Element>> objects = null;
            public List<Element> GetObjects()
            {
                List<Element> objs = new ArrayList<Element>();
                if (objects == null) objects = new ArrayList<List<Element>>();

                for (List<Element> elem : objects)
                {
                    if (elem.size() > 0)
                    {
                        objs.add(elem.get(0));
                    }
                }

                return objs;
            }
            public List<Element> GetWeapons()
            {
                List<Element> output = new ArrayList<Element>();

                for (Element obj : GetObjects())
                {
                    if (obj.GetType().equals("weapon"))
                    {
                        output.add(obj);
                    }
                }

                return output;
            }
            public List<Element> GetConnectors()
            {
                List<Element> output = new ArrayList<Element>();

                for (Element obj : GetObjects())
                {
                    if (!obj.GetGuid().equals("") && obj.GetType().trim().equals("connector"))
                    {
                        output.add(obj);
                    }
                }

                return output;
            }
            public void SetObjects(List<Element> val)
            {
                boolean found = false;

                for (Element o : val)
                {
                    AppendObject(o);
                }
            }
            public void AppendObject(Element val)
            {
                boolean found = false;
                if (objects == null) objects = new ArrayList<List<Element>>();

                for (List<Element> obj : objects)
                {
                    if (obj.size() > 0)
                    {
                        if (obj.get(0).GetGuid().equals(val.GetGuid()))
                        {
                            obj.add(val);
                            found = true;
                        }
                    }
                }

                if (!found)
                {
                    List<Element> olrow = new ArrayList<Element>();
                    olrow.add(val);
                    objects.add(olrow);
                }
            }
            public void ClearObjects()
            {
                objects = null;
                System.gc();
            }
            public boolean HasObjects()
            {
                boolean output = false;

                for (Element obj : GetObjects())
                {
                    if (!obj.GetGuid().equals(""))
                    {
                        output = true;
                        break;
                    }
                }

                return output;
            }
            public int GetObjectCount(String val)
            {
                int output = 0;
                if (objects == null) objects = new ArrayList<List<Element>>();

                for (List<Element> objs : objects)
                {
                    if (objs.size() > 0)
                    {
                        if (objs.get(0).GetGuid().equals(val))
                        {
                            output = objs.size();
                            break;
                        }
                    }
                }

                return output;
            }

            private List<Element> actionSets = null;
            public List<Element> GetActionSets() { if (actionSets == null) actionSets = new ArrayList<Element>(); return actionSets; }
            public void SetActionSets(List<Element> val) { actionSets = val; }
            public void AppendActionSet(Element val) { GetActionSets().add(val); }
            public boolean HasActionSets()
            {
                boolean output = false;

                if (GetActionSets().size() > 0)
                {
                    if (GetActionSets().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetActionSets().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> actions = null;
            public List<Element> GetActions() { if (actions == null) actions = new ArrayList<Element>(); return actions; }
            public void SetActions(List<Element> val) { actions = val; }
            public void AppendAction(Element val) { GetActions().add(val); }
            public boolean HasActions()
            {
                boolean output = false;

                if (GetActions().size() > 0)
                {
                    if (GetActions().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetActions().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> messageSets = null;
            public List<Element> GetMessageSets() { if (messageSets == null) messageSets = new ArrayList<Element>(); return messageSets; }
            public void SetMessageSets(List<Element> val) { messageSets = val; }
            public void AppendMessageSet(Element val) { GetMessageSets().add(val); }
            public boolean HasMessageSets()
            {
                boolean output = false;

                if (GetMessageSets().size() > 0)
                {
                    if (GetMessageSets().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetMessageSets().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> messages = null;
            public List<Element> GetMessages() { if (messages == null) messages = new ArrayList<Element>(); return messages; }
            public void SetMessages(List<Element> val) { messages = val; }
            public void AppendMessage(Element val) { GetMessages().add(val); }
            public boolean HasMessages()
            {
                boolean output = false;

                if (GetMessages().size() > 0)
                {
                    if (GetMessages().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetMessages().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> logicSets = null;
            public List<Element> GetLogicSets() { if (logicSets == null) logicSets = new ArrayList<Element>(); return logicSets; }
            public void SetLogicSets(List<Element> val) { logicSets = val; }
            public void AppendLogicSet(Element val) { GetLogicSets().add(val); }
            public void ClearLogicSets() { logicSets = null; }
            public boolean HasLogicSets()
            {
                boolean output = false;

                if (GetLogicSets().size() > 0)
                {
                    if (GetLogicSets().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetLogicSets().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> logicBlocks = null;
            public List<Element> GetLogicBlocks() { if (logicBlocks == null) logicBlocks = new ArrayList<Element>(); return logicBlocks; }
            public void SetLogicBlocks(List<Element> val) { logicBlocks = val; }
            public void AppendLogic(Element val) { GetLogicBlocks().add(val); }
            public boolean HasLogicBlocks()
            {
                boolean output = false;

                if (GetLogicBlocks().size() > 0)
                {
                    if (GetLogicBlocks().size() > 1)
                    {
                        output = true;
                    }
                    else
                    {
                        if (!GetLogicBlocks().get(0).GetGuid().equals(""))
                        {
                            output = true;
                        }
                    }
                }

                return output;
            }

            private List<Element> npcTravelSets = null;
            public List<Element> GetNpcTravelSets() { if (npcTravelSets == null) npcTravelSets = new ArrayList<Element>(); return npcTravelSets; }
            public void SetNpcTravelSets(List<Element> val) { npcTravelSets = val; }
            public void AppendNpcTravelSet(Element val) { GetNpcTravelSets().add(val); }
            public void ClearNpcTravelSets() { npcTravelSets = null; }
            public boolean HasNpcTravelSets()
            {
                boolean output = false;

                if (GetNpcTravelSets().size() > 1) return true;
                if (GetNpcTravelSets().size() == 1 && !GetNpcTravelSets().get(0).GetGuid().equals("")) return true;

                return output;
            }

            private List<Element> npcTravels = null;
            public List<Element> GetNpcTravels() { if (npcTravels == null) npcTravels = new ArrayList<Element>(); return npcTravels; }
            public void SetNpcTravels(List<Element> val) { npcTravels = val; }
            public void AppendNpcTravel(Element val) { GetNpcTravels().add(val); }
            public boolean HasNpcTravels()
            {
                boolean output = false;
                if (GetNpcTravels().size() > 1) return true;
                if (GetNpcTravels().size() == 1 && !GetNpcTravels().get(0).GetParentGuid().equals("")) return true;
                return output;
            }
        //
    //


    public Element(String guidIn, String elementTypeIn)
    {
        SetGuid(guidIn);
        SetElementType(elementTypeIn);
    }


    /* MESSAGE METHODS */
    // Process Output
    public String ProcessOutput(String rawMsg)
    {
        String output = rawMsg;
        String br = System.getProperty("line.separator");

        // Line Breaks
        output = output.replace("\\n", br);

        return output;
    }

    public void SetDefaultArmed(Element parent)
    {
        Element player = parent.GetPlayer(parent);

        if (player.GetArmedWeapon() == null || player.GetArmedWeapon().equals(""))
        {
            for (Element obj : player.GetObjects())
            {
                if (obj.GetType().equals("weapon"))
                {
                    player.SetArmedWeapon(obj.GetGuid());
                    break;
                }
            }
        }
    }

    public List<String> ListInventory(Statement cmd, Element parent)
    {
        List<String> output = new ArrayList<String>();
        Object object = new Object();
        boolean hasItems = false;
        String armed = "";

        output.add("\n\nInventory:");

        object.LoadObjects(cmd, parent);

        for (Element obj : parent.GetObjects())
        {
            if (!obj.GetGuid().equals(""))
            {
                armed = "";
                if (HasArmedWeapon() && parent.GetArmedWeapon().equals(obj.GetGuid())) armed = " (armed)";
                hasItems = true;
                output.add("- " + obj.GetLabel() + " (" + GetObjectCount(obj.GetGuid()) + ")" + armed);
            }
        }

        if (!hasItems)
        {
            output.add("No items.");
        }

        output.add("\n\n");

        return output;
    }


    // Attribute value fixing methods
    public String FixAttributeValue(Statement cmd, String rawString, Element attribute)
    {
        boolean handled = false;
        Global_Data global = new Global_Data();

        if (rawString.equals("")) return rawString;
        if (attribute.GetType() == null || attribute.GetType().equals("")) return rawString;

        if (!handled && attribute.GetType().equals("attribute"))
        {
            try
            {
                handled = true;
                if (rawString.indexOf(":") < 0)
                {
                    Element self = GetSelf(attribute);
                    if (self != null)
                    {
                        rawString = self.GetGuid() + ":" + rawString;
                    }
                }

                rawString = attribute.FixAliases(rawString, attribute);
                rawString = global.GetAttributeValue(cmd, rawString);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (!handled && (
                rawString.indexOf("{random}") >= 0) ||
                attribute.GetType().equals("random")
           )
        {
            int min = 0;
            int max = 100;

            try
            {
                // Perform the each-time random processing
                handled = true;
                rawString = rawString.replace("{random}|", "");
                if (!rawString.equals(""))
                {
                    if (rawString.indexOf(",") >= 0)
                    {
                        String[] arr = rawString.split(",", 2);
                        if (arr.length < 2) max = Integer.parseInt(arr[0].trim());
                        else
                        {
                            min = Integer.parseInt(arr[0].trim());
                            max = Integer.parseInt(arr[1].trim());
                        }
                    }
                }
                rawString = Functions.RandomInt(min, max) + "";
            }
            catch (Exception ex) {
                ex.printStackTrace();
                min = 0;
                max = 100;
            }
        }

        if (!handled && rawString.indexOf("{attribute}") >= 0)
        {
            try
            {
                // Perform the each-time random processing
                handled = true;
                rawString = rawString.replace("{attribute}|", "");
                if (!rawString.equals(""))
                {
                    rawString = attribute.FixAliases(rawString, attribute);
                    rawString = global.GetAttributeValue(cmd, rawString);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return rawString;
    }

    public String FixAttributeGetValue(Element attribute)
    {
        boolean handled = false;
        String rawString = attribute.GetValue();

        if (!handled && attribute.GetType().equals("random"))
        {
            handled = true;
            int min = 0;
            int max = 100;
            if (!rawString.equals(""))
            {
                try
                {
                    if (rawString.indexOf(",") >= 0)
                    {
                        String[] arr = rawString.split(",", 2);
                        if (arr.length < 2) max = Integer.parseInt(arr[0].trim());
                        else
                        {
                            min = Integer.parseInt(arr[0].trim());
                            max = Integer.parseInt(arr[1].trim());
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    min = 0;
                    max = 100;
                }
            }
            rawString = Functions.RandomInt(min, max) + "";
        }

        // Value is a random value every time
        if (!handled && rawString.indexOf("{random}|") >= 0)
        {
            handled = true;
            rawString = rawString.replace("{random}|", "");
            int min = 0;
            int max = 100;
            if (!rawString.equals(""))
            {
                try
                {
                    if (rawString.indexOf(",") >= 0)
                    {
                        String[] arr = rawString.split(",", 2);
                        if (arr.length < 2) max = Integer.parseInt(arr[0].trim());
                        else
                        {
                            min = Integer.parseInt(arr[0].trim());
                            max = Integer.parseInt(arr[1].trim());
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    min = 0;
                    max = 100;
                }
            }
            rawString = Functions.RandomInt(min, max) + "";
        }

        return rawString;
    }

    // Alias fixing methods
    public String FixAliases(String rawString, Element curElement)
    {
        String output = rawString;

        if (output.indexOf("{self}") >= 0) output = FixSelf(output, curElement);
        if (output.indexOf("{room}") >= 0) output = FixRoom(output);
        // if (output.indexOf("{player}") >= 0) output = FixPlayer(output);
        if (output.indexOf("{armed}") >= 0)
        {
            Element player = curElement.GetPlayer(curElement);
            if (player != null)
            {
                output = player.GetArmedWeapon();
            }
        }

        return output;
    }
    public String FixSelf(String rawString, Element curElement)
    {
        Element self = GetSelf(curElement);

        rawString = rawString.replace("{self}", self.GetGuid());

        return rawString;
    }
    public String FixRoom(String rawString)
    {
        Element room = GetRoom(this);

        rawString = rawString.replace("{room}", room.GetGuid());

        return rawString;
    }
    public String FixPlayer(String rawString)
    {
        Element game = GetGame(this);

        if (game == null) return rawString;

        Element player = game.GetPlayer();

        if (player == null) return rawString;

        rawString = rawString.replace("{room}", player.GetGuid());

        return rawString;
    }
    public void FixPlayerLife(Statement cmd)
    {
        Element game = GetGame(this);

        if (game == null) return;

        Element player = game.GetPlayer();

        if (player == null) return;

        Attribute attribute = new Attribute();
        attribute.LoadAttributes(cmd, player);

        String maxLifeString = player.GetAttributeValue("max_life");
        if (maxLifeString.equals("")) return;

        String lifeString = player.GetAttributeValue("life");
        if (lifeString.equals("")) return;

        try
        {
            int maxLife = Integer.parseInt(maxLifeString);
            int life = Integer.parseInt(lifeString);

            if (life > maxLife) life = maxLife;

            Element attr = player.GetAttribute("life");
            if (attr == null) return;
            attr.SetValue(life + "");
            attribute.SaveAttribute(cmd, attr);
        }
        catch (Exception ex) {}
    }
    public Element GetObjectByMeta(String meta, Element parent)
    {
        Element output = null;
        Element room = parent.GetRoom(parent);

        for (Element obj : room.GetObjects())
        {
            if (Functions.RegMatch(obj.GetMeta(), meta))
            {
                output = obj;
                break;
            }
        }

        return output;
    }
    public String GetPlayerLocation()
    {
        String output = "";
        Element room = GetRoom(this);

        try
        {
            output = room.GetGuid();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return output;
    }
    public List<String> GetConnectorDestinations(Statement cmd, Element connector)
    {
        List<String> output = new ArrayList<String>();
        Command command = new Command();
        ActionSet actionSet = new ActionSet();
        Action action = new Action();

        command.LoadCommands(cmd, connector);
        for (Element comm : connector.GetCommands())
        {
            actionSet.LoadActionSets(cmd, comm);
            for (Element aset : comm.GetActionSets())
            {
                action.LoadActions(cmd, aset);
                for (Element act : aset.GetActions())
                {
                    if (act.GetType().trim().equals("travel"))
                    {
                        if (!output.contains(act.GetNewValue()))
                        {
                            output.add(act.GetNewValue());
                        }
                    }
                }
            }
        }

        return output;
    }

    // Value fixing methods
    public String FixAttributeValue(String curValue, Element action)
    {
        String output = "";

        if (action.GetNewValue().equals("{++}"))
        {
            try
            {
                int tmpVal = Integer.parseInt(curValue);
                tmpVal++;
                output = tmpVal + "";
                return output;
            }
            catch (Exception ex) {}
        }

        if (action.GetNewValue().equals("{--}"))
        {
            try
            {
                int tmpVal = Integer.parseInt(curValue);
                tmpVal--;
                output = tmpVal + "";
                return output;
            }
            catch (Exception ex) {}
        }

        if (action.GetNewValue().indexOf("{+=") >= 0)
        {
            try
            {
                int tmpAdj = Integer.parseInt(action.GetNewValue().replace("{+=", "").replace("}", ""));
                int tmpVal = Integer.parseInt(curValue);
                output = tmpVal + tmpAdj + "";
                return output;
            }
            catch (Exception ex) {}
        }

        if (action.GetNewValue().indexOf("{-=") >= 0)
        {
            try
            {
                int tmpAdj = Integer.parseInt(action.GetNewValue().replace("{-=", "").replace("}", ""));
                int tmpVal = Integer.parseInt(curValue);
                output = tmpVal - tmpAdj + "";
                return output;
            }
            catch (Exception ex) {}
        }


        if (action.GetNewValue().equals("{!}"))
        {
            switch (curValue)
            {
                case "true":
                    output = "false";
                    break;

                case "false":
                    output = "true";
                    break;

                case "on":
                    output = "off";
                    break;

                case "off":
                    output = "on";
                    break;

                case "yes":
                    output = "no";
                    break;

                case "no":
                    output = "yes";
                    break;
            }

            if (!output.equals("")) return output;
        }

        if (action.GetNewValue().indexOf("{random}") >= 0)
        {
            try
            {
                String tmp = action.GetNewValue().replace("{random}|", "").replace("}", "");
                String[] arr = action.GetNewValue().split(",", 2);
                if (arr.length == 2)
                {
                    int min = Integer.parseInt(arr[0].trim());
                    int max = Integer.parseInt(arr[1].trim());
                    output = Functions.RandomInt(min, max) + "";
                    return output;
                }
            }
            catch (Exception ex) {}
        }

        if (output.equals("")) output = action.GetNewValue();

        return output;
    }

    // Move element
    public void MoveElement(String newLoc, Element curElement)
    {
        curElement.SetParentGuid(newLoc);
        curElement.SetLocation(newLoc);
    }



    // Element finding methods
    public Element GetNpc(Element curElem)
    {
        Element output = curElem;

        if (Functions.Match(curElem.GetElementType(), "npc"))
        {
            return output;
        }

        if (curElem.GetParent() != null) output = GetNpc(curElem.GetParent());

        return output;
    }
    public Element GetSelf(Element curElem)
    {
        Element output = curElem;

        if (Functions.StrictMatch(curElem.GetElementType(), "room|object|game|player|npc"))
        {
            return output;
        }

        if (curElem.GetParent() != null) output = GetSelf(curElem.GetParent());

        return output;
    }
    public Element GetElementRoom(Element curElem)
    {
        Element output = curElem;

        if (Functions.StrictMatch(curElem.GetElementType(), "room|game"))
        {
            return output;
        }

        if (curElem.GetParent() != null) output = GetElementRoom(curElem.GetParent());

        return output;
    }
    public Element GetGame(Element curElem)
    {
        Element output = curElem;

        try
        {
            if (!Functions.Match(GetType(), "game") &&
                curElem.GetParent() != null)
            {
                output = GetGame(curElem.GetParent());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Error("cur obj: " + curElem.GetGuid());
        }

        return output;
    }
    public Element GetRoom(Element curElem)
    {
        Element room = GetGame(curElem).GetRoom();
        return room;
    }
    public Element GetPlayer(Element curElem)
    {
        Element player = GetGame(curElem).GetPlayer();
        return player;
    }
    public Element GetElement(Statement cmd, String guid, Element curElement)
    {
        Element game = GetGame(curElement);
        Element room = GetRoom(curElement);
        Element player = GetPlayer(curElement);
        Object object = new Object();
        NpcTravelSet tset = new NpcTravelSet();
        Element output = null;

        // Search the Room
        if (output == null)
        {
            if (guid.equals("{room}") || room.GetGuid().equals(guid))
            {
                output = room;
            }
        }

        // Search the Game
        if (output == null)
        {
            if (guid.equals("{game}"))
            {
                output = game;
            }
        }

        if (output == null)
        {
            if (guid.equals("{self}"))
            {
                Element self = GetSelf(curElement);
                output = self;
            }
        }

        // Search the Player
        if (output == null)
        {
            if (guid.equals("{player}") || player.GetGuid().equals(guid))
            {
                output = player;
            }
        }

        // Search the Room Objects
        if (output == null)
        {
            output = GetElementObject(guid, room);
        }

        // Search the Player Objects
        if (output == null)
        {
            output = GetElementObject(guid, player);
        }

        // Search the NPCs
        if (output == null)
        {
            for (Element npc : game.GetNpcs())
            {
                if (npc.GetGuid().equals(guid))
                {
                    output = npc;
                    break;
                }
                else
                {
                    output = GetElementObject(guid, npc);
                    if (output != null) break;
                }

                // Search the TravelSets
                tset.LoadNpcTravelSets(cmd, npc);

                for (Element trv : npc.GetNpcTravelSets())
                {
                    if (trv.GetGuid().equals(guid))
                    {
                        output = trv;
                        break;
                    }
                }

                if (output != null)
                {
                    break;
                }
            }
        }

        // Search the Game
        if (output == null)
        {
            for (Element o : game.GetObjects())
            {
                if (o.GetGuid().equals(guid))
                {
                    output = o;
                    break;
                }
            }
        }

        // Get an ObJect from the DB
        if (output == null)
        {
            output = object.LoadObjectFromData(cmd, guid);
        }


        return output;
    }
    public Element GetElementObject(String guid, Element curElement)
    {
        Element output = null;

        // Search Objects
        for (Element obj : curElement.GetObjects())
        {
            if (obj.GetGuid().equals(guid))
            {
                output = obj;
            }
            else
            {
                output = GetElementObject(guid, obj);
            }

            if (output != null) break;
        }

        return output;
    }
}
