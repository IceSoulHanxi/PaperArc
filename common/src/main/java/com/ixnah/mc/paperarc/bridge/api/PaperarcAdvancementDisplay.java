package com.ixnah.mc.paperarc.bridge.api;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


/**
 * 把 NMS 的 {@link DisplayInfo} 适配成 paper 的 {@link AdvancementDisplay}
 * （{@code Advancement#getDisplay()}，B3-4）。
 *
 * <p>字段一一对应，没有降级；只有 {@code displayName()} 走 paper 的约定
 * （标题外面套一层"带悬浮描述的进度名"，这里等价成标题本身，与 Arclight 侧
 * {@code CraftAdvancement#displayName()} 的口径一致）。
 *
 * <p>不写成 mixin 的内部类：mixin 里的内嵌类合并后 InnerClasses 会与目标矛盾
 * （docs/mixin-conventions.md）。
 */
public final class PaperarcAdvancementDisplay implements AdvancementDisplay {

    private final DisplayInfo handle;

    public PaperarcAdvancementDisplay(DisplayInfo handle) {
        this.handle = handle;
    }

    private static Component adventure(net.minecraft.network.chat.Component vanilla) {
        net.minecraft.core.HolderLookup.Provider registries =
                ((CraftServer) org.bukkit.Bukkit.getServer()).getServer().registryAccess();
        return GsonComponentSerializer.gson().deserialize(
                org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer.toJson(vanilla, registries));
    }

    @Override
    public Frame frame() {
        AdvancementType type = this.handle.getType();
        return switch (type) {
            case CHALLENGE -> Frame.CHALLENGE;
            case GOAL -> Frame.GOAL;
            default -> Frame.TASK;
        };
    }

    @Override
    public Component title() {
        return adventure(this.handle.getTitle());
    }

    @Override
    public Component description() {
        return adventure(this.handle.getDescription());
    }

    @Override
    public ItemStack icon() {
        return CraftItemStack.asCraftMirror(this.handle.getIcon());
    }

    @Override
    public boolean doesShowToast() {
        return this.handle.shouldShowToast();
    }

    @Override
    public boolean doesAnnounceToChat() {
        return this.handle.shouldAnnounceChat();
    }

    @Override
    public boolean isHidden() {
        return this.handle.isHidden();
    }

    @Override
    public NamespacedKey backgroundPath() {
        return this.handle.getBackground()
                .map(asset -> new NamespacedKey(asset.id().getNamespace(), asset.id().getPath()))
                .orElse(null);
    }

    @Override
    public Component displayName() {
        return title();
    }
}
