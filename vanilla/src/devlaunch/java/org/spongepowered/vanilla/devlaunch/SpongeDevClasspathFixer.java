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

import net.minecraftforge.bootstrap.api.BootstrapClasspathModifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpongeDevClasspathFixer implements BootstrapClasspathModifier {
    private static final boolean DEBUG = Boolean.getBoolean("bsl.debug");

    @Override
    public String name() {
        return "sponge-dev";
    }

    /**
     * IntelliJ only builds source sets that are explicitly on the classpath.
     * So we declare everything into the initial classpath then we sort things and put them back in the right classloaders.
     * Since different IDEs might want to put things in different locations, we sadly have to identify the resources by their filenames.
     */
    @Override
    public boolean process(final List<Path[]> classpath) {
        final Set<String> gameManagedNames = Set.of(System.getProperty("sponge.dev.gameManaged").split(";"));
        final Set<String> gameShadedNames = Set.of(System.getProperty("sponge.dev.gameShaded").split(";"));

        final List<Path> transformersSourceSets = new ArrayList<>();
        final List<Path> appLaunchSourceSets = new ArrayList<>();
        final List<Path> gameSourceSets = new ArrayList<>();

        final List<Path> gameManagedResources = new ArrayList<>();
        final List<Path> gameShadedResources = new ArrayList<>();
        final List<Path> testResources = new ArrayList<>();

        final AtomicBoolean hasAPISourceSet = new AtomicBoolean(false);

        final boolean modified = classpath.removeIf((paths) -> {
            if (paths.length != 1) { // empty or already merged by another service
                return false;
            }

            final Path path = paths[0];
            final SourceSet sourceSet = SourceSet.identify(path);
            if (sourceSet != null) {
                if (DEBUG) {
                    System.out.println("SourceSet (" + sourceSet + "): " + path);
                }

                switch (sourceSet.project()) {
                    case "testplugins":
                        testResources.add(path);
                        break;
                    case "modlauncher-transformers":
                        transformersSourceSets.add(path);
                        break;
                    default:
                        switch (sourceSet.name()) {
                            case "devlaunch", "ap":
                                // ignore
                                break;
                            case "applaunch":
                                appLaunchSourceSets.add(path);
                                break;
                            case "main":
                                if (sourceSet.project().equals("SpongeAPI")) {
                                    hasAPISourceSet.set(true);
                                }
                                // no break, on purpose
                            default:
                                gameSourceSets.add(path);
                                break;
                        }
                        break;
                }
                return true;
            }

            final String fileName = path.getFileName().toString();

            if (gameManagedNames.contains(fileName)) {
                if (DEBUG) {
                    System.out.println("Game (managed): " + path);
                }
                gameManagedResources.add(path);
                return true;
            }

            if (gameShadedNames.contains(fileName)) {
                if (DEBUG) {
                    System.out.println("Game (shaded): " + path);
                }
                gameShadedResources.add(path);
                return true;
            }

            if (fileName.equals("testplugins.jar")) {
                if (DEBUG) {
                    System.out.println("TestPlugins jar: " + path);
                }
                testResources.add(path);
                return true;
            }

            if (DEBUG) {
                System.out.println("Bootstrap: " + path);
            }
            return false;
        });

        if (!modified) {
            return false;
        }

        if (!transformersSourceSets.isEmpty()) {
            classpath.add(transformersSourceSets.toArray(Path[]::new));
        }

        classpath.add(appLaunchSourceSets.toArray(Path[]::new));

        gameShadedResources.addAll(gameSourceSets);

        if (hasAPISourceSet.get()) {
            gameShadedResources.removeIf((path) -> {
                if (Files.isRegularFile(path)) {
                    final String fileName = path.getFileName().toString();
                    if (fileName.startsWith("spongeapi") && fileName.endsWith(".jar")) {
                        if (DEBUG) {
                            System.out.println("Removing spongeapi jar in favor of sourceset: " + path);
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        final StringBuilder gameResourcesEnvBuilder = new StringBuilder();
        for (final Path resource : gameManagedResources) {
            gameResourcesEnvBuilder.append(resource).append(';');
        }
        for (final Path resource : gameShadedResources) {
            gameResourcesEnvBuilder.append(resource).append('&');
        }
        gameResourcesEnvBuilder.setCharAt(gameResourcesEnvBuilder.length() - 1, ';');
        for (final Path resource : testResources) {
            gameResourcesEnvBuilder.append(resource).append('&');
        }
        gameResourcesEnvBuilder.setLength(gameResourcesEnvBuilder.length() - 1);
        final String gameResourcesEnv = gameResourcesEnvBuilder.toString();

        if (DEBUG) {
            System.out.println("Game resources env: " + gameResourcesEnv);
        }
        System.setProperty("sponge.gameResources", gameResourcesEnv);
        return true;
    }


}
