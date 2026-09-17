# PaperArc（1.20.1 分支）

**让 Paper 插件运行在 [Arclight](https://github.com/IzzelAliz/Arclight) 上。**

Arclight 是 Bukkit + Mod 的混合端服务端，但它只提供到 Spigot 级别的 Bukkit API。
大量插件是基于 **Paper API**（`io.papermc.paper.*` / `com.destroystokyo.paper.*`）
开发的，直接放进 Arclight 会因缺少这些类和接口而无法加载。

PaperArc 以 Mod 形式为 Arclight 补齐 **Paper API 兼容层**：安装后，仅依赖
Paper API 的插件可以像在 Paper 服务端上一样正常加载与运行。

> 本分支（`1.20.1`）只面向 **Arclight Forge 1.20.1**，单模块 `common`，工具链是
> ForgeGradle + JDK 17。1.21.1 / Fabric / NeoForge 三加载器的版本在 `main` 分支。

## 功能范围

- ✅ 提供 `io.papermc.paper.*` 与 `com.destroystokyo.paper.*` 的完整 API 表面：
  - 全部 Paper 专属**事件类型**（如 `ServerTickEndEvent`、`EntityJumpEvent`、
    `PlayerTradeEvent` 等），支持 `@EventHandler` 正常分发；
  - 在 Bukkit 原生接口上增补 Paper 独有方法（接口声明由 `bukkit/*IfaceMixin`
    注入运行时接口类，实现由 `api/Craft*ApiMixin` 注入对应 CraftBukkit 类）；
  - Paper 在 NMS 上新增的补充字段由 `mojmap/*FieldsMixin` 真实注入，跨类访问走
    `bridge/*Bridge` 接口；
  - 运行时缺失的 Paper 类型（枚举/内部类）由 `bridge/RuntimeClassInjector` 按
    `META-INF/paperarc/runtime/injections.json` 注入；
  - Adventure `Component` 运行时（随 Mod 内置提供）。
- ❌ 不移植 Paper 的性能优化（异步区块调度、tick 优化、Folia 区域化等）。
  PaperArc 只关心 *API 兼容*，不改变服务端的调度与性能行为。
- 插件若使用了 Paper 的**服务端内部实现类**（非公开 API），不在保证范围内。

## 兼容版本

| 组件 | 版本 |
|------|------|
| Minecraft | 1.20.1 |
| Java | 17 |
| Arclight | 1.20.1（Trials，1.0.6-SNAPSHOT） |
| 加载器 | Forge 47.4.18 |

## 安装

1. 把构建产物 `common/build/libs/common-<version>.jar` 放进 Arclight 服务端的
   `mods/`（可自行改名为 `paperarc.jar`）；
2. 同时需要 `mixinextras-forge-0.4.1.jar`（GAMELIBRARY，**不**打进本 Mod）也放进
   `mods/`；
3. 你的插件照常放在 `plugins/` 目录；
4. 启动服务器，日志出现 `Done (` 且无 `Mixin apply failed` 即安装成功。

## 构建

环境要求：**JDK 17**，Git。

```bash
cd paperarc
./gradlew :common:compileJava      # 快速编译检查
./gradlew :common:assemble         # 出包（含 versionCraftBukkit 产物重写）
```

产物：`common/build/libs/common-<version>.jar`。

要点：

- 源码一律写 mojmap NMS 名与**版本无关**的 `org.bukkit.craftbukkit.v` 包；
  `assemble` 挂的 `versionCraftBukkit` 任务会把产物里的 `v/` 重写回 `v1_20_R1`，
  对齐 Arclight 的 wipeVersion/reobfVersion 机制。
- NMS 私有/保护成员通过 `common/src/main/resources/META-INF/accesstransformer.cfg`
  （**srg 名**）放开后直访，或用 `@Accessor/@Invoker` mixin。
  **禁止按 mojmap 名字符串反射 NMS 成员**——运行时是 srg 名，必然失败。
- 新增 mixin 必须注册进 `paperarc-*.mixins.json`；`./gradlew check` 的
  `checkMixinRegistry` 任务会强制校验这一点。
- 构建依赖 `io.izzel.arclight.generated:spigot:1.20.1:deobf`（由 Arclight 的
  `:arclight-common:remapSpigotJar` 生成并发布到本地 arclight_repo）。

## 项目结构

```
paperarc/
├── common/src/main/java/com/ixnah/mc/paperarc/
│   ├── mixin/common/api/      # Craft* 类的 Paper 方法实现
│   ├── mixin/common/bukkit/   # org.bukkit 接口的 Paper 方法声明（IfaceMixin）
│   ├── mixin/common/…         # 事件注入（block/entity/item/player/server/world…）
│   ├── mixin/mojmap/          # 直接以 NMS 为目标的 mixin（字段注入等）
│   ├── bridge/                # 跨类访问接口、运行时类注入器等非 mixin 辅助类
│   └── event/ util/ forge/
├── common/src/main/resources/
│   ├── paperarc-{common,mojmap,forge}.mixins.json
│   ├── META-INF/accesstransformer.cfg
│   └── META-INF/paperarc/runtime/injections.json
└── buildSrc/                  # VersionCraftBukkitTask（产物 v/ → v1_20_R1）
```

## 验收

真机验收脚本在根工作区 `tools/`（不在本仓库）：

```bash
tools/start-arclight-forge-1201.sh   # 后台起测试服
tools/verify-forge-1201.sh           # 启动 + 探针门禁，exit 0 = PASS
tools/stop-arclight-forge-1201.sh
```

门禁判据：`Done (`、无 `Mixin apply failed`/`InjectionError`/`IllegalClassLoadError`、
`PaperArcProbe` 探针 P1–P7 全 `PASS(`（无在线玩家时 P5g 为 `WARN` 可接受）。

## 反馈与兼容性报告

遇到不兼容的 Paper 插件时，请附带：插件名称与版本、Arclight 日志中
`[PaperArc]` / `[mixin]` 相关片段、以及该插件声称所需的 Paper 版本。

## 许可证

MIT
