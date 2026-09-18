package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.MusicInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 加在 {@code MusicInstrument} 上的 {@code translationKey()}（checklist §1.10 am）。 */
@Mixin(MusicInstrument.class)
public abstract class MusicInstrumentApiMixin {

    @Unique
    public String translationKey() {
        return "instrument.minecraft." + ((MusicInstrument) (Object) this).getKey().getKey();
    }
}
