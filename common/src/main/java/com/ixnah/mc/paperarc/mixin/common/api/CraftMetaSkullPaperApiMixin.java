package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import org.bukkit.craftbukkit.v.inventory.CraftMetaSkull;
import org.bukkit.inventory.meta.SkullMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code SkullMeta#getPlayerProfile()/setPlayerProfile(..)}
 * （destroystokyo 的 PlayerProfile 类型；Bukkit 自带的同名方法用的是
 * {@code org.bukkit.profile.PlayerProfile}，两者不通用）。
 */
@Mixin(CraftMetaSkull.class)
public abstract class CraftMetaSkullPaperApiMixin {

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        org.bukkit.profile.PlayerProfile owner = ((SkullMeta) (Object) this).getOwnerProfile();
        if (owner == null) {
            return null;
        }
        if (owner instanceof org.bukkit.craftbukkit.v.profile.CraftPlayerProfile cb) {
            return new CraftPlayerProfile(cb.buildGameProfile());
        }
        return new CraftPlayerProfile(owner.getUniqueId(), owner.getName());
    }

    @Unique
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        if (profile == null) {
            ((SkullMeta) (Object) this).setOwnerProfile(null);
            return;
        }
        ((SkullMeta) (Object) this).setOwnerProfile(
                org.bukkit.Bukkit.createPlayerProfile(profile.getId(), profile.getName()));
    }
}
