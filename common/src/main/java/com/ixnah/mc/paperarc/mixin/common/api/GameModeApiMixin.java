package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/** paper 让 {@code GameMode} 实现 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(GameMode.class)
public abstract class GameModeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "gameMode." + ((GameMode) (Object) this).name().toLowerCase(Locale.ENGLISH);
    }
}
