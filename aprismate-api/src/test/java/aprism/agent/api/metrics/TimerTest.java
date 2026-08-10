package aprism.agent.api.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TimerTest {
    
    @Test
    void testTimerStart() {
        Timer.Sample sample = Timer.start();
        assertThat(sample).isNotNull();
    }
    
    @Test
    void testTimerMeasuresDuration() throws InterruptedException {
        Timer.Sample sample = Timer.start();
        Thread.sleep(10); // Sleep for at least 10ms
        long nanos = sample.stop();
        
        assertThat(nanos).isGreaterThan(0);
        // Should be at least 10ms (10 million nanos)
        assertThat(nanos).isGreaterThan(10_000_000);
    }
    
    @Test
    void testTimerCanBeStoppedMultipleTimes() throws InterruptedException {
        Timer.Sample sample = Timer.start();
        Thread.sleep(5);
        long nanos1 = sample.stop();
        
        Thread.sleep(5);
        long nanos2 = sample.stop();
        
        // Second stop should measure longer duration
        assertThat(nanos2).isGreaterThan(nanos1);
    }
    
    @Test
    void testTimerSampleConstructor() {
        long startNanos = System.nanoTime();
        Timer.Sample sample = new Timer.Sample(startNanos);
        
        long elapsed = sample.stop();
        assertThat(elapsed).isGreaterThanOrEqualTo(0);
    }
}
