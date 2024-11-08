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
package org.spongepowered.common.mixin.core.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.network.channel.SpongeChannelAnswerPayload;

// Loofah : inject into readPayload and take down the priority to make sure we supersede FAPI networking API
@Mixin(value = ServerboundCustomQueryAnswerPacket.class, priority = 999)
public abstract class ServerboundCustomQueryAnswerPacketMixin {
    // Loofah body
    // @formatter: off
    @Shadow
    @Final
    private static int MAX_PAYLOAD_SIZE;
    // @formatter: on

    @Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
    private static void impl$onReadUnknownPayload(int $$0, FriendlyByteBuf $$1, CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
        final int readableBytes = $$1.readableBytes();
        if (readableBytes >= 0 && readableBytes <= ServerboundCustomQueryAnswerPacketMixin.MAX_PAYLOAD_SIZE) {
            final var payload = $$1.readNullable(buf -> new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
            cir.setReturnValue(SpongeChannelAnswerPayload.bufferOnly(payload == null ? null : buf -> buf.writeBytes(payload, payload.readerIndex(), payload.readableBytes())));
        } else {
            throw new IllegalArgumentException("Payload may not be larger than " + ServerboundCustomQueryAnswerPacketMixin.MAX_PAYLOAD_SIZE + " bytes");
        }
    }

    // Upstream body
    /*@Inject(method = "readUnknownPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;skipBytes(I)Lnet/minecraft/network/FriendlyByteBuf;"), cancellable = true)
    private static void impl$onReadUnknownPayload(final FriendlyByteBuf $$0, final CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
        final var payload = $$0.readNullable(buf -> new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));

        cir.setReturnValue(SpongeChannelAnswerPayload.bufferOnly(payload == null ? null : buf -> buf.writeBytes(payload, payload.readerIndex(), payload.readableBytes())));
    }*/
}
