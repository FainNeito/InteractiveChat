import com.loohp.interactivechat.listeners.packet.TooltipBridge;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public class RankFixtureTest {
    public static void main(String[] args) throws Exception {
        int tested = 0;
        for (String line : Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8)) {
            String[] fields = line.split("\\t", 2);
            String prefix = new String(Base64.getDecoder().decode(fields[1]), StandardCharsets.UTF_8);
            var component = TooltipBridge.render(prefix, Audience.empty(), TagResolver.empty());
            var expectedComponent = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(prefix);
            String expectedJson = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(expectedComponent);
            String actualJson = GsonComponentSerializer.gson().serialize(component);
            if (!com.google.gson.JsonParser.parseString(expectedJson).equals(com.google.gson.JsonParser.parseString(actualJson)))
                throw new AssertionError(fields[0] + ": rendered component differs from the original format\nexpected=" + expectedJson + "\nactual=" + actualJson);
            String plain = PlainTextComponentSerializer.plainText().serialize(component);
            String expected = prefix.replaceAll("<[^>]*>", "");
            if (!expected.equals(plain)) throw new AssertionError(fields[0] + ": " + plain);
            String json = GsonComponentSerializer.gson().serialize(component).toLowerCase(Locale.ROOT);
            var m = Pattern.compile("<#[0-9a-fA-F]{6}>").matcher(prefix);
            while (m.find()) {
                String color = m.group().substring(1, 8).toLowerCase(Locale.ROOT);
                if (!json.contains(color) && !(color.equals("#ffffff") && json.contains("\"white\"")))
                    throw new AssertionError(fields[0] + " missing color " + m.group());
            }
            tested++;
        }
        System.out.println("PASS: " + tested + " configured direct rank prefixes");
    }
}
