package io.github.btarg.origami.util.parsers;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class AttributeParser {

    public static Map<Attribute, Map<UUID, AttributeModifier>> parseAttributes(List<Map<String, Map<String, Object>>> attributeList) {
        Map<Attribute, Map<UUID, AttributeModifier>> attributes = new HashMap<>();

        for (Map<String, Map<String, Object>> attributeMap : attributeList) {
            for (Map.Entry<String, Map<String, Object>> entry : attributeMap.entrySet()) {
                Attribute attribute = Attribute.valueOf(entry.getKey());
                Map<String, Object> attributeParams = entry.getValue();

                String modifierName = Optional.ofNullable((String) attributeParams.get("name")).filter(s -> !s.isBlank()).orElse(UUID.randomUUID().toString());
                double value = Optional.ofNullable(attributeParams.get("value")).map(Object::toString).map(Double::parseDouble).orElse(0.0);
                EquipmentSlot equipmentSlot = EquipmentSlot.valueOf(Optional.ofNullable((String) attributeParams.get("equipmentSlot")).orElse("HAND").toUpperCase());
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(Optional.ofNullable((String) attributeParams.get("operation")).orElse("ADD_NUMBER").toUpperCase());

                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), modifierName, value, operation, equipmentSlot);
                attributes.put(attribute, Map.of(modifier.getUniqueId(), modifier));
            }
        }

        return attributes;
    }

    public static List<Map<String, Map<String, Object>>> serializeAttributes(Map<Attribute, Map<UUID, AttributeModifier>> attributes) {
        List<Map<String, Map<String, Object>>> attributeList = new ArrayList<>();

        for (Map.Entry<Attribute, Map<UUID, AttributeModifier>> attributeEntry : attributes.entrySet()) {
            for (Map.Entry<UUID, AttributeModifier> modifierEntry : attributeEntry.getValue().entrySet()) {
                AttributeModifier modifier = modifierEntry.getValue();

                Map<String, Object> attributeParams = new HashMap<>();
                attributeParams.put("name", modifier.getName());
                attributeParams.put("value", modifier.getAmount());
                attributeParams.put("equipmentSlot", modifier.getSlot().name());
                attributeParams.put("operation", modifier.getOperation().name());

                attributeList.add(Map.of(attributeEntry.getKey().name(), attributeParams));
            }
        }

        return attributeList;
    }
}