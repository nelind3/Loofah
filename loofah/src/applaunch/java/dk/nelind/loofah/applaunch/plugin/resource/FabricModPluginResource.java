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
package dk.nelind.loofah.applaunch.plugin.resource;

import net.fabricmc.loader.impl.util.FileSystemUtil;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

// See also dk.nelind.loofah.applaunch.plugin.resource.FabricPluginResource
public class FabricModPluginResource implements JVMPluginResource {
    private final String locator;
    private final List<Path> paths;
    private final Manifest manifest;

    public FabricModPluginResource(final String locator, final Path[] paths) {
        this.locator = locator;
        try {
            this.paths = FabricModPluginResource.processPaths(paths);
            this.manifest = FabricModPluginResource.getManifest(this.paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> processPaths(Path[] paths) throws IOException {
        var processedPaths = new ArrayList<Path>();

        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                processedPaths.add(path);
                continue;
            }

            if (path.toString().endsWith(".jar")) {
                var fs = FileSystemUtil.getJarFileSystem(path, false);
                var jarPath = fs.get().getRootDirectories().iterator().next();
                processedPaths.add(jarPath);
                continue;
            }

            throw new IllegalStateException(String.format("Resource path \"%s\" of unknown type given!" +
                "Plugins can only be provided in jar or directory form!", path));
        }

        return processedPaths;
    }

    public static Manifest getManifest(List<Path> paths) throws IOException {
        var manifest = new Manifest();

        for (Path path : paths) {
            var manifestPath = path.resolve(JarFile.MANIFEST_NAME);
            if (Files.exists(manifestPath)) {
                manifest.read(Files.newInputStream(manifestPath));
                break;
            }
        }

        return manifest;
    }

    @Override
    public Optional<URI> locateResource(String path) {
        Optional<URI> resource = Optional.empty();
        for (Path resourcePath : this.paths) {
            final Path p = resourcePath.resolve(path);
            if (Files.exists(p)) {
                resource = Optional.of(p.toUri());
                break;
            }
        }
        return resource;
    }

    @Override
    public String locator() {
        return this.locator;
    }

    @Override
    public Path path() {
        return this.paths.getLast();
    }

    @Override
    public Manifest manifest() {
        return this.manifest;
    }

    @Override
    public Path resourcesRoot() {
        return this.path();
    }
}
