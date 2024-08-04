package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.EventManager;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import com.toxicstoxm.LEDSuite.logging.Logger;
import com.toxicstoxm.LEDSuite.logging.network.NetworkLogger;
import com.toxicstoxm.LEDSuite.settings.LocalSettings;
import com.toxicstoxm.LEDSuite.settings.ServerSettings;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteGuiRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteScheduler;
import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.LEDSuite.time.TimeManager;
import com.toxicstoxm.LEDSuite.ui.Window;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLSerializer;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.ServerError;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import lombok.Getter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.gnome.adw.Adw;
import org.gnome.adw.Application;
import org.gnome.adw.Toast;
import org.gnome.gio.ApplicationFlags;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.awt.Toolkit.getDefaultToolkit;

@CommandLine.Command(name = "LEDSuite",
        description = "Simple front end application that lets you control decorative matrix's.")
public class LEDSuite implements EventListener, Runnable {

    @Getter
    private static LEDSuite instance;
    @Getter
    private final Application app;
    public static LocalSettings settings;
    public static LocalSettings argumentsSettings;
    public static ServerSettings server_settings;
    public static Logger logger;
    public static NetworkLogger networkLogger;
    private static long start;
    public static EventManager eventManager;
    public static Window mainWindow;
    public static LEDSuiteScheduler ledSuiteScheduler;
    public static TickingSystem tickingSystem;
    public static ResourceBundle messages;

    @CommandLine.Option(
            names = {"-h", "--help"},
            description = "Show this help message and exit.",
            usageHelp = true
    )
    private boolean help;

    @CommandLine.Option(
            names = {"-v", "--version"},
            description = "Print version information and exit.",
            versionHelp = true
    )
    private boolean version;

    @CommandLine.Option(
            names = {"-l", "--log-level"},
            description = "Change the log level for the current session."
    )
    private Logger.log_level logLevel;

    @CommandLine.Option(
            names = {"-L", "--set-log-level"},
            description = "Permanently change the log level."
    )
    private Logger.log_level setLogLevel;

    @CommandLine.Option(
            names = {"--initial-window-width"},
            description = "Change the initial window width for the current session."
    )
    private int initialWindowWidth = -1;

    @CommandLine.Option(
            names = {"--set-initial-window-width"},
            description = "Permanently change the initial window width."
    )
    private int setInitialWindowWidth = -1;

    @CommandLine.Option(
            names = {"--initial-window-height"},
            description = "Change the initial window height for the current session."
    )
    private int initialWindowHeight = -1;

    @CommandLine.Option(
            names = {"--set-initial-window-height"},
            description = "Permanently change the initial window height."
    )
    private int setInitialWindowHeight = -1;

    @CommandLine.Option(
            names = {"--networking-clock"},
            description = "Change the networking clock for the current session."
    )
    private int networkingClock = -1;

    @CommandLine.Option(
            names = {"--set-networking-clock"},
            description = "Permanently change the networking clock."
    )
    private int setNetworkingClock = -1;

    @CommandLine.Option(
            names = {"--status-clock-passive"},
            description = "Change the status request passive clock for the current session."
    )
    private int statusClockPassive = -1;

    @CommandLine.Option(
            names = {"--set-status-clock-passive"},
            description = "Permanently change the status request passive clock."
    )
    private int setStatusClockPassive = -1;

    @CommandLine.Option(
            names = {"--status-clock-active"},
            description = "Change the status request active clock for the current session."
    )
    private int statusClockActive = -1;

    @CommandLine.Option(
            names = {"--set-status-clock-active"},
            description = "Permanently change the status request active clock."
    )
    private int setStatusClockActive = -1;

    /*@CommandLine.Option(
            names = {"-w", "--write-log-to-file"},
            description = "Change if the log should be written to a log file for the current session."
    )
    private boolean writeLogToFile;

    @CommandLine.Option(
            names = {"-W", "--set-write-log-to-file"},
            description = "Permanently change if the log should be written to a log file."
    )
    private boolean setWriteLogToFile;

    @CommandLine.Option(
            names = {"--write-log-level-all"},
            description = "Change if all log levels should be written to the log file for the current session."
    )
    private boolean writeLogLevelAll = true;

    @CommandLine.Option(
            names = {"--set-write-log-level-all"},
            description = "Permanently change if all log levels should be written to the log file."
    )
    private boolean setWriteLogLevelAll = false;*/

