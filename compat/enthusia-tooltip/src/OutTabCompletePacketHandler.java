/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.libs.com.loohp.platformscheduler.Scheduler;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class OutTabCompletePacketHandler {

    public static final AtomicReference<Map<String, UUID>> playerNames = new AtomicReference<>(Collections.emptyMap());

    public static void init() {
        schedulePlayerNamesUpdate();
    }

    private static void schedulePlayerNamesUpdate() {
        Scheduler.runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            if (InteractiveChat.useTooltipOnTab) {
                Map<String, UUID> names = new HashMap<>();
                for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                    addPlayerNames(names, player);
                }
                Scheduler.runTask(InteractiveChat.plugin, () -> playerNames.set(names));
            }
        }, 0, 100);
    }

    private static void addPlayerNames(Map<String, UUID> playernames, ICPlayer player) {
        playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
        if (!player.getName().equals(player.getDisplayName())) {
            playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
        }
        List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
        for (String name : names) {
            playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
        }
    }

    public static ICPlayer findICPlayer(String text) {
        for (Map.Entry<String, UUID> entry : playerNames.get().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(text)) {
                return ICPlayerFactory.getICPlayer(entry.getValue());
            }
        }
        return null;
    }

    public static Component createComponent(ICPlayer icplayer, Player tabCompleter) {
        Component component = ComponentReplacing.replace(InteractiveChat.tabTooltip, "(?i)%vault_prefix%", false, (match, original) -> {
            String token = match.group();
            String prefix = PlaceholderParser.parse(icplayer, token);
            if (token.equals(prefix)) return Component.empty().children(original);
            try {
                org.bukkit.plugin.Plugin nexo = org.bukkit.Bukkit.getPluginManager().getPlugin("Nexo");
                net.kyori.adventure.text.minimessage.tag.resolver.TagResolver resolver = net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.empty();
                java.util.function.UnaryOperator<net.kyori.adventure.text.Component> shorthand = java.util.function.UnaryOperator.identity();
                if (nexo != null && nexo.isEnabled()) {
                    Class<?> glyphTag = nexo.getClass().getClassLoader().loadClass("com.nexomc.nexo.glyphs.GlyphTag");
                    Object instance = glyphTag.getField("INSTANCE").get(null);
                    resolver = (net.kyori.adventure.text.minimessage.tag.resolver.TagResolver) glyphTag.getMethod("getRESOLVER").invoke(instance);
                    Object manager = nexo.getClass().getMethod("fontManager").invoke(nexo);
                    Object glyphConfigs = manager.getClass().getMethod("getGlyphConfigs").invoke(manager);
                    java.lang.reflect.Method replace = glyphConfigs.getClass().getMethod("replaceSplitPlaceholders", net.kyori.adventure.text.Component.class, Player.class);
                    shorthand = input -> {
                        try {
                            return (net.kyori.adventure.text.Component) replace.invoke(glyphConfigs, input, tabCompleter);
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException("Nexo shorthand rendering failed", e);
                        }
                    };
                }
                return TooltipBridge.render(prefix, tabCompleter, resolver, shorthand);
            } catch (RuntimeException | ReflectiveOperationException | LinkageError e) {
                InteractiveChat.plugin.getLogger().warning("Could not render tab-completion prefix: " + e.getClass().getSimpleName());
                return com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(prefix);
            }
        });
        component = PlaceholderParser.parse(icplayer, component);
        if (!PlayerUtils.canChatColor(tabCompleter)) {
            component = ComponentStyling.stripColor(component);
        }
        return component;
    }
}

