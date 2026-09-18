package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.Map;

/**
 * paper 的 {@code MetadataStoreBase#removeAll(Plugin)}（A6/X-2 第五批）：插件卸载时
 * 清掉它留下的全部元数据。逐行照抄 paper；{@code metadataMap} 就声明在这个类上，
 * {@code @Shadow} 直接拿（不是父类成员，不踩"子类 @Shadow 父类"那条坑）。
 */
@Mixin(MetadataStoreBase.class)
public abstract class MetadataStoreBaseApiMixin {

    @Shadow
    private Map<String, Map<Plugin, MetadataValue>> metadataMap;

    @Unique
    public void removeAll(Plugin owningPlugin) {
        Preconditions.checkNotNull(owningPlugin, "Plugin cannot be null");
        for (Iterator<Map<Plugin, MetadataValue>> it = this.metadataMap.values().iterator();
                it.hasNext(); ) {
            Map<Plugin, MetadataValue> values = it.next();
            values.remove(owningPlugin);
            if (values.isEmpty()) {
                it.remove();
            }
        }
    }
}