    @CommandLine.Option(
            names = {"--max-log-files"},
            description = "Change the maximum number of log files allowed for the current session."
    )
    private int maxLogFiles = -1;

    @CommandLine.Option(
            names = {"--set-max-log-files"},
            description = "Permanently change the maximum number of log files allowed."
    )
    private int setMaxLogFiles = -1;

    @CommandLine.Option(
            names = {"-R", "--reset-config"},
            description = "Reset configuration values to default. Type 'true' to confirm!"
    )
    private boolean resetConfig;

    @CommandLine.Option(
            names = {"-p", "--get-paths"},
            description = "Display all important paths."
    )
    private boolean getPaths;

    /*@CommandLine.Option(
            names = {"--libadwaita-args"},
            description = "Pass through arguments to libadwaita."
    )
    private String[] libadwaitaArguments;*/

    @CommandLine.Option(
            names = {"--stack-trace-depth"},
            description = "Change the maximum stack trace depth for the current session."
    )
    private int stackTraceDepth = -2;
    @CommandLine.Option(
            names = {"--set-stack-trace-depth"},
            description = "Permanently change the maximum stack trace depth."
    )
    private int setStackTraceDepth = -2;

    /*@CommandLine.Option(
            names = {"--color-code-log"},
            description = "Change if the log should be color coded for the current session."
    )
    private boolean colorCodeLog;
    @CommandLine.Option(
            names = {"--set-color-code-log"},
            description = "Permanently change if the log should be color coded."
    )
    private boolean setColorCodeLog;*/


    // main method
    public static void main(String[] args) {

        // create timestamp used to calculate starting time
        start = System.currentTimeMillis();

        // initialize picocli and parse the arguments
        CommandLine cmd = new CommandLine(new LEDSuite());
        cmd.getCommandSpec().version(Constants.Application.VERSION_DESC);
        try {
            cmd.parseArgs(args);
        } catch (CommandLine.ParameterException _) {
        }

        // processes commandline arguments and run application
        int statusCode = cmd.execute(args);
        if (logger != null) logger.debug("Picocli exit code = " + statusCode);
    }

    // constructor method
    public LEDSuite() {
        instance = this;
        // create new libadwaita application object
        app = new Application(Constants.Application.DOMAIN, ApplicationFlags.DEFAULT_FLAGS);
        app.setVersion(Constants.Application.VERSION);
        app.setApplicationId(Constants.Application.DOMAIN);
        // define function to be executed on application start
        app.onActivate(this::activate);
        // trigger exit() function
        app.onShutdown(() -> exit(0));
    }

    @Override
    public void run() {
        // initialize config, logger, ...
        logicInit();
        // start application
        app.run(null);
    }

