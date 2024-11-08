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
package org.spongepowered.common.mixin.core.world.ticks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.ticks.LevelChunkTicksBridge;
import org.spongepowered.common.bridge.world.ticks.LevelTicksBridge;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;

import java.util.Queue;
import java.util.function.BiConsumer;

@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin<T> implements LevelTicksBridge<T> {

    // @formatter:off
    @MonotonicNonNull private ServerLevel impl$level;
    // @formatter:on

    @SuppressWarnings("unchecked")
    @Inject(
        method = "runCollectedTicks",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false,
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void impl$markDataAsCompleted(BiConsumer<BlockPos, T> $$0, CallbackInfo ci, ScheduledTick<T> scheduledTick) {
        ((TickNextTickDataBridge<T>) (Object) scheduledTick).bridge$setState(ScheduledUpdate.State.FINISHED);
    }

    /**
     * A more streamlined solution to filtering without having to necessarily
     * deal with removing the data from both sets individually and potentially
     * cause an invariant. Since this means that the data can be cancelled
     * without removing from the set, we effectively need to just filter it here.
     * But the scheduled update will still end up being removed from the schedule
     *
     * @param queue The currentlyTicking queue to be ticked
     * @param data The next tick data to tick
     * @return False if the data was marked as cancelled
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Redirect(
        method = "scheduleForThisTick",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z",
            remap = false
        )
    )
    private boolean impl$validateHasNextUncancelled(Queue<ScheduledTick<T>> queue, Object data) {
        final ScheduledUpdate.State state = ((TickNextTickDataBridge<T>) data).bridge$internalState();
        if (state == ScheduledUpdate.State.CANCELLED) {
            return false;
        }
        return queue.add((ScheduledTick<T>) data);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "addContainer", at = @At("HEAD"))
    private void impl$onAddContainer(final ChunkPos $$0, final LevelChunkTicks<T> $$1, final CallbackInfo ci) {
        ((LevelChunkTicksBridge<T>) $$1).bridge$setTickList((LevelTicks<T>) (Object) this);
    }

    @Override
    public void bridge$level(final ServerLevel level) {
        this.impl$level = level;
    }

    @Override
    public ServerLevel bridge$level() {
        return this.impl$level;
    }
}
