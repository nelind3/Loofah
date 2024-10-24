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
package dk.nelind.loofah.mixin.api.minecraft.server.packs.repository;

import dk.nelind.loofah.bridge.server.packs.repository.PackRepositoryBridge_Fabric;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.plugin.PluginContainer;

import java.util.Objects;

/** Copied from {@link org.spongepowered.vanilla.mixin.api.minecraft.server.packs.repository.PackRepositoryMixin_Vanilla_API} */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin_Fabric_API implements org.spongepowered.api.resource.pack.PackRepository {
    @Override
    public Pack pack(final PluginContainer container) {
        return (Pack) ((PackRepositoryBridge_Fabric) this).bridge$pack(Objects.requireNonNull(container, "container"));
    }
}
