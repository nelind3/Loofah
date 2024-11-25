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

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.builtin.jvm.locator.JVMPluginResourceLocatorService;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FabricModPluginResourceLocatorService implements JVMPluginResourceLocatorService {
    @Override
    public String name() {
        return "fabric_mods";
    }

    @Override
    public Set<JVMPluginResource> locatePluginResources(Environment environment) {
        environment.logger().info("Locating '{}' resources...", this.name());
        final Set<JVMPluginResource> resources = new HashSet<>();

        final Collection<ModContainer> modContainers = FabricLoader.getInstance().getAllMods();
        for (ModContainer modContainer : modContainers) {
            final Path[] paths = modContainer.getRootPaths().toArray(new Path[0]);
            // Use mod specific PluginResource to avoid putting mod classes and resources on the Knot class path.
            // That's the loaders job. We will cause issues if we try to do it too.
            resources.add(new FabricModPluginResource(this.name(), paths));
        }

        environment.logger().info("Located [{}] resource(s) for '{}'...", resources.size(), this.name());
        return resources;
    }
}
