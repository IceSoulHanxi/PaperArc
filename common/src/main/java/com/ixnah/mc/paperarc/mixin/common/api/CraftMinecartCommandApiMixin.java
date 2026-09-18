package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftMinecartCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code CommandMinecart extends CommandBlockHolder}（A5-1）在命令矿车侧的实现体。
 * {@code getCommand}/{@code setCommand} 运行时本来就有，这里只补 successCount 与 lastOutput 四条。
 *
 * <p>1.20.1 的 {@code Component.Serializer.toJson/fromJson} 都不带 {@code RegistryAccess}
 * 参数（1.21.1 才加，见 docs/mixin-conventions.md 的差异表），Component 往返沿用本仓库的 gson 约定。</p>
 */
@Mixin(CraftMinecartCommand.class)
public abstract class CraftMinecartCommandApiMixin {

    @Unique
    private net.minecraft.world.level.BaseCommandBlock paperarc$commandBlock() {
        return ((MinecartCommandBlock) ((CraftEntity) (Object) this).getHandle()).getCommandBlock();
    }

    @Unique
    public int getSuccessCount() {
        return paperarc$commandBlock().getSuccessCount();
    }

    @Unique
    public void setSuccessCount(int successCount) {
        paperarc$commandBlock().setSuccessCount(successCount);
    }

    @Unique
    public Component lastOutput() {
        net.minecraft.network.chat.Component output = paperarc$commandBlock().getLastOutput();
        return output == null ? null : GsonComponentSerializer.gson().deserialize(Serializer.toJson(output));
    }

    @Unique
    public void lastOutput(Component lastOutput) {
        paperarc$commandBlock().setLastOutput(lastOutput == null ? null
                : Serializer.fromJson(GsonComponentSerializer.gson().serialize(lastOutput)));
    }
}
