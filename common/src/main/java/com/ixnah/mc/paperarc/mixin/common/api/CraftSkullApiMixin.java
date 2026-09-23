package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.craftbukkit.v.block.CraftSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-setPlayerProfile-API-for-Skulls.patch additions on
 * {@link CraftSkull}: {@code Skull#setPlayerProfile(PlayerProfile)} and
 * {@code Skull#getPlayerProfile()}.
 *
 * <p>Arclight ships no {@code com.destroystokyo.paper.profile} implementation
 * class (paper-server's {@code CraftPlayerProfile} is absent), so both
 * directions go through PaperArc's own
 * {@link com.ixnah.mc.paperarc.bridge.CraftPlayerProfile}.
 */
@Mixin(CraftSkull.class)
public abstract class CraftSkullApiMixin {

    @Shadow
    private ResolvableProfile profile;

    @Unique
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        this.profile = ResolvableProfile.createResolved(
            com.ixnah.mc.paperarc.bridge.CraftPlayerProfile.asAuthlibCopy(profile));
    }

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        ResolvableProfile resolvable = this.profile;
        return resolvable == null ? null
            : new com.ixnah.mc.paperarc.bridge.CraftPlayerProfile(resolvable.partialProfile());
    }
}
