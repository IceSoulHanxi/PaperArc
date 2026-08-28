package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.ClientOption;
import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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
 * Paper API 方法补齐批次 B30（part 1）：org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
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
        return ((org.bukkit.craftbukkit.v1_20_R1.CraftServer) org.bukkit.Bukkit.getServer()).getServer().getPlayerList();
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
        // 1.20.1 mending: getRandomItemWith(Enchantment, LivingEntity) returns Map.Entry<EquipmentSlot, ItemStack>
        java.util.Map.Entry<net.minecraft.world.entity.EquipmentSlot, ItemStack> stackEntry =
            net.minecraft.world.item.enchantment.EnchantmentHelper.getRandomItemWith(
                net.minecraft.world.item.enchantment.Enchantments.MENDING, sp);
        final ItemStack itemstack = stackEntry != null ? stackEntry.getValue() : ItemStack.EMPTY;
        if (!itemstack.isEmpty() && itemstack.getItem().canBeDepleted()) {
            net.minecraft.world.entity.ExperienceOrb orb = net.minecraft.world.entity.EntityType.EXPERIENCE_ORB.create(sp.level());
            if (orb == null) {
                return remaining;
            }
            try {
                com.ixnah.mc.paperarc.bridge.PaperArcMendingAccess.VALUE_FIELD.invokeExact(orb, remaining);
            } catch (Throwable t) {
                throw new IllegalStateException("ExperienceOrb.value failed", t);
            }
            orb.setPosRaw(sp.getX(), sp.getY(), sp.getZ());

            int i = Math.min(paperarc$xpToDurability(orb, remaining), itemstack.getDamageValue());
            org.bukkit.event.player.PlayerItemMendEvent event =
                org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory.callPlayerItemMendEvent(
                    sp, orb, itemstack, stackEntry.getKey(), i);
            i = event.getRepairAmount();
            orb.discard();
            if (!event.isCancelled()) {
                remaining -= paperarc$durabilityToXp(orb, i);
                itemstack.setDamageValue(itemstack.getDamageValue() - i);
            }
        }
        return Math.max(remaining, 0);
    }

    @Unique
    private static int paperarc$xpToDurability(net.minecraft.world.entity.ExperienceOrb orb, int amount) {
        try {
            return (int) com.ixnah.mc.paperarc.bridge.PaperArcMendingAccess.XP_TO_DURABILITY.invokeExact(orb, amount);
        } catch (Throwable t) {
            throw new IllegalStateException("ExperienceOrb.xpToDurability failed", t);
        }
    }

    @Unique
    private static int paperarc$durabilityToXp(net.minecraft.world.entity.ExperienceOrb orb, int amount) {
        try {
            return (int) com.ixnah.mc.paperarc.bridge.PaperArcMendingAccess.DURABILITY_TO_XP.invokeExact(orb, amount);
        } catch (Throwable t) {
            throw new IllegalStateException("ExperienceOrb.durabilityToXp failed", t);
        }
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
        if (option == ClientOption.SKIN_PARTS) {
            return new com.ixnah.mc.paperarc.bridge.PaperArcSkinParts(
                this.getHandle().getEntityData().get(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION));
        }
        if (option == ClientOption.CHAT_VISIBILITY) {
            net.minecraft.world.entity.player.ChatVisiblity vis = this.getHandle().getChatVisibility();
            return vis == null ? ClientOption.ChatVisibility.UNKNOWN
                : ClientOption.ChatVisibility.valueOf(vis.name());
        }
        if (option == ClientOption.CHAT_COLORS_ENABLED) {
            return this.getHandle().canChatInColor();
        }
        if (option == ClientOption.LOCALE) {
            String lang = ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) (Object) this).getLocale();
            return lang == null ? null : java.util.Locale.forLanguageTag(lang.replace('_', '-'));
        }
        if (option == ClientOption.VIEW_DISTANCE) {
            return ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) (Object) this).getClientViewDistance();
        }
        if (option == ClientOption.TEXT_FILTERING_ENABLED) {
            return this.getHandle().isTextFilteringEnabled();
        }
        if (option == ClientOption.MAIN_HAND) {
            return org.bukkit.inventory.MainHand.valueOf(this.getHandle().getMainArm().name());
        }
        if (option == ClientOption.ALLOW_SERVER_LISTINGS) {
            return this.getHandle().allowsListing();
        }
        throw new RuntimeException("Unknown settings type");
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
}
