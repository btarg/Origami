package io.github.btarg.origami.definitions.base;

import io.github.btarg.origami.events.EventDefinition;
import io.github.btarg.origami.util.ComponentHelper;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class BaseCustomDefinition extends AbstractBaseDefinition {

    public String contentPack;
    public String id;
    public String displayName;
    public List<String> lore;
    public String model;
    public Material baseMaterial;
    public Boolean cancelBaseEvents;

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
        this.cancelBaseEvents = (Boolean) map.getOrDefault("cancelBaseEvents", true);
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
                .filter(event -> Optional.ofNullable(event).map(EventDefinition::getEventName).orElse("").equals(eventName))
                .findFirst();
    }

    public void addEvent(String eventName, List<String> commands, int cooldown) {
        events.add(new EventDefinition(eventName, commands, cooldown));
    }

    protected abstract Integer getCustomModelData();

    protected ItemMeta getItemMeta(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        Component name = this.getDisplayName();
        meta.displayName(name);
        meta.lore(this.getLore());
        meta.setCustomModelData(getCustomModelData());
        meta.getPersistentDataContainer().set(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING, this.contentPack);
        return meta;
    }

    public ItemStack createCustomItemStack(int count) {
        ItemStack itemStack = new ItemStack(this.baseMaterial, count);
        itemStack.setItemMeta(getItemMeta(itemStack));

        return itemStack;
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
        map.put("cancelBaseEvents", this.cancelBaseEvents);

        return map;
    }
}
