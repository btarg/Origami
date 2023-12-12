package io.github.btarg.origami.util.parsers;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeParser {

    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([A-Za-z_]+)\\(([^)]+)\\)");

    public static Map<Attribute, Map<UUID, AttributeModifier>> parseAttributes(List<String> attributeStrings) {
        if (attributeStrings == null) return new HashMap<>();

        Map<Attribute, Map<UUID, AttributeModifier>> parsedAttributes = new HashMap<>();

        for (String attributeString : attributeStrings) {
            Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString.trim());
            if (matcher.matches()) {
                String attributeName = matcher.group(1);
                String attributeParams = matcher.group(2);

                Attribute attribute = Attribute.valueOf(attributeName.toUpperCase());
                Map<UUID, AttributeModifier> attributeModifiers = parseAttributeModifiers(attributeParams);

                parsedAttributes.put(attribute, attributeModifiers);
            } else {
                throw new IllegalArgumentException("Invalid attribute format: " + attributeString);
            }
        }

        return parsedAttributes;
    }

    private static Map<UUID, AttributeModifier> parseAttributeModifiers(String attributeParams) {
        Map<UUID, AttributeModifier> attributeModifiers = new HashMap<>();

        String[] modifierParts = attributeParams.split(",");
        for (String modifierPart : modifierParts) {
            String[] parts = modifierPart.trim().split(":");
            if (parts.length == 2 || parts.length == 3) {
                String modifierName = parts[0].trim();
                double value = Double.parseDouble(parts[1].trim());

                EquipmentSlot equipmentSlot = EquipmentSlot.HAND; // Default to HAND
                if (parts.length == 3) {
                    equipmentSlot = EquipmentSlot.valueOf(parts[2].trim().toUpperCase());
                }

                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), modifierName, value, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot);
                attributeModifiers.put(modifier.getUniqueId(), modifier);
            } else {
                throw new IllegalArgumentException("Invalid attribute modifier format: " + modifierPart);
            }
        }

        return attributeModifiers;
    }
}
