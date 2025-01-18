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
package dk.nelind.loofah.launch.plugin;

import com.google.inject.Injector;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.spongepowered.common.inject.plugin.PluginGuice;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.InvalidPluginException;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLoader;

import java.lang.invoke.MethodHandles;

/**
 * Adapted from {@link org.spongepowered.vanilla.launch.plugin.JavaPluginLoader}
 */
public class JavaPluginLoader implements PluginLoader<FabricJavaPluginContainer> {
    private static final ArtifactVersion VERSION = new DefaultArtifactVersion("1.0");

    private static final MethodHandles.Lookup LOOFAH_LOOKUP = MethodHandles.lookup();

    @Override
    public ArtifactVersion version() {
        return JavaPluginLoader.VERSION;
    }

    @Override
    public FabricJavaPluginContainer loadPlugin(
        final Environment environment,
        final PluginCandidate candidate,
        final ClassLoader targetClassLoader
    ) throws InvalidPluginException {
        final FabricJavaPluginContainer container = new FabricJavaPluginContainer(candidate);
        try {
            final String mainClass = container.metadata().entrypoint();
            final Class<?> pluginClass = Class.forName(mainClass, true, targetClassLoader);

            final Injector pluginInjector = PluginGuice.create(
                container,
                pluginClass,
                Launch.instance().lifecycle().platformInjector()
            );
            final Object plugin = pluginInjector.getInstance(pluginClass);
            container.setInjector(pluginInjector);
            container.initializeInstance(plugin);
            container.initializeLookup(MethodHandles.privateLookupIn(pluginClass, LOOFAH_LOOKUP));
            return container;
        } catch (final Exception ex) {
            throw new InvalidPluginException("An error occurred creating an instance of plugin '" + container.metadata().id() + "'!", ex);
        }
    }
}