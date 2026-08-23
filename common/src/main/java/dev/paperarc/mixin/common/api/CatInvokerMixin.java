package dev.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.animal.Cat;

/**
 * Exposes Cat's package-private {@code isRelaxStateOne}/{@code setRelaxStateOne}
 * for {@code CraftCatApiMixin#isHeadUp/setHeadUp}.
 * Paper ref: patches/server/Missing-Entity-API.patch (More cat api).
 */
@Mixin(Cat.class)
public interface CatInvokerMixin {

    @Invoker("isRelaxStateOne")
    boolean paperarc$invokeIsRelaxStateOne();

    @Invoker("setRelaxStateOne")
    void paperarc$invokeSetRelaxStateOne(boolean headUp);
}
