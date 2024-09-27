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

import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.nio.file.Path;
import java.util.jar.Manifest;

/** Adapted from {@link org.spongepowered.vanilla.applaunch.plugin.resource.SecureJarPluginResource} */
public class FabricPluginResource implements JVMPluginResource {
    private final String locator;
    private final SecureJar jar;

    public FabricPluginResource(final String locator, final Path[] paths) {
        this.locator = locator;
        this.jar = SecureJar.from(paths);
    }

    @Override
    public String locator() {
        return this.locator;
    }

    @Override
    public Path path() {
        return this.jar.getPrimaryPath();
    }

    @Override
    public Manifest manifest() {
        return this.jar.moduleDataProvider().getManifest();
    }

    @Override
    public Path resourcesRoot() {
        return this.jar.getRootPath();
    }
}
