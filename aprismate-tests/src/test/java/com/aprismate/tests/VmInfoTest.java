package com.aprismate.tests;

import jdk.aprismate.Agent;
import jdk.aprismate.VmInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic smoke tests for AprismJDK API surface.
 * 
 * <p>v26.0-Alpha.1: Verifies that stub APIs are accessible and return
 * expected default values on stock OpenJDK.
 */
class VmInfoTest {
    
    @Test
    void vmInfoShouldBeAccessible() {
        // On stock OpenJDK, these should return false/null
        assertThat(VmInfo.isAprismJdk()).isFalse();
        assertThat(VmInfo.getAprismJdkVersion()).isNull();
    }
    
    @Test
    void openjdkVersionShouldBeDetected() {
        int version = VmInfo.getOpenJdkVersion();
        assertThat(version).isGreaterThanOrEqualTo(17);
    }
    
    @Test
    void vendorShouldBeReported() {
        String vendor = VmInfo.getVendor();
        assertThat(vendor).isNotNull();
        assertThat(vendor).isNotEmpty();
    }
    
    @Test
    void capabilitiesShouldReturnFalseOnStockJdk() {
        // All capabilities should return false until implemented
        assertThat(VmInfo.hasClassRedefinerPlus()).isFalse();
        assertThat(VmInfo.hasMethodHookRegistryPlus()).isFalse();
        assertThat(VmInfo.hasBytecodeTransformer()).isFalse();
        assertThat(VmInfo.hasVmIntrospection()).isFalse();
    }
}
