package dev.paperarc.mixin.common.server;

import dev.paperarc.mixin.common.player.ServerPlayerSetSpawnMixin;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * COMMAND cause marker for {@link PlayerSetSpawnEvent}: marks the cause for the
 * duration of {@code SetSpawnCommand#setSpawn} (the /spawnpoint command), which
 * calls {@code ServerPlayer#setRespawnPosition} once per target. Handlers are
 * static because the target method is static.
 */
@Mixin(SetSpawnCommand.class)
public class SetSpawnCommandMixin {

    @Inject(method = "setSpawn", at = @At("HEAD"))
    private static void paperarc$pushCommandCause(CommandSourceStack source, Collection<ServerPlayer> targets,
                                                  BlockPos pos, float angle, CallbackInfoReturnable<Integer> cir) {
        dev.paperarc.bridge.SpawnCauseSupport.push(PlayerSetSpawnEvent.Cause.COMMAND);
    }

    @Inject(method = "setSpawn", at = @At("RETURN"))
    private static void paperarc$popCommandCause(CommandSourceStack source, Collection<ServerPlayer> targets,
                                                 BlockPos pos, float angle, CallbackInfoReturnable<Integer> cir) {
        dev.paperarc.bridge.SpawnCauseSupport.clear();
    }
}
