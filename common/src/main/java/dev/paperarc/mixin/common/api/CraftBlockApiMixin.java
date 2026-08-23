package dev.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.SoundGroup;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.CraftSoundGroup;
import org.bukkit.craftbukkit.v.block.CraftBiome;
import org.bukkit.craftbukkit.v.block.CraftBlockStates;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * B19: 补齐 paper-api {@code org.bukkit.block.Block} 相对 Arclight CraftBlock
 * 缺失的抽象方法（运行时会 AbstractMethodError）。实现参照 Paper 1.21.1
 * 对应 server 补丁，NMS 调用全部为 mojmap。注册清单由集成者统一处理。
 */
@Mixin(org.bukkit.craftbukkit.v.block.CraftBlock.class)
public abstract class CraftBlockApiMixin {

    @Shadow
    @Final
    private LevelAccessor world;

    @Shadow
    @Final
    private BlockPos position;

    @Shadow
    public abstract net.minecraft.world.level.block.state.BlockState getNMS();

    @Shadow
    public abstract org.bukkit.block.BlockState getState();

    @Shadow
    public abstract java.util.Collection<ItemStack> getDrops(ItemStack itemStack);

    @Unique
    private Level paperarc$asLevel() {
        return this.world instanceof Level level ? level : null;
    }

    @Unique
    private ServerLevel paperarc$asServerLevel() {
        return this.world instanceof ServerLevel serverLevel ? serverLevel : null;
    }

    @Unique
    public boolean breakNaturally(boolean triggerEffect, boolean dropExperience) {
        return this.breakNaturally(null, triggerEffect, dropExperience);
    }

    @Unique
    public boolean breakNaturally(org.bukkit.inventory.ItemStack item, boolean triggerEffect, boolean dropExperience) {
        // 参照 Paper「Improve-Block-breakNaturally-API」补丁，NMS 调用翻成 mojmap
        net.minecraft.world.level.block.state.BlockState iblockdata = this.getNMS();
        Block block = iblockdata.getBlock();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        boolean result = false;
        Level level = this.paperarc$asLevel();

        // Modelled off EntityHuman#hasBlock（Spigot 原体逻辑）
        if (block != Blocks.AIR && (item == null || !iblockdata.requiresCorrectToolForDrops() || nmsItem.isCorrectToolForDrops(iblockdata))) {
            BlockEntity tileentity = this.world.getBlockEntity(this.position);
            Block.dropResources(iblockdata, this.world, this.position, tileentity);
            if (triggerEffect) {
                if (block instanceof BaseFireBlock) {
                    this.world.levelEvent(1509 /* SOUND_EXTINGUISH_FIRE */, this.position, 0);
                } else {
                    this.world.levelEvent(2001 /* PARTICLES_DESTROY_BLOCK */, this.position, Block.getId(iblockdata));
                }
            }
            if (dropExperience) {
                ServerLevel serverLevel = this.paperarc$asServerLevel();
                if (serverLevel != null) {
                    this.paperarc$spawnAfterBreak(block, serverLevel, iblockdata, nmsItem);
                }
            }
            result = true;
        }

        boolean destroyed = this.world.removeBlock(this.position, false);
        if (destroyed) {
            block.destroy(this.world, this.position, iblockdata);
        }
        if (result && level != null) {
            // special cases（Paper 同款特判）
            if (block instanceof IceBlock) {
                level.setBlockAndUpdate(this.position, IceBlock.meltsInto());
            } else if (block instanceof TurtleEggBlock turtleEggBlock) {
                this.paperarc$turtleDecreaseEggs(turtleEggBlock, level, iblockdata);
            }
        }
        return destroyed && result;
    }

    /**
     * vanilla 无公开 getExpDrop（Paper 用 AT 加宽），等价语义：
     * 反射调 protected BlockBehaviour#spawnAfterBreak(state, level, pos, tool, true) 落经验。
     */
    @Unique
    private void paperarc$spawnAfterBreak(Block block, ServerLevel serverLevel,
                                          net.minecraft.world.level.block.state.BlockState state,
                                          net.minecraft.world.item.ItemStack tool) {
        try {
            Method cached = paperarc$spawnAfterBreakMethod;
            if (cached == null) {
                cached = net.minecraft.world.level.block.state.BlockBehaviour.class.getDeclaredMethod("spawnAfterBreak",
                    net.minecraft.world.level.block.state.BlockState.class, ServerLevel.class, BlockPos.class,
                    net.minecraft.world.item.ItemStack.class, boolean.class);
                cached.setAccessible(true);
                paperarc$spawnAfterBreakMethod = cached;
            }
            cached.invoke(block, state, serverLevel, this.position, tool, true);
        } catch (ReflectiveOperationException ignored) {
            // 反射失败时跳过经验掉落，不影响方块破坏主流程
        }
    }

