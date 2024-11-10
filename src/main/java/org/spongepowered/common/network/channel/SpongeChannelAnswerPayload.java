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
package org.spongepowered.common.network.channel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

// TODO(loofah): get the buffer handled in a FAPI style by just saving it directly on the sponge payload
//  is drifting a bit far from upstream imo. preferable to do something else if possible
/** Loofah only copy of {@link org.spongepowered.common.network.channel.SpongeChannelPayload} implementing only {@link net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload} to fix a remapping issue */
public record SpongeChannelAnswerPayload(@Nullable Type<? extends CustomPacketPayload> type, @Nullable ResourceLocation id, @Nullable Consumer<FriendlyByteBuf> consumer, /* Loofah : store a bytebuf handled like FAPI to achieve FAPI compat */ FriendlyByteBuf originalBuf) implements CustomPacketPayload, CustomQueryAnswerPayload {

    public void write(final FriendlyByteBuf buf) {
        this.consumer.accept(buf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return this.type;
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    public static SpongeChannelAnswerPayload bufferOnly(@Nullable Consumer<FriendlyByteBuf> consumer, FriendlyByteBuf originalBuf) {
        return new SpongeChannelAnswerPayload(null, null, consumer, originalBuf);
    }
}
