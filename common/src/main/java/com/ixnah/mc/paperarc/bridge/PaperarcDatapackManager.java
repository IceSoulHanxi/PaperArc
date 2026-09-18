package com.ixnah.mc.paperarc.bridge;

import com.ixnah.mc.paperarc.bridge.api.PaperarcComponents;
import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.datapack.DatapackSource;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code Server#getDatapackManager()}（B8/Y-4；原先直接抛 UOE）。
 *
 * <p>Paper 的 {@code DatapackManager} 就是 NMS {@code PackRepository} 的一层壳：
 * {@code getPacks()} = 全部可用数据包、{@code getEnabledPacks()} = 已选中的，
 * {@code setEnabled} 走 {@code reloadResources} 让它**真的生效**（只改选中集合不重载，
 * 配方/战利品表都还是旧的），{@code refreshPacks()} = {@code PackRepository#reload()}。
 */
public final class PaperarcDatapackManager implements DatapackManager {

    private final MinecraftServer server;

    public PaperarcDatapackManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void refreshPacks() {
        this.server.getPackRepository().reload();
    }

    @Override
    public Datapack getPack(String name) {
        Pack pack = this.server.getPackRepository().getPack(name);
        return pack == null ? null : new PaperarcDatapack(this.server, pack);
    }

    @Override
    public Collection<Datapack> getPacks() {
        return wrap(this.server.getPackRepository().getAvailablePacks());
    }

    @Override
    public Collection<Datapack> getEnabledPacks() {
        return wrap(this.server.getPackRepository().getSelectedPacks());
    }

    private Collection<Datapack> wrap(Collection<Pack> packs) {
        List<Datapack> wrapped = new ArrayList<>(packs.size());
        for (Pack pack : packs) {
            wrapped.add(new PaperarcDatapack(this.server, pack));
        }
        return wrapped;
    }

    /** 类放在这里而不是 mixin 里：mixin 内的嵌套类被合并进目标后 InnerClasses 会自相矛盾。 */
    private static final class PaperarcDatapack implements Datapack {

        private final MinecraftServer server;
        private final Pack pack;

        private PaperarcDatapack(MinecraftServer server, Pack pack) {
            this.server = server;
            this.pack = pack;
        }

        @Override
        public String getName() {
            return this.pack.getId();
        }

        @Override
        public Component getTitle() {
            return PaperarcComponents.fromVanilla(this.pack.getTitle());
        }

        @Override
        public Component getDescription() {
            return PaperarcComponents.fromVanilla(this.pack.getDescription());
        }

        @Override
        public Component computeDisplayName() {
            return PaperarcComponents.fromVanilla(this.pack.getChatLink(this.isEnabled()));
        }

        @Override
        public boolean isRequired() {
            return this.pack.isRequired();
        }

        @Override
        public Compatibility getCompatibility() {
            PackCompatibility compatibility = this.pack.getCompatibility();
            if (compatibility == PackCompatibility.TOO_OLD) {
                return Compatibility.TOO_OLD;
            }
            if (compatibility == PackCompatibility.TOO_NEW) {
                return Compatibility.TOO_NEW;
            }
            return Compatibility.COMPATIBLE;
        }

        @Override
        public Set<org.bukkit.FeatureFlag> getRequiredFeatures() {
            return java.util.Collections.unmodifiableSet(
                    org.bukkit.craftbukkit.v.CraftFeatureFlag.getFromNMS(this.pack.getRequestedFeatures()));
        }

        @Override
        public DatapackSource getSource() {
            PackSource source = this.pack.getPackSource();
            if (source == PackSource.BUILT_IN) {
                return DatapackSource.BUILT_IN;
            }
            if (source == PackSource.FEATURE) {
                return DatapackSource.FEATURE;
            }
            if (source == PackSource.WORLD) {
                return DatapackSource.WORLD;
            }
            if (source == PackSource.SERVER) {
                return DatapackSource.SERVER;
            }
            return DatapackSource.DEFAULT;
        }

        @Override
        public boolean isEnabled() {
            return this.server.getPackRepository().getSelectedIds().contains(this.pack.getId());
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (enabled == this.isEnabled()) {
                return;
            }
            Set<String> selected = new LinkedHashSet<>(this.server.getPackRepository().getSelectedIds());
            if (enabled) {
                selected.add(this.pack.getId());
            } else {
                selected.remove(this.pack.getId());
            }
            this.server.reloadResources(selected);
        }
    }
}
