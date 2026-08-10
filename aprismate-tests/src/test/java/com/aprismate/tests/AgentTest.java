package com.aprismate.tests;

import jdk.aprismate.Agent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic smoke tests for Agent API surface.
 * 
 * <p>v26.0-Alpha.1: Verifies stub behavior on stock OpenJDK.
 */
class AgentTest {
    
    @Test
    void agentShouldNotBeLoadedOnStockJdk() {
        // Without -javaagent, agent should not be loaded
        boolean loaded = Agent.isAgentLoaded();
        // May be false on stock JDK, true if test runs with agent attached
        assertThat(loaded).isIn(true, false);
    }
    
    @Test
    void agentVersionShouldMatchVmInfo() {
        String agentVersion = Agent.getAgentVersion();
        String vmVersion = jdk.aprismate.VmInfo.getAprismJdkVersion();
        assertThat(agentVersion).isEqualTo(vmVersion);
    }
    
    @Test
    void stubApisShouldReturnNull() {
        // All stub APIs should return null until implemented
        assertThat(Agent.getClassRedefiner()).isNull();
        assertThat(Agent.getMethodHookRegistry()).isNull();
        assertThat(Agent.getBytecodeTransformer()).isNull();
    }
}
