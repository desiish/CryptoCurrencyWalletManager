package bg.sofia.uni.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandTest {
    public Command command;

    @Test
    public void testNewCommandTwoArguments() {
        String[] args = new String[]{"arg1", "arg2"};
        command = new Command("test", args);
        Command second = Command.createCommand("test arg1 arg2");
        assertEquals(command.command(), second.command(),
            "Expected: " + command.command() + " but was actually "
                    + second.command());
        int it = 0;
        for (String s : command.arguments()) {
            assertEquals(s, second.arguments()[it],
                "Expected: " + s + " but was actually "
                    + second.arguments()[it++]);
        }
    }

    @Test
    public void testNewCommandZeroArguments() {
        command = new Command("test", null);
        assertEquals(command, Command.createCommand("test"),
            "Couldn`t parse command correctly.");
    }

    @Test
    public void testNewCommandClientInputIsNull() {
        command = new Command("test", null);
        assertThrows(IllegalArgumentException.class, () -> Command.createCommand(null),
            "IllegalArgumentException expected but was never thrown");
    }
}