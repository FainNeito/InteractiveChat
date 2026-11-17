package com.loohp.interactivechat.listeners.packet;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts only the trusted Vault prefix; player names remain ordinary text. */
public final class TooltipBridge {
    private static final Pattern LEGACY = Pattern.compile("(?i)§x(?:§[0-9a-f]){6}|§[0-9a-fk-or]");
    private static final String[] COLORS = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};

    public static com.loohp.interactivechat.libs.net.kyori.adventure.text.Component render(String value, Audience audience, TagResolver glyphs) {
        return render(value, audience, glyphs, java.util.function.UnaryOperator.identity());
    }

    public static com.loohp.interactivechat.libs.net.kyori.adventure.text.Component render(String value, Audience audience, TagResolver glyphs, java.util.function.UnaryOperator<net.kyori.adventure.text.Component> shorthand) {
        MiniMessage mini = MiniMessage.builder().tags(TagResolver.resolver(StandardTags.color(), StandardTags.decorations(), StandardTags.gradient(), StandardTags.rainbow(), StandardTags.reset(), StandardTags.font(), glyphs)).build();
        var parsed = shorthand.apply(mini.deserialize(legacyToMini(value), audience));
        String json = GsonComponentSerializer.gson().serialize(parsed);
        return com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(json);
    }

    static String legacyToMini(String input) {
        Matcher matcher = LEGACY.matcher(input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String code = matcher.group();
            char c = Character.toLowerCase(code.charAt(1));
            String tag;
            if (c == 'x') {
                StringBuilder hex = new StringBuilder("<reset><#");
                for (int i = 3; i < code.length(); i += 2) hex.append(code.charAt(i));
                tag = hex.append('>').toString();
            } else if (Character.digit(c, 16) >= 0) {
                tag = "<reset><" + COLORS[Character.digit(c, 16)] + ">";
            } else {
                tag = switch (c) {
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    default -> "<reset>";
                };
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(tag));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
