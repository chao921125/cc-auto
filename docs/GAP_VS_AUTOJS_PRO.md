---
topics: [gap-analysis, autojs-pro]
doc_kind: note
created: 2026-11-10
---

# CcAuto vs AutoJS Pro 功能差异核对

> CcAuto 侧：以代码与模拟器实测为准（截至本文件提交时）。
> AutoJS Pro 侧：以官方第二代 API 文档（v8/v9 模块清单）与产品简介页为准。
> 标记：✅ 已对齐（实测可用）｜🟡 部分对齐（可用但深度/形态有差）｜❌ 缺失｜⚠️ 文档超前于实现

## 一、脚本 API（对照官方 34 模块清单）

| AutoJS Pro 模块 | CcAuto 状态 | 说明 |
|---|---|---|
| 自动化-控件操作 | ✅ | `auto.*` + 顶层简写（text/id/desc/clazz…，UiQuery: findOne/find/each/click/setText/scroll） |
| 自动化-坐标操作 | ✅ | click/longClick/swipe/gesture（无障碍 dispatchGesture） |
| app | ✅ | launch/openUrl/viewFile/isInstalled/installed 列表等 |
| base64 | ✅ | encode/decode，值实测正确 |
| colors | ✅ | parse/rgb/rgba/red/green/blue/alpha/hex/mix/darker/lighter/isSimilar（值实测正确） |
| canvas | ❌ | 无 |
| console | ✅ | 对象形式 log/info/warn/error/verbose/debug（show/hide 为 no-op） |
| crypto | ✅ | md5/sha1/sha256，值实测正确 |
| debug | ❌ | 无（有 VSCode 侧 devplugin 雏形，见产品级） |
| device | ✅ | 属性 + keepScreenOn；缺 imei/屏幕分辨率扩展项 |
| dialogs | ✅ | alert/confirm/rawInput/singleChoice（阻塞+回调）；缺 build 链式与 widgets |
| engines | ✅ | myScript/myId/myEngine/runningScriptCount/all/stopAllScripts |
| events | 🟡 | emitter + 全局总线 + observeKey/onKeyDown/onKeyUp（无障碍+root 双通道）；**按键端到端待真机复验**；触摸监听（root getevent ABS）未做 |
| files | ✅ | read/write/copy/move/list/zip 等（注意：有内部沙箱路径限制） |
| floaty | ✅ | window（id 绑定+控件事件实测）/close/closeAll/console |
| globals | ✅ | log/sleep/exit/toast/random/shell/剪贴板 等 |
| http | ✅ | get/post/postJson/put/delete/download/uploadFile/newWebSocket |
| images | ✅ | 截图/找图(NCC+多尺度)/找色/多点找色/像素/裁剪缩放 |
| keys（按键模拟） | 🟡 | 音量类按键走 AudioManager（免 root，本模拟器镜像音量策略有怪癖未观测到变化）；其他按键需 root 通道，待真机复验 |
| media | 🟡 | 播放/暂停/恢复/停止/音量（MediaPlayer）；录音未做 |
| module（require） | ✅ | 文件模块 + 内置模块解析 |
| ocr | ✅ | ML Kit 中文识别实测链路（示例待跑通核验） |
| plugins / thirdPartyPlugins | ❌ | 无插件商店/插件协议 |
| power_manager | ❌ | 无 |
| sensors | ❌ | 无（未列入本批） |
| shell | ✅ | 全局可调用 + exec/isRootAvailable，root 降级实测 |
| storages | ✅ | MMKV 键值 |
| threads | ✅ | start/shutDownAll/currentThread（子线程实测） |
| timers | ✅ | setTimeout/setInterval/clear（EventLoop 修复后实测） |
| ui | ✅ | layout/id 绑定/on 事件/run/finish（模拟器实测渲染+交互）；缺 RecyclerView/ListView 数据绑定、setContentView 全家桶 |
| util | ✅ | format/uuid |
| WebSocket | 🟡 | 有 `http.newWebSocket`，非独立模块 |
| zip | 🟡 | 有 `files.zip/unzip`，非独立模块 |
| 进阶-Java 交互（Packages/Java 类访问） | ❌ | 沙箱 ClassShutter 有意隔离，与安全模型冲突，需决策 |
| ml（ONNX 检测） | ✅ | 已注册，typeof 探针实测可用（detect 需模型文件，功能级待测） |
| notifications | ✅ | 已注册，typeof 探针实测可用（需通知使用权，功能级待测） |
| shizuku | ✅ | 已注册，typeof 探针实测可用（需 Shizuku 服务激活，功能级待测） |

## 二、产品/IDE 能力

| AutoJS Pro | CcAuto 状态 |
|---|---|
| 双引擎（Rhino + Node/V8 ES2021） | ❌ 仅 Rhino 1.8（第二代 Node 引擎是大工程） |
| npm 生态 / dex/jar/so 加载 | ❌ 仅 JS 文件模块 |
| 多 Tab 编辑器 + 树状文件管理 + LSP | 🟡 单文件编辑器（sora-editor）+ 外部目录文件浏览；无多 Tab/LSP |
| VSCode 调试（远程单步/补全/文件同步） | 🟡 devplugin 服务器雏形；无单步调试/补全协议 |
| 可视化 UI 设计器 | ❌ |
| 打包（签名/图标/启动图/权限/架构/混淆、在线加密） | 🟡 packager v1：模板+V1/V2 签名+AES 脚本加密；缺自定义图标/启动图/权限裁剪/组件混淆 |
| 插件商店 / 代码商店（云分享） | ❌ |
| AI/LLM 协议支持 | ❌ |
| 授权/License 体系（设备数绑定） | 🟡 加密器有雏形（README 提及 License） |
| 任务/定时（脚本级 work_manager API） | 🟡 App 侧 TaskScheduler/TriggerManager 有；无脚本级 API |
| Tasker 集成 | ✅ feature/tasker |
| 桌面小部件 | ✅ ScriptWidget |
| 布局审查/拾取 | ✅ inspector（LayoutInspector/ElementPicker）；无脚本级暴露 |

## 三、AutoJS Pro 明确"不做"的事（对齐参考）
- 不提供读写其他进程内存、抓取修改其他应用网络数据。
- Pro 已移除自动化测试、图片处理（测试向）、消息通知模块——CcAuto 的 images/notifications 定位与之不同，属差异化能力，保留即可。

## 四、行动建议（按性价比排序）
1. ~~立即：注册 ml/notifications/shizuku~~ 已完成（typeof 实测），功能级验证待模型文件/服务激活。
2. 短平快：~~colors/keys/media/settings~~ 已完成（见上表）；剩余：脚本级 work_manager 最小版、sensors、canvas。
3. 中期：编辑器多 Tab + 目录树（对齐"项目"概念）、packager v2（图标/启动图/权限裁剪）、devplugin 升级为真实调试协议（DAP）。
4. 长期决策：V8/Node 第二引擎、npm 生态、Java 互操作与安全模型的取舍、插件商店。
