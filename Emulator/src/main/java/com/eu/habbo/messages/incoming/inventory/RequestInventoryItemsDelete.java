package com.eu.habbo.messages.incoming.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItems;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class RequestInventoryItemsDelete extends MessageHandler {
    public int getRatelimit() {
        return 500;
    }

    public void handle() {
        int itemId = this.packet.readInt();
        int amount = this.packet.readInt();
        HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(itemId);
        if (habboItem == null)
            return;
        Item item = habboItem.getBaseItem();
        if (item == null)
            return;
        if (!hasFurnitureInInventory(this.client.getHabbo(), item, Math.abs(amount)))
            return;
        final Habbo habbo = this.client.getHabbo();
        if (habbo == null)
            return;
        TIntObjectHashMap<HabboItem> toRemove = new TIntObjectHashMap();
        for (int i = 0; i < Math.abs(amount); i++) {
            HabboItem habboInventoryItem = habbo.getInventory().getItemsComponent().getAndRemoveHabboItem(item);
            if (habboInventoryItem != null)
                toRemove.put(habboInventoryItem.getId(), habboInventoryItem);
        }
        toRemove.forEachValue(object -> {
            habbo.getClient().sendResponse(new RemoveHabboItemComposer(object.getGiftAdjustedId()));
            return true;
        });
        habbo.getClient().sendResponse(new InventoryRefreshComposer());
        Emulator.getThreading().run(new QueryDeleteHabboItems(toRemove));
    }

    private boolean hasFurnitureInInventory(Habbo habbo, Item item, Integer amount) {
        int count = 0;
        for (TObjectHashIterator<HabboItem> tObjectHashIterator = habbo.getInventory().getItemsComponent().getItemsAsValueCollection().iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem habboItem = tObjectHashIterator.next();
            if (habboItem.getBaseItem().getId() == item.getId())
                count++;
        }
        if (count < amount)
            return false;
        return true;
    }
}