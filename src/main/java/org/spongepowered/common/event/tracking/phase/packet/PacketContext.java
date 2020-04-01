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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class PacketContext<P extends PacketContext<P>> extends PhaseContext<P> {

    @SuppressWarnings("NullableProblems") protected ServerPlayerEntity packetPlayer; // Set by packetPlayer(EntityPlayerMP)
    @Nullable IPacket<?> packet;
    private ItemStackSnapshot cursor = ItemStackSnapshot.empty();
    private ItemStack itemUsed = ItemStack.empty();
    private ItemStackSnapshot itemUsedSnapshot = ItemStackSnapshot.empty();
    @Nullable private HandType handUsed;
    private boolean ignoreCreative;
    private boolean interactItemChanged;

    protected PacketContext(final PacketState<? extends P> state, final PhaseTracker tracker) {
        super(state, tracker);
    }

    public P packet(final IPacket<?> packet) {
        this.packet = packet;
        return (P) this;
    }

    public P packetPlayer(final ServerPlayerEntity playerMP) {
        this.packetPlayer = playerMP;
        return (P) this;
    }

    public P cursor(final ItemStackSnapshot snapshot) {
        this.cursor = snapshot;
        return (P) this;
    }

    public P ignoreCreative(final boolean creative) {
        this.ignoreCreative = creative;
        return (P) this;
    }

    public ServerPlayerEntity getPacketPlayer() {
        return this.packetPlayer;
    }

    @SuppressWarnings("ConstantConditions")
    public Player getSpongePlayer() {
        return (Player) this.packetPlayer;
    }

    public <K extends IPacket<?>> K getPacket() {
        return (K) this.packet;
    }

    public ItemStackSnapshot getCursor() {
        return this.cursor;
    }

    public boolean getIgnoringCreative() {
        return this.ignoreCreative;
    }

    public P itemUsed(final ItemStack stack) {
        this.itemUsed = stack;
        this.itemUsedSnapshot = this.itemUsed.createSnapshot();
        return (P) this;
    }

    public ItemStack getItemUsed() {
        return this.itemUsed;
    }

    public ItemStackSnapshot getItemUsedSnapshot() {
        return this.itemUsedSnapshot;
    }

    public P interactItemChanged(final boolean changed) {
        this.interactItemChanged = changed;
        return (P) this;
    }

    public boolean getInteractItemChanged() {
        return this.interactItemChanged;
    }

    public P handUsed(final HandType hand) {
        this.handUsed = hand;
        return (P) this;
    }

    public HandType getHandUsed() {
        return this.handUsed;
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$"+indent+"s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "PacketPlayer", this.packetPlayer)
            .add(s + "- %s: %s", "Packet", this.packet)
            .add(s + "- %s: %s", "IgnoreCreative", this.ignoreCreative)
            .add(s + "- %s: %s", "ItemStackUsed", this.itemUsed);

    }

    @Override
    protected void reset() {
        super.reset();
        this.packetPlayer = null;
        this.packet = null;
        this.cursor = ItemStackSnapshot.empty();
        this.itemUsed = ItemStack.empty();
        this.itemUsedSnapshot = ItemStackSnapshot.empty();
        this.handUsed = null;
        this.ignoreCreative = false;
        this.interactItemChanged = false;
    }
}