    // logic initialization function
    public void logicInit() {
        // program initialization
        // creates new settings and server_settings classes to hold config settings
        settings = new LocalSettings();
        argumentsSettings = new LocalSettings();
        server_settings = new ServerSettings();
        // create new logger instance
        logger = new Logger();
        // create new networkLogger instance
        networkLogger = new NetworkLogger();
        // initializing task scheduler with an initial max pool size of 5
        // that means only 5 tasks can be run at once,
        // this is adjusted dynamically to ensure efficient resource usage
        ledSuiteScheduler = new LEDSuiteScheduler();

        // defining config files and log file
        File config_file = new File(Constants.File_System.config);
        File server_config_file = new File(Constants.File_System.server_config);
        File log_file = new File(Constants.File_System.logFile);
        File temp = null;
        try {
            if (resetConfig) {
                Scanner scanner = new Scanner(System.in);
                String confirmString = "confirm reset";
                String input;

                logger.log("Type '" + confirmString + "' to reset configuration: ", false);
                input = scanner.nextLine();

                if (input.equals(confirmString)) {
                    if (config_file.delete() || server_config_file.delete() || log_file.delete()) {
                        logger.log("Config has been reset successfully! Starting application...");
                    }
                } else {
                    logger.log("Received wrong confirmation string '" + input + "'. Cancelled config reset!");
                    getInstance().app.emitShutdown();
                }
            }

            // checking if the config file already exists
            if (!config_file.exists()) {
                // if the config file doesn't exist, the program tries to create the parent directory first to prevent errors if it's the first startup
                if (config_file.getParentFile().mkdirs())
                    logger.verbose("Successfully created parent directory for config file: " + config_file.getParentFile().getAbsolutePath());
                // then a new config file is loaded with the default values from internal resources folder using the configs saveDefaultConfig() function
                if (config_file.createNewFile()) {
                    logger.debug("New config file was successfully created: " + config_file.getAbsolutePath());
                    settings.saveDefaultConfig();
                } else {
                    // if the config can't be created for some reason, a waring message is displayed in the console
                    logger.warn("Config couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            }
            // checking if the server side config file already exists
            if (!server_config_file.exists()) {
                // if the config file doesn't exist, a new one gets loaded from the internal resources folder with its default values using the configs saveDefaultConfig() function
                if (server_config_file.createNewFile()) {
                    logger.debug("New server config file was successfully created: " + server_config_file.getAbsolutePath());
                    server_settings.saveDefaultConfig();
                } else {
                    // if the config can't be created for some reason, a waring message is displayed in the console
                    logger.warn("Server config couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            }
            // check if log file already exists
            if (!log_file.exists()) {
                Files.createDirectories(Path.of(log_file.getParent()));
                // if it does not exist, a new one will be created
                if (log_file.createNewFile()) {
                    logger.debug("New log config file was successfully created: " + log_file.getAbsolutePath());
                } else {
                    // if there are any exceptions during the creation of a new log file, a warning message is displayed in the console
                    logger.warn("Log file couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            } else {
                temp = getFile(log_file);
                logger.debug("Moving existing 'latest.log' to '" + temp.getName() + "'...");
                logger.debug(log_file.renameTo(new File(temp.getAbsolutePath())) ? "Moving success!" : "Moving failed!");
                logger.debug("Erasing old log file contents...");
                // if the log file already exists, its contents are erased using a file writer
                // the file writer is configured to start writing at the beginning of the file
                try (FileWriter writer = new FileWriter(log_file, false)) {
                    // the file writer then writes an empty string to the file
                    // this erases the other file contents
                    writer.write("");
                    logger.debug("Existing log file contents erased: " + log_file.getAbsolutePath());
                }
            }
        } catch (IOException | NullPointerException e) {
            // if any exceptions occur during file creation / modification, an error is displayed in the console
            // additionally the program is halted to prevent any further issues or unexpected behavior
            logger.error("Settings failed to load!");
            logger.warn("Application was halted!");
            logger.warn("If this keeps happening please open an issue on GitHub!");
            logger.warn("Please restart the application!");
            getInstance().app.emitShutdown();
            return;
        }

        // creating a new event manager
        eventManager = new EventManager();
        // registering event listeners for settings classes
        eventManager.registerEvents(settings);
        eventManager.registerEvents(server_settings);

        // user settings are loaded from config files
        loadConfigsFromFile();

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        eventManager.fireEvent(new Events.Startup("Starting application! Current date and time: " + df.format(new Date())));

        argumentsSettings.copyImpl(settings, false);
        if (setLogLevel != null) {
            argumentsSettings.setLogLevel(setLogLevel.getValue());
            settings.setLogLevel(setLogLevel.getValue());
        } else if (logLevel != null) {
            argumentsSettings.setLogLevel(logLevel.getValue());
        }

        if (setInitialWindowWidth > 0) {
            argumentsSettings.setWindowDefWidth(setInitialWindowWidth);
            settings.setWindowDefWidth(setInitialWindowWidth);
        } else if (initialWindowWidth > 0) {
            argumentsSettings.setWindowDefWidth(initialWindowWidth);
        }

        if (setInitialWindowHeight > 0) {
            argumentsSettings.setWindowDefHeight(setInitialWindowHeight);
            settings.setWindowDefHeight(setInitialWindowHeight);
        } else if (initialWindowHeight > 0) {
            argumentsSettings.setWindowDefHeight(initialWindowHeight);
        }

        if (setNetworkingClock > -1) {
            argumentsSettings.setNetworkingCommunicationClock(setNetworkingClock);
            settings.setNetworkingCommunicationClock(setNetworkingClock);
        } else if (networkingClock > -1) {
            argumentsSettings.setNetworkingCommunicationClock(networkingClock);
        }

        if (setStatusClockPassive > -1) {
            argumentsSettings.setStatusRequestClockPassive(setStatusClockPassive);
            settings.setStatusRequestClockPassive(setStatusClockPassive);
        } else if (statusClockPassive > -1) {
            argumentsSettings.setStatusRequestClockPassive(statusClockPassive);
        }

        if (setStatusClockActive > -1) {
            argumentsSettings.setStatusRequestClockActive(setStatusClockActive);
            settings.setStatusRequestClockActive(setStatusClockActive);
        } else if (statusClockActive > -1) {
            argumentsSettings.setStatusRequestClockActive(statusClockActive);
        }

        /*if (setWriteLogToFile != argumentsSettings.isLogFileEnabled()) {
            argumentsSettings.setLogFileEnabled(setWriteLogToFile);
            settings.setLogFileEnabled(setWriteLogToFile);
        } else if (writeLogToFile != argumentsSettings.isLogFileEnabled()) {
            argumentsSettings.setLogFileEnabled(writeLogToFile);
        }

        if (setWriteLogLevelAll != argumentsSettings.isLogFileLogLevelAll()) {
            argumentsSettings.setLogFileLogLevelAll(setWriteLogLevelAll);
            settings.setLogFileLogLevelAll(setWriteLogLevelAll);
        } else if (writeLogLevelAll != argumentsSettings.isLogFileLogLevelAll()) {
            argumentsSettings.setLogFileLogLevelAll(writeLogLevelAll);
        }*/

        if (setMaxLogFiles > -1 && setMaxLogFiles != argumentsSettings.getLogFileMaxFiles()) {
            if (setMaxLogFiles < 1) {
                logger.warn("Invalid value '" + setMaxLogFiles + "' for '--set-max-log-files', value must be a valid integer greater than 1!");
            } else {
                argumentsSettings.setLogFileMaxFiles(setMaxLogFiles);
                settings.setLogFileMaxFiles(argumentsSettings.getLogFileMaxFiles());
            }
        } else if (maxLogFiles > -1 && maxLogFiles != argumentsSettings.getLogFileMaxFiles()) {
            if (maxLogFiles < 1) {
                logger.warn("Invalid value '" + maxLogFiles + "' for '--max-log-files', value must be a valid integer greater than 1!");
            } else {
                argumentsSettings.setLogFileMaxFiles(maxLogFiles);
            }
        }

        if (setStackTraceDepth > -2 ) {
            argumentsSettings.setStackTraceDepth(setStackTraceDepth);
            settings.setStackTraceDepth(setStackTraceDepth);
        } else if (stackTraceDepth > -2) argumentsSettings.setStackTraceDepth(stackTraceDepth);

        /*if (setColorCodeLog != argumentsSettings.isLogColorCodingEnabled()) {
            argumentsSettings.setLogColorCodingEnabled(setColorCodeLog);
            settings.setLogColorCodingEnabled(setColorCodeLog);
        } else if (colorCodeLog != argumentsSettings.isLogColorCodingEnabled()) {
            argumentsSettings.setLogColorCodingEnabled(colorCodeLog);
        }*/

        if (getPaths) {
            logger.log("Paths:");
            logger.log(" -> Directories");
            logger.log("    App directory: '" + Constants.File_System.getAppDir() + "'");
            logger.log("    Temp directory: '" + Constants.File_System.getTmpDir() + "'");
            logger.log("    Data directory: '" + Constants.File_System.getDataDir() + "'");
            logger.log(" -> Files");
            logger.log("    Configuration file: '" + Constants.File_System.config + "'");
            logger.log("    Server configuration file: '" + Constants.File_System.server_config + "'");
            logger.log("    Log file: '" + Constants.File_System.logFile + "'");
            TimeManager.lock("logger");
            getInstance().app.emitShutdown();
        }

        logger.debug("Processing log files...");
        // process log files
        // checks if log files need to be moved or deleted
        processLogFiles(log_file, temp);

        tickingSystem = new TickingSystem();

        TimeManager.initTimeTracker("status", argumentsSettings.getStatusRequestClockPassive(), argumentsSettings.getStatusRequestClockPassive() * 2L);
        TimeManager.initTimeTracker("animations", 1000, System.currentTimeMillis() - 10000);

        // general startup information displayed in the console upon starting the program
        logger.info("Welcome back!");
        logger.info("Starting Program...");

        logger.info("System environment: " + Constants.System.NAME + " " + Constants.System.VERSION);

        // loads language bundle based on the specified locale
        // displays specified language and country in the console
        logger.info("Loading messages...");
        messages = ResourceBundle.getBundle("LEDSuite");
        logger.info("Language: " + messages.getLocale().getDisplayLanguage());
        logger.info("Country: " + messages.getLocale().getDisplayCountry());
        logger.info("Successfully loaded messages!");

        //System.out.println(messages.getString("greeting"));

        // check for window os
        // app does not normally work on windows, since windows doesn't natively support libadwaita
        if (Constants.System.NAME.toLowerCase().contains("windows")) {
            logger.warn("Our application does not have official Windows support. We do not fix any windows only bugs!");
            logger.warn("You will be ignored if you open an issue for a windows only bug! You can fork the repo though and fix the bug yourself!");
        }

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                try {
                    Networking.Communication.NetworkHandler.init(_ -> {
                    });
                } catch (Networking.NetworkException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously();

        new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                if (!Networking.Communication.NetworkHandler.isConnected() && !TimeManager.alternativeCall("status")) {
                    try {
                        Networking.Communication.sendYAMLDefaultHost(
                                YAMLMessage.defaultStatusRequest().build()
                        );
                    } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                             YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                        LEDSuite.logger.verbose("Auto status request attempt failed!");
                    }
                }
            }
        }.runTaskTimerAsynchronously(10000, 5000);
    }

    private void processLogFiles(File log_file, File temp) {
        // checks if log files are enabled
        if (!argumentsSettings.isLogFileEnabled()) {
            // reverting standard behaviour if log files are disabled
            if (temp != null) {
                logger.debug("Moving '" + temp.getName() + "' back to latest.log, since log file is disabled...");
                logger.debug(temp.renameTo(log_file) ? "Moving success!" : "Moving failed!");
            }
        } else {
            try {
                logger.debug("Constructing sorted file structure according to file names!");
                // loads all log files into memory
                File[] fileList = getFiles(log_file);
                // counts how many log files currently exist
                int fileCount = fileList.length;
                // calculates how many files need to be removed to bring the log file count withing the allowed threshold
                int difference = getDifference(fileCount);
                // if the previous function call completes without throwing an exception,
                // we know that log files need to be removed
                logger.debug("Log file count [" + fileCount + "] is NOT withing allowed threshold [" + settings.getLogFileMaxFiles() + "]!");
                logger.debug("Removing " + difference + " old log files...");
                // load the log files into a sorted map to sort them by name (creation date)
                TreeMap<Integer, TreeMap<Integer, File>> files = new TreeMap<>();
                // sort files based on their names
                for (File f : fileList) {
                    String fileName = f.getName();
                    String name = fileName.split("\\.")[0];
                    if (!name.equals("latest")) {
                        if (name.length() > 10) {
                            String index = name.substring(0, 11).replace("-", "").strip();
                            int parentIndex = Integer.parseInt(index);
                            String nestedIndex = name.substring(11);
                            int childIndex = Integer.parseInt(nestedIndex);
                            files.putIfAbsent(parentIndex, new TreeMap<>());
                            files.get(parentIndex).put(childIndex, f);
                        } else {
                            String index = name.replace("-", "").strip();
                            files.putIfAbsent(Integer.parseInt(index), new TreeMap<>());
                            files.get(Integer.parseInt(index)).put(0, f);
                        }
                    } else {
                        files.putIfAbsent(-1, new TreeMap<>());
                        files.get(-1).put(-1, f);
                    }
                }
                // removing as many files as needed to comply with the specified threshold
                for (int i = difference; i > 0; i--) {
                    logger.debug("Remaining log file groups: " + files.size());
                    SortedMap.Entry<Integer, TreeMap<Integer, File>> logFileGroup = files.pollLastEntry();
                    TreeMap<Integer, File> logFileGroupValue = logFileGroup.getValue();
                    String logFileGroupName = logFileGroupValue.get(0).getName();
                    logger.debug("Remaining log files in group '" + logFileGroupName + "': " + logFileGroupValue.size());
                    File f = new File(logFileGroupValue.pollLastEntry().getValue().getAbsolutePath());
                    logger.debug("Deleting log file '" + f.getName() + "'...");
                    logger.debug(f.delete() ? "Successfully deleted log file!" : "Failed to delete log file!");
                    if (!logFileGroupValue.isEmpty())
                        files.put(logFileGroup.getKey(), logFileGroup.getValue());
                    else logger.debug("Deleting log file group '" + logFileGroupName + "' since it's empty.");
                }
            // Handle errors and early returns
            } catch (IndexOutOfBoundsException e) {
                logger.error("Error while handling log files! Error message: " + e.getMessage());
                getInstance().app.emitShutdown();
            } catch (NullPointerException | IllegalArgumentException e) {
                logger.error("Error while handling log files! Error message: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.debug(e.getMessage());
            }
        }
    }

    private int getDifference(int fileCount) throws InterruptedException {
        int difference = fileCount - settings.getLogFileMaxFiles();
        // if the log file count is already within the allowed threshold, throw exception with a message to allow for fewer exit points
        if (difference <= 0) throw new InterruptedException("Log file count [" + fileCount + "] is withing allowed threshold [" + settings.getLogFileMaxFiles() + "]!");
        return difference;
    }

    private static File[] getFiles(File log_file) {
        String parent = log_file.getParent();
        if (parent == null || parent.isBlank())
            throw new NullPointerException("Couldn't get parent directory of '" + log_file.getName() + "'!");
        File parentFile = new File(parent);
        if (!parentFile.exists() || !parentFile.isDirectory())
            throw new IllegalArgumentException("Parent '" + parentFile.getName() + "' does not exist or is not a directory!");
        File[] fileList = parentFile.listFiles();
        if (fileList == null)
            throw new NullPointerException("Parent directory '" + parentFile.getName() + "' does not contain any files!");
        return fileList;
    }

    // formatting log file name
    // format yyyy-MM-dd-i (i = index, if more then one log file are created within one day)
    // this iterates through indexes in ascending order until it reaches one that is unused
    private static File getFile(File log_file) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder filePath = new StringBuilder(df.format(log_file.lastModified()));
        File temp = new File(Constants.File_System.getTmpDir() + filePath + ".log");
        while (temp.exists()) {
            if (filePath.length() > 10) {
                int in = Integer.parseInt(filePath.substring(11)) + 1;
                filePath.replace(11, filePath.length(), String.valueOf(in));
            } else {
                filePath.append("-1");
            }
            temp = new File(Constants.File_System.getTmpDir() + filePath + ".log");
        }
        return temp;
    }

    // activate function
    // this is triggered on libadwaita application activated
    public void activate() {
        // registering event listener for this class
        eventManager.registerEvents(this);
        // creating the main window of the application
        mainWindow = new Window(app);
        // registering event listener for the main window
        eventManager.registerEvents(mainWindow);
        // showing the main window on screen
        mainWindow.present();
        // trigger started to send a started message to console
        // calculating time elapsed during startup and displaying it in the console
        long timeElapsed = System.currentTimeMillis() - start;
        eventManager.fireEvent(new Events.Started("Successfully started program! (took " + timeElapsed / 1000 + "." + timeElapsed % 1000 + "s)"));
    }

    // display a start message with starting duration
    public static void started(String message) {
        logger.info(message);
        try {
            Networking.Communication.sendYAMLDefaultHost(YAMLMessage.defaultStatusRequest().build());
        } catch (ConfigurationException | YAMLSerializer.YAMLException e) {
            LEDSuite.logger.error("Failed to send / get available animations list from the server!");
            LEDSuite.logger.displayError(e);
        }
    }

    // exiting program with specified status code
    public void exit(int status) {
        // firing new shutdown event
        if (eventManager != null) eventManager.fireEvent(new Events.Shutdown("Shutdown"));
        if (logger != null) LEDSuite.logger.info("Saving...");
        // firing new save event to save user settings
        if (eventManager != null) eventManager.fireEvent(new Events.Save("Shutdown - Save"));
        if (logger != null) LEDSuite.logger.debug("Stopping ticking system!");
        if (tickingSystem != null) tickingSystem.stop();
        if (logger != null) LEDSuite.logger.info("Successfully saved!");
        if (logger != null) LEDSuite.logger.info("Shutting down...");
        if (logger != null) LEDSuite.logger.info("Goodbye!");
        // displaying status code in the console
        if (logger != null) LEDSuite.logger.info("Status code: " + status);
        // exiting program with the specified status code
        System.exit(status);
    }

    // triggering system-specific beep using java.awt.toolkit
    // commonly used when something fails or an error happens
    public static void sysBeep() {
        LEDSuite.logger.verbose("Triggered system beep!");
        getDefaultToolkit().beep();
    }

    // function used to load user settings from config files
    public void loadConfigsFromFile() {
        // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/config.yaml)
        // using Apache-Commons-Configuration2 and SnakeYaml
        try {
            // defining new YamlConfig object from apache-commons-configuration2 lib
            YAMLConfiguration yamlConfig = new YAMLConfiguration();

            // Loading the YAML file from disk using a file handler
            FileHandler fileHandler = new FileHandler(yamlConfig);
            fileHandler.load(Constants.File_System.config);

            // settings are loaded into the current instance of the settings class, so they can be used during runtime without any IO-Calls
            settings.load(yamlConfig);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing, an error is displayed in the console;
            // the program is halted to prevent any further unwanted behavior
            LEDSuite.logger.error("Failed to parse config.yaml!");
            LEDSuite.logger.warn("Application was halted!");
            LEDSuite.logger.warn("If this keeps happening please open an issue on GitHub!");
            LEDSuite.logger.warn("Please restart the application!");
            getInstance().exit(1);
            return;
        }

        try {
            // defining new YamlConfig object from apache-commons-configuration2 lib
            YAMLConfiguration yamlConfig = new YAMLConfiguration();

            // Load the YAML file from disk using file manager
            FileHandler fileHandler = new FileHandler(yamlConfig);
            fileHandler.load(Constants.File_System.server_config);

            // settings are loaded into the current instance of the settings class, so they can be used during runtime without any IO-Calls
            server_settings.load(yamlConfig);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing, an error is displayed in the console
            // the program is halted to prevent any further unwanted behavior
            LEDSuite.logger.error("Failed to parse server_config.yaml!");
            LEDSuite.logger.warn("Application was halted!");
            LEDSuite.logger.warn("If this keeps happening please open an issue on GitHub!");
            LEDSuite.logger.warn("Please restart the application!");
            getInstance().exit(1);
        }
    }

    public static LEDSuiteScheduler getScheduler() {
        return ledSuiteScheduler;
    }

    // listener function for reload event
    @EventHandler
    public void onReload(Events.Reload e) {
        // default console message response to a reload event
        logger.verbose("Fulfilling reload request: " + e.message());
        // reloading values that may've changed
        mainWindow.setResizable(settings.isWindowResizeable());
    }
    // listener function for startup event
    @EventHandler
    public void onStartup(Events.Startup e) {
        // default console message response to a startup event
        logger.verbose("Fulfilling startup request: " + e.message());
    }
    @EventHandler
    public void onStarted(Events.Started e) {
        started(e.message());
    }
    // listener function for save event
    @EventHandler
    public void onSave(Events.Save e) {
        // default console message response to a save event
        logger.verbose("Fulfilling save request: " + e.message());
    }
    // listener function for shutdown event
    @EventHandler
    public void onShutdown(Events.Shutdown e) {
        // default console message response to a shutdown event
        logger.verbose("Fulfilling shutdown request: " + e.message());
        //server = false;
        networkLogger.printEvents();
        logger.info("New log file was saved to: '" + Constants.File_System.logFile + "'");
    }
    @EventHandler
    public void onDataReceived(Events.DataIn e) {
        YAMLMessage yaml = e.yamlMessage();
        String id = "[" + yaml.getNetworkID() + "] ";
        logger.verbose(id + "-------------------- Internal Data Event ----------------------");
        logger.verbose(id + "Data stream direction: in");
        logger.verbose(id + "Network: Received data!");
        logger.verbose(id + "Data: " + yaml);

        switch (yaml.getPacketType()) {
            case reply -> {
                switch (yaml.getReplyType()) {
                    case status -> eventManager.fireEvent(new Events.Status(StatusUpdate.fromYAMLMessage(yaml)));
                    case menu -> LEDSuite.logger.fatal("Redundancy catcher caught a menu reply while not expecting it!");
                }

            }
            case error ->
                    eventManager.fireEvent(
                            new Events.Error(
                                    ServerError.fromYAMLMessage(yaml)
                            )
            );
        }
        logger.debug(id + "---------------------------------------------------------------");
    }
    @EventHandler
    public void onDataSend(Events.DataOut e) {
        YAMLConfiguration yaml = e.yaml();
        String id;
        try {
            id = "[" + yaml.getProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID) + "] ";
            logger.verbose(id + "-------------------- Internal Data Event ----------------------");
        } catch (NoSuchElementException ex) {
            id = "[failed to get id] ";
            logger.verbose(id + "-------------------- Internal Data Event ----------------------");
            logger.error(id + "Failed to get internal network event id from YAML!");
            logger.error(id + "Error message: " + ex.getMessage());
        }
        logger.debug(id + "Data stream direction: out");
        try {
            logger.verbose(id + "Data: " + YAMLSerializer.deserializeYAML(e.yaml()));
        } catch (YAMLSerializer.YAMLException ex) {
            logger.warn(id + "Data: failed to deserialize yaml data");
            logger.warn(id + "Error message: " + ex.getMessage());
        }
        logger.verbose(id + "---------------------------------------------------------------");
    }
    @EventHandler
    public void onStatus(Events.Status e) {
        StatusUpdate status = e.statusUpdate();
        TimeManager.ping("status");
        String id = "[" + status.getNetworkEventID() + "] ";
        logger.debug(id + "Received status update from server!");
        logger.debug(id + "Status: " + status);
    }
    @EventHandler
    public void onSend(Events.DataOut e) {
        YAMLConfiguration yaml = e.yaml();
        String id;
        try {
            id = "[" + yaml.getProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID) + "] ";
            logger.verbose(id + "-------------------- Internal Data Event ----------------------");
        } catch (NoSuchElementException ex) {
            id = "[failed to get id] ";
            logger.verbose(id + "-------------------- Internal Data Event ----------------------");
            logger.error(id + "Failed to get internal network event id from YAML!");
            logger.error(id + "Error message: " + ex.getMessage());
        }

    }
    @EventHandler
    public void onError(Events.Error e) {
        ServerError error = e.serverError();
        String id = "[" + error.getNetworkEventID() + "] ";
        logger.debug(id + "Received error from server!");
        logger.debug("id" + "Error: " + error);
        sysBeep();
        mainWindow.toastOverlay.addToast(
                Toast.builder()
                        .setTitle(error.humanReadable())
                        .setTimeout(Adw.DURATION_INFINITE)
                        .build()
        );
    }
}
