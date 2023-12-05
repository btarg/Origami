package io.github.btarg.events;

import io.github.btarg.util.items.CommandRunner;
import io.github.btarg.util.items.CooldownManager;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EventDefinition {
    @Getter
    private int eventCooldown;
    @Getter
    private String eventName;
    @Getter
    private List<String> commands;

    public EventDefinition(String eventName, List<String> commands, int cooldown) {
        this.eventName = eventName;
        this.commands = commands;
        this.eventCooldown = cooldown;
    }

    public static EventDefinition deserialize(Map<String, Object> map) {
        String eventName = map.keySet().iterator().next();
        Map<String, Object> eventData = (Map<String, Object>) map.get(eventName);

        List<String> commands = (List<String>) eventData.get("commands");
        int cooldown = (Integer) eventData.getOrDefault("cooldown", 0);

        return new EventDefinition(eventName, commands, cooldown);
    }

    public void executeCommands(Player player) {
        if (CooldownManager.getCooldown(player, eventName + "_cooldown") > 0) {
            return;
        }
        String playerSelector = (player != null) ? player.getUniqueId().toString() : null;
        CommandRunner.runCommands(commands, playerSelector);

        if (eventCooldown > 0) {
            CooldownManager.addCooldown(player, eventName + "_cooldown", eventCooldown);
        }
    }


    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> eventData = new HashMap<>();

        eventData.put("commands", this.commands);
        if (this.eventCooldown != 0) {
            eventData.put("cooldown", this.eventCooldown);
        }

        map.put(this.eventName, eventData);
        return map;
    }
}

