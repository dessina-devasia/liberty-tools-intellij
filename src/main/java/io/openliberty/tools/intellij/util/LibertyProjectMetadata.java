/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.tools.intellij.util;

import java.util.List;

/**
 * Assisted by IBM Bob
 *
 * Common contract for build-tool metadata extracted from a project's build file.
 * Implemented by {@link MavenProjectMetadata} and {@link GradleProjectMetadata}.
 */
public interface LibertyProjectMetadata {

    /** Returns the project name as declared in the build file (artifactId / rootProject.name). */
    String getProjectName();

    /**
     * Returns the name of the parent/aggregator project, or {@code null} when this
     * project is a standalone root.
     */
    String getParentProjectName();

    /**
     * Returns the list of child module directory names declared by this aggregator,
     * or an empty list when this project is not an aggregator.
     */
    List<String> getSubprojects();

    /** Returns {@code true} when the Liberty plugin is configured in this build file. */
    boolean isLibertyPluginConfigured();

    /**
     * Returns {@code true} when this project is an aggregator (parent pom / Gradle root
     * with includes) that declares child modules.
     */
    boolean isAggregator();

    /** Returns the absolute path to the build file (pom.xml or build.gradle). */
    String getBuildFilePath();

    /**
     * Returns {@code true} when Liberty dev mode has been explicitly disabled for
     * this module (Maven {@code <skip>true</skip>} configuration). Always {@code false}
     * for Gradle projects (no equivalent skip mechanism in LGP).
     */
    boolean isModuleDisabled();

    /**
     * Returns the list of intra-project dependency names declared in the build file
     * (Maven {@code <dependency>} artifactIds / Gradle {@code project(':name')} refs).
     */
    List<String> getProjectDependencies();
}
