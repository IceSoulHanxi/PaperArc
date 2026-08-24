package com.ixnah.mc.paperarc.bridge.craft;

import net.minecraft.world.entity.Entity;
import org.bukkit.World;

/**
 * Duck-typing bridge merged onto {@code CraftEntity} by its provider mixin.
 * Entity api mixins on subclasses cast to this interface instead of
 * shadowing the inherited {@code getHandle()} family.
 */
public interface CraftEntityBridge {

    Entity paperarc$getHandle();

    World paperarc$getWorld();

    void paperarc$update();
}
