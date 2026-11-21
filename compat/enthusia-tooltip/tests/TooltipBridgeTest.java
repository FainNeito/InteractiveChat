import com.loohp.interactivechat.listeners.packet.TooltipBridge;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TooltipBridgeTest {
    private static final TagResolver GLYPH = TagResolver.resolver("glyph", (args, ctx) -> Tag.selfClosingInserting(
        net.kyori.adventure.text.Component.text("뀄").font(net.kyori.adventure.key.Key.key("nexo:icons"))));
    private static void check(boolean condition, String name) { if (!condition) throw new AssertionError(name); }
    private static String plain(Component c) { return PlainTextComponentSerializer.plainText().serialize(c); }
    private static String json(Component c) { return GsonComponentSerializer.gson().serialize(c).toLowerCase(java.util.Locale.ROOT); }
    public static void main(String[] args) {
        Component rank = TooltipBridge.render("<gold>✦ <#E99804><bold>[Founder]</bold><reset> ", Audience.empty(), GLYPH);
        check(plain(rank).equals("✦ [Founder] "), "rank plain text");
        check(json(rank).contains("#e99804"), "exact rank hex");
        Component nested = TooltipBridge.render("<gradient:#ff0000:#00ff00><bold>Glorious</bold></gradient>", Audience.empty(), GLYPH);
        check(plain(nested).equals("Glorious") && !json(nested).contains("gradient"), "gradient");
        Component mixed = TooltipBridge.render("§x§E§9§9§8§0§4Rank§r <gray>Member", Audience.empty(), GLYPH);
        check(plain(mixed).equals("Rank Member") && json(mixed).contains("#e99804"), "legacy hex");
        Component icon = TooltipBridge.render("<glyph:enthusia_logo> <white>Member", Audience.empty(), GLYPH);
        check(plain(icon).equals("뀄 Member") && json(icon).contains("nexo:icons"), "custom font");
        Component shorthand = TooltipBridge.render(":enthusia: <white>Member", Audience.empty(), GLYPH,
            input -> input.replaceText(net.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral(":enthusia:")
                .replacement(net.kyori.adventure.text.Component.text("뀄").font(net.kyori.adventure.key.Key.key("nexo:icons"))).build()));
        check(plain(shorthand).equals("뀄 Member") && json(shorthand).contains("nexo:icons"), "Nexo shortcode");
        Component unsafe = TooltipBridge.render("<click:run_command:'/op x'>x</click>", Audience.empty(), GLYPH);
        check(!json(unsafe).contains("clickevent"), "action tag rejected");
        System.out.println("PASS: rank, gradient, legacy, custom font, action isolation");
    }
}
