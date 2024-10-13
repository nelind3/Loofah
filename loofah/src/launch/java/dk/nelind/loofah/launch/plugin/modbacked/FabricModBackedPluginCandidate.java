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
package dk.nelind.loofah.launch.plugin.modbacked;

import dk.nelind.loofah.applaunch.plugin.resource.FabricPluginResource;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginContributor;

import java.nio.file.Path;

// TODO(loofah): consider making a mod backed plugin locator/language service/loader triplet
public class FabricModBackedPluginCandidate implements PluginCandidate {
    private final PluginMetadata pluginMetadata;
    private final FabricPluginResource pluginResource;

    public FabricModBackedPluginCandidate(final ModContainer modContainer) {
        this.pluginResource = new FabricPluginResource(
            "fabric_loader",
            modContainer.getOrigin().getPaths().toArray(new Path[0])
        );
        this.pluginMetadata = FabricModBackedPluginCandidate.pluginMetadataFromMod(modContainer.getMetadata());
    }

    private static PluginMetadata pluginMetadataFromMod(ModMetadata modMetadata) {
        return FabricModBackedPluginCandidate.pluginMetadataBuilderFromMod(modMetadata).build();
    }

    private static StandardPluginMetadata.Builder pluginMetadataBuilderFromMod(ModMetadata modMetadata) {
        final StandardPluginMetadata.Builder builder = StandardPluginMetadata.builder();
        builder
            .id(modMetadata.getId())
            .name(modMetadata.getName())
            .version(modMetadata.getVersion().getFriendlyString())
            .description(modMetadata.getDescription())
            .entrypoint("unkown"); // TODO(loofah): probably cant do anything about this due to the way fabric handles
                                   //  entrypoints but it should be looked into

        for (var author : modMetadata.getAuthors()) {
            builder.addContributor(StandardPluginContributor.builder()
                .name(author.getName())
                .build());
        }

        for (var contributor : modMetadata.getContributors()) {
            builder.addContributor(StandardPluginContributor.builder()
                .name(contributor.getName())
                .build());
        }

        return builder;
    }

    @Override
    public PluginMetadata metadata() {
        return this.pluginMetadata;
    }

    @Override
    public PluginResource resource() {
        return this.pluginResource;
    }
}
