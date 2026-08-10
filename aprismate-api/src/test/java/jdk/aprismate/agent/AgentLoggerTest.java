package jdk.aprismate.agent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentLoggerTest {
    
    @TempDir
    Path tempDir;
    
    private String originalLogFile;
    
    @BeforeEach
    void setUp() {
        originalLogFile = System.getProperty("aprismate.agent.logFile");
        Path logFile = tempDir.resolve("test-agent.log");
        System.setProperty("aprismate.agent.logFile", logFile.toString());
        AgentLogger.reset(); // Reset singleton to pick up new property
    }
    
    @AfterEach
    void tearDown() {
        AgentLogger.reset(); // Reset after test
        if (originalLogFile != null) {
            System.setProperty("aprismate.agent.logFile", originalLogFile);
        } else {
            System.clearProperty("aprismate.agent.logFile");
        }
    }
    
    @Test
    void testInfoLogging() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        logger.info("Test info message");
        
        Path logFile = logger.getLogFile();
        assertTrue(Files.exists(logFile), "Log file should exist");
        
        List<String> lines = Files.readAllLines(logFile);
        assertTrue(lines.stream().anyMatch(line -> line.contains("INFO") && line.contains("Test info message")),
            "Log should contain info message");
    }
    
    @Test
    void testWarnLogging() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        logger.warn("Test warning message");
        
        List<String> lines = Files.readAllLines(logger.getLogFile());
        assertTrue(lines.stream().anyMatch(line -> line.contains("WARN") && line.contains("Test warning message")),
            "Log should contain warning message");
    }
    
    @Test
    void testErrorLogging() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        logger.error("Test error message");
        
        List<String> lines = Files.readAllLines(logger.getLogFile());
        assertTrue(lines.stream().anyMatch(line -> line.contains("ERROR") && line.contains("Test error message")),
            "Log should contain error message");
    }
    
    @Test
    void testErrorWithException() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        Exception testException = new RuntimeException("Test exception");
        logger.error("Error with exception", testException);
        
        List<String> lines = Files.readAllLines(logger.getLogFile());
        String logContent = String.join("\n", lines);
        
        assertTrue(logContent.contains("ERROR"), "Log should contain ERROR level");
        assertTrue(logContent.contains("Error with exception"), "Log should contain error message");
        assertTrue(logContent.contains("RuntimeException"), "Log should contain exception type");
        assertTrue(logContent.contains("Test exception"), "Log should contain exception message");
    }
    
    @Test
    void testDebugLoggingDisabledByDefault() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        assertFalse(logger.isDebugEnabled(), "Debug should be disabled by default");
        
        logger.debug("Debug message");
        
        List<String> lines = Files.readAllLines(logger.getLogFile());
        assertFalse(lines.stream().anyMatch(line -> line.contains("DEBUG") && line.contains("Debug message")),
            "Log should not contain debug message when debug is disabled");
    }
    
    @Test
    void testLogFileCreation() {
        AgentLogger logger = AgentLogger.getInstance();
        Path logFile = logger.getLogFile();
        
        assertNotNull(logFile, "Log file path should not be null");
        assertTrue(Files.exists(logFile), "Log file should be created");
    }
    
    @Test
    void testMultipleLogEntries() throws IOException {
        AgentLogger logger = AgentLogger.getInstance();
        
        logger.info("Message 1");
        logger.warn("Message 2");
        logger.error("Message 3");
        
        List<String> lines = Files.readAllLines(logger.getLogFile());
        
        long infoCount = lines.stream().filter(line -> line.contains("INFO") && line.contains("Message 1")).count();
        long warnCount = lines.stream().filter(line -> line.contains("WARN") && line.contains("Message 2")).count();
        long errorCount = lines.stream().filter(line -> line.contains("ERROR") && line.contains("Message 3")).count();
        
        assertEquals(1, infoCount, "Should have one info message");
        assertEquals(1, warnCount, "Should have one warn message");
        assertEquals(1, errorCount, "Should have one error message");
    }
}
