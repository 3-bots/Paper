package com.spectatorpossession.plugin;

import com.spectatorpossession.plugin.AbilityRegistry.AbilityDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.List;

/** Live sidebar (top-right of screen) listing a possessed mob's abilities and which key triggers each one. */
public final class AbilitySidebar {

    private AbilitySidebar() {
    }

    public static void show(Player player, Mob mob, List<AbilityDefinition> abilities) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("possession", Criteria.DUMMY,
                Component.text("Possessing").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String mobName = mob.getType().name().toLowerCase().replace('_', ' ');
        int score = abilities.isEmpty() ? 2 : abilities.size() + 2;

        objective.getScore(ChatColor.GRAY + mobName).setScore(score--);
        objective.getScore(" ").setScore(score--);

        if (abilities.isEmpty()) {
            objective.getScore(ChatColor.DARK_GRAY + "No abilities").setScore(score--);
        } else {
            for (AbilityDefinition ability : abilities) {
                String line = ChatColor.YELLOW + String.valueOf(ability.slot()) + ChatColor.GRAY + ": " + ChatColor.WHITE + ability.name();
                objective.getScore(line).setScore(score--);
            }
        }

        player.setScoreboard(scoreboard);
    }

    public static void hide(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }
    }
}
