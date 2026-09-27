
# DuskByteClient 1.21.1

**炫酷的 Fabric 1.21.1 工具客户端** — 灵感来自 [OpenMio](https://github.com/GzSakura1338/OpenMio)、[OpenTroll-Recode](https://github.com/GzSakura1338/OpenTroll-Recode) 和 [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)。

> **零遥测 · 零后端 · 纯客户端**  
> 参考 OpenTroll-Recode 精神：不上传坐标、不采集 HWID、不连任何服务器。

---

## ✨ 功能

| 分类 | 模块 |
|------|------|
| **战斗** | 杀戮光环、反击退、暴击、自动点击、自动嘲讽、自动武器、触发攻击、无失误延迟、自动双持、自动打水晶、盾牌失效 |
| **移动** | 自动疾跑、速度、飞行、防摔伤、瞬步上墙、鞘翅飞行、水上行走、跑酷跳跃、高跳、潜行、防减速 |
| **渲染** | 透视高亮、追踪线、夜视全亮、实体穿透、名称标签、箱子透视、缩放、无弹跳 |
| **玩家** | 自动图腾、箱子偷取、快速放置、自动进食、自动工具、自动装备、自动重生、箱子光环、视角锁定、快速挖掘、防转向、快速使用 |
| **杂项** | 防挂机、计时加速、聊天后缀、自动退出、中键末影珍珠、无跳跃延迟、无挖掘延迟 |

---

## 🌐 中英文翻译

支持根据 Minecraft 语言设置自动切换：
- **中文** → 所有模块、分类、设置项显示中文
- **English** → 所有模块、分类、设置项显示英文

参考 Meteor Client 的 `Text.translatable()` 方式，通过 `lang/en_us.json` 和 `lang/zh_cn.json` 实现。

---

## 🛠 编译（需要 JDK 21）

```powershell
# 先获取 gradle wrapper（二进制文件无法在线创建）
git clone --depth 1 https://github.com/GzSakura1338/OpenMio.git _tmp
xcopy _tmp\gradle gradle\ /E /Y
copy _tmp\gradlew . /Y
copy _tmp\gradlew.bat . /Y
rmdir /s /q _tmp

# 构建
.\gradlew.bat build
```

**产物：** `build\libs\duskbyte-client-1.0.0-git-xxxxxxx.jar`（版本号自动包含 git commit hash）  
**启动开发客户端：** `.\gradlew.bat runClient`

---

## ⚙ 配置

- **打开 GUI：** `Right Shift`
- **模块开关：** 左键切换
- **模块设置：** 右键展开
- **按键绑定：** 中键绑定
- **配置文件：** `.minecraft/config/duskbyte/config.json`（自动保存）

---

## 🎨 炫酷特性

- **渐变水印** — HUD 左上角彩虹标题
- **彩虹模块列表** — 右侧按宽度排序，颜色循环
- **标题画面品牌** — 主菜单左下角 DuskByte 水印
- **ClickGUI** — 分类面板、左侧颜色条、设置展开、按键绑定

---

## 🔧 技术栈

| 组件 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| Yarn | 1.21.1+build.3 |
| Fabric Loader | 0.16.12 |
| Fabric API | 0.102.0+1.21.1 |
| Loom | 1.7.4 |
| Java | 21 |

---

## 🙏 致谢

- **OpenMio** — 模块重建、反混淆管线
- **OpenTroll-Recode** — ZKM 混淆破解、后门移除安全审计
- **Meteor Client** — 模块架构、翻译系统、渲染模式
- **FabricMC** — Loom、Yarn、Fabric API

---

## ⚠ 免责声明

仅供研究、教学和单人/私人服务器使用。  
本项目不含任何遥测、认证或远程代码。

---

## 📜 许可证

GPL-3.0
