package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.scoreboard.numbers.FixedFormat;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import io.papermc.paper.scoreboard.numbers.StyledFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.scoreboard.Score;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Adventure/scoreboard API to CraftObjective.
 *
 * CraftObjective is a package-private final class, so it is targeted by name
 * ({@code targets = "..."}) and never referenced as a type.
 *
 * Paper refs: Adventure.patch, add-number-format-api.patch,
 * Improve-scoreboard-entries.patch, add-more-scoreboard-API.patch.
 *
 * NMS Objective (mojmap, verified via javap): getDisplayName()/setDisplayName(Component),
 * numberFormat()/setNumberFormat(NumberFormat), displayAutoUpdate()/setDisplayAutoUpdate(boolean).
 * Number-format and Style conversions are done here via gson round-trips because
 * Paper's io.papermc.paper.util.PaperScoreboardFormat / PaperAdventure are server-only classes.
 *
 * CraftObjective#checkState() is package-private; its IllegalStateException-on-unregistered
 * semantics are replicated inline ({@link #paperarc$checkedObjective()}) instead of shadowing.
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftObjective")
public abstract class CraftObjectiveApiMixin {

    @Shadow
    private net.minecraft.world.scores.Objective objective;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$FIXED_VALUE_FIELD;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$STYLED_STYLE_FIELD;

    @Unique
    private static volatile java.lang.reflect.Constructor<?> PAPERARC$CRAFT_SCORE_CTOR;

    // ------------------------------------------------------------------ API

    @Unique
    public Component displayName() {
        return paperarc$asAdventure(paperarc$checkedObjective().getDisplayName());
    }

    @Unique
    public void displayName(Component displayName) {
        net.minecraft.world.scores.Objective obj = paperarc$checkedObjective();
        obj.setDisplayName(displayName == null
                ? net.minecraft.network.chat.Component.empty()
                : paperarc$asVanilla(displayName));
    }

    @Unique
    public Score getScoreFor(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        paperarc$checkedObjective();
        try {
            // CraftScore(CraftObjective, ScoreHolder) is package-private; Entity implements ScoreHolder.
            return (Score) paperarc$craftScoreCtor().newInstance(
                    this, ((CraftEntity) entity).getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot construct CraftScore for entity", e);
        }
    }

    @Unique
    public NumberFormat numberFormat() {
        net.minecraft.network.chat.numbers.NumberFormat vanilla = paperarc$checkedObjective().numberFormat();
        return vanilla == null ? null : paperarc$asPaperNumberFormat(vanilla);
    }

    @Unique
    public void numberFormat(NumberFormat format) {
        paperarc$checkedObjective().setNumberFormat(format == null ? null : paperarc$asVanillaNumberFormat(format));
    }

    @Unique
    public boolean willAutoUpdateDisplay() {
        return paperarc$checkedObjective().displayAutoUpdate();
    }

    @Unique
    public void setAutoUpdateDisplay(boolean autoUpdateDisplay) {
        paperarc$checkedObjective().setDisplayAutoUpdate(autoUpdateDisplay);
    }

    // -------------------------------------------------------------- helpers

    /**
     * Replicates package-private CraftObjective#checkState(): throws
     * IllegalStateException when this objective is no longer registered.
     */
    @Unique
    private net.minecraft.world.scores.Objective paperarc$checkedObjective() {
        net.minecraft.world.scores.Objective obj = this.objective;
        if (!obj.getScoreboard().getObjectives().contains(obj)) {
            throw new IllegalStateException("Unregistered scoreboard component");
        }
        return obj;
    }

    @Unique
    private static java.lang.reflect.Constructor<?> paperarc$craftScoreCtor() {
        java.lang.reflect.Constructor<?> c = PAPERARC$CRAFT_SCORE_CTOR;
        if (c == null) {
            synchronized (CraftObjectiveApiMixin.class) {
                if ((c = PAPERARC$CRAFT_SCORE_CTOR) == null) {
                    try {
                        Class<?> craftObjective = Class.forName(
                                "org.bukkit.craftbukkit.v.scoreboard.CraftObjective");
                        java.lang.reflect.Constructor<?> resolved = Class.forName(
                                        "org.bukkit.craftbukkit.v.scoreboard.CraftScore")
                                .getDeclaredConstructor(craftObjective,
                                        net.minecraft.world.scores.ScoreHolder.class);
                        resolved.setAccessible(true);
                        PAPERARC$CRAFT_SCORE_CTOR = resolved;
                        return resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("CraftScore(CraftObjective, ScoreHolder) not found", e);
                    }
                }
            }
        }
        return c;
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
        try {
            java.lang.reflect.Field f = PAPERARC$FIXED_VALUE_FIELD;
            if (f == null) {
                synchronized (CraftObjectiveApiMixin.class) {
                    if ((f = PAPERARC$FIXED_VALUE_FIELD) == null) {
                        java.lang.reflect.Field resolved =
                                net.minecraft.network.chat.numbers.FixedFormat.class.getDeclaredField("value");
                        resolved.setAccessible(true);
                        PAPERARC$FIXED_VALUE_FIELD = resolved;
                    }
                }
                f = PAPERARC$FIXED_VALUE_FIELD;
            }
            return (net.minecraft.network.chat.Component) f.get(format);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("FixedFormat.value not readable", e);
        }
    }

    /** Reads package-private {@code StyledFormat.style}. */
    @Unique
    private static net.minecraft.network.chat.Style paperarc$styledStyle(
            net.minecraft.network.chat.numbers.StyledFormat format) {
        try {
            java.lang.reflect.Field f = PAPERARC$STYLED_STYLE_FIELD;
            if (f == null) {
                synchronized (CraftObjectiveApiMixin.class) {
                    if ((f = PAPERARC$STYLED_STYLE_FIELD) == null) {
                        java.lang.reflect.Field resolved =
                                net.minecraft.network.chat.numbers.StyledFormat.class.getDeclaredField("style");
                        resolved.setAccessible(true);
                        PAPERARC$STYLED_STYLE_FIELD = resolved;
                    }
                }
                f = PAPERARC$STYLED_STYLE_FIELD;
            }
            return (net.minecraft.network.chat.Style) f.get(format);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("StyledFormat.style not readable", e);
        }
    }
}
