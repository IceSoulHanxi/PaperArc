package com.ixnah.mc.paperarc.mixin.common.api;

import java.io.File;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_20_R1.CraftOfflinePlayer;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's {@code Replace-OfflinePlayer-getLastPlayed},
 * {@code Add-Offline-PDC-API} and {@code Implement-OfflinePlayer-isConnected}
 * patches on {@link CraftOfflinePlayer}: {@code getLastLogin()}, {@code getLastSeen()},
 * {@code getPersistentDataContainer()} and {@code isConnected()}.
 */
@Mixin(CraftOfflinePlayer.class)
public abstract class CraftOfflinePlayerApiMixin {

    @Shadow
    private CompoundTag getData() {
        throw new AssertionError();
    }

    @Shadow
    private File getDataFile() {
        throw new AssertionError();
    }

    @Shadow
    public abstract Player getPlayer();

    @Unique
    public long getLastLogin() {
        // Paper delegates to CraftPlayer#getLastLogin for online players, but that API (and
        // ServerPlayer#loginTime) does not exist in this codebase yet; the persisted-data path
        // below is what Paper itself uses for offline players.
        CompoundTag data = this.paperarc$getPaperData();
        if (data != null) {
            if (data.contains("LastLogin")) {
                return data.getLong("LastLogin");
            }
            // if the player file cannot provide accurate data, this is probably the closest we can approximate
            return this.getDataFile().lastModified();
        }
        return 0L;
    }

    @Unique
    public long getLastSeen() {
        Player player = this.getPlayer();
        if (player != null) {
            return System.currentTimeMillis();
        }
        CompoundTag data = this.paperarc$getPaperData();
        if (data != null) {
            if (data.contains("LastSeen")) {
                return data.getLong("LastSeen");
            }
            // if the player file cannot provide accurate data, this is probably the closest we can approximate
            return this.getDataFile().lastModified();
        }
        return 0L;
    }

    @Unique
    public boolean isConnected() {
        // As in Paper: CraftOfflinePlayer never reports a live connection.
        return false;
    }

    /**
     * Read-only variant of Paper's {@code getPaperData()} helper (Paper mutates the loaded tag to
     * insert an empty "Paper" compound; we avoid writing to storage-backed NBT).
     */
    @Unique
    private CompoundTag paperarc$getPaperData() {
        CompoundTag result = this.getData();
        if (result == null) {
            return null;
        }
        return result.contains("Paper", 10) ? result.getCompound("Paper") : new CompoundTag();
    }
}
