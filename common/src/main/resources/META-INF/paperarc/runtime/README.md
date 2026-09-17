# 运行时注入类清单

`injections.json` 列出运行时 Arclight 1.21.1 缺失、但被 paper-api 方法签名引用或自身含
抽象方法的 `org.bukkit` 类型。

- 清单来源：`docs/data/main-121-audit/missing-types.txt`（审计得到 25 个），剔除 4 个合成类
  （`Entity$1Holder`、`EventExecutor$1`、`EventExecutor$2`、`JavaPlugin$DummyPluginLoaderImplHolder`），
  余 21 个。
- 字节由 Gradle 任务 `embedRuntimeClasses` 从 paper-api jar 抽出，写到本目录下与原 jar 相同的
  路径（**不能**放 `org/bukkit/**`：ModLauncher 的模块系统会认为 paperarc 也导出该包，
  与 arclight 冲突）。抽取时会剥掉嵌套类的 `InnerClasses`/`EnclosingMethod`/`NestHost`，
  否则插件对这些类型调 `getSimpleName()` 即 `IncompatibleClassChangeError`。
- 运行时由 `bridge/RuntimeClassInjector` 分两阶段 `defineClass` 注入，见该类的 javadoc。
