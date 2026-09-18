package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityDeathBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcDeathEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Paper {@code Improve-death-events.patch} 的触发点一侧（gaps.md §3.1「死亡一族」）。
 * 三件事：
 *
 * <ol>
 *   <li><b>把死亡音效从 {@code hurt} 挪进事件。</b> vanilla 在
 *       {@code LivingEntity#hurt} 里 {@code if (flag) this.makeSound(this.getDeathSound())}，
 *       那时候事件还没派发，插件改不了。这里把那一次 {@code makeSound} 掐掉，
 *       改由 {@code ArclightEventFactoryDeathMixin} 在事件之后按事件上的四个属性补放。
 *       同时按 Paper 记下 {@code silentDeath = !flag}，供
 *       {@code EntityDeathEvent#shouldPlayDeathSound()} 的初值。</li>
 *   <li><b>装备快照。</b> {@code dropAllDeathLoot → dropEquipment} 在事件之前就把装备槽清了，
 *       取消死亡必须还原。Paper 走的是"延迟清空"（{@code clearedEquipmentSlots}），
 *       我们改成在 {@code die} 的 HEAD 拍一张快照，取消时写回 —— 对插件可见的结果相同，
 *       且不需要改 {@code Mob#dropEquipment} 那一层。</li>
 *   <li><b>取消消费。</b> {@code dropAllDeathLoot} 之后把事件读回来，取消则还原血量/装备、
 *       清掉 {@code dead} 标志并跳过 {@code die} 的后半段
 *       （createWitherRose / broadcastEntityEvent(3) / setPose(DYING)）。</li>
 * </ol>
 *
 * <p><b>与 Paper 的差异（已知，记在 gaps.md）</b>：Paper 把 {@code awardKillScore} /
 * {@code killedEntity} / {@code gameEvent(ENTITY_DIE)} 整体挪到了事件之后，取消时一并跳过；
 * Arclight 的 {@code die} 没有那次重排，这三件事在 {@code dropAllDeathLoot} 之前就发生了，
 * 取消也收不回来。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathMixin implements LivingEntityDeathBridge {

    @Shadow
    protected boolean dead;

    @Unique
    private boolean paperarc$silentDeath;

    @Unique
    private EntityDeathEvent paperarc$deathEvent;

    @Unique
    private ItemStack[] paperarc$deathEquipment;

    @Unique
    private List<org.bukkit.inventory.ItemStack> paperarc$cancelledDrops;

    @Unique
    private List<org.bukkit.inventory.ItemStack> paperarc$keptItems;

    @Override
    public boolean paperarc$isSilentDeath() {
        return this.paperarc$silentDeath;
    }

    @Override
    public void paperarc$setSilentDeath(boolean silentDeath) {
        this.paperarc$silentDeath = silentDeath;
    }

    @Override
    public EntityDeathEvent paperarc$getDeathEvent() {
        return this.paperarc$deathEvent;
    }

    @Override
    public void paperarc$setDeathEvent(EntityDeathEvent event) {
        this.paperarc$deathEvent = event;
    }

    @Override
    public ItemStack[] paperarc$getDeathEquipment() {
        return this.paperarc$deathEquipment;
    }

    @Override
    public void paperarc$setDeathEquipment(ItemStack[] equipment) {
        this.paperarc$deathEquipment = equipment;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> paperarc$getCancelledDrops() {
        return this.paperarc$cancelledDrops;
    }

    @Override
    public void paperarc$setCancelledDrops(List<org.bukkit.inventory.ItemStack> drops) {
        this.paperarc$cancelledDrops = drops;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> paperarc$getKeptItems() {
        return this.paperarc$keptItems;
    }

    @Override
    public void paperarc$setKeptItems(List<org.bukkit.inventory.ItemStack> items) {
        this.paperarc$keptItems = items;
    }

    /**
     * Paper 写的是 {@code silentDeath = !flag1}（{@code flag1} 是 {@code hurt} 里"该不该出声"
     * 的局部）。这里不取局部变量 —— 它的 LVT 槽位**三个加载器不一样**
     * （fabric 槽 6、neoforge 槽 7，`@Local(index=…)` 在 neoforge 上直接 `Scanned 0`），
     * 改成用两个调用点的先后关系表达同一件事：
     * {@code checkTotemDeathProtection} 是死亡分支里无条件先走的一步（默认置"不响"），
     * {@code makeSound} 只在 {@code flag1} 为真时才到（置回"会响"）。
     */
    @WrapOperation(method = "hurt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean paperarc$markSilentDeath(LivingEntity self, DamageSource source, Operation<Boolean> original) {
        ((LivingEntityDeathBridge) self).paperarc$setSilentDeath(true);
        return original.call(self, source);
    }

    /** vanilla 的死亡音效：掐掉，改由事件之后放（见类注释第 1 条）；同时记下"本来会响"。 */
    @WrapOperation(method = "hurt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;makeSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void paperarc$suppressVanillaDeathSound(LivingEntity self, SoundEvent sound, Operation<Void> original) {
        ((LivingEntityDeathBridge) self).paperarc$setSilentDeath(false);
        // 故意不调 original：音效改由 EntityDeathEvent 之后补放。
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void paperarc$snapshotEquipment(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide) {
            this.paperarc$deathEquipment = PaperarcDeathEvents.snapshotEquipment(self);
        }
    }

    @Inject(method = "die",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void paperarc$consumeDeathEvent(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (PaperarcDeathEvents.revertDeath(self, source)) {
            this.dead = false;
            ci.cancel();
        }
    }
}
