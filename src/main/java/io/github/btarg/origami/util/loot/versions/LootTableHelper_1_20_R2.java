package io.github.btarg.origami.util.loot.versions;

import io.github.btarg.origami.util.loot.LootTableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootTableHelper_1_20_R2 implements LootTableHelper {
    public List<ItemStack> getBlockDrops(String dropLootTable, Location loc, ItemStack minedWith) {
        List<ItemStack> dropStacks = new ArrayList<>();

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();

        LootTable lootTable = server.getLootData().getLootTable(ResourceLocation.of(dropLootTable, ':'));
        if (lootTable == null) return null;

        LootParams.Builder lp = new LootParams.Builder(level);

        lp.withParameter(LootContextParams.TOOL, CraftItemStack.asNMSCopy(minedWith));
        lp.withParameter(LootContextParams.ORIGIN, new Vec3(loc.x(), loc.y(), loc.z()));
        lp.withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(BlockPos.containing(loc.x(), loc.y(), loc.z())));

        LootParams lootParams = lp.create(LootContextParamSets.BLOCK);
        List<net.minecraft.world.item.ItemStack> list = lootTable.getRandomItems(lootParams);
        for (var e : list) {
            dropStacks.add(e.getBukkitStack());
        }
        return dropStacks;
    }

}
