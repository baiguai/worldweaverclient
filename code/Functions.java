import java.io.*;
import java.util.*;
import java.util.Random;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import javax.swing.*;

/*
    GENERAL HELPER FUNCTIONS
    ----------------------------------------------------------------------------
    Methods designed to make life easier.
    ----------------------------------------------------------------------------
*/
public class Functions
{
    private static Random gRnd = new Random();
    private static boolean Debugging = true;

    // Debuggig message
    public static void Debug(String file, String message)
    {
        if (Debugging)
        {
            System.out.println("\nDebug (" + file + "):\n - " + message + "\n--------\n");
        }
    }
    public static void Debug(String file, List<String> messages)
    {
        if (Debugging)
        {
            System.out.println("\nDebug (" + file + "):\n");
            for (String s : messages)
            {
                System.out.println(" - " + s + "\n");
            }
            System.out.println("--------\n");
        }
    }

    // Cleanup ID
    public static String IDCleanup(String id)
    {
        id = id.replace("[", "").replace("]", "").trim();
        return id;
    }

    // GUID Generator
    public static String GetGUID()
    {
        String output = "";

        UUID uuid = UUID.randomUUID();
        output = uuid.toString();

        return output;
    }

    // Case insensitive match - or contains.
    public static boolean StrictMatch(String source1, String source2)
    {
        boolean isMatch = false;
        String[] arr = GetArray(source1);
        String[] arr2 = GetArray(source2);

        for (String itm : arr)
        {
            for (String itm2 : arr2)
            {
                if (itm.equalsIgnoreCase(itm2))
                {
                    isMatch = true;
                    break;
                }
            }
        }

        return isMatch;
    }

    public static boolean Match(String source1, String source2)
    {
        boolean isMatch = false;
        String[] arr = GetArray(source1);
        String[] arr2 = GetArray(source2);

        for (String itm : arr)
        {
            for (String itm2 : arr2)
            {
                if (itm.equalsIgnoreCase(itm2) ||
                    itm.toLowerCase().contains(itm2.toLowerCase()))
                {
                    isMatch = true;
                    break;
                }
            }
        }

        return isMatch;
    }

    public static boolean MatchWord(String source1, String source2)
    {
        boolean isMatch = false;
        String[] arr = GetArray(source2);

        for (String itm: arr)
        {
            if (source1.equalsIgnoreCase(itm) ||
                source1.toLowerCase().equals(source2.toLowerCase()))
            {
                isMatch = true;
                break;
            }
        }

        return isMatch;
    }

    // Wildcard Match
    public static boolean RegMatch(String target, String input)
    {
        boolean isMatch = false;

        target = target.toLowerCase();
        input = input.toLowerCase();

        String[] arr = GetArray(target);
        String[] arr2 = GetArray(input);

        input = input.trim();

        for (String sTarget : arr)
        {
            for (String sInput : arr2)
            {
                if (Pattern.matches(WildcardToRegex(sTarget), sInput))
                {
                    isMatch = true;
                    break;
                }
            }
        }

        return isMatch;
    }

