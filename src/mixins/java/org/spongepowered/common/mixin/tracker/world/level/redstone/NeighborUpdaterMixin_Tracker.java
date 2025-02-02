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
package org.spongepowered.common.mixin.tracker.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.world.volume.VolumeStreamUtils;

import java.util.function.Supplier;

@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin_Tracker {

    /**
     * Logs a transaction to the {@link PhaseContext} for neighbor notifications. Otherwise,
     * we will fail to create notification events in the API. This avoids a redirect or overwrite
     *
     * @param level
     * @param targetState
     * @param targetPos
     * @param fromBlock
     * @param fromPos
     * @param movement
     * @param ci
     */
    @Inject(method = "executeUpdate", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;handleNeighborChanged(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V"
    ))
    private static void tracker$wrapNeighborUpdateInSideEffect(Level level, BlockState targetState, BlockPos targetPos, Block fromBlock, BlockPos fromPos, boolean movement, CallbackInfo ci) {
        // Sponge start - prepare notification
        final PhaseContext<@NonNull ?> peek = PhaseTracker.getInstance().getPhaseContext();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        final var targetChunk = serverLevel.getChunkAt(targetPos);
        final Supplier<ServerLevel> worldSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(serverLevel, "ServerWorld");
        final net.minecraft.world.level.block.entity.@Nullable BlockEntity existingTile = targetChunk.getBlockEntity(
            targetPos,
            LevelChunk.EntityCreationType.CHECK
        );
        peek.getTransactor().logNeighborNotification(worldSupplier, fromPos, fromBlock, targetPos, targetState, existingTile);

        peek.associateNeighborStateNotifier(fromPos, targetState.getBlock(), targetPos, serverLevel, PlayerTracker.Type.NOTIFIER);
    }
}
