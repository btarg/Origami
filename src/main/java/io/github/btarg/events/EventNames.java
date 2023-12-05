package io.github.btarg.events;

import java.util.stream.Stream;

public enum EventNames {
    ON_RIGHT_CLICK("onRightClick"),
    ON_LEFT_CLICK("onLeftClick"),
    ON_RIGHT_CLICK_AIR("onRightClickAir"),
    ON_LEFT_CLICK_AIR("onLeftClickAir"),
    ON_RIGHT_CLICK_BLOCK("onRightClickBlock"),
    ON_LEFT_CLICK_BLOCK("onLeftClickBlock"),
    ON_PLACED("onPlaced"),
    ON_BROKEN("onBroken"),
    ON_DAMAGED("onDamaged"),
    ON_PUSHED("onPushed");

    private final String eventName;

    EventNames(String eventName) {
        this.eventName = eventName;
    }

    public static String[] getAllEventNames() {
        return Stream.of(EventNames.values())
                .map(Enum::toString)
                .toArray(String[]::new);
    }

    @Override
    public String toString() {
        return eventName;
    }
}
