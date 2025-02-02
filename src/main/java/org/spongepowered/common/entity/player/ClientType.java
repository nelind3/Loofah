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
package org.spongepowered.common.entity.player;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClientType {
    private static final Map<String, ClientType> lookup = new HashMap<>();

    public static final ClientType VANILLA = new ClientType("vanilla");
    public static final ClientType SPONGE_VANILLA = new ClientType("sponge_vanilla");
    public static final ClientType FORGE = new ClientType("forge");
    public static final ClientType SPONGE_FORGE = new ClientType("sponge_forge");
    // Loofah start
    public static final ClientType LOOFAH = new ClientType("loofah");
    // Loofah end

    private final String name;

    public ClientType(final String name) {
        this.name = name;
        ClientType.lookup.put(name, this);
    }

    public static @Nullable ClientType from(final String value) {
        return ClientType.lookup.get(value);
    }

    public String getName() {
        return this.name;
    }
}
