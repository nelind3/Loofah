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

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.common.mixin.plugin.AbstractMixinConfigPlugin;

public class FabricCompatMixinPlugin extends AbstractMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {

        if (
            mixinClassName.equals("dk.nelind.loofah.mixin.compat.architectury.world.level.ExplosionMixin") &&
            FabricLoader.getInstance().isModLoaded("architectury")
        ) {
            FabricMixinPlugin.LOGGER.info("Architectury API detected. Enabling compatibility features in Loofah");
            return true;
        }

        return false;
    }
}
