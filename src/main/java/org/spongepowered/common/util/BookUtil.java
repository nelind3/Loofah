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
package org.spongepowered.common.util;

import net.kyori.adventure.inventory.Book;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collection;

public final class BookUtil {

    public static final int PLAYER_INVENTORY = -2;

    public static void fakeBookView(final Book book, final Collection<? extends Player> players) {
        final ItemStack item = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        item.offer(Keys.CUSTOM_NAME, book.title());
        item.offer(Keys.AUTHOR, book.author());
        item.offer(Keys.PAGES, book.pages());

        for (final Player player : players) {
            final ServerPlayer mcPlayer = (ServerPlayer) player;
            final ServerGamePacketListenerImpl receiver = mcPlayer.connection;

            final Inventory inventory = mcPlayer.getInventory();
            final int bookSlot = inventory.selected;
            final net.minecraft.world.item.ItemStack oldItem = inventory.getSelected();

            // First we need to send a fake a Book ItemStack with the BookView's
            // contents to the player's hand
            receiver.send(new ClientboundContainerSetSlotPacket(BookUtil.PLAYER_INVENTORY, 0, bookSlot, ItemStackUtil.toNative(item)));

            // Next we tell the client to open the Book GUI
            receiver.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND));

            // Now we can remove the fake Book since it's contents will have already
            // been transferred to the GUI
            receiver.send(new ClientboundContainerSetSlotPacket(BookUtil.PLAYER_INVENTORY, 0, bookSlot, oldItem));
        }
    }

    private BookUtil() {
    }
}
