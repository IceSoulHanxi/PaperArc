package com.ixnah.mc.paperarc.bridge;

import io.izzel.arclight.api.EnumHelper;

import java.util.Collections;

/**
 * B6-2 走通实验：给运行时已有的枚举补 paper 新增的常量。
 *
 * <p>Mixin 结构性加不了枚举常量（{@code $VALUES}/{@code ENUM$VALUES} 是 {@code <clinit>} 里 new 出来的数组，
 * 常量对象本身也在那里构造），但两件事合起来可以：</p>
 * <ol>
 *   <li>Mixin 把一个 {@code @Unique private static} 字段合并进目标枚举，
 *       {@code @Widen} 在 postApply 放宽成 public —— 插件的 {@code getstatic} 才解析得到；</li>
 *   <li>{@code @Inject} 到目标枚举 {@code <clinit>} 的 <b>TAIL</b>，此时 {@code $VALUES}
 *       已经赋值，调用 Arclight 自带的 {@link EnumHelper} 造出实例、把 {@code $VALUES} 扩容、
 *       清掉 {@code Class} 上的三个枚举缓存（{@code enumConstantDirectory} /
 *       {@code enumConstants} / {@code enumVars}），再写回我们那个静态字段。</li>
 * </ol>
 *
 * <p><b>为什么必须在 {@code <clinit>} 里做</b>：{@code switch} 语句编译出的
 * {@code $SwitchMap$xxx} 数组长度 = 该 switch 所在类初始化时的 {@code values().length}。
 * 在 {@code <clinit>} 尾部扩容，等于任何消费方类初始化之前 {@code values()} 就已经是全量，
 * {@code $SwitchMap} 天然够长，新常量的 {@code ordinal()} 不会越界；换成"模组构造器里再补"
 * 就会留下 {@code ArrayIndexOutOfBoundsException} 的窗口。</p>
 *
 * <p>{@code EnumHelper} 走 {@code Unsafe.lookup().findConstructor(...)}
 * （不是 {@code Constructor#newInstance} —— 那条路上 JDK 有
 * "Cannot reflectively create enum objects" 的硬检查）+ {@code Unsafe} 直写静态字段，
 * JDK 17 上可用；它是 {@code arclight-api} 的公开 API，Arclight 自己给模组扩
 * {@code EntityType}/{@code Material} 用的就是它。</p>
 *
 * <p><b>1.20.1 特有的一条</b>：arclight-api <b>1.5.4</b> 的 {@code EnumHelper} 找的常量数组
 * 字段名是 {@code ENUM$VALUES}（不是 javac 的 {@code $VALUES}）；1.20.1 运行时的
 * {@code org.bukkit.scoreboard.DisplaySlot} 等枚举正好也是这个名字
 * （`javap -p` 核对：{@code private static final DisplaySlot[] ENUM$VALUES}），两边对得上。
 * 移植到别的枚举前先 `javap` 确认目标枚举用的是哪一个名字。</p>
 */
public final class PaperarcEnumConstants {

    private PaperarcEnumConstants() {
    }

    /**
     * 给 {@code type} 追加一个无构造形参的枚举常量并返回它。
     *
     * <p>{@code EnumHelper.addEnum} 内部把 {@code Throwable} 吞掉只返回 null，
     * 这里转成异常：走不通必须让启动直接失败，不许留一个 null 的 public 静态字段
     * （插件读到 null 比 {@code NoSuchFieldError} 更难查）。</p>
     */
    public static <T> T add(Class<T> type, String name) {
        T added = EnumHelper.addEnum(type, name, Collections.emptyList(), Collections.emptyList());
        if (added == null) {
            throw new IllegalStateException("补枚举常量失败：" + type.getName() + "." + name);
        }
        return added;
    }
}
