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

import dk.nelind.loofah.applaunch.plugin.resource.FabricPluginResource;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.Manifest;

public class FabricModBackedPluginResource implements JVMPluginResource {
    private final ModContainer mod;
    private final Manifest manifest;

    public FabricModBackedPluginResource(ModContainer mod) {
        this.mod = mod;
        try {
            // TODO(loofah): idk this feels weird but i don't see an immediate need to pull this out into a
            //  util class so in FabricPluginResource it shall stay
            this.manifest = FabricPluginResource.getManifest(mod.getRootPaths());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Manifest manifest() {
        return this.manifest;
    }

    @Override
    public Path resourcesRoot() {
        return this.mod.getRootPaths().getLast();
    }

    @Override
    public String locator() {
        return "fabric_loader";
    }

    @Override
    public Path path() {
        return this.mod.getOrigin().getPaths().getLast();
    }
}
