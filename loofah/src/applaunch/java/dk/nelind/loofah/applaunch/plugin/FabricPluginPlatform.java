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
package dk.nelind.loofah.applaunch.plugin;

import dk.nelind.loofah.applaunch.plugin.resource.FabricPluginResource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.plugin.*;
import org.spongepowered.plugin.blackboard.Keys;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.JVMKeys;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Adapted from {@link org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform} and
 * {@link org.spongepowered.forge.applaunch.plugin.ForgePluginPlatform}
 */
public class FabricPluginPlatform implements PluginPlatform {
    private static volatile boolean bootstrapped;

    private final StandardEnvironment standardEnvironment;
    private final Map<String, PluginResourceLocatorService<?>> locatorServices;
    private final Map<String, PluginLanguageService> languageServices;

    private final Map<String, Set<? extends PluginResource>> locatorResources;
    private final Map<PluginLanguageService, List<PluginCandidate>> pluginCandidates;

    public FabricPluginPlatform() {
        this.locatorServices = new HashMap<>();
        this.languageServices = new HashMap<>();
        this.locatorResources = new HashMap<>();
        this.pluginCandidates = new IdentityHashMap<>();
        this.standardEnvironment = new StandardEnvironment(LogManager.getLogger("Loofah/AppLaunch"));
    }

    public static synchronized void bootstrap() {
        if (FabricPluginPlatform.bootstrapped) { return; }

        FabricPluginPlatform pluginPlatform = new FabricPluginPlatform();
        AppLaunch.setPluginPlatform(pluginPlatform);
        FabricLoader loader = FabricLoader.getInstance();
        pluginPlatform.setBaseDirectory(loader.getGameDir());
        ModContainer loofahModContainer = loader
            .getModContainer("loofah")
            .orElseThrow(() -> new IllegalStateException("Tried to get own ModContainer, but it wasn't available. This should be impossible!!"));
        String loofahVersion = loofahModContainer.getMetadata().getVersion().getFriendlyString();
        pluginPlatform.setVersion(loofahVersion);
        ArrayList<Path> pluginDirectories = new ArrayList<>(2);
        pluginPlatform.setPluginDirectories(pluginDirectories);
        pluginDirectories.add(loader.getGameDir().resolve("mods"));
        pluginDirectories.add(Paths.get(SpongeConfigs.getCommon().get().general.pluginsDir.getParsed()));
        pluginPlatform.setMetadataFilePath(PluginPlatformConstants.METADATA_FILE_LOCATION);

        FabricPluginPlatform.bootstrapped = true;
    }

    public Environment getStandardEnvironment() {
        return this.standardEnvironment;
    }

    @Override
    public String version() {
        return this.standardEnvironment.blackboard().get(Keys.VERSION);
    }

    @Override
    public void setVersion(String version) {
        this.standardEnvironment.blackboard().set(Keys.VERSION, version);
    }

    @Override
    public Logger logger() {
        return this.standardEnvironment.logger();
    }

    @Override
    public Path baseDirectory() {
        return this.standardEnvironment.blackboard().get(Keys.BASE_DIRECTORY);
    }

    @Override
    public void setBaseDirectory(Path path) {
        this.standardEnvironment.blackboard().set(Keys.BASE_DIRECTORY, path);
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.standardEnvironment.blackboard().get(Keys.PLUGIN_DIRECTORIES);
    }

    @Override
    public void setPluginDirectories(List<Path> list) {
        this.standardEnvironment.blackboard().set(Keys.PLUGIN_DIRECTORIES, list);
    }

    @Override
    public String metadataFilePath() {
        return this.standardEnvironment.blackboard().get(Keys.METADATA_FILE_PATH);
    }

    @Override
    public void setMetadataFilePath(String path) {
        this.standardEnvironment.blackboard().set(Keys.METADATA_FILE_PATH, path);
    }

    public Map<String, PluginResourceLocatorService<? extends PluginResource>> getLocatorServices() {
        return Collections.unmodifiableMap(this.locatorServices);
    }

    public Map<String, PluginLanguageService> getLanguageServices() {
        return Collections.unmodifiableMap(this.languageServices);
    }

    public Map<String, Set<? extends PluginResource>> getResources() {
        return Collections.unmodifiableMap(this.locatorResources);
    }

    public Map<PluginLanguageService, List<PluginCandidate>> getCandidates() {
        return Collections.unmodifiableMap(this.pluginCandidates);
    }

    public void discoverLocatorServices() {
        final var serviceLoader = ServiceLoader.load(PluginResourceLocatorService.class, FabricPluginPlatform.class.getClassLoader());

        for (final var iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginResourceLocatorService<?> next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.standardEnvironment.logger().error("Error encountered initializing plugin resource locator!", e);
                continue;
            }

            this.logger().info("Plugin resource locator '{}' found.", next.name());
            this.locatorServices.put(next.name(), next);
        }
    }

    public void discoverLanguageServices() {
        this.standardEnvironment.blackboard().set(JVMKeys.JVM_PLUGIN_RESOURCE_FACTORY, FabricPluginResource::new);
        this.standardEnvironment.blackboard().set(JVMKeys.ENVIRONMENT_LOCATOR_VARIABLE_NAME, "SPONGE_PLUGINS");
        final ServiceLoader<PluginLanguageService> serviceLoader = ServiceLoader.load(
            PluginLanguageService.class, FabricPluginPlatform.class.getClassLoader()
        );

        for (final Iterator<PluginLanguageService> iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginLanguageService next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.standardEnvironment.logger().error("Error encountered initializing plugin language service!", e);
                continue;
            }

            this.logger().info("Plugin language loader '{}' found.", next.name());
            this.languageServices.put(next.name(), next);
        }
    }

    public void locatePluginResources() {
        for (final Map.Entry<String, PluginResourceLocatorService<?>> locatorEntry : this.locatorServices.entrySet()) {
            final PluginResourceLocatorService<?> locatorService = locatorEntry.getValue();
            final Set<? extends PluginResource> resources = locatorService.locatePluginResources(this.standardEnvironment);
            if (!resources.isEmpty()) {
                this.locatorResources.put(locatorEntry.getKey(), resources);
            }
        }
    }

    public void createPluginCandidates() {
        for (final PluginLanguageService languageService : this.languageServices.values()) {
            for (final Set<? extends PluginResource> resources : this.locatorResources.values()) {

                for (final PluginResource pluginResource : resources) {
                    try {
                        final List<PluginCandidate> candidates = languageService.createPluginCandidates(this.standardEnvironment,
                            pluginResource);
                        if (candidates.isEmpty()) {
                            continue;
                        }
                        this.pluginCandidates.computeIfAbsent(languageService, k -> new LinkedList<>()).addAll(candidates);
                    } catch (final Exception ex) {
                        this.standardEnvironment.logger().error("Failed to create plugin candidates", ex);
                    }
                }
            }
        }
    }
}
