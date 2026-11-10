# CcAuto — API 文档

> 版本 v1.0 | 基于自研引擎，对标 AutoJS Pro

## 命名空间速查

| 命名空间 | 说明 | 优先级 |
|---------|------|--------|
| `auto` | 无障碍 UI 自动化 | P0 |
| `images` | 截图/找图/找色 | P0 |
| `ocr` | 文字识别 | P1 |
| `floaty` | 悬浮窗 | P0 |
| `ui` | 脚本 UI (ui.layout) | P1 |
| `files` | 文件读写/压缩 | P0 |
| `storages` | 键值持久化 | P0 |
| `http` | HTTP 请求 | P0 |
| `app` | 应用管理 | P0 |
| `device` | 设备信息 | P0 |
| `shell` | Shell 命令 | P1 |
| `threads` | 多线程 | P0 |
| `timers` | 定时器 | P0 |
| `events` | 事件总线 | P0 |
| `console` | 控制台 | P0 |
| `engines` | 引擎管理 | P0 |

---

## auto — 无障碍自动化

### 选择器
```javascript
auto.text("登录")          // 文本匹配
auto.textContains("登")    // 文本包含
auto.id("btn_submit")      // ID 匹配
auto.desc("按钮")          // 描述匹配
auto.clazz("Button")       // 类名匹配
auto.clickable()           // 可点击
auto.depth(3)              // 深度
```

### 查询方法
```javascript
selector.findOne(timeout)   // 查找单个（带超时 ms）
selector.findAll()          // 查找全部
selector.waitFor(timeout)   // 等待出现
selector.exists()           // 是否存在
selector.each(fn)           // 遍历
```

### 操作
```javascript
auto.click(x, y)            // 坐标点击
auto.longClick(x, y)        // 坐标长按
auto.swipe(x1,y1,x2,y2,dur) // 滑动
auto.gesture(dur, [x,y]...) // 手势路径
auto.input(text)            // 输入文本
auto.scrollDown()           // 向下滚动
auto.scrollUp()             // 向上滚动
auto.waitFor()              // 等待服务开启
```

---

## images — 视觉引擎

```javascript
images.requestScreenCapture()           // 申请截图权限
images.captureScreen()                  // 截图
images.findImage(src, template, opts)   // 找图
images.findColor(img, color, opts)      // 找色
images.findMultiColors(img, color, offsets) // 多点找色
images.detectsColor(img, x, y, color)   // 检测颜色
images.crop(img, [x,y,w,h])             // 裁剪
images.resize(img, w, h)                // 缩放
images.pixel(img, x, y)                 // 像素颜色
images.read(path)                       // 读取图片
images.save(img, path)                  // 保存图片
```

---

## ocr — 文字识别

```javascript
let result = ocr.recognize(img, {region: [x,y,w,h]})
// result.items: [{text, bounds, confidence}]
// result.fullText: 拼接文本
```

---

## floaty — 悬浮窗

```javascript
let w = floaty.window('<frame><text text="Hello"/></frame>')
floaty.close(w)
floaty.closeAll()
floaty.isOverlaysEnabled()
let console = floaty.console()
```

---

## ui — 脚本 UI

以 `"ui";` 开头或调用 `ui.layout(xml)` 进入脚本 UI 模式（全屏 Activity 渲染）：

```javascript
"ui";
ui.layout('<vertical padding="16">' +
    '<text id="status" text="就绪"/>' +
    '<button id="btnGo" text="开始"/></vertical>');

// XML 中带 id 的控件自动绑定为 ui.<id>
ui.btnGo.on("click", function () {
    ui.status.setText("运行中...");
});
// 常用方法：setText/text、setProgress/getProgress/setMax、
// setVisibility、setChecked、on("click"/"check", fn)、click(fn)
ui.run(function () { /* 在主线程执行并返回结果 */ });
ui.finish();   // 关闭脚本 UI
```

