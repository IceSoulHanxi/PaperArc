package dev.paperarc.mixin.common.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.paperarc.bridge.ApiState;
import dev.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's HumanEntity additions to CraftHumanEntity.
 *
 * Paper refs: patches/server/{InventoryCloseEvent-Reason-API,Add-Player-getFishHook,
 * Potential-bed-API,add-isDeeplySleeping-to-HumanEntity,Shoulder-Entities-Release-API,
 * Expose-LivingEntity-hurt-direction,Add-additional-open-container-api-to-HumanEntity,
 * Add-openSign-method-to-HumanEntity}.patch.
 *
 * Mapping notes vs Paper source:
 * - Spigot NMS has no {@code closeContainer(Reason)} overload (Paper-added): the reason
 *   parameter is accepted but the close falls back to the vanilla path, so the fired
 *   InventoryCloseEvent reports UNKNOWN.
 * - The six open*Table/Anvil methods replicate Paper's openInventory helper with vanilla
 *   menus + SimpleMenuProvider instead of the protected Block#getMenuProvider; titles use
 *   each block's vanilla translation key. checkReachable / getBukkitView are CraftBukkit
 *   runtime-only members on AbstractContainerMenu -> reflective access.
 * - 1.21.1 mojmap LivingEntity has no hurtDir storage field (removed upstream); Paper
 *   re-adds it in its own patches which are absent from Arclight -> ApiState side-map.
 * - Shoulder release: vanilla only clears the tag; spawning is done here at the player's
 *   position (Paper spawns via its patched ServerPlayer#release*ShoulderEntity).
 */
@Mixin(org.bukkit.craftbukkit.v.entity.CraftHumanEntity.class)
public abstract class CraftHumanEntityApiMixin {

    @Shadow
    public abstract Player getHandle();

    @Unique
    private static final MethodHandle PAPERARC$CLOSE_CONTAINER = paperarc$buildCloseHandle();

    @Unique
    private static MethodHandle paperarc$buildCloseHandle() {
        try {
            return MethodHandles.privateLookupIn(net.minecraft.world.entity.player.Player.class, MethodHandles.lookup())
                    .findVirtual(net.minecraft.world.entity.player.Player.class, "closeContainer",
                            MethodType.methodType(void.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Spigot-patched {@code AbstractContainerMenu.checkReachable} (runtime-only,
     * absent from the vanilla compile jar); null -> degraded path below.
     */
    @Unique
    private static final MethodHandle PAPERARC$CHECK_REACHABLE_FIELD = paperarc$buildCheckReachableHandle();

    /** Spigot-patched {@code AbstractContainerMenu#getBukkitView} (runtime-only). */
    @Unique
    private static final MethodHandle PAPERARC$GET_BUKKIT_VIEW_METHOD = paperarc$buildGetBukkitViewHandle();

    /** Protected vanilla/spigot {@code Player#setShoulderEntityLeft(CompoundTag)}. */
    @Unique
    private static final MethodHandle PAPERARC$SET_SHOULDER_LEFT_METHOD = paperarc$buildShoulderHandle(false);

    /** Protected vanilla/spigot {@code Player#setShoulderEntityRight(CompoundTag)}. */
    @Unique
    private static final MethodHandle PAPERARC$SET_SHOULDER_RIGHT_METHOD = paperarc$buildShoulderHandle(true);

    @Unique
    private static MethodHandle paperarc$buildCheckReachableHandle() {
        try {
            return MethodHandles.privateLookupIn(AbstractContainerMenu.class, MethodHandles.lookup())
                    .findSetter(AbstractContainerMenu.class, "checkReachable", boolean.class);
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    @Unique
    private static MethodHandle paperarc$buildGetBukkitViewHandle() {
        try {
            return MethodHandles.privateLookupIn(AbstractContainerMenu.class, MethodHandles.lookup())
                    .findVirtual(AbstractContainerMenu.class, "getBukkitView",
                            MethodType.methodType(InventoryView.class));
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    @Unique
    private static MethodHandle paperarc$buildShoulderHandle(boolean right) {
        try {
            return MethodHandles.privateLookupIn(Player.class, MethodHandles.lookup())
                    .findVirtual(Player.class, right ? "setShoulderEntityRight" : "setShoulderEntityLeft",
                            MethodType.methodType(void.class, CompoundTag.class));
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    @Unique
    public void closeInventory(InventoryCloseEvent.Reason reason) {
        // degraded: spigot NMS lacks ServerPlayer.closeContainer(Reason); the reason is not
        // plumbed into the internally-fired InventoryCloseEvent.
        // Player.closeContainer() is public in vanilla but protected in spigot NMS, so a
        // plain call does not compile against both. Per project rule (prefer MethodHandles
        // over reflection), a single MethodHandle is built once via privateLookupIn and is
        // fully JIT-inlinable at the call site.
        try {
            PAPERARC$CLOSE_CONTAINER.invokeExact(this.getHandle());
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot invoke Player.closeContainer", t);
        }
    }

    @Unique
    public org.bukkit.entity.FishHook getFishHook() {
        FishingHook hook = this.getHandle().fishing;
        if (hook == null || !hook.isAlive()) {
            return null;
        }
        return PaperArcBridge.<org.bukkit.entity.FishHook>bukkitEntity(hook);
    }

    @Unique
    public Location getPotentialBedLocation() {
        if (!(this.getHandle() instanceof ServerPlayer player)) {
            return null;
        }
        BlockPos bed = player.getRespawnPosition();
        if (bed == null) {
            return null;
        }
        ResourceKey<Level> dimension = player.getRespawnDimension();
        ServerLevel level = player.server.getLevel(dimension);
        if (level == null) {
            return null;
        }
        return new Location(PaperArcBridge.bukkitWorld(level), bed.getX(), bed.getY(), bed.getZ());
    }

    @Unique
    public boolean isDeeplySleeping() {
        return this.getHandle().isSleepingLongEnough();
    }

    @Unique
    public InventoryView openAnvil(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.ANVIL);
    }

    @Unique
    public InventoryView openCartographyTable(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.CARTOGRAPHY_TABLE);
    }

    @Unique
    public InventoryView openGrindstone(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.GRINDSTONE);
    }

    @Unique
    public InventoryView openLoom(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.LOOM);
    }

    @Unique
    public InventoryView openSmithingTable(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.SMITHING_TABLE);
    }

    @Unique
    public InventoryView openStonecutter(Location location, boolean force) {
        return this.paperarc$openInventory(location, force, Material.STONECUTTER);
    }

    @Unique
    public void openSign(org.bukkit.block.Sign sign, Side side) {
        Preconditions.checkArgument(sign != null, "sign cannot be null");
        Preconditions.checkArgument(side != null, "side cannot be null");
        if (!(this.getHandle() instanceof ServerPlayer)) {
            throw new UnsupportedOperationException("Cannot open a sign for a non-player human entity");
        }
        CraftBlock craftBlock = (CraftBlock) sign.getBlock();
        ServerLevel level = ((CraftWorld) craftBlock.getWorld()).getHandle();
        BlockEntity blockEntity = level.getBlockEntity(craftBlock.getPosition());
        Preconditions.checkState(blockEntity instanceof SignBlockEntity,
                "The sign at %s is no longer a sign", craftBlock.getPosition());
        ((ServerPlayer) this.getHandle()).openTextEdit((SignBlockEntity) blockEntity, side == Side.FRONT);
    }

    @Unique
    public org.bukkit.entity.Entity releaseLeftShoulderEntity() {
        Player handle = this.getHandle();
        CompoundTag tag = handle.getShoulderEntityLeft();
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        net.minecraft.world.entity.Entity released = paperarc$spawnFromShoulder(handle, tag);
        paperarc$setShoulder(handle, false, new CompoundTag());
        return released == null ? null : PaperArcBridge.<org.bukkit.entity.Entity>bukkitEntity(released);
    }

    @Unique
    public org.bukkit.entity.Entity releaseRightShoulderEntity() {
        Player handle = this.getHandle();
        CompoundTag tag = handle.getShoulderEntityRight();
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        net.minecraft.world.entity.Entity released = paperarc$spawnFromShoulder(handle, tag);
        paperarc$setShoulder(handle, true, new CompoundTag());
        return released == null ? null : PaperArcBridge.<org.bukkit.entity.Entity>bukkitEntity(released);
    }

    @Unique
    public void setHurtDirection(float hurtDirection) {
        // side-map: 1.21.1 vanilla has no hurtDir storage field on LivingEntity and
        // Arclight does not carry Paper's re-added field; nothing reads it back yet
        ApiState.put(this, "hurtDirection", hurtDirection);
    }

    // ------------------------------------------------------------------
    // helpers

    /**
     * Replicates Paper's private openInventory(Location, boolean, Material) using vanilla
     * menus + SimpleMenuProvider because Block#getMenuProvider is protected and its CB
     * runtime behaviour is not visible at compile time.
     */
    @Unique
    private InventoryView paperarc$openInventory(Location location, boolean force, Material material) {
        if (!(this.getHandle() instanceof ServerPlayer player)) {
            throw new UnsupportedOperationException("Cannot open an inventory for a non-player human entity");
        }
        if (location == null) {
            location = ((CraftEntity) (Object) this).getLocation();
        }
        if (!force && location.getBlock().getType() != material) {
            return null;
        }
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, pl) -> paperarc$menu(material, containerId, inventory, access),
                net.minecraft.network.chat.Component.translatable(paperarc$titleKey(material)));
        player.openMenu(provider);
        if (PAPERARC$CHECK_REACHABLE_FIELD == null || PAPERARC$GET_BUKKIT_VIEW_METHOD == null) {
            throw new IllegalStateException("PaperArc: CraftBukkit container members unavailable");
        }
        AbstractContainerMenu menu = player.containerMenu;
        try {
            PAPERARC$CHECK_REACHABLE_FIELD.invokeExact(menu, !force);
            return (InventoryView) PAPERARC$GET_BUKKIT_VIEW_METHOD.invokeExact(menu);
        } catch (Throwable t) {
            throw new IllegalStateException("PaperArc: CraftBukkit container members unavailable", t);
        }
    }

    @Unique
    private static AbstractContainerMenu paperarc$menu(Material material, int containerId,
            net.minecraft.world.entity.player.Inventory inventory, ContainerLevelAccess access) {
        return switch (material) {
            case ANVIL -> new AnvilMenu(containerId, inventory, access);
            case CARTOGRAPHY_TABLE -> new CartographyTableMenu(containerId, inventory, access);
            case GRINDSTONE -> new GrindstoneMenu(containerId, inventory, access);
            case LOOM -> new LoomMenu(containerId, inventory, access);
            case SMITHING_TABLE -> new SmithingMenu(containerId, inventory, access);
            case STONECUTTER -> new StonecutterMenu(containerId, inventory, access);
            default -> throw new IllegalArgumentException("Unsupported inventory type: " + material);
        };
    }

    @Unique
    private static String paperarc$titleKey(Material material) {
        // vanilla menu title keys of the corresponding blocks
        return switch (material) {
            case ANVIL -> "container.repair";
            case CARTOGRAPHY_TABLE -> "container.cartography_table";
            case GRINDSTONE -> "container.grindstone_title";
            case LOOM -> "container.loom";
            case SMITHING_TABLE -> "container.upgrade";
            case STONECUTTER -> "container.stonecutter";
            default -> throw new IllegalArgumentException("Unsupported inventory type: " + material);
        };
    }

    @Unique
    private static net.minecraft.world.entity.Entity paperarc$spawnFromShoulder(Player player,
            CompoundTag tag) {
        Optional<net.minecraft.world.entity.Entity> created =
                EntityType.create(tag, player.level());
        net.minecraft.world.entity.Entity entity = created.orElse(null);
        if (entity != null) {
            // approximation of Paper's spawn logic: release at the player's position
            entity.setPos(player.getX(), player.getY() + 0.7D, player.getZ());
            player.level().addFreshEntity(entity);
        }
        return entity;
    }

    @Unique
    private static void paperarc$setShoulder(Player player, boolean right, CompoundTag value) {
        MethodHandle handle = right ? PAPERARC$SET_SHOULDER_RIGHT_METHOD : PAPERARC$SET_SHOULDER_LEFT_METHOD;
        if (handle == null) {
            throw new IllegalStateException("PaperArc: Player shoulder setter not found");
        }
        try {
            handle.invokeExact(player, value);
        } catch (Throwable t) {
            throw new IllegalStateException("PaperArc: Player shoulder setter not found", t);
        }
    }
}
