package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.trim.CraftTrimMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer;
// alias: ChatSerializer
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;

/**
 * Adds Adventure description missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Adventure.patch + Improve-Registry.patch
 * (PaperAdventure.asAdventure(handle.description()); gson round-trip used instead).
 */
@Mixin(CraftTrimMaterial.class)
public abstract class CraftTrimMaterialApiMixin {

    // 1.21.11：getHandle() 上移到泛型父类 CraftRegistryItem<M>（擦除为 Object），类型化的 @Shadow 对不上
    @Unique
    private TrimMaterial paperarc$handle() {
        return ((CraftTrimMaterial) (Object) this).getHandle();
    }

    @Unique
    public Component description() {
        String json = ChatSerializer.toJson(this.paperarc$handle().description(), ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
        return GsonComponentSerializer.gson().deserialize(json);
    }
}
