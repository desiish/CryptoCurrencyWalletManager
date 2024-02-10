package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import java.util.Arrays;

public record Command(String command, String[] arguments) {
    private static final int ONE_WORD_COMMAND_LEN = 1;
    private static final String DELIMITER = " ";

    public static Command createCommand(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        String[] tokens = input.split(DELIMITER);

        if (tokens.length == ONE_WORD_COMMAND_LEN) {
            return new Command(input, null);
        }

        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        return new Command(tokens[0], args);
    }
}