    @Unique
    private static volatile Method paperarc$spawnAfterBreakMethod;

    /** TurtleEggBlock#decreaseEggs 为 private，反射调用；失败时静默降级。 */
    @Unique
    private void paperarc$turtleDecreaseEggs(TurtleEggBlock eggBlock, Level level,
                                             net.minecraft.world.level.block.state.BlockState state) {
        try {
            Method method = TurtleEggBlock.class.getDeclaredMethod("decreaseEggs",
                Level.class, BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
            method.setAccessible(true);
            method.invoke(eggBlock, level, this.position, state);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void tick() {
        ServerLevel serverLevel = this.paperarc$asServerLevel();
        if (serverLevel == null) {
            return; // 非 ServerLevel（生成中等）无法执行计划刻
        }
        this.getNMS().tick(serverLevel, this.position, serverLevel.random);
    }

    @Unique
    public void randomTick() {
        ServerLevel serverLevel = this.paperarc$asServerLevel();
        if (serverLevel == null) {
            return; // 非 ServerLevel（生成中等）无法执行随机刻
        }
        this.getNMS().randomTick(serverLevel, this.position, serverLevel.random);
    }

    @Unique
    public void fluidTick() {
        Level level = this.paperarc$asLevel();
        if (level == null) {
            return; // 非 Level 世界无法执行流体刻
        }
        this.getNMS().getFluidState().tick(level, this.position);
    }

    @Unique
    public SoundGroup getBlockSoundGroup() {
        return CraftSoundGroup.getSoundGroup(this.getNMS().getSoundType());
    }

    @Unique
    public com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() {
        // deobf jar 无 CraftBlockSoundGroup（Paper 侧类），内置最小实现委托 CraftSoundGroup
        return new PaperarcApiBlockSoundGroup((CraftSoundGroup) this.getBlockSoundGroup());
    }

    /** paper-api com.destroystokyo.paper.block.BlockSoundGroup 的最小实现。 */
    public static class PaperarcApiBlockSoundGroup implements com.destroystokyo.paper.block.BlockSoundGroup {

        private final SoundGroup handle;

        public PaperarcApiBlockSoundGroup(SoundGroup handle) {
            this.handle = handle;
        }

        @Override
        public org.bukkit.Sound getBreakSound() {
            return this.handle.getBreakSound();
        }

        @Override
        public org.bukkit.Sound getStepSound() {
            return this.handle.getStepSound();
        }

        @Override
        public org.bukkit.Sound getPlaceSound() {
            return this.handle.getPlaceSound();
        }

        @Override
        public org.bukkit.Sound getHitSound() {
            return this.handle.getHitSound();
        }

        @Override
        public org.bukkit.Sound getFallSound() {
            return this.handle.getFallSound();
        }
    }

    @Unique
    public Biome getComputedBiome() {
        return CraftBiome.minecraftHolderToBukkit(this.world.getNoiseBiome(
            QuartPos.fromBlock(this.position.getX()),
            QuartPos.fromBlock(this.position.getY()),
            QuartPos.fromBlock(this.position.getZ())));
    }

    /**
     * deobf jar 无 CraftBlockStates#getBlockState(*, boolean)（Paper 侧重载），
     * useSnapshot=false 无法真正关快照：退化为按当前 NMS 状态新建 state。
     */
    @Unique
    public org.bukkit.block.BlockState getState(boolean useSnapshot) {
        if (useSnapshot) {
            return this.getState();
        }
        return CraftBlockStates.getBlockState(this.world, this.position, this.getNMS(), (CompoundTag) null);
    }

    // Paper: This is in fact isSolid, despite the fact that isSolid below returns blocksMotion
    @Unique
    public boolean isBuildable() {
        return this.getNMS().isSolid();
    }

    @Unique
    public boolean isBurnable() {
        return this.getNMS().ignitedByLava();
    }

    @Unique
    public boolean isCollidable() {
        try {
            Field field = paperarc$hasCollisionField;
            if (field == null) {
                field = net.minecraft.world.level.block.state.BlockBehaviour.class.getDeclaredField("hasCollision");
                field.setAccessible(true);
                paperarc$hasCollisionField = field;
            }
            return field.getBoolean(this.getNMS().getBlock());
        } catch (ReflectiveOperationException e) {
            return false; // 反射失败降级为无碰撞
        }
    }

    @Unique
    private static volatile Field paperarc$hasCollisionField;

    @Unique
    public boolean isReplaceable() {
        return this.getNMS().canBeReplaced();
    }

    @Unique
    public boolean isSolid() {
        return this.getNMS().blocksMotion();
    }

    @Unique
    public boolean isValidTool(org.bukkit.inventory.ItemStack itemStack) {
        // Paper 同款：能掉落物即视为有效工具（getDrops(ItemStack) 为宿主已有方法）
        return !this.getDrops(itemStack).isEmpty();
    }
}
