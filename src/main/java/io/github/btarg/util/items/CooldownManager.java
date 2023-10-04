package io.github.btarg.util.items;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager implements Listener {
    private static final Map<Player, Map<String, Integer>> cooldownMap = new HashMap<>();

    public static void addCooldown(Player player, String cooldownId, int cooldownTicks) {
        cooldownMap.computeIfAbsent(player, k -> new HashMap<>()).put(cooldownId, cooldownTicks);
    }

    public static int getCooldown(Player player, String cooldownId) {
        Map<String, Integer> playerCooldowns = cooldownMap.get(player);
        if (playerCooldowns != null && playerCooldowns.containsKey(cooldownId)) {
            return playerCooldowns.get(cooldownId);
        }
        return 0; // No cooldown found for the specified id.
    }

    public static boolean hasCooldown(Player player, String cooldownId) {
        Map<String, Integer> playerCooldowns = cooldownMap.get(player);
        return playerCooldowns != null && playerCooldowns.containsKey(cooldownId) && playerCooldowns.get(cooldownId) > 0;
    }

    @EventHandler
    public void onTick(ServerTickEndEvent e) {
        for (Map<String, Integer> playerCooldowns : cooldownMap.values()) {
            playerCooldowns.entrySet().removeIf(entry -> {
                int remainingTicks = entry.getValue();
                if (remainingTicks <= 0) {
                    return true; // Remove expired cooldowns.
                } else {
                    entry.setValue(remainingTicks - 1); // Decrease remaining ticks.
                    return false;
                }
            });
        }
    }
}
