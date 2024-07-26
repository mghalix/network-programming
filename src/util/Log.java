package util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class Log {
    public static Log makeMe(String name, String logPath) {
        Log log = new Log(name);
        log.setupLogger(logPath);
        return log;
    }

    public static Log makeMe(String name, String logPath, boolean verbose) {
        Log.verbose = verbose;
        return makeMe(name, logPath);
    }

    public void check(String message, boolean verbose) {
        if (verbose) {
            logger.info(message);
        }
    }

    public void check(String message, boolean verbose, Level level) {
        if (verbose) {
            logger.log(level, message);
        }
    }

    public void err(String message) {
        message = colorMessage(message, ANSI_RED);
        logger.log(Level.SEVERE, message);
    }

    public void err(String message, Exception e) {
        message = colorMessage(message, ANSI_RED);
        logger.log(Level.SEVERE, message, e);
    }

    public void warn(String message) {
        message = colorMessage(message, ANSI_YELLOW);
        logger.log(Level.WARNING, message);
    }

    public void ok(String message) {
        message = colorMessage(message, ANSI_CYAN);
        this.check(message, verbose);
    }

    public void info(String message) {
        message = colorMessage(message, ANSI_GREEN);
        logger.info("\u001B[32m" + message + "\u001B[0m");
    }

    // --------------------------------------------------------------------
    private void createLogDir() {
        var logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdir();
        }
    }

    private void createLogFile(String logFile) {
        var log = new File(LOG_DIR + logFile);
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                logger.warning("Error creating log file: " + e.getMessage());
            }
        }
    }

    private void initLogFile(String logFile) {
        createLogDir();
        createLogFile(logFile);
    }

    private void setupLogger(String logFile) {
        initLogFile(logFile);
        try {
            var fileHandler = new FileHandler(LOG_DIR + logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.warning("Error setting up logger: " + e.getMessage());
        }

    }

    private Log(String name) {
        logger = Logger.getLogger(name);
    }

    private String colorMessage(String message, String color) {
        return color + message + ANSI_RESET;
    }

    // ANSI escape code
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    // --------------------------------------------------------------------
    private Logger logger;
    private static boolean verbose = false;
    final String LOG_DIR = "log/";
}


class LogTest {
    public static void main(String[] args) {
        var log = Log.makeMe(LogTest.class.getName(), "test.log");
        log.check("This is a check", true);
        log.check("This is a check", true, Level.WARNING);
        log.err("This is an error", new Exception("This is an exception"));
        log.warn("This is a warning");
        log.ok("This is an ok");
    }
}
