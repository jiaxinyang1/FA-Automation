# Forbidden Arcanus Automation

[English](README.md)

Forbidden & Arcanus（禁断奥术）的自动化辅助插件。不修改原 mod，全部通过公开 API 实现。

## 功能

- **管道/漏斗支持**（修复 1.21.1 移植后丢失的自动化能力）：
  - 赫菲斯托斯锻造炉：四个精华输入槽按精华类型校验收料；主槽接受单个仪式原料；增强器槽保持 GUI 专属
  - 基座：单格物品存取，物品变动会同步熔炉的仪式配料缓存
  - Clibano 熔炉：复用原 mod 自带的 `ClibanoItemHandler`（顶面进原料、底面出产物、侧面进燃料/灵魂）
- **仪式保护**：
  - 仪式进行期间，基座的插入/抽取与主槽操作全部锁定（配料变动会导致仪式失败）
  - 主槽只有仪式成功产出的结果可被管道抽走，等待开仪式的原料不会被抽走（快照比对实现）
- **精华流体**：注册 `fa_automation:aureal / souls / blood / experience` 四种流体（1 mB = 1 精华）：
  - 锻造炉暴露四罐（容量随熔炉等级），精华罐暴露单罐
  - AE2 等流体自动化模组可直接存取
  - aureal/blood/experience 贴图引用 FA 的液体材质；souls 为自制贴图
- **自动模式**：手持锻工锤（任意材质）Shift+右键锻造炉切换（音效 + 动作栏提示，存档持久）。
  满足条件（结构完整、有效仪式、精华充足）时自动启动仪式。仪式归属开启自动模式的玩家，
  该玩家离线时仪式暂停等待。

## 构建

需要 JDK 21。

```
./gradlew build
```

产物在 `build/libs/fa_automation-<version>.jar`。

编译依赖 jar 不随仓库分发（FA 为 All Rights Reserved，不可再分发）：请将未修改的
`forbidden_arcanus-2.6.1.jar` 与 `valhelsia_core-neoforge-1.21.1-1.1.5.jar` 放入 `libs/` 目录
（CurseForge / Modrinth 下载，详见 `libs/README.md`），两者仅用于编译，不会打进插件 jar。

## 安装

`mods` 目录放入：

1. Forbidden & Arcanus 2.6.1+（官方版即可）
2. Valhelsia Core（FA 的前置）
3. `fa_automation-<version>.jar`

## 已知边界

- 自动模式需要开启者在线才能自动启动仪式（上游 `startRitual` 需要玩家实例）
- 服务器重启后，已在主槽的旧产物需等下一场仪式完成才重新开放管道抽取（追踪快照不落盘）
- 多数量仪式产物被管道部分抽取后，剩余部分只能从 GUI 取出
