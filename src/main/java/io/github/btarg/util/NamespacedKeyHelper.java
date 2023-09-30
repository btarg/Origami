package io.github.btarg.util;

import io.github.btarg.OrigamiMain;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NamespacedKeyHelper {

    private static final List<String> usedIds = new ArrayList<>();

    public static NamespacedKey getUniqueNamespacedKey(String name) {
        // add numbers onto the end of already used names
        AtomicInteger counter = new AtomicInteger(1);
        String finalName = name;
        usedIds.stream().filter(id -> usedIds.contains(finalName)).forEach(id -> counter.getAndIncrement());

        if (counter.get() > 1) {
            name = name + counter;
        }
        usedIds.add(name);
        return new NamespacedKey(OrigamiMain.getInstance(), name);
    }

}