> 支持标签：vertical/horizontal/linear/frame/relative/text/button/input/img/checkbox/switch/scroll/list/progressbar/seekbar/spinner/radiogroup；
> 常用属性：id/text/hint/w/h/padding/margin*/layout_weight/bg/textColor/textSize/gravity/max/progress/checked。
> 注意：Rhino 模板字符串不支持多行，XML 请用字符串拼接书写。

---

## http — 网络请求

```javascript
http.get(url, headers)        // GET 请求
http.post(url, data, headers) // POST 请求
http.postJson(url, json)      // POST JSON
http.download(url, dest)      // 下载文件
http.uploadFile(url, path)    // 上传文件
```

---

## files — 文件操作

```javascript
files.read(path)      files.write(path, content)
files.append(path, content)   files.copy(src, dest)
files.move(src, dest)         files.remove(path)
files.exists(path)   files.list(path)
files.zip(src, dest)  files.unzip(src, dest)
```

---

## storages — 键值存储

```javascript
let s = storages.create("namespace")
s.put("key", value)
s.get("key", default)
s.contains("key")
s.remove("key")
s.clear()
```

---

## device — 设备信息

```javascript
device.width       device.height
device.density     device.brand
device.model       device.sdkInt
device.battery     device.isCharging
device.androidId
```

---

## app — 应用管理

```javascript
app.launch(packageName)
app.isInstalled(packageName)
app.getAppName(packageName)
app.uninstall(packageName)
app.openUrl(url)
```

---

## shell — Shell 命令

```javascript
let r = shell.exec("ls -la", false)
// r.code, r.result, r.error
shell.isRootAvailable()
```

---

## threads — 多线程

```javascript
let t = threads.start(function () { /* 子线程逻辑 */ })
t.interrupt()          // 中断子线程
t.join()               // 等待子线程
t.isAlive()
threads.shutDownAll()  // 停止本脚本创建的所有子线程
threads.currentThread()
threads.currentThreadName()
```

---

## engines — 引擎管理

```javascript
engines.myScript()            // 当前脚本名
engines.myId()                // 当前引擎 id
engines.myEngine()            // 当前引擎对象
engines.runningScriptCount()  // 运行中脚本数
engines.all()                 // 运行中脚本名列表
engines.stopAllScripts()      // 停止全部脚本
```

---

## events — 事件总线

```javascript
// 全局事件总线
events.on("event", handler)
events.once("event", handler)
events.emit("event", data)
events.removeAllListeners("event")

// 独立发射器
var e = events.emitter()
e.on("x", fn); e.emit("x", 1); e.off("x")
```

> 键盘/触摸等系统级事件监听需 root 或无障碍注入，属后续增强。

---

## dialogs — 对话框

```javascript
dialogs.alert(title, content[, callback])
var ok = dialogs.confirm(title, content)          // 返回 Boolean
var s  = dialogs.rawInput(title[, hint])          // 返回 String / null
var i  = dialogs.singleChoice(title, [items])     // 返回索引 / -1
```

> UI 在主线程显示；无回调参数时为阻塞式（挂起当前脚本线程直到用户操作）。

---

## 顶层简写（对齐 AutoJS Pro）

选择器与常用动作可直接顶层调用，无需 `auto.` 前缀：

```javascript
text("登录") / textContains(...) / id(...) / desc(...) / clazz(...) / className(...)
click(x, y) / longClick(x, y) / swipe(x1,y1,x2,y2,dur)
waitFor() / scrollDown() / scrollUp()
```

---

## 全局函数

```javascript
log(msg)           // 打印日志（同时输出到 logcat，tag=CcAuto）
toast(msg)         // 弹出提示
sleep(ms)          // 休眠
exit()             // 结束当前脚本
random()           // [0,1) 随机小数
random(min, max)   // [min,max] 随机整数
require("module")  // 加载模块
setTimeout(fn, ms) / setInterval(fn, ms)
clearTimeout(id)   / clearInterval(id)
console.log/info/warn/error/verbose/debug/show/hide/clear
base64.encode(str|bytes) / base64.decode(str)
crypto.md5(str) / crypto.sha1(str) / crypto.sha256(str)
util.format(fmt, ...) / util.uuid()
```
