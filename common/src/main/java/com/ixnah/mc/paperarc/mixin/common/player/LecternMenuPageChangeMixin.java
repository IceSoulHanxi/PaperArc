package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerLecternPageChangeEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryLectern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PlayerLecternPageChangeEvent 触发点。
 * <p>
 * 对照 Paper：LecternMenu#clickMenuButton 中 case 1（LEFT，setData(0,j-1)）
 * 与 case 2（RIGHT，setData(0,j+1)）前发事件；取消时 clickMenuButton 返回
 * false；成功时 setData 使用 event.getNewPage()（插件可改写页码）。
 * id >= 100 的页码跳转分支（setData ordinal 0）Paper 不发事件，保持原样。
 * <p>
 * 实现：cancellable @Inject 于两处 LecternMenu#setData INVOKE 前
 * （bytecode 顺序 ordinal 1 = RIGHT、ordinal 2 = LEFT），handler 内自行
 * setData(event.getNewPage()) 并 setReturnValue(true) 跳过原指令。
 * <p>
 * 偏差：Paper 经 getBukkitView() 取 CraftInventoryLectern；本环境无该方法，
 * 直接 new CraftInventoryLectern(this.lectern)，holder/book 等价。
 */
@Mixin(LecternMenu.class)
public abstract class LecternMenuPageChangeMixin {

    @Shadow
    @Final
    private Container lectern;

    @Shadow
    @Final
    private ContainerData lecternData;

    @Unique
    private void paperarc$firePageChange(Player player, PlayerLecternPageChangeEvent.PageChangeDirection direction,
                                         int oldPage, int delta, CallbackInfoReturnable<Boolean> cir) {
        CraftInventoryLectern inv = new CraftInventoryLectern(this.lectern);
        PlayerLecternPageChangeEvent event = new PlayerLecternPageChangeEvent(
            PaperArcBridge.bukkitPlayer(player),
            inv.getHolder(),
            inv.getBook(),
            direction,
            oldPage,
            oldPage + delta
        );
        if (!event.callEvent()) {
            cir.setReturnValue(false); // Paper：取消时返回 false
            return;
        }
        this.setData(0, event.getNewPage());
        cir.setReturnValue(true); // 跳过原版 setData 指令
    }

    @Shadow
    public abstract void setData(int id, int value);

    @Inject(
        method = "clickMenuButton",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/LecternMenu;setData(II)V", ordinal = 1),
        cancellable = true
    )
    private void paperarc$onPageRight(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        this.paperarc$firePageChange(player, PlayerLecternPageChangeEvent.PageChangeDirection.RIGHT,
            this.lecternData.get(0), 1, cir);
    }

    @Inject(
        method = "clickMenuButton",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/LecternMenu;setData(II)V", ordinal = 2),
        cancellable = true
    )
    private void paperarc$onPageLeft(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        this.paperarc$firePageChange(player, PlayerLecternPageChangeEvent.PageChangeDirection.LEFT,
            this.lecternData.get(0), -1, cir);
    }
}
