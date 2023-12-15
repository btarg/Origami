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

                String modifierName = (String) attributeParams.get("name");
                double value = Double.parseDouble(String.valueOf(attributeParams.get("value")));
                EquipmentSlot equipmentSlot = Objects.requireNonNullElse(EquipmentSlot.valueOf(((String) attributeParams.get("equipmentSlot")).toUpperCase()), EquipmentSlot.HAND);
                String operationStr = (String) attributeParams.getOrDefault("operation", "ADD_NUMBER");
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(operationStr.toUpperCase());
                
                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), modifierName, value, operation, equipmentSlot);
                attributes.put(attribute, Map.of(modifier.getUniqueId(), modifier));
            }
        }

        return attributes;
    }
}