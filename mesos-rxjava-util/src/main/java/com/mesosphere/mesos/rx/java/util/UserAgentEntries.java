/*
 *    Copyright (C) 2015 Mesosphere, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mesosphere.mesos.rx.java.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

/**
 * A set of utility methods that can be used to easily create {@link UserAgentEntry} objects.
 */
public final class UserAgentEntries {

    private UserAgentEntries() {}

    /**
     * Convenience method used to construct a {@link UserAgentEntry} for passing to
     * {@link UserAgent#UserAgent(Function[])}.
     *
     * @param name    The {@code name} for the constructed {@code UserAgentEntry}
     * @param version The {@code version} for the constructed {@code UserAgentEntry}
     * @return A function that will produce a new {@code UserAgentEntry} with the specified {@code name}
     * and {@code version}
     */
    @NotNull
    public static Function<Class<?>, UserAgentEntry> literal(@NotNull final String name, @NotNull final String version) {
        return (Class<?> c) -> new UserAgentEntry(name, version);
    }

    /**
     * Convenience method used to construct a {@link UserAgentEntry} for passing to
     * {@link UserAgent#UserAgent(Function[])}.
     *
     * @param name    The {@code name} for the constructed {@code UserAgentEntry}
     * @param version The {@code version} for the constructed {@code UserAgentEntry}
     * @param details The {@code details} for the constructed {@code UserAgentEntry}
     * @return A function that will produce a new {@code UserAgentEntry} with the specified {@code name},
     * {@code version} and {@code details}
     */
    @NotNull
    public static Function<Class<?>, UserAgentEntry> literal(
        @NotNull final String name,
        @NotNull final String version,
        @Nullable final String details
    ) {
        return (Class<?> c) -> new UserAgentEntry(name, version, details);
    }

    /**
     * Creates a {@link Function} capable of creating a {@link UserAgentEntry} for the Gradle artifact designated by
     * {@code artifactId}.  The UserAgentEntry returned will be of the form "artifactId / Implementation-Version", where
     * {@code Implementation-Version} is resolved from the artifact.properties generated by a Gradle build.
     * <p>
     * The process of locating the specified artifact on the classpath will be evaluated relative to the class provided
     * when the resulting function is invoked (Typically when calling {@link UserAgent#UserAgent(Function[])}.
     *
     * @param artifactId Artifact Id used to resolve the Gradle artifact on the classpath.
     * @return A function that will attempt to resolve the {@code Implementation-Version} of a Gradle artifact.
     */
    @NotNull
    public static Function<Class<?>, UserAgentEntry> userAgentEntryForGradleArtifact(@NotNull final String artifactId) {
        return (Class<?> c) -> {
            final Properties props = loadProperties(c, String.format("/META-INF/%s.properties", artifactId));
            return new UserAgentEntry(props.getProperty("artifactId", artifactId), props.getProperty("Implementation-Version", "unknown-version"));
        };
    }

    /**
     * Creates a {@link Function} capable of creating a {@link UserAgentEntry} for the Maven artifact designated by
     * {@code artifactId}.  The UserAgentEntry returned will be of the form "artifactId / version", where
     * {@code version} is resolved from the pom.properties generated by a Maven build.
     * <p>
     * The process of locating the specified artifact on the classpath will be evaluated relative to the class provided
     * when the resulting function is invoked (Typically when calling {@link UserAgent#UserAgent(Function[])}.
     *
     * @param groupId    Group Id used to resolve the Maven artifact on the classpath.
     * @param artifactId Artifact Id used to resolve the Maven artifact on the classpath.
     * @return A function that will attempt to resolve the {@code Implementation-Version} of a Maven artifact.
     */
    @NotNull
    public static Function<Class<?>, UserAgentEntry> userAgentEntryForMavenArtifact(@NotNull final String groupId, @NotNull final String artifactId) {
        return (Class<?> c) -> {
            final Properties props = loadProperties(c, String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
            return new UserAgentEntry(props.getProperty("artifactId", artifactId), props.getProperty("version", "unknown-version"));
        };
    }

    @NotNull
    private static Properties loadProperties(@NotNull final Class c, @NotNull final String resourcePath) {
        final Properties props = new Properties();
        try {
            final InputStream resourceAsStream = c.getResourceAsStream(resourcePath);
            if (resourceAsStream != null) {
                props.load(resourceAsStream);
                resourceAsStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load classpath resource " + resourcePath, e);
        }
        return props;
    }
}
