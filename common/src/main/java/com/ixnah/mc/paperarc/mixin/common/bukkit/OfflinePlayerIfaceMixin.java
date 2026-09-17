package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.OfflinePlayer} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.OfflinePlayer", remap = false)
public interface OfflinePlayerIfaceMixin extends io.papermc.paper.persistence.PersistentDataViewHolder {

    @Unique
    public abstract boolean isConnected();

    @Unique
    public abstract long getLastLogin();

    @Unique
    public abstract long getLastSeen();

    @Unique
    public abstract io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer();

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();

    @Unique
    public default BanEntry banPlayer(String reason) {
        OfflinePlayer self = (OfflinePlayer) this;
        return self.banPlayer(reason, (Date) null, (String) null);
    }

    @Unique
    public default BanEntry banPlayer(String reason, String source) {
        OfflinePlayer self = (OfflinePlayer) this;
        return self.banPlayer(reason, (Date) null, source);
    }

    @Unique
    public default BanEntry banPlayer(String reason, Date expires) {
        OfflinePlayer self = (OfflinePlayer) this;
        return self.banPlayer(reason, expires, (String) null);
    }

    @Unique
    public default BanEntry banPlayer(String reason, Date expires, String source) {
        OfflinePlayer self = (OfflinePlayer) this;
        return self.banPlayer(reason, expires, source, true);
    }

    @Unique
    public default BanEntry banPlayer(String reason, Date expires, String source, boolean kickIfOnline) {
        OfflinePlayer self = (OfflinePlayer) this;
        BanEntry banEntry = Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(self.getName(), reason, expires, source);

        if (kickIfOnline && self.isOnline()) {
            self.getPlayer().kickPlayer(reason);
        }

        return banEntry;
    }
}
