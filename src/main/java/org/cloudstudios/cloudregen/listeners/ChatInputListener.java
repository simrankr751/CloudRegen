package org.cloudstudios.cloudregen.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.gui.ChatInputService;

public final class ChatInputListener implements Listener {
    private final ChatInputService inputService;
    private final MessageManager messages;

    public ChatInputListener(ChatInputService inputService, MessageManager messages) {
        this.inputService = inputService;
        this.messages = messages;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!inputService.hasPending(event.getPlayer().getUniqueId())) {
            return;
        }
        if ("cancel".equalsIgnoreCase(event.getMessage())) {
            inputService.consume(event.getPlayer().getUniqueId(), "cancel");
            event.getPlayer().sendMessage(messages.format("prefix") + messages.get("input-cancelled"));
            event.setCancelled(true);
            return;
        }
        if (inputService.consume(event.getPlayer().getUniqueId(), event.getMessage())) {
            event.setCancelled(true);
        }
    }
}
