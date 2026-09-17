package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.util.CraftVector;

/**
 * {@code ServerboundInteractPacket.Handler}，把对"不存在的实体 id"的交互转成 Paper 的
 * {@link PlayerUseUnknownEntityEvent}。
 *
 * <p>放在 {@code bridge} 而不是写成 mixin 里的匿名类：Mixin 会把 mixin 的内部类搬进目标类，
 * 搬完的 InnerClasses/NestHost 属性仍指向原 mixin 外围类，与目标类互相矛盾 ——
 * 轻则访问外围私有成员时 IllegalAccessError，重则 {@code IncompatibleClassChangeError}
 * （main 分支在 CraftServer$PaperarcPotionBrewer 上实测，见 checklist §1.6 c/d）。
 * 这个 Handler 只在交互到虚拟实体时才实例化，启动门禁看不见。</p>
 */
public final class PaperarcUnknownEntityHandler implements ServerboundInteractPacket.Handler {

    private final ServerPlayer sender;
    private final int entityId;

    public PaperarcUnknownEntityHandler(ServerPlayer sender, int entityId) {
        this.sender = sender;
        this.entityId = entityId;
    }

    @Override
    public void onInteraction(InteractionHand hand) {
        this.fire(false, hand, null);
    }

    @Override
    public void onInteraction(InteractionHand hand, Vec3 pos) {
        this.fire(false, hand, pos);
    }

    @Override
    public void onAttack() {
        this.fire(true, InteractionHand.MAIN_HAND, null);
    }

    private void fire(boolean isAttack, InteractionHand hand, Vec3 pos) {
        new PlayerUseUnknownEntityEvent(
            PaperArcBridge.bukkitPlayer(this.sender),
            this.entityId,
            isAttack,
            CraftEquipmentSlot.getHand(hand),
            pos != null ? CraftVector.toBukkit(pos) : null
        ).callEvent();
    }
}
