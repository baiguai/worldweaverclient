import java.io.File;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Random;

/*
    INPUT HELPER METHODS
    ----------------------------------------------------------------------------
    Parses the user input into commands, parameters, and concat. parameter
    string
    ----------------------------------------------------------------------------
*/
public class Input
{
    /* Properties */
    //
        private boolean match = false;
        public boolean GetMatch() { return match; }
        public void SetMatch(boolean val) { match = val; }

        private String userInput = "";
        public String GetUserInput() { return userInput; }
        public void SetUserInput(String val) { userInput = val; }

        private String inputCommand = "";
        public String GetInputCommand() { return inputCommand; }
        public void SetInputCommand(String val) { inputCommand = val; }

        private String paramString = "";
        public String GetParamString() { return paramString; }
        public void SetParamString(String val) { paramString = val; }

        private String[] params;
        public String[] GetParams() { return params; }
        public void SetParams(String[] val) { params = val; }

        private List<String> output;
        public List<String> GetOutput() { if (output == null) output = new java.util.concurrent.CopyOnWriteArrayList<String>(); return output; }
        public void SetOutput(String val) { ClearOutput(); GetOutput().add(val); }
        public void SetOutput(List<String> val) { output = val; }
        public void ClearOutput() { output = null; }
        public void AppendOutput(String val) { GetOutput().add(val); }
        public void AppendOutput(List<String> val)
        {
            for (String s : val)
            {
                GetOutput().add(s);
            }
        }
        public int GetOutputSize() { return GetOutput().size(); }
        public boolean HasOutput() { return GetOutputSize() > 0; }
    //



    // Constructor
    public Input (
        String userInputIn
    )
    {
        SetUserInput(userInputIn);
        String[] arr = userInputIn.split(" ");
        SetInputCommand(userInputIn);
        if (arr.length > 1)
        {
            SetInputCommand(arr[0]);
            SetParamString(Functions.ArrayToString(arr, 1, " "));
        }

        if (!GetParamString().equals(""))
        {
            SetParams(GetParamString().split(","));
        }
    }


    public boolean CmdMatch(String input)
    {
        boolean output = false;

        output = Functions.Match(GetInputCommand(), input);

        return output;
    }
}
