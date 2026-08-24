package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.scoreboard.numbers.FixedFormat;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import io.papermc.paper.scoreboard.numbers.StyledFormat;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's score API to CraftScore.
 *
 * CraftScore/CraftObjective are package-private final classes, so they are
 * targeted/named reflectively and never referenced as types.
 *
 * Paper refs: add-number-format-api.patch, Improve-scoreboard-entries.patch,
 * Add-API-for-resetting-a-single-score.patch.
 *
 * NMS (mojmap, javap-verified): Scoreboard#getOrCreatePlayerScore(ScoreHolder, Objective) ->
 * ScoreAccess{display/display(Component), numberFormatOverride, lock/unlock};
 * #getPlayerScoreInfo -> ReadOnlyScoreInfo{isLocked(), numberFormat()};
 * #resetSinglePlayerScore(ScoreHolder, Objective).
 *
 * CraftObjective#getHandle()/checkState() are package-private, so the NMS handle is fetched
 * through a privateLookupIn MethodHandle and the unregistered-check is replicated inline
 * ({@link #paperarc$checkBoard()}), mirroring checkState semantics.
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftScore")
public abstract class CraftScoreApiMixin {

    @Shadow
    @Final
    private net.minecraft.world.scores.ScoreHolder entry;

    /**
     * Package-private {@code CraftScore.objective}, resolved against the runtime
     * CraftBukkit class (not compile-visible); null -> degraded path below.
     */
    @Unique
    private static final MethodHandle PAPERARC$OBJ_FIELD = paperarc$buildObjectiveGetter();

    /** Package-private {@code CraftObjective#getHandle()}. */
    @Unique
    private static final MethodHandle PAPERARC$OBJ_GET_HANDLE = paperarc$buildGetHandleInvoker();

    /** Package-private {@code FixedFormat.value} NMS component backing field. */
    @Unique
    private static final MethodHandle PAPERARC$FIXED_VALUE_FIELD = paperarc$buildNumberFormatGetter(
            net.minecraft.network.chat.numbers.FixedFormat.class, "value",
            net.minecraft.network.chat.Component.class);

    /** Package-private {@code StyledFormat.style} NMS style backing field. */
    @Unique
    private static final MethodHandle PAPERARC$STYLED_STYLE_FIELD = paperarc$buildNumberFormatGetter(
            net.minecraft.network.chat.numbers.StyledFormat.class, "style",
            net.minecraft.network.chat.Style.class);

    @Unique
    private static MethodHandle paperarc$buildObjectiveGetter() {
        try {
            Class<?> craftScore = Class.forName("org.bukkit.craftbukkit.v.scoreboard.CraftScore");
            Class<?> craftObjective = Class.forName("org.bukkit.craftbukkit.v.scoreboard.CraftObjective");
            return MethodHandles.privateLookupIn(craftScore, MethodHandles.lookup())
                    .findGetter(craftScore, "objective", craftObjective);
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    @Unique
    private static MethodHandle paperarc$buildGetHandleInvoker() {
        try {
            Class<?> craftObjective = Class.forName("org.bukkit.craftbukkit.v.scoreboard.CraftObjective");
            return MethodHandles.privateLookupIn(craftObjective, MethodHandles.lookup())
                    .findVirtual(craftObjective, "getHandle",
                            MethodType.methodType(net.minecraft.world.scores.Objective.class));
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    @Unique
    private static MethodHandle paperarc$buildNumberFormatGetter(Class<?> owner, String name, Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(owner, MethodHandles.lookup())
                    .findGetter(owner, name, type);
        } catch (ReflectiveOperationException e) {
            return null; // caller degrades with IllegalStateException
        }
    }

    // ------------------------------------------------------------------ API

    @Unique
    public Component customName() {
        Scoreboard board = paperarc$checkBoard();
        ReadOnlyScoreInfo info = board.getPlayerScoreInfo(this.entry, paperarc$nmsObjective());
        if (info == null) {
            return null; // If score doesn't exist, don't create one
        }
        net.minecraft.network.chat.Component display =
                board.getOrCreatePlayerScore(this.entry, paperarc$nmsObjective()).display();
        return display == null ? null : paperarc$asAdventure(display);
    }

    @Unique
    public void customName(Component customName) {
        paperarc$checkBoard().getOrCreatePlayerScore(this.entry, paperarc$nmsObjective())
                .display(paperarc$asVanilla(customName));
    }

    @Unique
    public boolean isTriggerable() {
        if (!paperarc$isTrigger()) {
            return false;
        }
        ReadOnlyScoreInfo info = paperarc$checkBoard().getPlayerScoreInfo(this.entry, paperarc$nmsObjective());
        return info != null && !info.isLocked();
    }

    @Unique
    public void setTriggerable(boolean triggerable) {
        Preconditions.checkArgument(paperarc$isTrigger(),
                "the criteria isn't 'trigger'");
        ScoreAccess access = paperarc$checkBoard()
                .getOrCreatePlayerScore(this.entry, paperarc$nmsObjective());
        if (triggerable) {
            access.unlock();
        } else {
            access.lock();
        }
    }

    @Unique
    public NumberFormat numberFormat() {
        ReadOnlyScoreInfo info = paperarc$checkBoard().getPlayerScoreInfo(this.entry, paperarc$nmsObjective());
        if (info == null) {
            return null;
        }
        net.minecraft.network.chat.numbers.NumberFormat vanilla = info.numberFormat();
        return vanilla == null ? null : paperarc$asPaperNumberFormat(vanilla);
    }

    @Unique
    public void numberFormat(NumberFormat format) {
        ScoreAccess access = paperarc$checkBoard()
                .getOrCreatePlayerScore(this.entry, paperarc$nmsObjective());
        if (format == null) {
            access.numberFormatOverride(null);
            return;
        }
        access.numberFormatOverride(paperarc$asVanillaNumberFormat(format));
    }

    @Unique
    public void resetScore() {
        net.minecraft.world.scores.Objective obj = paperarc$nmsObjective();
        obj.getScoreboard().resetSinglePlayerScore(this.entry, obj);
    }

    // -------------------------------------------------------------- helpers

    /** MethodHandle accessor for package-private {@code CraftScore.objective}. */
    @Unique
    private static Object paperarc$craftObjectiveOf(CraftScoreApiMixin self) {
        if (PAPERARC$OBJ_FIELD == null) {
            throw new IllegalStateException("CraftScore.objective not accessible");
        }
        try {
            return PAPERARC$OBJ_FIELD.invoke(self);
        } catch (Throwable t) {
            throw new IllegalStateException("CraftScore.objective not accessible", t);
        }
    }

    /**
     * Replicates package-private CraftObjective#checkState() for this score:
     * resolves the NMS handle and throws IllegalStateException when the owning
     * objective is no longer registered on its scoreboard.
     */
    @Unique
    private Scoreboard paperarc$checkBoard() {
        net.minecraft.world.scores.Objective obj = paperarc$nmsObjective();
        Scoreboard board = obj.getScoreboard();
        if (!board.getObjectives().contains(obj)) {
            throw new IllegalStateException("Unregistered scoreboard component");
        }
        return board;
    }

    /** Whether the owning objective uses the vanilla trigger criterion (bukkit Criteria.TRIGGER). */
    @Unique
    private boolean paperarc$isTrigger() {
        return net.minecraft.world.scores.criteria.ObjectiveCriteria.TRIGGER.getName()
                .equals(paperarc$nmsObjective().getCriteria().getName());
    }

    /** MethodHandle accessor for package-private CraftObjective#getHandle(). */
    @Unique
    private net.minecraft.world.scores.Objective paperarc$nmsObjective() {
        if (PAPERARC$OBJ_GET_HANDLE == null) {
            throw new IllegalStateException("CraftObjective.getHandle not accessible");
        }
        try {
            return (net.minecraft.world.scores.Objective) PAPERARC$OBJ_GET_HANDLE
                    .invoke(paperarc$craftObjectiveOf(this));
        } catch (Throwable t) {
            throw new IllegalStateException("CraftObjective.getHandle not accessible", t);
        }
    }

    @Unique
    private static Component paperarc$asAdventure(net.minecraft.network.chat.Component vanilla) {
        return GsonComponentSerializer.gson().deserialize(Serializer.toJson(vanilla,
                ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess()));
    }

    @Unique
    private static net.minecraft.network.chat.Component paperarc$asVanilla(Component adventure) {
        return Serializer.fromJson(GsonComponentSerializer.gson().serialize(adventure),
                ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
    }

    @Unique
    private static net.kyori.adventure.text.format.Style paperarc$styleAsAdventure(
            net.minecraft.network.chat.Style vanillaStyle) {
        return GsonComponentSerializer.gson()
                .deserialize(Serializer.toJson(
                        net.minecraft.network.chat.Component.empty().withStyle(vanillaStyle),
                        ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess()))
                .style();
    }

    @Unique
    private static net.minecraft.network.chat.Style paperarc$styleAsVanilla(
            net.kyori.adventure.text.format.Style adventureStyle) {
        return Serializer.fromJson(GsonComponentSerializer.gson().serialize(
                        Component.empty().style(adventureStyle)),
                ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess()).getStyle();
    }

    @Unique
    private static net.minecraft.network.chat.numbers.NumberFormat paperarc$asVanillaNumberFormat(NumberFormat format) {
        if (format instanceof FixedFormat fixed) {
            return new net.minecraft.network.chat.numbers.FixedFormat(paperarc$asVanilla(fixed.component()));
        }
        if (format instanceof StyledFormat styled) {
            return new net.minecraft.network.chat.numbers.StyledFormat(paperarc$styleAsVanilla(styled.style()));
        }
        return net.minecraft.network.chat.numbers.BlankFormat.INSTANCE;
    }

    @Unique
    private static NumberFormat paperarc$asPaperNumberFormat(net.minecraft.network.chat.numbers.NumberFormat vanilla) {
        if (vanilla instanceof net.minecraft.network.chat.numbers.FixedFormat fixed) {
            return NumberFormat.fixed(paperarc$asAdventure(paperarc$fixedValue(fixed)));
        }
        if (vanilla instanceof net.minecraft.network.chat.numbers.StyledFormat styled) {
            return NumberFormat.styled(paperarc$styleAsAdventure(paperarc$styledStyle(styled)));
        }
        return NumberFormat.blank();
    }

    /** Reads package-private {@code FixedFormat.value}. */
    @Unique
    private static net.minecraft.network.chat.Component paperarc$fixedValue(
            net.minecraft.network.chat.numbers.FixedFormat format) {
        if (PAPERARC$FIXED_VALUE_FIELD == null) {
            throw new IllegalStateException("FixedFormat.value not readable");
        }
        try {
            return (net.minecraft.network.chat.Component) PAPERARC$FIXED_VALUE_FIELD.invokeExact(format);
        } catch (Throwable t) {
            throw new IllegalStateException("FixedFormat.value not readable", t);
        }
    }

    /** Reads package-private {@code StyledFormat.style}. */
    @Unique
    private static net.minecraft.network.chat.Style paperarc$styledStyle(
            net.minecraft.network.chat.numbers.StyledFormat format) {
        if (PAPERARC$STYLED_STYLE_FIELD == null) {
            throw new IllegalStateException("StyledFormat.style not readable");
        }
        try {
            return (net.minecraft.network.chat.Style) PAPERARC$STYLED_STYLE_FIELD.invokeExact(format);
        } catch (Throwable t) {
            throw new IllegalStateException("StyledFormat.style not readable", t);
        }
    }
}
