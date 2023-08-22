package io.github.btarg.rendering;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@Getter
public class BrokenBlock {

    private final double time;
    private final Block block;
    private double damage = 0;
    private float oldAnimation;

    public BrokenBlock(Block block, double time) {
        this.block = block;
        this.time = time;
    }

    public void incrementDamage(Player player, double multiplier) {

        if (isBroken() || multiplier == 0) return;

        damage += multiplier;
        if (damage == 0) return;

        float animation = getAnimation();

        if (animation != oldAnimation) {
            if (animation < 1) {
                player.sendBlockDamage(block.getLocation(), animation);
            } else if (animation >= 1) {
                breakBlock(player);
                return;
            }
        }

        oldAnimation = animation;
    }

    public float getAnimation() {
        return (float) (Math.floor(damage) / time);
    }

    public boolean isBroken() {
        return getAnimation() >= 1;
    }


    public void breakBlock(Player breaker) {
        if (breaker == null) return;
        breaker.breakBlock(block);
    }

}