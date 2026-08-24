# PaperArc

**让 Paper 插件运行在 [Arclight](https://github.com/IzzelAliz/Arclight) 上。**

Arclight 是 Bukkit + Mod 的混合端服务端，但它只提供到 Spigot 级别的 Bukkit API。
大量插件是基于 **Paper API**（`io.papermc.paper.*` / `com.destroystokyo.paper.*`）
开发的，直接放进 Arclight 会因缺少这些类和接口而无法加载。

PaperArc 以 Mod 形式为 Arclight 补齐 **Paper API 兼容层**：安装后，仅依赖
Paper API 的插件可以像在 Paper 服务端上一样正常加载与运行。

## 功能范围

- ✅ 提供 `io.papermc.paper.*` 与 `com.destroystokyo.paper.*` 的完整 API 表面：
  - 全部 Paper 专属**事件类型**（如 `ServerTickEndEvent`、`EntityJumpEvent`、
    `PlayerTradeEvent` 等），支持 `@EventHandler` 正常分发；
  - 在 Bukkit 原生接口上增补 **630 个 Paper 独有方法**与 **106 个 Paper 接口**
    （如 `Player#tickBoostedEnderPearl`、实体/fluid/材料家族的 Paper 扩展）；
  - Adventure `Component` 运行时（随 Mod 内置提供）。
- ❌ 不移植 Paper 的性能优化（异步区块调度、tick 优化、Folia 区域化等）。
  PaperArc 只关心 *API 兼容*，不改变服务端的调度与性能行为。
- 插件若使用了 Paper 的**服务端内部实现类**（非公开 API），不在保证范围内。

## 兼容版本

| 组件 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| Java | 21 |
| Arclight | 1.21.1（FeudalKings 分支，1.0.2-SNAPSHOT） |
| 加载器 | Fabric Loader 0.19.x ／ NeoForge 21.1.x ／ Forge 52.1.x |

三个加载器的 Arclight 发行版均可使用，按你现有的 Arclight 类型选择对应产物即可。

## 安装

1. 从下方构建说明获取（或下载）与你 Arclight 加载器对应的 Mod Jar：
   - Arclight-Fabric → `paperarc-<version>.jar`（fabric 产物）
   - Arclight-NeoForge → neoforge 产物
   - Arclight-Forge → forge 产物
2. 将 Jar 放入 Arclight 服务端的 `mods/` 目录；
   你的插件照常放在 `plugins/` 目录。
3. 启动服务器，日志出现 `Done (` 即安装成功。

## 构建

环境要求：**JDK 21**，Git。

```bash
git clone <本仓库>
cd paperarc
./gradlew build
```

构建产物位于各加载器子目录：

| 目标 Arclight | 产物路径 |
|---|---|
| Fabric | `fabric/build/libs/paperarc-<version>.jar` |
| NeoForge | `neoforge/build/libs/paperarc-<version>.jar` |
| Forge | `forge/build/libs/paperarc-<version>.jar` |

> 构建需要访问 spigotmc / maven.izzel.io 等 Maven 仓库；网络受限时请自行
> 为 Gradle/JVM 配置代理后再执行。

## 项目结构

基于 [Architectury](https://github.com/architectury)（Loom + Plugin + API）的多加载器工程：

```
paperarc/
├── common/    # 跨平台共享主体：Paper API 实现、公共 Mixin、访问权限扩展
├── fabric/    # Fabric 入口 + Fabric 专属 Mixin
├── neoforge/  # NeoForge 入口 + 共享 Mojang 映射 Mixin
├── forge/     # Forge 入口（复用共享 Mixin）
└── buildSrc  # 构建辅助任务（CraftBukkit 包版本化）
```

## 反馈与兼容性报告

遇到不兼容的 Paper 插件时，请附带：插件名称与版本、Arclight 日志中
`[PaperArc]` / `[mixin]` 相关片段、以及该插件声称所需的 Paper 版本。

## 许可证

MIT
