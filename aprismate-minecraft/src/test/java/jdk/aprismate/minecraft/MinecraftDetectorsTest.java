package jdk.aprismate.minecraft;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MinecraftDetectorsTest {

    @Test
    void sharedInstanceIsSingleton() {
        assertThat(MinecraftDetectors.getDetector())
                .isSameAs(MinecraftDetectors.getDetector());
    }

    @Test
    void detectNeverThrows() {
        MinecraftRuntime r = MinecraftDetectors.detect();
        assertThat(r).isNotNull();
    }

    @Test
    void notDetectedFactoryIsConsistent() {
        MinecraftRuntime r = MinecraftRuntime.notDetected();
        assertThat(r.detected()).isFalse();
        assertThat(r.gameVersion()).isEmpty();
        assertThat(r.mainClass()).isEmpty();
    }
}
