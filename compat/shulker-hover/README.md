# Oversized shulker item hovers

This standalone Bukkit plugin runs after InteractiveChat builds an outgoing message. When its existing packet guard is enabled and a message containing a shulker item hover exceeds the limit, the plugin retains the hover item type and count. It preserves the click action and the full item inventory cached by InteractiveChat. The final packet guard continues to enforce the limit.

From this directory, compile src/enthusia/chat/ShulkerHover.java with javac --release 21 -proc:none -d classes against the deployed InteractiveChat 2026.1.1.0 JAR and Paper API dependencies. Package with `jar cf ShulkerHover-1.0.0.jar -C classes . -C . plugin.yml`. Install alongside InteractiveChat. The SHA-256 of the SMP Test JAR is 72CC25EFF2FF30443703009DD23D001A99485231082A45C7745D27A6195FCED4.

The SMP Test fixture measured 42175 characters before compaction and 2959 afterward against a 32767-character limit. A normal sword kept its full lore. Clicking the box opened all 27 original slots. Unrelated oversized text was still subject to the guard.
