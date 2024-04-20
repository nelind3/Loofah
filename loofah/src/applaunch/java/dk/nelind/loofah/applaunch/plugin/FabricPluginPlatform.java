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

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FabricPluginPlatform implements PluginPlatform {
    private static volatile boolean bootstrapped;

    private final FabricLoader loader;
    private final Logger logger;
    private final List<Path> pluginDirectories;

    public FabricPluginPlatform() {
        this.loader = FabricLoader.getInstance();
        this.logger = LogManager.getLogger("loofah");
        this.pluginDirectories = new ArrayList<>();

        this.pluginDirectories.add(loader.getGameDir().resolve("mods"));
    }

    /**
     * Bootstrap the Fabric Plugin platform. This is intended for use by a mixin configuration plugin to make the plugin platform available during mixin application.
     */
    public static synchronized void bootstrap() {
        if (FabricPluginPlatform.bootstrapped) {
            return;
        }
        FabricPluginPlatform.bootstrapped = true;
        final FabricPluginPlatform platform = new FabricPluginPlatform();
        AppLaunch.setPluginPlatform(platform);
        platform.init();
    }

    public void init() {
        final Path alternativePluginsDirectory = Paths.get(SpongeConfigs.getCommon().get().general.pluginsDir.getParsed());
        try {
            Files.createDirectories(alternativePluginsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.pluginDirectories.add(alternativePluginsDirectory);
    }

    @Override
    public String version() {
        /*Optional<ModContainer> optionalModContainer = loader.getModContainer("loofah");
        if (optionalModContainer.isPresent()) {
            return optionalModContainer.get().getMetadata().getVersion().getFriendlyString();
        } else {
            throw new IllegalStateException("Tried to get own ModContainer, but it wasn't available.");
        }*/

        return "FabricPluginPlatform.version()"; //TODO: Find what version we should respond with (game or sponge version) and respond appropriately ... please be sponge version
    }

    @Override
    public void setVersion(String s) {

    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path baseDirectory() {
        return loader.getGameDir();
    }

    @Override
    public void setBaseDirectory(Path path) {

    }

    @Override
    public List<Path> pluginDirectories() {
        return pluginDirectories;
    }

    @Override
    public void setPluginDirectories(List<Path> list) {

    }

    @Override
    public String metadataFilePath() {
        return "spongemetadata";
    }

    @Override
    public void setMetadataFilePath(String s) {

    }
}
