package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.block.Sign;
import io.papermc.paper.math.Position;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.Colorable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Sign} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Sign", remap = false)
public interface SignIfaceMixin {

    @Unique
    public abstract java.util.List lines();

    @Unique
    public abstract java.util.UUID getAllowedEditorUniqueId();

    @Unique
    public abstract void setAllowedEditorUniqueId(java.util.UUID p0);

    @Unique
    public abstract org.bukkit.block.sign.Side getInteractableSideFor(double p0, double p1);

    @Unique
    public abstract net.kyori.adventure.text.Component line(int p0);

    @Unique
    public abstract void line(int p0, net.kyori.adventure.text.Component p1);

    @Unique
    public default Side getInteractableSideFor(Entity entity) {
        Sign self = (Sign) this;
        return self.getInteractableSideFor((Position) entity.getLocation());
    }

    @Unique
    public default Side getInteractableSideFor(Position position) {
        Sign self = (Sign) this;
        return self.getInteractableSideFor(position.x(), position.z());
    }
}
