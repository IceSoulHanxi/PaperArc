package dev.paperarc.mixin.common.api;

import com.destroystokyo.paper.ClientOption;
import dev.paperarc.bridge.ApiState;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Paper API 方法补齐批次 B30（part 1）：org.bukkit.craftbukkit.v.entity.CraftPlayer
 * 覆盖切片 docs/api-slices/B30.json 中 part=1 的前 19 个方法
 * （activeBossBars .. getSimulationDistance；其余由 Part2 批次负责）。
 */
@Mixin(CraftPlayer.class)
public abstract class CraftPlayerApiMixinPart1 {

    @Shadow
    public abstract ServerPlayer getHandle();

    @Shadow
    public abstract String getDisplayName();

    @Shadow
    public abstract void setDisplayName(String displayName);

    @Unique
    private static PlayerList paperarc$playerList() {
        return ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer()).getServer().getPlayerList();
    }

    // ---- activeBossBars ----
    @Unique
    public Iterable activeBossBars() {
        // vanilla 服务端不维护每玩家 BossBar 注册表（龙/凋灵血条无法反向回查），保守返回空集合
        return Collections.emptyList();
    }
    @Unique
    public void addAdditionalChatCompletions(Collection completions) {
        this.getHandle().connection.send(new ClientboundCustomChatCompletionsPacket(
            ClientboundCustomChatCompletionsPacket.Action.ADD, new ArrayList<>(completions)));
    }

    // ---- applyMending ----
    @Unique
    public int applyMending(int amount) {
        ServerPlayer sp = this.getHandle();
        sp.resetLastActionTime();
        int remaining = amount;
        for (int guard = 0; guard < 64 && remaining > 0; guard++) {
            Optional<EnchantedItemInUse> opt = EnchantmentHelper.getRandomItemWith(
                EnchantmentEffectComponents.REPAIR_WITH_XP, sp, ItemStack::isDamaged);
            if (opt.isEmpty()) {
                break;
            }
            ItemStack stack = opt.get().itemStack();
            if (stack.isEmpty() || !stack.isDamaged()) {
                break;
            }
            int want = EnchantmentHelper.modifyDurabilityToRepairFromXp(sp.serverLevel(), stack, remaining * 2);
            int heal = Math.min(want, stack.getDamageValue());
            if (heal <= 0) {
                break;
            }
            stack.setDamageValue(stack.getDamageValue() - heal);
            remaining -= heal / 2;
        }
        return Math.max(remaining, 0);
    }

    // ---- calculateTotalExperiencePoints ----
    @Unique
    public int calculateTotalExperiencePoints() {
        return this.getHandle().totalExperience;
    }

    // ---- displayName getter/setter ----
    @Unique
    public Component displayName() {
        Component stored = ApiState.get(this, "displayName", null);
        if (stored != null) {
            return stored;
        }
        String legacy = this.getDisplayName();
        return legacy == null ? Component.empty()
            : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void displayName(Component component) {
        ApiState.put(this, "displayName", component);
        this.setDisplayName(component == null ? null
            : LegacyComponentSerializer.legacySection().serialize(component));
    }

    // ---- getAffectsSpawning ----
    @Unique
    public boolean getAffectsSpawning() {
        Boolean flag = ApiState.get(this, "affectsSpawning", null);
        return flag == null || flag;
    }

    // ---- getClientBrandName ----
    @Unique
    public String getClientBrandName() {
        // vanilla 服务端不持久化客户端 brand（MC|Brand 即弃），需上层注入 ApiState
        return ApiState.get(this, "clientBrandName", null);
    }

    // ---- getClientOption ----
    @Unique
    public Object getClientOption(ClientOption option) {
        net.minecraft.server.level.ClientInformation info = this.getHandle().clientInformation();
        if (option == ClientOption.SKIN_PARTS) {
            return paperarc$skinParts((byte) info.modelCustomisation());
        }
        if (option == ClientOption.CHAT_VISIBILITY) {
            return ClientOption.ChatVisibility.valueOf(info.chatVisibility().name());
        }
        if (option == ClientOption.CHAT_COLORS_ENABLED) {
            return info.chatColors();
        }
        if (option == ClientOption.LOCALE) {
            String lang = info.language();
            return lang == null ? null : java.util.Locale.forLanguageTag(lang.replace('_', '-'));
        }
        if (option == ClientOption.VIEW_DISTANCE) {
            return info.viewDistance();
        }
        if (option == ClientOption.TEXT_FILTERING_ENABLED) {
            return info.textFilteringEnabled();
        }
        if (option == ClientOption.MAIN_HAND) {
            return MainHand.valueOf(info.mainHand().name());
        }
        if (option == ClientOption.ALLOW_SERVER_LISTINGS) {
            return info.allowsListing();
        }
        // 未知/无数据选项（如粒子可见性）返回 null
        return null;
    }

    // ---- getCooldownPeriod / getCooledAttackStrength ----
    @Unique
    public float getCooldownPeriod() {
        return this.getHandle().getCurrentItemAttackStrengthDelay();
    }

    @Unique
    public float getCooledAttackStrength(float adjustTicks) {
        return this.getHandle().getAttackStrengthScale(adjustTicks);
    }

    // ---- getExperiencePointsNeededForNextLevel ----
    @Unique
    public int getExperiencePointsNeededForNextLevel() {
        return this.getHandle().getXpNeededForNextLevel();
    }

    // ---- getHAProxyAddress ----
    @Unique
    public InetSocketAddress getHAProxyAddress() {
        // 需要 HAProxy proxy-protocol 基建在握手期保存真实地址，当前仅 side-map
        return ApiState.get(this, "haProxyAddress", null);
    }

    // ---- getIdleDuration ----
    @Unique
    public Duration getIdleDuration() {
        return Duration.ofMillis(Math.max(0L, Util.getMillis() - this.getHandle().getLastActionTime()));
    }

    // ---- getResourcePackStatus ----
    @Unique
    public PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        // spigot 收到资源包状态后只发事件不存储，需上层在事件里回填 ApiState
        return ApiState.get(this, "resourcePackStatus", null);
    }

    // ---- getSendViewDistance ----
    @Unique
    public int getSendViewDistance() {
        Integer override = ApiState.get(this, "sendViewDistance", null);
        if (override != null) {
            return override;
        }
        // vanilla 无每玩家发送距离，回退服务器级 view distance
        return paperarc$playerList().getViewDistance();
    }

    // ---- getSentChunkKeys ----
    @Unique
    public Set getSentChunkKeys() {
        // 需要 PlayerChunkLoader 发送队列基建（Paper 内部），保守返回空集合
        return new HashSet();
    }

    // ---- getSentChunks ----
    @Unique
    public Set getSentChunks() {
        // 同上，无 chunk 追踪基建，保守返回空集合
        return Collections.emptySet();
    }

    // ---- getSimulationDistance ----
    @Unique
    public int getSimulationDistance() {
        // vanilla 无每玩家模拟距离，回退服务器级 simulation distance
        return paperarc$playerList().getSimulationDistance();
    }

    // ---- helpers ----
    @Unique
    private static com.destroystokyo.paper.SkinParts paperarc$skinParts(final byte raw) {
        return new com.destroystokyo.paper.SkinParts() {
            @Override
            public boolean hasCapeEnabled() {
                return (raw & 0x01) != 0;
            }

            @Override
            public boolean hasJacketEnabled() {
                return (raw & 0x02) != 0;
            }

            @Override
            public boolean hasLeftSleeveEnabled() {
                return (raw & 0x04) != 0;
            }

            @Override
            public boolean hasRightSleeveEnabled() {
                return (raw & 0x08) != 0;
            }

            @Override
            public boolean hasLeftPantsEnabled() {
                return (raw & 0x10) != 0;
            }

            @Override
            public boolean hasRightPantsEnabled() {
                return (raw & 0x20) != 0;
            }

            @Override
            public boolean hasHatsEnabled() {
                return (raw & 0x40) != 0;
            }

            @Override
            public int getRaw() {
                return raw;
            }
        };
    }
}
