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
package dk.nelind.loofah.mixin.core.client;

import dk.nelind.loofah.FabricClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.entity.player.ClientType;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Fabric implements MinecraftBridge, FabricClient {
    @Override
    public ClientType bridge$getClientType() {
        // TODO(loofah): proper loofah client type since parts of
        //  SpongeCommon use it to check for sponge enabled clients
        return ClientType.from("fabric/loofah");
    }
}
