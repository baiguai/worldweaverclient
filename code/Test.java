import java.io.*;
import java.io.Console;
import java.sql.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

/**
    The Test class runs defined unit tests, making game testing far less tedious...
    And it allows for test driven game development.
    The output is always a string so evaluations are generally indexOf checks etc.

    This class hijacks the game's output until the test is complete, so that the
    output can be evaluated.

    For example, to test picking up an item, the take command can be called, then
    the Player's inventory shown, and in indexOf that taken item's label could be
    the eval.

    Use the admin commands as well as game commands, like you would for normal WorldWeaver
    tests which are basically macros.
*/
public class Test
{
    /* PROPERTIES */
    //
    //

    public void RunUnitTest(String testAlias)
    {
        //
    }
}
