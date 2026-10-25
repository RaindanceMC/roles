package de.raindancer.modules.roles.rules;

import de.raindancer.core.world.time.Times;
import de.raindancer.modules.roles.model.Role;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/** What the roles' screens count and say. */
public final class ScreenLineRule implements IRolesRule {

    /** Screen text goes straight to MiniMessage, never through a message argument. */
    private static final String FULL = "<gray>Perks <green>at full strength";

    public record Count(Role role, int online) {
    }

    /** @param held the role of each player online who has one */
    public List<Count> counts(List<Role> held, List<Role> roles) {
        return roles.stream().map(role -> new Count(role, (int) held.stream().filter(each -> each.id().equals(role.id())).count()))
                .filter(count -> count.online() > 0)
                .sorted(Comparator.comparingInt(Count::online).reversed().thenComparing(count -> count.role().title()))
                .toList();
    }

    public String strength(double strength, Duration untilFull) {
        if (untilFull.isZero() || strength >= 1.0) {
            return FULL;
        }
        return "<gray>Perks <white>" + Math.round(strength * 100) + "%</white> <dark_gray>· full in "
                + Times.brief(untilFull);
    }

    /** One perk or ability as a line; empty for one that does nothing right now. */
    public String line(String says, int now) {
        return now == 0 ? "" : "<white>" + MiniMessage.miniMessage().escapeTags(says);
    }

    @Override
    public String describe() {
        return "what the roles' screens count and say";
    }
}
