package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v.inventory.CraftMetaSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code SkullMeta#getPlayerProfile()} / {@code setPlayerProfile(PlayerProfile)}
 * （A5-3，pairing 基线 NO_IMPL）。
 *
 * <p>运行时只有 spigot 的 {@code getOwnerProfile}/{@code setOwnerProfile}
 * （{@code org.bukkit.profile.PlayerProfile}，没有属性集）；paper 这两条走的是
 * {@code com.destroystokyo.paper.profile.PlayerProfile}，本仓库已有
 * {@code bridge/CraftPlayerProfile} 做 authlib {@code GameProfile} 的双向适配。</p>
 */
@Mixin(CraftMetaSkull.class)
public abstract class CraftMetaSkullApiMixin {

    @Shadow
    private GameProfile profile;

    @Shadow
    private void setProfile(GameProfile profile) {
        throw new AssertionError();
    }

    @Unique
    public PlayerProfile getPlayerProfile() {
        return this.profile == null ? null : CraftPlayerProfile.asBukkitMirror(this.profile);
    }

    @Unique
    public void setPlayerProfile(PlayerProfile profile) {
        this.setProfile(profile == null ? null : CraftPlayerProfile.asAuthlibCopy(profile));
    }
}
