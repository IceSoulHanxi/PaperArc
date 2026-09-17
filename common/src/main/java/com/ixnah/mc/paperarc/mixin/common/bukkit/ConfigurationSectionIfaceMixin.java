package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentDecoder;
import net.kyori.adventure.text.serializer.ComponentEncoder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.configuration.ConfigurationSection} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.configuration.ConfigurationSection", remap = false)
public interface ConfigurationSectionIfaceMixin {

    @Unique
    public default Component getRichMessage(String path) {
        ConfigurationSection self = (ConfigurationSection) this;
        return self.getRichMessage(path, (Component) null);
    }

    @Unique
    public default Component getRichMessage(String path, Component fallback) {
        ConfigurationSection self = (ConfigurationSection) this;
        return self.getComponent(path, MiniMessage.miniMessage(), fallback);
    }

    @Unique
    public default void setRichMessage(String path, Component value) {
        ConfigurationSection self = (ConfigurationSection) this;
        self.setComponent(path, MiniMessage.miniMessage(), value);
    }

    @Unique
    public default <C extends Component> C getComponent(String path, ComponentDecoder<? super String, C> decoder) {
        ConfigurationSection self = (ConfigurationSection) this;
        return self.getComponent(path, decoder, (C) null);
    }

    @Unique
    public default <C extends Component> C getComponent(String path, ComponentDecoder<? super String, C> decoder, C fallback) {
        ConfigurationSection self = (ConfigurationSection) this;
        Objects.requireNonNull(decoder, "decoder");
        String value = self.getString(path);

        return decoder.deserializeOr(value, fallback);
    }

    @Unique
    public default <C extends Component> void setComponent(String path, ComponentEncoder<C, String> encoder, C value) {
        ConfigurationSection self = (ConfigurationSection) this;
        Objects.requireNonNull(encoder, "encoder");
        self.set(path, encoder.serializeOrNull(value));
    }
}
