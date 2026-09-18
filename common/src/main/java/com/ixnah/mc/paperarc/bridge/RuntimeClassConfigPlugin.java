package com.ixnah.mc.paperarc.bridge;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Mixin config plugin for {@code paperarc-common.mixins.json}.
 *
 * <p>{@link #onLoad} runs when the mixin config is loaded — <em>before any</em>
 * mixin in that config is applied. That is the earliest reliable hook for the
 * runtime-missing paper-api types listed in
 * {@code META-INF/paperarc/runtime/injections.json}, so this is where
 * {@link RuntimeClassInjector#hookAndDefineSafe()} runs: it wires the bytecode
 * provider (needed before the iface/api mixins in this very config are applied,
 * see run36/run37) and defines the types that cannot drag a bukkit runtime class
 * in. The rest is deferred to the mod constructor — see
 * {@link RuntimeClassInjector} for why.</p>
 *
 * <p>All other plugin methods are left at their defaults; PaperArc has no other
 * need for config-level customisation.</p>
 */
public final class RuntimeClassConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        RuntimeClassInjector.hookAndDefineSafe();
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

    /**
     * {@code @Widen}：把已经合并进目标类的 {@code @Unique private} 成员放宽到
     * paper-api 的访问级别（见 {@link WidenPostProcessor}）。这是唯一能给目标类添加
     * <b>public static</b> 方法的时机 —— Mixin 的 applicator 拒绝合并非 private 的
     * static 方法，而 AT/AW 都在 Mixin 之前跑、只能改已存在的成员。
     */
    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        WidenPostProcessor.postApply(targetClassName, targetClass, mixinInfo);
    }
}
