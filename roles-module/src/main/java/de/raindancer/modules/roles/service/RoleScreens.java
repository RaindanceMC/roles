package de.raindancer.modules.roles.service;

import de.raindancer.core.ui.hologram.ScreenKind;
import de.raindancer.core.ui.hologram.Screens;
import de.raindancer.modules.roles.model.Ability;
import de.raindancer.modules.roles.model.Perk;
import de.raindancer.modules.roles.model.Role;
import de.raindancer.modules.roles.rules.ScreenLineRule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** The roles' screens for Core's {@code /screen}: everybody's own role with what it gives them, and who plays what. */
public final class RoleScreens {

    public static final List<String> KINDS = List.of("role-you", "role-online");
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final int SHOWN = 9;
    private static final int LINES = 3;

    private final RoleService roles;
    private final Server server;
    private final Predicate<UUID> vanished;
    private final ScreenLineRule rule = new ScreenLineRule();

    public RoleScreens(RoleService roles, Server server, Predicate<UUID> vanished) {
        this.roles = roles;
        this.server = server;
        this.vanished = vanished;
    }

    public void register(Screens screens, Plugin owner) {
        screens.register(owner, ScreenKind.personal("role-you", "Your role",
                "Everybody's own role, how far its perks have grown and what they give.", Material.GOLDEN_PICKAXE,
                this::you));
        screens.register(owner, ScreenKind.everyone("role-online", "Roles online",
                "How many players online play each role.", Material.BOOKSHELF, this::online));
    }

    private Component you(ScreenKind.Viewer viewer) {
        var held = roles.roleOf(viewer.id());
        String title = "<gold><bold>Your role</bold></gold>";
        if (held.isEmpty()) {
            return MINI.deserialize(title + "\n<gray>None yet — <white>/role");
        }
        Role role = held.get();
        StringBuilder text = new StringBuilder(title).append('\n').append(role.coloured());
        text.append('\n').append(rule.strength(roles.strength(viewer.id()), roles.untilFull(viewer.id())));
        for (Perk perk : role.perks().stream().limit(LINES).toList()) {
            append(text, rule.line(perk.says(roles.perkNow(viewer.id(), perk.percent())),
                    roles.perkNow(viewer.id(), perk.percent())));
        }
        for (Ability ability : role.abilities().stream().limit(LINES).toList()) {
            append(text, rule.line(ability.says(roles.abilityNow(viewer.id(), ability)),
                    roles.abilityNow(viewer.id(), ability)));
        }
        return MINI.deserialize(text.toString());
    }

    private static void append(StringBuilder text, String line) {
        if (!line.isEmpty()) {
            text.append('\n').append(line);
        }
    }

    private Component online() {
        String title = "<gold><bold>Roles online</bold></gold>";
        List<Role> held = server.getOnlinePlayers().stream().filter(player -> !vanished.test(player.getUniqueId()))
                .map(player -> roles.roleOf(player.getUniqueId())).flatMap(java.util.Optional::stream).toList();
        var counts = rule.counts(held, roles.roles());
        if (counts.isEmpty()) {
            return MINI.deserialize(title + "\n<gray>Nobody online has a role");
        }
        StringBuilder text = new StringBuilder(title);
        counts.stream().limit(SHOWN).forEach(count -> text.append('\n').append(count.role().coloured())
                .append(" <white>").append(count.online()));
        return MINI.deserialize(text.toString());
    }
}
