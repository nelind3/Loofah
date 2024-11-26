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
package dk.nelind.loofah.applaunch.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.StandardPluginCandidate;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginContributor;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginDependency;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginLinks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class FabricModPluginLanguageService implements PluginLanguageService {
    private final Logger logger = LogManager.getLogger(this.name());

    @Override
    public String name() {
        return "fabric_mod";
    }

    @Override
    public String pluginLoader() {
        // hard coded string to avoid circular gradle task dependency hell
        return "dk.nelind.loofah.launch.plugin.FabricModPluginLoader";
    }

    @Override
    public void initialize(Environment environment) {

    }

    @Override
    public List<PluginCandidate> createPluginCandidates(Environment environment, PluginResource resource) throws Exception {
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(resource, "resource");

        final List<PluginCandidate> candidates = new LinkedList<>();

        final Optional<InputStream> optStream = resource.openResource("fabric.mod.json");
        if (optStream.isEmpty()) {
            this.logger.debug("Container in path '{}' doesn't have a fabric.mod.json file, skipping...", resource.path());
            return candidates;
        }

        try (final InputStream stream = optStream.get()) {
            final Set<PluginMetadata> metadataSet = this.parseFabricMetadata(JsonParser.parseReader(new InputStreamReader(stream)));
            for (PluginMetadata metadata : metadataSet) {
                candidates.add(new StandardPluginCandidate(metadata, resource));
            }
        }

        return candidates;
    }

    private Set<PluginMetadata> parseFabricMetadata(JsonElement jsonRoot) {
        // TODO(loofah): test if this is actually true after the class loading issues
        //  (see dk.nelind.loofah.applaunch.plugin.resource.FabricModPluginResourceLocatorService) where fixed
        // We manually parse out only the parts of the fabric.mod.json we need since the loaders parser is inaccessible
        // to mods and the metadata available through the loader api doesn't give entrypoint information.
        // We also assume that the file is spec compliant and parsing thus won't error out since if we got this far
        // the loader should already have verified that it is.

        // The loader doesn't currently support using an array as the root of a fabric.mod.json but we still parse it
        if (jsonRoot.isJsonArray()) {
            var metadataSet = new HashSet<PluginMetadata>();
            for (JsonElement jsonElement : jsonRoot.getAsJsonArray()) {
                metadataSet.add(this.parseFabricMetadata(jsonElement.getAsJsonObject()));
            }
            return metadataSet;
        }

        return Set.of(this.parseFabricMetadata(jsonRoot.getAsJsonObject()));
    }

    private PluginMetadata parseFabricMetadata(JsonObject rootObject) {
        var builder = StandardPluginMetadata.builder();

        // We only check for the schema version here because the loader technically allows for mods that either
        // don't have it set and treats it as schema version 0 used by pre 0.4.0 loader, and we don't support this
        JsonElement schemaVersion = rootObject.get("schemaVersion");
        if (schemaVersion == null || schemaVersion.getAsInt() != 1) {
            throw new IllegalArgumentException("Unknown or no schemaVersion in fabric.mod.json!");
        }

        JsonElement id = rootObject.get("id");
        // Replace - with _ because Sponge Plugins aren't allowed to have - in their ids
        builder.id(id.getAsString().replace("-", "_"));

        JsonElement version = rootObject.get("version");
        builder.version(version.getAsString());

        JsonElement entryPoints = rootObject.get("entrypoints");
        if (entryPoints != null) {
            var entryPointsObject = entryPoints.getAsJsonObject();
            if (entryPointsObject.has("main")) {
                var firstMainEntrypoint = entryPointsObject.get("main").getAsJsonArray().get(0);
                if (firstMainEntrypoint.isJsonObject()) {
                    builder.entrypoint(firstMainEntrypoint.getAsJsonObject().get("value").getAsString());
                } else {
                    builder.entrypoint(firstMainEntrypoint.getAsString());
                }
            } else if (entryPointsObject.has("server")) {
                var firstServerEntrypoints = entryPointsObject.get("server").getAsJsonArray().get(0);
                if (firstServerEntrypoints.isJsonObject()) {
                    builder.entrypoint(firstServerEntrypoints.getAsJsonObject().get("value").getAsString());
                } else {
                    builder.entrypoint(firstServerEntrypoints.getAsString());
                }
            } else if (entryPointsObject.has("client")) {
                var firstClientEntrypoints = entryPointsObject.get("client").getAsJsonArray().get(0);
                if (firstClientEntrypoints.isJsonObject()) {
                    builder.entrypoint(firstClientEntrypoints.getAsJsonObject().get("value").getAsString());
                } else {
                    builder.entrypoint(firstClientEntrypoints.getAsString());
                }
            } else
                builder.entrypoint("unknown");
        } else {
            builder.entrypoint("none");
        }

        JsonElement name = rootObject.get("name");
        if (name != null) {
            builder.name(name.getAsString());
        }

        JsonElement description = rootObject.get("description");
        if (description != null) {
            builder.description(description.getAsString());
        }

        JsonElement contact = rootObject.get("contact");
        if (contact != null) {
            builder.links(this.parseFabricModLinks(contact.getAsJsonObject()));
        }

        JsonElement authors = rootObject.get("authors");
        if (authors != null)  {
            for (JsonElement author : authors.getAsJsonArray()) {
                if (author.isJsonPrimitive()) {
                    builder.addContributor(this.parseFabricModPersonWithDescription(
                        author.getAsString(), "Author"
                    ));
                } else {
                    builder.addContributor(this.parseFabricModPersonWithDescription(
                        author.getAsJsonObject(), "Author"
                    ));
                }
            }
        }

        JsonElement contributors = rootObject.get("contributors");
        if (contributors != null)  {
            for (JsonElement contributor : contributors.getAsJsonArray()) {
                if (contributor.isJsonPrimitive()) {
                    builder.addContributor(this.parseFabricModPersonWithDescription(
                        contributor.getAsString(), "Contributor"
                    ));
                } else {
                    builder.addContributor(this.parseFabricModPersonWithDescription(
                        contributor.getAsJsonObject(), "Contributor"
                    ));
                }
            }
        }

        var dependencies = new HashSet<StandardPluginDependency>();

        JsonElement depends = rootObject.get("depends");
        if (depends != null) {
            dependencies.addAll(this.parseFabricModDependency(depends.getAsJsonObject(), false));
        }

        JsonElement recommends = rootObject.get("recommends");
        if (recommends != null) {
            dependencies.addAll(this.parseFabricModDependency(recommends.getAsJsonObject(), true));
        }

        builder.dependencies(dependencies);

        return builder.build();
    }

    private StandardPluginLinks parseFabricModLinks(JsonObject contactInfoObject) {
        var builder = StandardPluginLinks.builder();

        try {

            JsonElement homepageLink = contactInfoObject.get("homepage");
            if (homepageLink != null) {
                builder.homepage(URI.create(homepageLink.getAsString()).toURL());
            }

            JsonElement issuesLink = contactInfoObject.get("issues");
            if (issuesLink != null) {
                builder.issues(URI.create(issuesLink.getAsString()).toURL());
            }

            JsonElement sourcesLink = contactInfoObject.get("sources");
            if (sourcesLink != null) {
                builder.source(URI.create(sourcesLink.getAsString()).toURL());
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Fabric mod had a malformed URL!", e);
        }

        return builder.build();
    }

    private StandardPluginContributor parseFabricModPersonWithDescription(String name, String description) {
        var builder = StandardPluginContributor.builder();

        builder.name(name);
        builder.description(description);

        return builder.build();
    }

    private StandardPluginContributor parseFabricModPersonWithDescription(JsonObject personObject, String description) {
        var builder = StandardPluginContributor.builder();

        builder.name(personObject.get("name").getAsString());
        builder.description(description);

        return builder.build();
    }

    private Set<StandardPluginDependency> parseFabricModDependency(JsonObject dependenciesObject, boolean optional) {
        var dependencies = new HashSet<StandardPluginDependency>();

        dependenciesObject.entrySet().forEach((entry) -> {
            // Sponge has no concept of a java dependency
            if (entry.getKey().equals("java")) return;
            var builder = StandardPluginDependency.builder();

            // Sponge does not support - in ids
            builder.id(entry.getKey().replace("-", "_"));
            builder.version(entry.getValue().getAsString());
            builder.optional(optional);

            dependencies.add(builder.build());
        });

        return dependencies;
    }
}
