package io.github.btarg.definitions.base;

import io.github.btarg.events.EventDefinition;
import io.github.btarg.util.ComponentHelper;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BaseCustomDefinition extends AbstractBaseDefinition {

    public String contentPack;
    public String id;
    public String displayName;
    public List<String> lore;
    public String model;
    public Material baseMaterial;

    @Getter
    private List<EventDefinition> events;

    public BaseCustomDefinition(Map<String, Object> map) {

        String baseMaterialString = (String) map.getOrDefault("baseMaterial", map.getOrDefault("baseBlock", map.get("baseItem")));
        this.baseMaterial = (baseMaterialString != null) ? Material.matchMaterial(baseMaterialString.trim().toUpperCase()) : null;

        this.model = (String) map.get("model"); // allow null models for default base material instead
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Item");

        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());
        // Deserialize events
        this.events = deserializeEvents(map);
    }

    public Component getDisplayName() {
        Component nameComponent = ComponentHelper.deserializeGenericComponent(displayName);
        return ComponentHelper.removeItalicsIfAbsent(nameComponent);
    }

    public List<Component> getLore() {
        List<Component> toReturn = new ArrayList<>();
        lore.forEach(loreString -> {
            Component loreComponent = ComponentHelper.deserializeGenericComponent(loreString);
            toReturn.add(ComponentHelper.removeItalicsIfAbsent(loreComponent));
        });
        return toReturn;
    }

    private List<EventDefinition> deserializeEvents(Map<String, Object> map) {
        List<Map<String, Object>> eventsData = Optional.ofNullable((List<Map<String, Object>>) map.get("events"))
                .orElse(Collections.emptyList());

        return eventsData.stream()
                .map(EventDefinition::deserialize)
                .collect(Collectors.toList());
    }

    public void executeEvent(String eventName, Player player) {
        Optional<EventDefinition> optionalEvent = findEventByName(eventName);
        optionalEvent.ifPresent(eventDefinition -> eventDefinition.executeCommands(player));
    }

    private Optional<EventDefinition> findEventByName(String eventName) {
        return events.stream()
                .filter(event -> event.getEventName().equals(eventName))
                .findFirst();
    }

    public void addEvent(String eventName, List<String> commands, int cooldown) {
        events.add(new EventDefinition(eventName, commands, cooldown));
    }

    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("baseMaterial", this.baseMaterial.toString());
        map.put("displayName", this.displayName);
        map.put("lore", this.lore);
        map.put("model", this.model);

        // Serialize events
        List<Map<String, Object>> eventsData = new ArrayList<>();
        for (EventDefinition event : this.events) {
            eventsData.add(event.serialize());
        }
        map.put("events", eventsData);

        return map;
    }

    @Override
    public void registerDefinition(CommandSender sender) {
    }

    @Override
    public AbstractBaseDefinition getDefaultDefinition() {
        return null;
    }
}
