package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.persistence.PersistentDataHolder;
import org.spongepowered.asm.mixin.Mixin;

/**
 * {@code PersistentDataHolder} / {@code OfflinePlayer} 新增父接口
 * {@code io.papermc.paper.persistence.PersistentDataViewHolder} 的**协变返回桥方法**（B3-2）。
 *
 * <p>父接口声明的是 {@code getPersistentDataContainer()LPersistentDataContainerView;}，
 * 运行时实现类上只有返回 {@code PersistentDataContainer} 的那份。JVM 解析接口方法认的是
 * <b>完整描述符</b>，光靠"返回类型是子类型"不会自动对上（javac 平时是靠编译期生成桥方法
 * 补这一层，我们没有编译期）。少了这个桥，插件按 {@code PersistentDataViewHolder} 调用
 * 就是 {@code AbstractMethodError}。
 *
 * <p>两个注意点：
 * <ul>
 *   <li><b>不能写 {@code @Unique}</b> —— Mixin 按方法名判冲突，目标已有同名方法，
 *       整个方法会被丢掉（只留一行 WARN）。普通 {@code public} 方法因为描述符不同，
 *       是"新增"而不是"替换"。</li>
 *   <li>方法体里的 {@code getPersistentDataContainer()} 经 {@code PersistentDataHolder}
 *       接口调用，描述符是返回 {@code PersistentDataContainer} 的那份，不会自调用递归。</li>
 * </ul>
 *
 * <p>目标是所有"自己带 PDC 实现体"的根类；它们的子类沿继承链拿到这个桥。
 */
@Mixin({
        org.bukkit.craftbukkit.v.entity.CraftEntity.class,
        org.bukkit.craftbukkit.v.CraftChunk.class,
        org.bukkit.craftbukkit.v.CraftWorld.class,
        org.bukkit.craftbukkit.v.CraftOfflinePlayer.class,
        org.bukkit.craftbukkit.v.CraftRaid.class,
        org.bukkit.craftbukkit.v.block.CraftBlockEntityState.class,
        org.bukkit.craftbukkit.v.generator.structure.CraftGeneratedStructure.class,
        org.bukkit.craftbukkit.v.inventory.CraftMetaItem.class,
        org.bukkit.craftbukkit.v.structure.CraftStructure.class,
})
public abstract class PersistentDataViewHolderBridgeMixin {

    public PersistentDataContainerView getPersistentDataContainer() {
        return ((PersistentDataHolder) (Object) this).getPersistentDataContainer();
    }
}
