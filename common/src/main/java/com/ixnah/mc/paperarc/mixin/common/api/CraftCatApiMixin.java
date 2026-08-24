package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Cat;
import org.bukkit.craftbukkit.v.entity.CraftCat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's More-cat-api additions on {@link CraftCat}:
 * {@code is/setLyingDown} (NMS {@code isLying/setLying}) and
 * {@code is/setHeadUp} (NMS {@code isRelaxStateOne/setRelaxStateOne}).
 */
@Mixin(CraftCat.class)
public abstract class CraftCatApiMixin {

    @Shadow
    public abstract Cat getHandle();

    // Paper start - More cat api
    @Unique
    public boolean isLyingDown() {
        return this.getHandle().isLying();
    }

    @Unique
    public void setLyingDown(boolean lyingDown) {
        this.getHandle().setLying(lyingDown);
    }

    @Unique
    public boolean isHeadUp() {
        return ((CatInvokerMixin) (Object) this.getHandle()).paperarc$invokeIsRelaxStateOne();
    }

    @Unique
    public void setHeadUp(boolean headUp) {
        ((CatInvokerMixin) (Object) this.getHandle()).paperarc$invokeSetRelaxStateOne(headUp);
    }
    // Paper end - More cat api
}
