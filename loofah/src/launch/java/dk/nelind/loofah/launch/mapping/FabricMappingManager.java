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
package dk.nelind.loofah.launch.mapping;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.spongepowered.common.launch.mapping.SpongeMappingManager;

public class FabricMappingManager implements SpongeMappingManager {
    private final MappingResolver mappingResolver;

    public FabricMappingManager() {
        this.mappingResolver = FabricLoader.getInstance().getMappingResolver();
    }

    @Override
    public String toRuntimeClassName(final String srcName) {
        return this.mappingResolver.mapClassName("mojmap", srcName);
    }

    // TODO(loofah): cache these two maybe? it's quite the song and dance to do every time if this is done a lot.

    // Fabric released mappings (Intermediary and Yarn) don't include inherited members,
    // so we have to do this song and dance to check all of a classes super classes
    // if the requested name was found but wasn't present in the runtime mapping.
    @Override
    public String toRuntimeFieldName(final Class<?> owner, final String srcName) {
        String attemptedRuntimeName = this.mappingResolver.mapFieldName(
            "mojmap",
            // Gotta pass in the owning class with the mojmap name, so we have to unmap again ...
            this.mappingResolver.unmapClassName(
                "mojmap",
                owner.getName()
            ),
            srcName,
            null
        );

        if (attemptedRuntimeName == null) {
            return this.toRuntimeFieldName(owner.getSuperclass(), srcName);
        }

        return attemptedRuntimeName;
    }

    @Override
    public String toRuntimeMethodName(final Class<?> owner, final String srcName, final Class<?>... params) {
        String attemptedRuntimeName = mappingResolver.mapMethodName(
            "mojmap",
            // Gotta pass in the owning class with the mojmap name, so we have to unmap again ...
            this.mappingResolver.unmapClassName(
                "mojmap",
                owner.getName()
            ),
            srcName,
            null
        );

        if (attemptedRuntimeName == null) {
            return this.toRuntimeMethodName(owner.getSuperclass(), srcName, params);
        }

        return attemptedRuntimeName;
    }
}
