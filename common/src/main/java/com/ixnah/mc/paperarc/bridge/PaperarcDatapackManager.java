package com.ixnah.mc.paperarc.bridge;

import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code Server#getDatapackManager()}（A8/Y-4；原先直接抛 UOE）。
 *
 * <p>Paper 的 {@code DatapackManager} 就是 NMS {@code PackRepository} 的一层壳：
 * {@code getPacks()} = 全部可用数据包、{@code getEnabledPacks()} = 已选中的，
 * {@code setEnabled} 走 {@code addPack}/{@code removePack} 再
 * {@code reloadResources} 让它真的生效 —— 不是只改个标记。
 */
public final class PaperarcDatapackManager implements DatapackManager {

    private final MinecraftServer server;

    public PaperarcDatapackManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Collection<Datapack> getPacks() {
        List<Datapack> packs = new ArrayList<>();
        for (Pack pack : this.server.getPackRepository().getAvailablePacks()) {
            packs.add(new PaperarcDatapack(this.server, pack));
        }
        return packs;
    }

    @Override
    public Collection<Datapack> getEnabledPacks() {
        List<Datapack> packs = new ArrayList<>();
        for (Pack pack : this.server.getPackRepository().getSelectedPacks()) {
            packs.add(new PaperarcDatapack(this.server, pack));
        }
        return packs;
    }

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
            // 与 paper 一样走完整的 reload：只改选中集合不重载，配方/战利品表都还是旧的
            this.server.reloadResources(selected);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof PaperarcDatapack that && this.pack.getId().equals(that.pack.getId());
        }

        @Override
        public int hashCode() {
            return this.pack.getId().hashCode();
        }
    }
}
