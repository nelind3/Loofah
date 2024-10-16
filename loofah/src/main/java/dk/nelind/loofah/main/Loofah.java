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
package dk.nelind.loofah.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Client;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.packet.SpongePacketHandler;

/**
 * Lifecycle registrations and calls based on {@link org.spongepowered.forge.SpongeForgeMod}
 * since what events are and aren't called by SpongeCommon is ... not straight forward
 */
public class Loofah implements ModInitializer, ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Loofah");

    @Override
    public void onInitialize() {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callConstructEvent();
        lifecycle.callRegisterFactoryEvent();
        lifecycle.callRegisterBuilderEvent();
        lifecycle.callRegisterChannelEvent();
        lifecycle.establishGameServices();
        lifecycle.establishDataKeyListeners();

        SpongePacketHandler.init((SpongeChannelManager) Sponge.channelManager());
        this.registerLifecycleEvents();
    }

    @Override
    public void onInitializeClient() {
        final Client minecraft = (Client) Minecraft.getInstance();
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.establishClientRegistries(minecraft);
        lifecycle.callStartingEngineEvent(minecraft);

        Loofah.LOGGER.info("Loofah v{} initialized on Client", Launch.instance().platformPlugin().metadata().version());
    }

    private void registerLifecycleEvents() {
        final Lifecycle lifecycle = Launch.instance().lifecycle();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Save config now that registries have been initialized
            ConfigHandle.setSaveSuppressed(false);

            lifecycle.establishServerServices();
            lifecycle.establishServerFeatures();
            lifecycle.establishServerRegistries((Server) server);
            lifecycle.callStartingEngineEvent((Server) server);

            Loofah.LOGGER.info("Loofah v{} initialized on Server", Launch.instance().platformPlugin().metadata().version());
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            lifecycle.callStartedEngineEvent((Server) server);
            lifecycle.callLoadedGameEvent();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            lifecycle.callStoppingEngineEvent((Server) server);
        });
    }
}
