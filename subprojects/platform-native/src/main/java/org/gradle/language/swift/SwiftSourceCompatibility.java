/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.swift;

import org.gradle.api.Incubating;
import org.gradle.util.VersionNumber;

/**
 * Swift source compatibility.
 *
 * @since 4.6
 */
@Incubating
public enum SwiftSourceCompatibility {
    SWIFT3(3), SWIFT4(4);

    private final int version;

    SwiftSourceCompatibility(int version) {
        this.version = version;
    }

    /**
     * Returns the Swift language version in numerical value
     */
    public int getVersion() {
        return version;
    }

    public static SwiftSourceCompatibility of(VersionNumber swiftCompilerVersion) {
        if (swiftCompilerVersion.getMajor() == 3) {
            return SWIFT3;
        } else if (swiftCompilerVersion.getMajor() == 4) {
            return SWIFT4;
        } else {
            throw new IllegalArgumentException(String.format("Swift language version is unknown for the specified swift compiler version (%s)", swiftCompilerVersion.toString()));
        }
    }
}