package com.spectatorpossession.plugin;

import com.spectatorpossession.plugin.AbilityRegistry.AbilityDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

/** Builds the popup book shown when a player starts possessing a mob, listing its controls. */
public final class ControlsBook {

    private ControlsBook() {
    }

    public static ItemStack build(Mob mob, List<AbilityDefinition> abilities) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        String mobName = mob.getType().name().toLowerCase().replace('_', ' ');

        meta.setTitle("Possessing: " + mobName);
        meta.setAuthor("SpectatorPossession");

        StringBuilder page = new StringBuilder();
        page.append("You are now possessing a ").append(mobName).append(".\n\n");
        page.append("Fly to move it.\n");
        page.append("Swap hands to release it.\n\n");

        if (abilities.isEmpty()) {
            page.append("This mob has no abilities.");
        } else {
            page.append("Abilities:\n");
            for (AbilityDefinition ability : abilities) {
                page.append(ability.slot()).append(": ").append(ability.name())
                        .append(" - ").append(ability.description()).append("\n");
            }
        }

        meta.setPages(page.toString());
        book.setItemMeta(meta);
        return book;
    }
}
