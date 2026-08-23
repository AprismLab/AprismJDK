package jdk.aprismate.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Agent logging facility with file-based output and fail-safe error handling.
 * Logs are written to a dedicated file to avoid interfering with application output.
 * 
 * <p>All logging operations are fail-safe: errors during logging are silently
 * suppressed to prevent agent failures from crashing the JVM.
 * 
 * <p>Usage:
 * <pre>{@code
 * AgentLogger logger = AgentLogger.getInstance();
 * logger.info("Agent initialized");
 * logger.error("Failed to transform class", exception);
 * }</pre>
 * 
 * @since 26.1-Alpha.1
 */
public final class AgentLogger {
    private static volatile AgentLogger INSTANCE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final Path logFile;
    private volatile boolean enabled = true;
    
    private AgentLogger() {
        String logPath = System.getProperty("aprismate.agent.logFile", "aprismate-agent.log");
        this.logFile = Paths.get(logPath);
        initializeLogFile();
    }
    
    /**
     * Returns the singleton logger instance.
     */
    public static AgentLogger getInstance() {
        if (INSTANCE == null) {
            synchronized (AgentLogger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AgentLogger();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Resets the logger instance (for testing purposes only).
     */
    static synchronized void reset() {
        INSTANCE = null;
    }
    
    private void initializeLogFile() {
        try {
            // Ensure parent directory exists
            Path parent = logFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
            log("INFO", "AgentLogger initialized: " + logFile.toAbsolutePath());
        } catch (Exception e) {
            // Fail-safe: suppress initialization errors
            enabled = false;
        }
    }
    
    /**
     * Logs an informational message.
     */
    public void info(String message) {
        log("INFO", message);
    }
    
    /**
     * Logs a warning message.
     */
    public void warn(String message) {
        log("WARN", message);
    }
    
    /**
     * Logs a warning message with exception details.
     */
    public void warn(String message, Throwable throwable) {
        log("WARN", message + "\n" + formatThrowable(throwable));
    }
    
    /**
     * Logs an error message.
     */
    public void error(String message) {
        log("ERROR", message);
    }
    
    /**
     * Logs an error message with exception details.
     */
    public void error(String message, Throwable throwable) {
        log("ERROR", message + "\n" + formatThrowable(throwable));
    }
    
    /**
     * Logs a debug message (only if debug mode is enabled).
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            log("DEBUG", message);
        }
    }
    
    /**
     * Checks if debug mode is enabled.
     */
    public boolean isDebugEnabled() {
        return Boolean.getBoolean("aprismate.agent.debug");
    }
    
    private void log(String level, String message) {
        if (!enabled) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            String threadName = Thread.currentThread().getName();
            String logLine = String.format("[%s] [%s] [%s] %s%n", timestamp, level, threadName, message);
            
            Files.writeString(logFile, logLine, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND);
        } catch (Exception e) {
            // Fail-safe: suppress logging errors
        }
    }
    
    private String formatThrowable(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage());
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\n\tat ").append(element);
        }
        
        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ").append(formatThrowable(cause));
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the log file path.
     */
    public Path getLogFile() {
        return logFile;
    }
    
    /**
     * Disables logging (for testing purposes).
     */
    void disableLogging() {
        enabled = false;
    }
    
    /**
     * Enables logging (for testing purposes).
     */
    void enableLogging() {
        enabled = true;
    }
}
