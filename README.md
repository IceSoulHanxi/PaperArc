# PaperArc

为 [Arclight](https://github.com/IzzelAliz/Arclight)（多加载器 Bukkit 混合端）实现
**Paper API 兼容层**的跨加载器 Mod 项目。

## 目标与边界

- ✅ 实现 `io.papermc.paper.*` / `com.destroystokyo.paper.*` 的 **API 表面**
  （接口、事件、工具类），使仅依赖 Paper API 的插件能在 Arclight 上运行。
- ❌ **不移植** Paper 的性能优化（异步区块调度、tick 优化、Folia 等）。
- 目标 MC 版本：**1.21.1**（对齐 Arclight FeudalKings 分支）。

## 架构

基于 [Architectury](https://github.com/architectury)（loom + plugin + API）的多加载器结构：

```
paperarc/
├── common/    # 跨平台共享：paper-api 实现主体 + 公共 mixin（挂接 Arclight/CraftBukkit 层）
├── forge/     # Forge 52.x 入口 + 平台 mixin
├── neoforge/  # NeoForge 21.1.x 入口 + 平台 mixin
└── fabric/    # Fabric Loader 0.16+ 入口 + 平台 mixin
```

- 平台差异通过 `@ExpectPlatform`（`dev.paperarc.PaperArcPlatform`）抽象。
- 运行时依赖：Arclight（提供 Bukkit/Spigot 层与 Minecraft 服务端）。
- 编译期依赖：`org.spigotmc:spigot-api`、`io.izzel.arclight:arclight-api`（均由 Arclight 运行时提供，不打进 jar）。

## 版本矩阵（与 Arclight FeudalKings 对齐）

| 组件 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| Java | 21 |
| Architectury Loom | 1.9-SNAPSHOT |
| Architectury Plugin | 3.4-SNAPSHOT |
| Architectury API | 13.0.x |
| Forge | 52.1.x |
| NeoForge | 21.1.x |
| Fabric Loader / API | 0.19.x / 0.116.x+1.21.1 |
| Spigot API | 1.21.1-R0.1-SNAPSHOT |
| Arclight API | 1.7.3 |

## 构建

需要 JDK 21。部分依赖仓库（spigotmc / maven.izzel.io 等）在受限网络下需配置代理：

```bash
./gradlew build \
  -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=10808 \
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=10808
```

产物：`forge/build/libs/`、`neoforge/build/libs/`、`fabric/build/libs/` 下的 remapJar。

## 实现路线（建议顺序）

1. **事件 API 骨架**：`io.papermc.paper.event.*` 事件类 + Bukkit 事件桥接。
2. **Paper 专属接口**：如 `PaperServerListPingEvent`、`PlayerProfileComponent` 等
   纯 API 类（不依赖服务端内部）。
3. **`com.destroystokyo.paper.*`** 事件与工具（PaperLib 风格的 block/state API）。
4. **Adventure Component 桥接**：评估在 Arclight 上以独立库形式提供
   `net.kyori.adventure`（Arclight 的 Spigot 层不含 Adventure）。
5. **注册机制**：通过 mixin 在 Arclight 的 `CraftServer`/插件加载流程中注册
   Paper 事件类型，使 `@EventHandler` 能正常分发。

## 注意

- mixin 包名约定：`dev.paperarc.mixin.<common|forge|neoforge|fabric>`，
  分别对应各模块 resources 下的 `paperarc-*.mixins.json`。
- 新增 mixin 后需在对应 `*.mixins.json` 的 `mixins` / `server` 数组中登记。
- Arclight 自身大量使用 mixin 改造 CraftBukkit；挂接点选择需参考
  Arclight 源码（`arclight-common` 的 mixin 结构），避免冲突。
