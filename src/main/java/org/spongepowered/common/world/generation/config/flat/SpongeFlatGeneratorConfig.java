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
package org.spongepowered.common.world.generation.config.flat;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfig;
import org.spongepowered.api.world.generation.config.flat.LayerConfig;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.levelgen.flat.FlatLevelGeneratorSettingsAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SpongeFlatGeneratorConfig {

    public static final class BuilderImpl implements FlatGeneratorConfig.Builder {

        public final List<LayerConfig> layers = new ArrayList<>();
        @Nullable public RegistryReference<org.spongepowered.api.world.biome.Biome> biome;
        public boolean performDecoration, populateLakes;
        @Nullable private List<org.spongepowered.api.world.generation.structure.StructureSet> structureSets;

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public FlatGeneratorConfig.Builder addLayer(final int index, final LayerConfig config) {
            this.layers.add(index, Objects.requireNonNull(config, "config"));
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder addLayer(final LayerConfig config) {
            this.layers.add(Objects.requireNonNull(config, "config"));
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder addLayers(final List<LayerConfig> layers) {
            this.layers.addAll(Objects.requireNonNull(layers, "layers"));
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder removeLayer(final int index) {
            this.layers.remove(index);
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder biome(final RegistryReference<org.spongepowered.api.world.biome.Biome> biome) {
            this.biome = Objects.requireNonNull(biome, "biome");
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder performDecoration(final boolean performDecoration) {
            this.performDecoration = performDecoration;
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder populateLakes(final boolean populateLakes) {
            this.populateLakes = populateLakes;
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder structureSets(@Nullable final List<org.spongepowered.api.world.generation.structure.StructureSet> structureSets) {
            this.structureSets = structureSets;
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder reset() {
            this.biome = null;
            this.layers.clear();
            this.performDecoration = false;
            this.populateLakes = true;
            this.structureSets = null;
            return this;
        }

        @Override
        public FlatGeneratorConfig.Builder from(final FlatGeneratorConfig value) {
            this.biome = value.biome();
            this.layers.addAll(value.layers());
            this.performDecoration = value.performDecoration();
            this.populateLakes = value.populateLakes();
            this.structureSets = value.structureSets().orElse(null);
            return this;
        }

        @Override
        public @NonNull FlatGeneratorConfig build() {
            if (this.biome == null) {
                throw new IllegalStateException("Flat generation requires a biome to be specified!");
            }
            if (this.layers.isEmpty()) {
                throw new IllegalStateException("Flat generation requires at least 1 Layer!");
            }
            final Registry<Biome> biomeRegistry = SpongeCommon.vanillaRegistry(Registries.BIOME);
            final HolderLookup.RegistryLookup<PlacedFeature> placedFeatureRegistryLookup = SpongeCommon.vanillaRegistry(Registries.PLACED_FEATURE).asLookup();

            final Holder.Reference<Biome> biome =
                    biomeRegistry.asLookup().getOrThrow(ResourceKey.create(Registries.BIOME, (ResourceLocation) (Object) this.biome.location()));
            return (FlatGeneratorConfig) FlatLevelGeneratorSettingsAccessor.invoker$new(
                    this.structureSets == null ? Optional.empty() : Optional.of(HolderSet.direct((Function) Holder::direct, this.structureSets)),
                    (List<FlatLayerInfo>) (Object) this.layers,
                    this.populateLakes,
                    this.performDecoration,
                    Optional.of(biome), biome,
                    placedFeatureRegistryLookup.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND),
                    placedFeatureRegistryLookup.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_SURFACE));
        }
    }

}
