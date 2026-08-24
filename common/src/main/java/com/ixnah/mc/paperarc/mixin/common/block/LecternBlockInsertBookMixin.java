package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PlayerInsertLecternBookEvent 触发点。
 * <p>
 * 对照 Paper：LecternBlock#placeBook 中，仅当 user 为 ServerPlayer 时发事件
 * （book = asCraftMirror(stack.copyWithCount(1))，可被插件改写）；取消时直接
 * return（跳过 setBook/resetBookState/playSound）。非玩家（如投掷器）走原版路径。
 * <p>
 * 实现：cancellable @Inject 于 placeBook HEAD，完整复刻方法体：事件成功时
 * stack.consume(1) + lectern.setBook(event 书本) + resetBookState + playSound
 * 后 ci.cancel()；取消时仅 ci.cancel()。复刻体逐条对照原版字节码
 * （getBlockEntity → instanceof → setBook(consumeAndReturn) → resetBookState
 * → playSound(BOOK_PUT)）。
 * <p>
 * 偏差：Paper 用 CraftItemStack.unwrap(event.getBook()) 取回 NMS 栈；
 * 本环境 CraftBukkit 无 unwrap 方法，改用 asNMSCopy（多一次拷贝，语义一致）。
 */
@Mixin(LecternBlock.class)
public abstract class LecternBlockInsertBookMixin {

    @Inject(method = "placeBook", at = @At("HEAD"), cancellable = true)
    private static void paperarc$onInsertBook(LivingEntity user, Level world, BlockPos pos, BlockState state,
                                              ItemStack stack, CallbackInfo ci) {
        if (!(user instanceof ServerPlayer serverPlayer)) {
            return; // 非玩家放置：原版路径
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof LecternBlockEntity lectern)) {
            return; // 原版路径（instanceof 失败，无副作用）
        }
        PlayerInsertLecternBookEvent event = new PlayerInsertLecternBookEvent(
            PaperArcBridge.bukkitPlayer(serverPlayer),
            CraftBlock.at(world, pos),
            CraftItemStack.asCraftMirror(stack.copyWithCount(1))
        );
        if (!event.callEvent()) {
            ci.cancel(); // 取消：不 setBook、不 resetBookState、不播音效
            return;
        }
        stack.consume(1, user);
        lectern.setBook(CraftItemStack.asNMSCopy(event.getBook()));
        LecternBlock.resetBookState(serverPlayer, world, pos, state, true);
        world.playSound((Player) null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
        ci.cancel(); // 已完整替代原版后续逻辑
    }
}
