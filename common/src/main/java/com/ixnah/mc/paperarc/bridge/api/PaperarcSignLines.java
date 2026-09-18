package com.ixnah.mc.paperarc.bridge.api;

import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v.block.sign.CraftSignSide;

import java.lang.reflect.Field;
import java.util.AbstractList;

/**
 * {@code CraftSignSide#lines()} 的返回值：直接读写 NMS {@code SignText} 的活动视图。
 *
 * <p>为什么不在 {@code CraftSignSide} 上缓存一份 {@code List}：
 * 方块状态是快照，{@code Block#getState()} 每次都 new 一个新的 Craft 包装对象
 * （javap：{@code CraftBlockStates.getBlockState} 方法体逐次 new），
 * 缓存在包装对象上的行文本取一次就丢，对返回列表的 {@code set} 也永远写不回去
 * （checklist §1.10 an）。这里每次 get/set 都穿透到 {@code SignText}。</p>
 *
 * <p>类放在 {@code bridge} 包而不是 mixin 包：mixin 包内的类不能被目标类引用。</p>
 */
public final class PaperarcSignLines extends AbstractList<Component> {

    private static volatile Field signTextField;

    private final CraftSignSide side;

    public PaperarcSignLines(CraftSignSide side) {
        this.side = side;
    }

    @Override
    public Component get(int index) {
        return PaperarcComponents.fromVanilla(signText().getMessage(index, false));
    }

    @Override
    public Component set(int index, Component element) {
        if (element == null) {
            throw new IllegalArgumentException("Line cannot be null");
        }
        Component old = get(index);
        setSignText(signText().setMessage(index, PaperarcComponents.toVanilla(element)));
        return old;
    }

    @Override
    public int size() {
        return signText().getMessages(false).length;
    }

    private net.minecraft.world.level.block.entity.SignText signText() {
        try {
            return (net.minecraft.world.level.block.entity.SignText) field().get(this.side);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS SignText field not accessible on CraftSignSide", e);
        }
    }

    private void setSignText(net.minecraft.world.level.block.entity.SignText text) {
        try {
            field().set(this.side, text);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS SignText field not accessible on CraftSignSide", e);
        }
    }

    /** B2-1：目标是 CraftBukkit 类的成员，类名/成员名三端一致且不参与重映射，保留反射。 */
    private static Field field() throws NoSuchFieldException {
        Field field = signTextField;
        if (field == null) {
            synchronized (PaperarcSignLines.class) {
                if (signTextField == null) {
                    Field declared = CraftSignSide.class.getDeclaredField("signText");
                    declared.setAccessible(true);
                    signTextField = declared;
                }
                field = signTextField;
            }
        }
        return field;
    }
}
