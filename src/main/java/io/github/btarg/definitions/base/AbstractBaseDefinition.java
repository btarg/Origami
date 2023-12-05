package io.github.btarg.definitions.base;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class AbstractBaseDefinition implements ConfigurationSerializable {
    public abstract void registerDefinition(CommandSender sender);

    public abstract AbstractBaseDefinition getDefaultDefinition();
}
