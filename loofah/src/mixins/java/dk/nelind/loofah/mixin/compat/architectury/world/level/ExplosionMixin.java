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
package dk.nelind.loofah.mixin.compat.architectury.world.level;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Replaces Architectury's detonate event call with a Loofah/SpongeCommon compatible implementation if
 * Architectury is installed
 */
@Debug(export = true)
@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow @Final private Level level;

    @Inject(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void callArchitecturyExplodePostEvent(CallbackInfo ci, float q, int k, int l, int r, int s, int t, int u, List list) {
        // Call the event using reflection. It's technically a bit brittle, but I think it'll be fine.
        try {
            var eventClass = Class.forName("dev.architectury.event.Event");
            var eventInkoverMethod = eventClass.getDeclaredMethod("invoker");

            var explosionEventClass = Class.forName("dev.architectury.event.events.common.ExplosionEvent");
            var detonateEventClass = Class.forName("dev.architectury.event.events.common.ExplosionEvent$Detonate");
            var detonateEventInstance = explosionEventClass.getField("DETONATE");
            var eventInvoker = eventInkoverMethod.invoke(detonateEventInstance.get(null));
            var eventCallMethod = detonateEventClass.getDeclaredMethod("explode", Level.class, Explosion.class, List.class);

            eventCallMethod.invoke(eventInvoker, this.level, this, list);
            LogManager.getLogger().info("Called arch event");
        } catch (
            ClassNotFoundException |
            NoSuchFieldException |
            NoSuchMethodException |
            InvocationTargetException |
            IllegalAccessException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
