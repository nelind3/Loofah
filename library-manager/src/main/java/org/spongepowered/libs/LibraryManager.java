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
package org.spongepowered.libs;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.spongepowered.libs.model.Libraries;
import org.spongepowered.libs.model.SonatypeResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class LibraryManager {
    public static final String SPONGE_NEXUS_DOWNLOAD_URL = "https://repo.spongepowered.org/service/rest/v1/search/assets?md5=%s&maven"
        + ".groupId=%s&maven.artifactId=%s&maven.baseVersion=%s&maven.extension=jar";

    private final Logger logger;
    private final boolean checkLibraryHashes;
    private final Path rootDirectory;
    private final URL librariesUrl;
    private final Map<String, Set<Library>> libraries;
    private final ExecutorService preparationWorker;
    private final Gson gson;

    public LibraryManager(final Logger logger, final boolean checkLibraryHashes, final Path rootDirectory, final URL librariesUrl) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.checkLibraryHashes = checkLibraryHashes;
        this.rootDirectory = Objects.requireNonNull(rootDirectory, "rootDirectory");
        this.librariesUrl = Objects.requireNonNull(librariesUrl, "librariesUrl");

        this.libraries = new LinkedHashMap<>();
        final int availableCpus = Runtime.getRuntime().availableProcessors();
        // We'll be performing mostly IO-blocking operations, so more threads will help us for now
        // It might make sense to make this overridable eventually
        this.preparationWorker = new ThreadPoolExecutor(
            Math.min(Math.max(4, availableCpus * 2), 64), Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>() // this is the number of tasks allowed to be waiting before the pool will spawn off a new thread (unbounded)
        );
        this.gson = new Gson();
    }

    public Path getRootDirectory() {
        return this.rootDirectory;
    }

    public Set<Library> getAll(final String collection) {
        return Collections.unmodifiableSet(this.libraries.getOrDefault(collection, Collections.emptySet()));
    }

    public void addLibrary(final String set, final Library library) {
        this.libraries.computeIfAbsent(set, $ -> Collections.synchronizedSet(new LinkedHashSet<>())).add(library);
    }

    public void validate() throws Exception {
        this.logger.info("Scanning and verifying libraries in '{}'. Please wait, this may take a moment...", this.rootDirectory);

        final Libraries dependencies;
        try (final JsonReader reader = new JsonReader(new InputStreamReader(this.librariesUrl.openStream(), StandardCharsets.UTF_8))) {
            dependencies = this.gson.fromJson(reader, Libraries.class);
        }

        final Map<String, Set<Library>> downloadedDeps = new HashMap<>();
        final Map<String, CompletableFuture<Path>> operations = new HashMap<>();
        final Set<String> failures = ConcurrentHashMap.newKeySet();

        for (final Map.Entry<String, List<Libraries.Dependency>> setEntry : dependencies.dependencies.entrySet()) {
            downloadedDeps.put(setEntry.getKey(), this.scheduleDownloads(setEntry.getKey(), setEntry.getValue(), operations, failures));
        }

        CompletableFuture.allOf(operations.values().toArray(new CompletableFuture<?>[0])).handle((result, err) -> {
            if (err != null) {
                failures.add(err.getMessage());
                this.logger.error("Failed to download library", err);
            }
            return result;
        }).join();

        if (!failures.isEmpty()) {
            this.logger.error("Failed to download some libraries:");
            for (final String message : failures) {
                this.logger.error(message);
            }
            System.exit(-1);
        }

        this.libraries.putAll(downloadedDeps);
    }

    private Set<Library> scheduleDownloads(
        final String collection,
        final List<Libraries.Dependency> dependencies,
        final Map<String, CompletableFuture<Path>> operations,
        final Set<String> failures
    ) {
        final Set<Library> downloadedDeps = Collections.synchronizedSet(new LinkedHashSet<>(dependencies.size()));
        for (final Libraries.Dependency dependency : dependencies) {
            operations.computeIfAbsent(asId(dependency), $ -> AsyncUtils.asyncFailableFuture(() -> {
                final String groupPath = dependency.group.replace(".", "/");
                final Path depDirectory =
                    this.rootDirectory.resolve(groupPath).resolve(dependency.module).resolve(dependency.version);
                Files.createDirectories(depDirectory);
                final Path depFile = depDirectory.resolve(dependency.module + "-" + dependency.version + ".jar");
                final MessageDigest md5 = MessageDigest.getInstance("MD5");

                final boolean checkHashes = this.checkLibraryHashes;

                if (Files.exists(depFile)) {
                    if (!checkHashes) {
                        this.logger.info("Detected existing '{}', skipping hash checks...", depFile);
                        return depFile;
                    }

                    // Pipe the download stream into the file and compute the SHA-1
                    final byte[] bytes = Files.readAllBytes(depFile);
                    final String fileMd5 = InstallerUtils.toHexString(md5.digest(bytes));

                    if (dependency.md5.equals(fileMd5)) {
                        this.logger.debug("'{}' verified!", depFile);
                    } else {
                        this.logger.error("Checksum verification failed: Expected {}, {}. Deleting cached '{}'...",
                            dependency.md5, fileMd5, depFile);
                        Files.delete(depFile);

                        final SonatypeResponse response = this.getResponseFor(this.gson, dependency);

                        if (response.items.isEmpty()) {
                            failures.add("No data received from '" + new URL(String.format(LibraryManager.SPONGE_NEXUS_DOWNLOAD_URL,
                                dependency.md5, dependency.group,
                                dependency.module, dependency.version)) + "'!");
                            return null;
                        }
                        final SonatypeResponse.Item item = response.items.get(0);
                        final URL url = item.downloadUrl;

                        InstallerUtils.downloadCheckHash(this.logger, url, depFile, md5, item.checksum.md5);
                    }
                } else {
                    final SonatypeResponse response = this.getResponseFor(this.gson, dependency);

                    if (response.items.isEmpty()) {
                        failures.add("No data received from '" + new URL(String.format(LibraryManager.SPONGE_NEXUS_DOWNLOAD_URL,
                            dependency.md5, dependency.group,
                            dependency.module, dependency.version)) + "'!");
                        return null;
                    }

                    final SonatypeResponse.Item item = response.items.get(0);
                    final URL url = item.downloadUrl;

                    if (checkHashes) {
                        InstallerUtils.downloadCheckHash(this.logger, url, depFile, md5, item.checksum.md5);
                    } else {
                        InstallerUtils.download(this.logger, url, depFile, true);
                    }
                }

                return depFile;
            }, this.preparationWorker)).whenComplete((res, err) -> {
                if (res != null) {
                    downloadedDeps.add(new Library(asId(dependency), res));
                }
            });
        }
        return downloadedDeps;
    }

    private SonatypeResponse getResponseFor(final Gson gson, final Libraries.Dependency dependency) throws IOException {
        final URL requestUrl = new URL(String.format(LibraryManager.SPONGE_NEXUS_DOWNLOAD_URL, dependency.md5, dependency.group,
            dependency.module, dependency.version));

        final HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Sponge-Downloader");

        connection.connect();

        try (final JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
            return gson.fromJson(reader, SonatypeResponse.class);
        }
    }

    public ExecutorService preparationWorker() {
        return this.preparationWorker;
    }

    public void finishedProcessing() {
        if (this.preparationWorker.isTerminated()) {
            return;
        }

        this.preparationWorker.shutdown();
        boolean successful;
        try {
            successful = this.preparationWorker.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            successful = false;
        }

        if (!successful) {
            this.logger.warn("Failed to shut down library preparation pool in 10 seconds, forcing shutdown now.");
            this.preparationWorker.shutdownNow();
        }
    }

    public record Library(String name, Path file) {}

    private static String asId(final Libraries.Dependency dep) {
        return dep.group + ':' + dep.module + ':' + dep.version;
    }
}