    public static String WildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }

    public static String[] GetArray(String input)
    {
        input = input.replace("|", ",");
        String[] arr = input.split(",");
        return arr;
    }

    public static void OutputRaw(String message)
    {
        System.out.println(message);
    }
    public static void Output(List<String> message)
    {
        int maxLine = 80;
        String div = "";
        boolean isIndented = false;
        for (int ix = 0; ix < maxLine; ix++) { div += "-"; }

        System.out.println("\n\n");
        for (String s : message)
        {
            if (s == ">>")
            {
                isIndented = true;
                continue;
            }
            if (s == "<<")
            {
                isIndented = false;
                continue;
            }
            if (isIndented == true)
            {
                s = "    " + s;
            }
            if (s == "---")
            {
                s = div;
            }
            System.out.println(s);
        }
        System.out.println("\n\n");
    }
    public static void Output(JTextArea txt, String message)
    {
        txt.setText(message.replace("\\n", "\n"));
    }
    public static void OutputWrapped(List<String> message)
    {
        OutputWrapped(message, 80);
    }
    public static void OutputWrapped(List<String> message, int maxLength)
    {
        int maxLine = 80;
        String div = "";
        boolean isIndented = false;
        for (int ix = 0; ix < maxLine; ix++) { div += "-"; }

        System.out.println("\n\n");
        for (String s : message)
        {
            if (s == ">>")
            {
                isIndented = true;
                continue;
            }
            if (s == "<<")
            {
                isIndented = false;
                continue;
            }
            if (isIndented == true)
            {
                s = "    " + s;
            }
            if (s == "---")
            {
                s = div;
            }

            OutputWrapped(s, maxLength);
        }
        System.out.println("\n\n");
    }
    public static void Output(String message)
    {
        System.out.println("\n\n" + message + "\n\n");
    }
    public static void OutputWrapped(String message)
    {
        OutputWrapped(message, 80);
    }
    public static void OutputWrapped(String message, int maxLength)
    {
        System.out.println(WrapString(message, "\n", maxLength));
    }
    public static void Error(String message)
    {
        System.out.println("\n\n!ERROR!\n" + message + "\n\n");
    }

    public static List<String> TrimAll(List<String> message)
    {
        List<String> output = new ArrayList<String>();

        for (String s : message)
        {
            output.add(s.trim());
        }

        return output;
    }

    public static List<String> CleanList(List<String> lst)
    {
        boolean hasContents = false;

        for (String s : lst)
        {
            if (s.length() > 0)
            {
                hasContents = true;
                break;
            }
        }

        if (hasContents) return lst;
        else return new ArrayList<String>();
    }

    public static boolean ListHasData(List<String> lst)
    {
        boolean output = false;

        if (lst.size() > 0)
        {
            if (lst.size() > 1 ||
                !lst.get(0).trim().equals(""))
            {
                output = true;
            }
        }

        return output;
    }

    public static String WrapString(String s, String delim, int length) {
        String result = "";

        s = s.replace("\\n", "\n");

        for (String line : s.split("\n"))
        {
            result += WrapLine(line.trim(), delim, length).replace("\\t", "   ");
        }

        return result;
    }
    public static String WrapLine(String s, String delim, int length) {
        String result = "";
        String tmp = "";

        int lastdelimPos = 0;

        for (String token : s.split(" ", -1)) {
            if (tmp.length() - lastdelimPos + token.length() > length) {
                tmp = tmp + delim + token;
                lastdelimPos = tmp.length() + 1;
            }
            else {
                tmp += (tmp.isEmpty() ? "" : " ") + token;
            }
        }

        for (String sTmp : tmp.split(delim))
        {
            result += sTmp + delim;
        }

        return result;
    }

    // Convert to Table name
    public static String ToTable(String name)
    {
        name = name.trim().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    // True Test from String
    public static boolean TrueString(String checkVal)
    {
        boolean output = false;
        if (checkVal.equals("")) return output;

        checkVal = checkVal.trim().toLowerCase();

        if (checkVal.equals("true") ||
            checkVal.equals("1"))
        {
            output = true;
        }

        return output;
    }

    // Array to string.
    public static String ArrayToString(String[] array, int startIndex, String delimiter)
    {
        String output = "";

        for (int ix = startIndex; ix < array.length; ix++)
        {
            if (!output.equals("")) output += delimiter;
            output += array[ix];
        }

        return output;
    }

    // Random Int
    // if min = -1, only use the max value
    public static int RandomInt(int min, int max)
    {
        Random rnd = new Random();
        int output = -1;
        if (min >= max)
        {
            max = min;
            min = -1;
        }
        if (min < 0) min = 0;

        while (output < min)
        {
            output = rnd.nextInt(max + 1);
        }
        
        return output;
    }

    // Clear the console
    public static void ClearConsole()
    {
        try
        {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows"))
            {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else
            {
                System.out.print("\033[H\033[2J");
            }
        }
        catch (final Exception e)
        {
            //  Handle any exceptions.
        }
    }

    // Get Setting
    /*
        -   file
            This parameter is the file path without extension.
            It is relative to the application root.
            It must have a .properties extension.
    */
    public static String GetSetting(String file, String key, String defVal)
    {
        Properties prop = new Properties();
        String value = "";

        try
        {
            Properties props = new Properties();
            String propFile = file;
            FileInputStream inputStream = new FileInputStream(propFile);

            if (inputStream != null)
            {
                prop.load(inputStream);
                value = prop.getProperty(key);
            }
            else
            {
                key = "";
                //throw new FileNotFoundException("Config file not found: " + propFile);
            }
        }
        catch (Exception ex)
        {
            value = "";
        }

        if (value == null) value = "";

        if (value.equals("")) value = defVal;

        return value;
    }


    // Get Formatted Date
    public static String GetDate()
    {
        String output = "";
        
        Date today = new Date();
        SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        output = TS_FORMAT.format(today);

        return output;
    }


    public static List<String> ListFiles(String path)
    {
        List<String> files = new ArrayList<String>();

        try
        {
            // Iterate through the plugins dir
            File[] fileList = new File(path).listFiles();

            if (fileList == null) return files;

            for (File file : fileList)
            {
                if (!file.isDirectory())
                {
                    files.add(file.getName());
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return files;
    }

    public static List<String> ListFiles(String path, String extension)
    {
        List<String> files = new ArrayList<String>();

        try
        {
            // Iterate through the plugins dir
            File[] fileList = new File(path).listFiles();

            if (fileList == null) return files;

            for (File file : fileList)
            {
                if (extension.equals("") || !file.isDirectory() && file.getName().indexOf(extension) >= 0)
                {
                    files.add(file.getName());
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return files;
    }

    public static List<String> ListJarFiles(String jar, String extension)
    {
        List<String> files = new ArrayList<String>();

        try
        {
            JarFile jarFile = new JarFile(jar);
            Enumeration allEntries = jarFile.entries();

            while (allEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) allEntries.nextElement();
                String name = entry.getName();

                if (name.indexOf(extension) >= 0)
                {
                    files.add(name);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return files;
    }

    public static List<String> ListDirContents(String path)
    {
        List<String> files = new ArrayList<String>();

        try
        {
            // Iterate through the plugins dir
            File[] fileList = new File(path).listFiles();

            if (fileList == null) return files;

            for (File file : fileList)
            {
                if (file.isDirectory())
                {
                    files.add("[" + file.getName() + "]");
                }
            }
            for (File file : fileList)
            {
                if (!file.isDirectory())
                {
                    files.add(file.getName());
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return files;
    }

    public static List<String> ReadFile(String file)
    {
        return ReadFile(file, false);
    }
    public static List<String> ReadFileRaw(String file)
    {
        return ReadFile(file, true);
    }

    public static List<String> ReadFile(String file, boolean raw)
    {
        List<String> inp = new ArrayList<String>();
        String line;

        try
        {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    if (!raw)
                    {
                        line = line.trim();
                        // # at the start of the line is a comment, don't read
                        if (!line.equals(""))
                        {
                            if (!line.substring(0, 1).equals("#"))
                            {
                                inp.add(line);
                            }
                        }
                    }
                    else
                    {
                        inp.add(line);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("File: " + file + "\ndoes not exist.");
        }

        return inp;
    }

    public static void Log(String line)
    {
        Log(line, 0);
    }
    public static void Log(String line, int indentLevel)
    {
        if (!Debugging) return;

        String time = GetDate();
        String ind = "   ";
        String indent = "";
        String file = "debug.log";

        for (int i = 0; i < indentLevel; i++)
        {
            indent += ind;
        }

        WriteToFile(file, "[" + time + "] : " + indent + line);
    }


    public static void WriteToFile(String file, String line)
    {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            out.println(line);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Functions.Output("File: " + file);
        }
    }

    public static void WriteToFile(String file, List<String> contents)
    {
        try
        {
            PrintWriter writer = new PrintWriter(file, "UTF-8");

            for (String s : contents)
            {
                writer.println(s);
            }

            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Functions.Output("File: " + file);
        }
    }

    public static boolean CopyFile(String source, String dest) throws IOException
    {
        boolean success = false;

        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            success = true;
        }
        catch (Exception ex)
        {
            success = false;
        }
        finally {
            is.close();
            os.close();
        }

        return success;
    }

    public static Document GetDoc(String file)
    {
        Document doc = null;
        File xmlFile = null;
        DocumentBuilderFactory dbFactory = null;
        DocumentBuilder dBuilder = null;

        try
        {
            xmlFile = new File(file);
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return doc;
    }


    // Encryption / Decryption
    private static Cipher GenerateCipher()
    {
        Cipher cipher = null;
        byte[] keyBytes = ("60061c8c2de01ce9").getBytes();
        byte[] ivBytes = ("5188b8b8").getBytes();

        try
        {
            // wrap key data in Key/IV specs to pass to cipher
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            // create the cipher with the algorithm you choose
            // see javadoc for Cipher class for more info, e.g.
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return cipher;
    }

    public static String EncryptString(String inputString)
    {
        byte[] encrypted = null;

        try
        {
            Cipher cipher = GenerateCipher();

            byte[] input = inputString.getBytes();
            encrypted = new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
            enc_len += cipher.doFinal(encrypted, enc_len);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return encrypted.toString();
    }


    // General Helper Methods
    public static boolean BooleanString(String input)
    {
        boolean output = true;

        if (input.trim().equals("0") ||
            input.trim().toLowerCase().equals("false"))
        {
            output = false;
        }

        return output;
    }


    // Sql Cleanup.
    public static String Encode(String input)
    {
        return SqlCleanup(input);
    }
    public static String SqlCleanup(String input)
    {
        if (input.equals("")) return input;

        input = input.replaceAll("'", "''");

        return input;
    }
}
