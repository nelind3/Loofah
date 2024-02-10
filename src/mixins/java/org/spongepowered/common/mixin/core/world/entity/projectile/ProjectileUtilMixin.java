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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.DirectionUtil;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {

    @Inject(method = "getHitResultOnMoveVector", at = @At("RETURN"), cancellable = true)
    private static void impl$onGetHitResultOnMoveVector(final Entity entity, final Predicate<Entity> $$1, final CallbackInfoReturnable<HitResult> cir) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_MOVE) {
            return;
        }

        final HitResult hitResult = cir.getReturnValue();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            final BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            final BlockState state = entity.level().getBlockState(blockHitResult.getBlockPos());
            if (SpongeCommonEventFactory.handleCollideBlockEvent(state.getBlock(), entity.level(), blockHitResult.getBlockPos(), state,
                    entity, DirectionUtil.getFor(blockHitResult.getDirection()), SpongeCommonEventFactory.CollisionType.MOVE)) {
                final Vec3 from = entity.position();
                final Vec3 velocity = entity.getDeltaMovement();
                final Vec3 to = from.add(velocity);
                final Vec3 direction = from.subtract(to);

                cir.setReturnValue(BlockHitResult.miss(to, Direction.getNearest(direction.x, direction.y, direction.z), BlockPos.containing(to)));
            }
        }
    }
}
