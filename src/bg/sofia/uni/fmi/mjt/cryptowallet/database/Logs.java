package bg.sofia.uni.fmi.mjt.cryptowallet.database;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logs {
    private static final String LOG_PATH = "database" + File.separator + "logs.log";

    public static void logErrorWithStackTrace(StackTraceElement[] ste, String message) {
        Logger logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);
        FileHandler fh;

        try {
            fh = new FileHandler(LOG_PATH, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            StringBuilder stackTrace = new StringBuilder();
            for (var el : ste) {
                stackTrace.append(System.lineSeparator()).append(el.toString());
            }

            logger.info(message + stackTrace);
            fh.close();
        } catch (SecurityException | IOException e) {
            System.out.println("Could not log error");
        }
    }
}
