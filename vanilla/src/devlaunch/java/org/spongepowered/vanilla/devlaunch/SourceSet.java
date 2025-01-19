/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.devlaunch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record SourceSet(Path project, String name, String format) {

    @Override
    public String toString() {
        return this.project + ":" + this.name + ":" + this.format;
    }

    /**
     * Identify source sets from output dirs in the following formats:
     *
     * <p>Gradle:
     * <p>{@code {project}/build/classes/java/{name}/}
     * <p>{@code {project}/build/resources/{name}/}
     * <p>{@code {project}/build/generated/{name}/}
     *
     * <p>IntelliJ:
     * <p>{@code {project}/out/{name}/classes/}
     * <p>{@code {project}/out/{name}/resources/}
     *
     * <p>Eclipse:
     * <p>{@code {project}/bin/{name}/}
     */
    public static SourceSet identify(Path path) {
        if (!Files.isDirectory(path)) {
            return null;
        }

        // from right to left
        final List<String> names = new ArrayList<>();
        final List<Path> paths = new ArrayList<>();
        while (path != null) {
            names.add(path.getFileName().toString());
            paths.add(path);
            if (names.size() >= 5) {
                break;
            }
            path = path.getParent();
        }

        if (names.size() >= 4 && (names.get(0).equals("classes") || names.get(0).equals("resources"))) {
            final String name = names.get(1);
            return new SourceSet(paths.get(3), name.equals("production") ? "main" : name, "IntelliJ");
        }

        if (names.size() >= 4 && (names.get(1).equals("resources") || names.get(1).equals("generated"))) {
            return new SourceSet(paths.get(3), names.get(0), "Gradle");
        }

        if (names.size() >= 5 && names.get(2).equals("classes")) {
            return new SourceSet(paths.get(4), names.get(0), "Gradle");
        }

        if (names.size() >= 3 && names.get(1).equals("bin")) {
            return new SourceSet(paths.get(2), names.get(0), "Eclipse");
        }

        return new SourceSet(paths.get(0), "?", "Unknown");
    }
}
