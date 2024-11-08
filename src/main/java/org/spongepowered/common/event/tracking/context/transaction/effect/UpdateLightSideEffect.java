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
package org.spongepowered.common.event.tracking.context.transaction.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.BlockPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.PipelineCursor;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

public final class UpdateLightSideEffect implements ProcessingSideEffect {

    private static final class Holder {
        static final UpdateLightSideEffect INSTANCE = new UpdateLightSideEffect();
    }

    public static UpdateLightSideEffect getInstance() {
        return UpdateLightSideEffect.Holder.INSTANCE;
    }

    UpdateLightSideEffect() {
    }

    @Override
    public EffectResult processSideEffect(
        final BlockPipeline pipeline, final PipelineCursor oldState,
        final BlockState newState, final SpongeBlockChangeFlag flag, final int limit
    ) {
        if (!flag.updateLighting()) {
            return EffectResult.NULL_PASS;
        }
        final int originalOpactiy = oldState.opacity;
        final ServerLevel serverWorld = pipeline.getServerWorld();
        final LevelChunk chunk = pipeline.getAffectedChunk();
        final BlockState currentState = chunk.getBlockState(oldState.pos);
        // local variable notes:
        // var2 = oldState.state
        // var3 = currentState
        // if (
        //      (param2 & 128) == 0 // this is handled above as flag.updateLighting()
        //      && var3 != var2
        //      && (
        //          var3.getLightBlock(this, param0) != var2.getLightBlock(this, param0)
        //          || var3.getLightEmission() != var2.getLightEmission()
        //          || var3.useShapeForLightOcclusion()
        //          || var2.useShapeForLightOcclusion()
        //         )
        //     ) {
        if (oldState.state != currentState
            && (currentState.getLightBlock(serverWorld, oldState.pos) != originalOpactiy
            || currentState.getLightEmission() != oldState.state.getLightEmission()
            || currentState.useShapeForLightOcclusion()
            || oldState.state.useShapeForLightOcclusion()
        )) {
            final int x = oldState.pos.getX() & 15;
            final int y = oldState.pos.getY();
            final int z = oldState.pos.getZ() & 15;
            // ProfilerFiller $$12 = this.level.getProfiler();
            final ProfilerFiller profiler = serverWorld.getProfiler();
            profiler.push("updateSkyLightSources");
            // this.skyLightSources.update(this, x, y, z);
            chunk.getSkyLightSources().update(chunk, x, y, z);
            profiler.popPush("queueCheckLight");
            // this.level.getChunkProvider().getLightManager().checkBlock(pos);
            serverWorld.getChunkSource().getLightEngine().checkBlock(oldState.pos);
            profiler.pop();
        }
        return EffectResult.NULL_PASS;
    }

}
