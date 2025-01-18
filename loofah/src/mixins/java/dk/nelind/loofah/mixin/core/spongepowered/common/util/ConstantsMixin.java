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
package dk.nelind.loofah.mixin.core.spongepowered.common.util;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.util.Constants;

// TODO(loofah): Find a better solution to runtime remapping of these values? See also the Constants class
@Mixin(Constants.class)
public class ConstantsMixin {
    /**
     * @author Nelind
     * @reason Remapping at runtime
     */
    @Overwrite
    private static String getClientClassName() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? "net.minecraft.client.Minecraft" : "net.minecraft.class_310";
    }

    /**
     * @author Nelind
     * @reason Remapping at runtime
     */
    @Overwrite
    private static String getDedicatedServerClassName() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? "net.minecraft.server.dedicated.DedicatedServer" : "net.minecraft.class_3176";
    }

    /**
     * @author Nelind
     * @reason Remapping at runtime
     */
    @Overwrite
    private static String getServerClassName() {
        return "net.minecraft.server.MinecraftServer";
    }

    /**
     * @author Nelind
     * @reason Remapping at runtime
     */
    @Overwrite
    private static String getIntegratedServerClassName() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? "net.minecraft.client.server.IntegratedServer" : "net.minecraft.class_1132";
    }
}
