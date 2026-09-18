package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerInteractEvent#getInteractionPoint()}。
 *
 * <p>paper 的实现体就是「点到的方块的 Location 加上方块内相对坐标」，
 * 两样运行时都有（{@code getClickedBlock()} / Spigot 的 {@code getClickedPosition()}），
 * 所以这里照抄 paper 的公式现算（`javap -c` 核对 paper-api：
 * {@code blockClicked == null || clickedPosistion == null ? null
 * : blockClicked.getLocation().add(clickedPosistion)}）。
 */
@Mixin(PlayerInteractEvent.class)
public abstract class PlayerInteractEventApiMixin {

    @Unique
    public Location getInteractionPoint() {
        PlayerInteractEvent self = (PlayerInteractEvent) (Object) this;
        Block block = self.getClickedBlock();
        Vector clicked = self.getClickedPosition();
        if (block == null || clicked == null) {
            return null;
        }
        return block.getLocation().add(clicked);
    }
}
