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
package dk.nelind.loofah.launch.plugin.modbacked;

import dk.nelind.loofah.launch.FabricLaunch;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.DummyPluginContainer;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.builtin.StandardPluginContainer;

// TODO(loofah): consider making a mod backed plugin locator/language service/loader triplet
public final class FabricModBackedPluginContainer extends StandardPluginContainer implements DummyPluginContainer {
    public static FabricModBackedPluginContainer of(final ModContainer modContainer) {
        return new FabricModBackedPluginContainer(
            new FabricModBackedPluginCandidate(modContainer),
            FabricLaunch.instance().logger(),
            modContainer
        );
    }

    private FabricModBackedPluginContainer(final PluginCandidate candidate, final Logger logger, final Object instance) {
        super(candidate, logger);
        this.initializeInstance(instance);
    }
}
