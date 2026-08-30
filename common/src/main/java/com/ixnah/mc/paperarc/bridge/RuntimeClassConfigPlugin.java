package com.ixnah.mc.paperarc.bridge;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Mixin config plugin for {@code paperarc-common.mixins.json}.
 *
 * <p>{@link #onLoad} runs when the mixin config is loaded — <em>before any</em>
 * mixin in that config is applied. That is the earliest reliable hook to inject
 * the runtime-missing paper-api types listed in
 * {@code META-INF/paperarc/runtime/injections.json} (see
 * {@link RuntimeClassInjector}): the bukkit-side iface/api mixins in this very
 * config reference those types in their method signatures, so they must exist
 * before the mixins are applied (which otherwise happens during Arclight's
 * bukkit layer initialisation, after the mod constructor — see run36/run37
 * failures).</p>
 *
 * <p>All other plugin methods are left at their defaults; PaperArc has no other
 * need for config-level customisation.</p>
 */
public final class RuntimeClassConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        RuntimeClassInjector.inject();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
