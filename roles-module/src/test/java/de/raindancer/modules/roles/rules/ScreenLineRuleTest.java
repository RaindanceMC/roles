package de.raindancer.modules.roles.rules;

import de.raindancer.modules.roles.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("What the roles' screens count and say")
class ScreenLineRuleTest {

    private final ScreenLineRule rule = new ScreenLineRule();

    private static Role role(String id, String title) {
        return new Role(id, title, "STONE", "#e8a33d", List.of(), List.of());
    }

    private final Role cook = role("cook", "Cook");
    private final Role miner = role("miner", "Miner");
    private final Role farmer = role("farmer", "Farmer");

    @Test
    @DisplayName("players online per role: most first, then by title; roles nobody online has are left out")
    void counts() {
        var counts = rule.counts(List.of(miner, cook, miner, cook, farmer), List.of(cook, miner, farmer, role("x", "Unused")));
        assertThat(counts).extracting(count -> count.role().id()).containsExactly("cook", "miner", "farmer");
        assertThat(counts).extracting(ScreenLineRule.Count::online).containsExactly(2, 2, 1);
        assertThat(rule.counts(List.of(), List.of(cook))).isEmpty();
    }

    @Test
    @DisplayName("how far the perks have grown: a percentage and what is left, or full")
    void strength() {
        assertThat(rule.strength(0.6, Duration.ofDays(3))).isEqualTo("<gray>Perks <white>60%</white> <dark_gray>· full in 3d");
        assertThat(rule.strength(1.0, Duration.ZERO)).isEqualTo("<gray>Perks <green>at full strength");
    }

    @Test
    @DisplayName("a perk or ability line is white; one that does nothing now is not shown")
    void lines() {
        assertThat(rule.line("6% off buying food", 6)).isEqualTo("<white>6% off buying food");
        assertThat(rule.line("anything", 0)).isEmpty();
    }
}
