/*
 * This file is part of Loofah, licensed under the MIT License (MIT).
 *
 * Copyright (c) Nelind <https://www.nelind.dk>
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
package dk.nelind.loofah.applaunch.plugin.resource;

import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/** Adapted from {@link org.spongepowered.vanilla.applaunch.plugin.resource.SecureJarPluginResource} */
public class FabricPluginResource implements JVMPluginResource {
    private final String locator;
    private final Manifest manifest;
    private final Path primaryPath;
    private final Path resourcesRoot;

    // TODO(loofah): This whole thing is a mess. Implement to more properly fit SV behaviour.
    //  Maybe bring in Secure Jar Handler https://github.com/McModLauncher/securejarhandler and just do as SV does?
    public FabricPluginResource(final String locator, final Path[] paths) {
        this.locator = locator;
        this.primaryPath = paths[paths.length - 1];
        this.resourcesRoot = Path.of("/");

        if (Files.isDirectory(this.primaryPath)) {
            var manifest = this.primaryPath.resolve(JarFile.MANIFEST_NAME);
            if (Files.exists(manifest)) {
                try (var inputStream = Files.newInputStream(manifest)) {
                    this.manifest = new Manifest(inputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // if the directory has no manifest just use the default empty manifest
                this.manifest = new Manifest();
            }
        } else {
            try (var jar = new JarFile(primaryPath.toFile())) {
                this.manifest = jar.getManifest();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Manifest manifest() {
        return this.manifest;
    }

    @Override
    public Path resourcesRoot() {
        return this.resourcesRoot;
    }

    @Override
    public String locator() {
        return this.locator;
    }

    @Override
    public Path path() {
        return this.primaryPath;
    }
}
