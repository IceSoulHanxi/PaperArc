package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * paper 的 {@code Team extends ForwardingAudience}（B3-2）：队伍的 Audience 成员是
 * 队里**当前在线**的玩家。
 *
 * <p>队伍条目存的是记分板名（玩家是玩家名，非玩家实体是 UUID 串，见 checklist §1.6 e），
 * 所以这里按名字查在线玩家，查不到的条目（离线玩家、非玩家实体）跳过。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftTeam")
public abstract class CraftTeamAudienceApiMixin {

    @Unique
    public Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        List<net.kyori.adventure.audience.Audience> out = new ArrayList<>();
        for (String entry : ((org.bukkit.scoreboard.Team) (Object) this).getEntries()) {
            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayerExact(entry);
            if (player != null) {
                out.add(player);
            }
        }
        return out;
    }
}
