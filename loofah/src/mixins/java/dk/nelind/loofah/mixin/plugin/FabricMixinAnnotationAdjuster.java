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
package dk.nelind.loofah.mixin.plugin;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableRedirectNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

public class FabricMixinAnnotationAdjuster implements MixinAnnotationAdjuster {
    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode handlerNode, AdjustableAnnotationNode annotationNode) {
        // Don't inject Architectury's MixinExplosion inject as the priorities are in the wrong order
        // and even if they were in the right order the LVT has been changed by an overwrite in
        // SpongeCommon ExplosionMixin and the local capture would fail. Not the pretties thing ever but
        // sometimes modding is just inherently ugly. If some other better solution presents itself at some point
        // that is mostly likely preferable
        //
        // See also dk.nelind.loofah.mixin.compat.world.level.ExplosionMixin
        if (mixinClassName.equals("dev.architectury.mixin.fabric.MixinExplosion") && annotationNode.is(Inject.class)) {
            return null;
        }

        // TODO(loofah): revert once bug is fixed upstream
        // Manually remap target string for production. See https://github.com/FabricMC/tiny-remapper/issues/126
        if (
            !FabricLoader.getInstance().isDevelopmentEnvironment() &&
            mixinClassName.equals("org.spongepowered.common.mixin.core.world.ticks.LevelChunkTicksMixin") &&
            handlerNode.name.equals("impl$onSaveSkipCancelled2") &&
            annotationNode.is(Redirect.class)
        ) {
            return annotationNode.as(AdjustableRedirectNode.class)
                .withAt(at -> {
                    at.setTarget("Lnet/minecraft/class_2499;add(Ljava/lang/Object;)Z");
                    return at;
                });
        }

        return annotationNode;
    }
}
