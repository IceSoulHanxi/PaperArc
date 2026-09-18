package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.MusicInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 让 {@code MusicInstrument} 实现 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(MusicInstrument.class)
public abstract class MusicInstrumentApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "instrument.minecraft." + ((MusicInstrument) (Object) this).getKey().getKey();
    }
}
