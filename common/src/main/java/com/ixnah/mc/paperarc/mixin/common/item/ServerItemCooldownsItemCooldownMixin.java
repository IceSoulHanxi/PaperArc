package com.ixnah.mc.paperarc.mixin.common.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * PlayerItemCooldownEvent 触发点。
 * <p>
 * 对照 Paper：ServerItemCooldowns 覆写 addCooldown(Item,int)，发事件
 * （material = CraftItemType.minecraftToBukkit(item)），取消则不加冷却；
 * 成功用 event.getCooldown()（插件可改写时长）调用 super。
 * <p>
 * 实现：addCooldown 声明于父类 ItemCooldowns（ServerItemCooldowns 未覆写，
 * mixin 无法注入子类继承方法），故 @WrapMethod 于 ItemCooldowns#addCooldown，
 * 以 instanceof ServerItemCooldowns 区分：服务端走事件逻辑（跳过或改时长后
 * original.call），客户端/其他直接 original.call 透传。等价 Paper 覆写语义。
 * 玩家字段经文件内嵌的 accessor mixin（ServerItemCooldowns#player）获取。
 */
@Mixin(ItemCooldowns.class)
public abstract class ServerItemCooldownsItemCooldownMixin {

    @WrapMethod(method = "addCooldown(Lnet/minecraft/world/item/Item;I)V")
    private void paperarc$onAddCooldown(Item item, int duration, Operation<Void> original) {
        if (!((Object) this instanceof ServerItemCooldowns)) {
            original.call(item, duration); // 客户端等：原版路径
            return;
        }
        ServerPlayer player = ((ServerItemCooldownsPlayerAccessor) (Object) this).paperarc$player();
        PlayerItemCooldownEvent event = new PlayerItemCooldownEvent(
            PaperArcBridge.bukkitPlayer(player),
            CraftMagicNumbers.getMaterial(item),
            duration
        );
        if (event.callEvent()) {
            original.call(item, event.getCooldown());
        }
    }
}

@Mixin(ServerItemCooldowns.class)
abstract class ServerItemCooldownsPlayerAccessor {

    @Accessor("player")
    abstract ServerPlayer paperarc$player();
}
