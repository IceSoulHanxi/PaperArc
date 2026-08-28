package com.ixnah.mc.paperarc.mixin.common.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.world.StructuresLocateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.bukkit.craftbukkit.v.generator.structure.CraftStructure;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

/**
 * Fires Paper's {@link StructuresLocateEvent} at the head of
 * {@link ChunkGenerator#findNearestMapStructure}, mirroring the Paper patch:
 * cancel -> null result, explicit Result -> short-circuit return, otherwise the
 * (possibly mutated) origin/radius/structures/findUnexplored values are passed
 * into the vanilla search via {@code Operation}.
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorStructuresLocateMixin {

    @WrapMethod(method = "findNearestMapStructure")
    private Pair<BlockPos, Holder<Structure>> paperarc$structuresLocate(ServerLevel world, HolderSet<Structure> structures,
                                                                        BlockPos center, int radius, boolean skipReferencedStructures,
                                                                        Operation<Pair<BlockPos, Holder<Structure>>> original) {
        final org.bukkit.World bukkitWorld = PaperArcBridge.bukkitWorld(world);
        final org.bukkit.Location origin = new org.bukkit.Location(bukkitWorld, center.getX(), center.getY(), center.getZ());
        final Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        final List<org.bukkit.generator.structure.Structure> apiStructures = new ArrayList<>();
        for (Holder<Structure> holder : structures) {
            apiStructures.add(CraftStructure.minecraftToBukkit(holder.value(), world.registryAccess()));
        }
        if (apiStructures.isEmpty()) {
            return original.call(world, structures, center, radius, skipReferencedStructures);
        }
        final StructuresLocateEvent event = new StructuresLocateEvent(bukkitWorld, origin, apiStructures, radius, skipReferencedStructures);
        if (!event.callEvent()) {
            return null;
        }
        final StructuresLocateEvent.Result result = event.getResult();
        if (result != null) {
            final BlockPos pos = new BlockPos(result.pos().blockX(), result.pos().blockY(), result.pos().blockZ());
            return Pair.of(pos, registry.wrapAsHolder(CraftStructure.bukkitToMinecraft(result.structure())));
        }
        final org.bukkit.Location newOrigin = event.getOrigin();
        final BlockPos newCenter = new BlockPos(newOrigin.getBlockX(), newOrigin.getBlockY(), newOrigin.getBlockZ());
        final List<Holder<Structure>> holders = new ArrayList<>();
        for (org.bukkit.generator.structure.Structure api : event.getStructures()) {
            holders.add(registry.wrapAsHolder(CraftStructure.bukkitToMinecraft(api)));
        }
        return original.call(world, HolderSet.direct(holders), newCenter, event.getRadius(), event.shouldFindUnexplored());
    }
}
