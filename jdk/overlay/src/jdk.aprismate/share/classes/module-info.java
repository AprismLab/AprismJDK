/*
 * Copyright (c) 2026, AprismLab. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. AprismLab designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code (GPLv2+CE, tracking
 * upstream OpenJDK licensing practice).
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 */

/**
 * AprismJDK stable API module: opened JVM interfaces for the Aprism
 * loader ecosystem and developer-power tooling.
 *
 * <p>Additive-only by contract: never removes or narrows upstream JDK
 * APIs; every capability degrades gracefully on stock OpenJDK via the
 * capability descriptor ({@link jdk.aprismate.VmInfo}).
 */
module jdk.aprismate {

    requires transitive java.instrument;
    requires java.management;

    exports jdk.aprismate;
    exports jdk.aprismate.agent;
    exports jdk.aprismate.async;
    exports jdk.aprismate.config;
    exports jdk.aprismate.concurrent;
    exports jdk.aprismate.event;
    exports jdk.aprismate.event.lifecycle;
    exports jdk.aprismate.ffi;
    exports jdk.aprismate.gc;
    exports jdk.aprismate.invoke;
    exports jdk.aprismate.jit;
    exports jdk.aprismate.memory;
    exports jdk.aprismate.mod;
    exports jdk.aprismate.network;
    exports jdk.aprismate.profiler;
    exports jdk.aprismate.reflection;
    exports jdk.aprismate.resource;
    exports jdk.aprismate.runtime;
    exports jdk.aprismate.serialization;
    exports jdk.aprismate.serialization.impl;
    exports jdk.aprismate.util;

    // aprism.agent.api.metrics: internal support surface consumed by the
    // embedded AprismateAgent running on the application class loader;
    // implementations cross the module boundary, so it must be exported
    // even though it is not public API.
    exports aprism.agent.api.metrics;
}
