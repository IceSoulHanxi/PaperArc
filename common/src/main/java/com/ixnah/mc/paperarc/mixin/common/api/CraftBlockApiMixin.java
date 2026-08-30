package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.SoundGroup;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_20_R1.CraftSoundGroup;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
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
@Mixin(org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock.class)
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

    @Unique
    public String getTranslationKey() {
        return this.getNMS().getBlock().getDescriptionId();
    }

    /**
     * vanilla 无公开 getExpDrop（Paper 用 AT 加宽），等价语义：
     * 调 protected BlockBehaviour#spawnAfterBreak(state, level, pos, tool, true) 落经验
     * （AT 加宽 m_213646_ 后直接调用）。
     */
    @Unique
    private void paperarc$spawnAfterBreak(Block block, ServerLevel serverLevel,
                                          net.minecraft.world.level.block.state.BlockState state,
                                          net.minecraft.world.item.ItemStack tool) {
        block.spawnAfterBreak(state, serverLevel, this.position, tool, true);
    }

    /** TurtleEggBlock#decreaseEggs 为 private（AT 加宽 m_57791_ 后直接调用）。 */
    @Unique
    private void paperarc$turtleDecreaseEggs(TurtleEggBlock eggBlock, Level level,
                                             net.minecraft.world.level.block.state.BlockState state) {
        eggBlock.decreaseEggs(level, this.position, state);
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
        return new com.ixnah.mc.paperarc.bridge.PaperarcApiBlockSoundGroup((CraftSoundGroup) this.getBlockSoundGroup());
    }


    @Unique
    public Biome getComputedBiome() {
        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry =
            this.world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        return org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock.biomeBaseToBiome(registry,
            this.world.getNoiseBiome(
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
        // 1.20.1 CraftBlockStates has no (LevelAccessor, BlockPos, BlockState, CompoundTag)
        // overload; the (BlockPos, BlockState, CompoundTag) form carries the same semantics.
        return CraftBlockStates.getBlockState(this.position, this.getNMS(), (CompoundTag) null);
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
        return this.getNMS().getBlock().hasCollision; // AT 加宽 f_60443_ 后直访
    }

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
