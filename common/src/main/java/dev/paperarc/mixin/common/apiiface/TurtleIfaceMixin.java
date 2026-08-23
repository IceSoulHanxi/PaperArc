package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Turtle} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Turtle", remap = false)
public interface TurtleIfaceMixin {

    public abstract org.bukkit.Location getHome();

    public abstract void setHome(org.bukkit.Location p0);

    public abstract boolean isGoingHome();

    public abstract boolean isDigging();

    public abstract void setHasEgg(boolean p0);
}
