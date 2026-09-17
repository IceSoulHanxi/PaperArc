package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code AbstractFurnaceBlockEntity
 * .cookSpeedMultiplier} supplementary field to the api mixins. Paper's patch
 * adds the field without an NMS accessor method, so the bridge methods carry
 * the {@code paper$} prefix.
 */
public interface AbstractFurnaceBlockEntityBridge {

    // 注：1.20.1 分支这里还有一个 paper$getRecipeType()，main 上无人调用，故不移植 ——
    // 它要 @Shadow 一个泛型 private 字段，而注解处理器不给泛型 @Shadow 字段生成 refmap 条目，
    // Fabric 上运行期会报 "@Shadow field recipeType was not located"（实测）。

    double paper$getCookSpeedMultiplier();

    void paper$setCookSpeedMultiplier(double multiplier);

}
