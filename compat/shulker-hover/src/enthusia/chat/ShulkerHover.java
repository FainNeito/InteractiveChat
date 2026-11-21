package enthusia.chat;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.*;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.event.HoverEvent;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import java.util.ArrayList;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkerHover extends JavaPlugin implements Listener {
    @Override public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPacket(PostPacketComponentProcessEvent event) {
        if (!InteractiveChat.sendOriginalIfTooLong) return;
        Component original = event.getComponent();
        for (Component part : original.iterable(ComponentIteratorType.DEPTH_FIRST,
                ComponentIteratorFlag.INCLUDE_TRANSLATABLE_COMPONENT_ARGUMENTS)) {
            if (!hasShulkerHover(part)) continue;
            var serializer = InteractiveChat.version.isLegacyRGB()
                    ? InteractiveChatComponentSerializer.legacyGson() : InteractiveChatComponentSerializer.gson();
            if (serializer.serialize(original).length() > InteractiveChat.packetStringMaxLength) {
                event.setComponent(compact(original));
            }
            return;
        }
    }

    private static boolean hasShulkerHover(Component part) {
        var hover = part.hoverEvent();
        if (hover == null || !(hover.value() instanceof HoverEvent.ShowItem item)) return false;
        String id = item.item().value();
        return item.item().namespace().equals("minecraft") && (id.equals("shulker_box") || id.endsWith("_shulker_box"))
                && (item.nbt() != null || !item.dataComponents().isEmpty());
    }

    private static Component compact(Component part) {
        if (hasShulkerHover(part)) {
            var item = (HoverEvent.ShowItem) part.hoverEvent().value();
            part = part.hoverEvent(HoverEvent.showItem(item.item(), item.count()));
        }
        if (part instanceof TranslatableComponent translation) {
            var args = new ArrayList<ComponentLike>(translation.arguments());
            boolean changed = false;
            for (int i = 0; i < args.size(); i++) {
                if (translation.arguments().get(i).value() instanceof Component old) {
                    Component next = compact(old);
                    if (old != next) {
                        args.set(i, next);
                        changed = true;
                    }
                }
            }
            if (changed) part = translation.arguments(args);
        }
        var children = part.children();
        ArrayList<Component> replaced = null;
        for (int i = 0; i < children.size(); i++) {
            Component old = children.get(i);
            Component next = compact(old);
            if (old != next) {
                if (replaced == null) replaced = new ArrayList<>(children);
                replaced.set(i, next);
            }
        }
        return replaced == null ? part : part.children(replaced);
    }
}
