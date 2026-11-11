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
| `sensors` | 传感器读取/监听 | P1 |
| `power_manager` | 电源/电量/唤醒 | P1 |
| `canvas` | 2D 画布绘图 | P1 |

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
let w = floaty.window('<frame><text id="t" text="悬浮窗"/></frame>')
w.t.setText("计数: 1")        // XML 中 id 自动绑定到返回值
w.t.on("click", () => { ... })
w.close()                      // 或 floaty.close(w)
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

> 支持标签：vertical/horizontal/linear/frame/relative/text/button/input/img/checkbox/switch/scroll/list/progressbar/seekbar/spinner/radiogroup/canvas；
> 常用属性：id/text/hint/w/h/padding/margin*/layout_weight/bg/textColor/textSize/gravity/max/progress/checked。
> `<canvas>` 控件通过 `on("draw", fn)` 绑定绘制回调（参数为画布对象，见 canvas 章节），`invalidate()` 触发重绘。
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

var r = shell("echo hi")               // 全局可调用形式
var r2 = shell("getprop ro.product.brand", true)  // true = 经 su 执行
```

---

## roots — Root 能力（对齐 AutoJS Pro）

```javascript
roots.available()                 // 是否已 root（结果缓存）
var r = roots.exec("id")          // 同步 root 执行：r.code / r.result / r.error
roots.shell("getprop ro.product.brand", function (res) {
    log(res.result)               // 异步回调式
})
```

无 root 设备安全降级：`available()=false`、`exec` 返回错误码不崩溃。
root 设备上 `events.observeKey()` 会自动启用 **root 按键通道**（`su -c getevent -l` 读内核事件，作为无障碍过滤的兜底）。

---

## 剪贴板

```javascript
setClipBoard("文本")   // 别名 setClipboard
getClipBoard()         // 别名 getClipboard（Android 10+ 限前台读取）
```

---

## colors — 颜色运算

```javascript
colors.parse("#FF8000")        // → ARGB 整型
colors.rgb(255, 128, 0)        // 亦提供 rgba(r,g,b,a)
colors.red(c) / green / blue / alpha
colors.hex(c)                  // "#FF8000"
colors.mix(c1, c2[, ratio])    // 通道线性混合
colors.darker(c) / lighter(c)
colors.isSimilar(c1, c2[, threshold])
```

---

## keys — 按键模拟

```javascript
keys.press("volume_up")   // 音量类按键走 AudioManager，免 root
keys.press("back")        // 其他按键：root 经 su input；非 root 无法注入（返回 false）
keys.longPress(key) / keys.down(key) / keys.up(key)
```

> 键名同 events 按键（back/home/volume_up/enter/dpad_* 或数字 keyCode）。

---

## media — 音频播放

```javascript
media.playMusic("/sdcard/a.mp3"[, loop])   // 文件不存在/解码失败返回 false
media.pauseMusic() / media.resumeMusic() / media.stopMusic()
media.isMusicPlaying()
media.setMusicVolume(0.5)                  // 0.0–1.0
```

---

## settings — 系统设置

```javascript
settings.get("screen_brightness")          // 先 System 后 Secure
settings.getFloat(key)
settings.set("screen_brightness", 100)     // 需 WRITE_SETTINGS，未授权返回 false
settings.canWrite() / settings.requestWritePermission()
```

---

## sensors — 传感器

```javascript
sensors.has(sensors.ACCELEROMETER)          // 是否支持该类型
sensors.getSensorList()                     // 全部传感器（数组，元素含 name/vendor/type/maxRange...）
sensors.getSensorList(sensors.ACCELEROMETER)// 按类型过滤
sensors.register(sensors.GYROSCOPE, sensors.SENSOR_DELAY_UI)  // 开启采集（缓存最近读数）
sensors.on(sensors.ACCELEROMETER, function(type, values, ts){  // 事件回调
    log(values[0], values[1], values[2]);
});
sensors.acceleration()                      // 最近读数 {x,y,z,values,timestamp,accuracy}；首次访问自动注册
sensors.gravity() / sensors.linearAcceleration() / sensors.gyroscope()
sensors.magneticField() / sensors.light() / sensors.proximity() / sensors.pressure()
sensors.get(sensors.LIGHT)                  // 通用读取
sensors.unregister(sensors.ACCELEROMETER)   // 关闭；无参=全部
// 常量：ACCELEROMETER/MAGNETIC_FIELD/GYROSCOPE/LIGHT/PRESSURE/PROXIMITY/GRAVITY/
//       LINEAR_ACCELERATION/ROTATION_VECTOR/... 及 SENSOR_DELAY_NORMAL/UI/GAME/FASTEST
```

> 模拟器仅有部分虚拟传感器；真机更全。读数对象用 JS 属性访问（.x/.y/.z）。

---

## power_manager — 电源与电量

```javascript
power_manager.isScreenOn()                  // 屏幕是否亮（PowerManager.isInteractive）
power_manager.wakeUp()                      // 唤醒屏幕（input keyevent WAKEUP，免 root）
power_manager.sleep()                       // 熄屏（通常需 root，失败返回 false）
power_manager.batteryLevel()                // 0-100
power_manager.batteryStatus()               // 状态常量（STATUS_CHARGING/FULL/DISCHARGING...）
power_manager.isCharging()                  // 是否充电
power_manager.plugType()                    // PLUGGED_AC/USB/WIRELESS
power_manager.temperature() / power_manager.voltage() / power_manager.health()
power_manager.batteryInfo()                 // 汇总对象 {level,scale,status,health,plugged,temperature,voltage,isCharging}
power_manager.keepAwake(true)               // PARTIAL_WAKE_LOCK 保持 CPU（不锁屏）
power_manager.isDeviceIdleMode()
```

---

## canvas — 2D 画布

对齐 AutoJS Pro 一代 canvas 模块（Android Canvas 包装）。两种画布来源：

1. **位图画布**：`canvas.create(w, h)` / `canvas.fromBitmap(bmp)`（CcAuto 工厂入口；Pro 经 Java 交互 `new Canvas(bitmap)` 获得，本引擎沙箱不开放 Java 交互）。绘制结果用 `getBitmap()` 取出，可接 `images.save` / 找色。
2. **视图画布**：ui / floaty XML 中的 `<canvas id="board"/>` 控件，`ui.board.on("draw", function(c){...})` 回调参数即画布（与 Pro 一致）；`ui.board.invalidate()` 触发重绘。

```javascript
// 位图画布
let c = canvas.create(200, 120);
c.drawColor(colors.rgb(255, 255, 255));
let paint = new Paint();                 // 全局 Paint 构造器（对齐 Pro 写法）
paint.setColor(colors.rgb(255, 0, 0));
paint.setStyle(Paint.Style.FILL);        // FILL / STROKE / FILL_AND_STROKE
paint.setAntiAlias(true);
c.drawRect(10, 10, 50, 50, paint);       // 也接受 {left,top,right,bottom} 或 [l,t,r,b]
c.drawCircle(100, 60, 20, paint);
images.save(c.getBitmap(), "/sdcard/canvas_out.png");

// 视图画布（"ui" 模式）
ui.layout('<frame w="*" h="*"><canvas id="board" w="*" h="*"/></frame>');
ui.board.on("draw", function (c) {
    c.drawColor(colors.rgb(255, 255, 255));
    c.drawText("hello", 40, 100, paint);
});
```

画布方法（均已模拟器逐像素实测）：

```javascript
c.getWidth() / c.getHeight()
c.drawRGB(r, g, b) / c.drawARGB(a, r, g, b)
c.drawColor(color)                 // color 为整数或 "#RRGGBB" 字符串
c.drawColor(color, mode)           // mode 为 PorterDuff 模式名，如 "CLEAR"/"SRC_OVER"
c.drawPaint(paint)
c.drawPoint(x, y, paint) / c.drawPoints([x0,y0,x1,y1,...], paint)
c.drawLine(x1, y1, x2, y2, paint) / c.drawLines([x0,y0,x1,y1,...], paint)
c.drawRect(l, t, r, b, paint)      // 或 drawRect(rectObj, paint)
c.drawRoundRect(l, t, r, b, rx, ry, paint)   // 或 drawRoundRect(rectObj, rx, ry, paint)
c.drawCircle(cx, cy, radius, paint)
c.drawOval(l, t, r, b, paint)      // 或 drawOval(rectObj, paint)
c.drawArc(l, t, r, b, startAngle, sweepAngle, useCenter, paint)  // 或 drawArc(rectObj, ...)
c.drawPath(path, paint)
c.drawBitmap(bitmap, left, top[, paint])
c.drawText(text, x, y, paint)
c.drawTextOnPath(text, path, hOffset, vOffset, paint)
c.translate(dx, dy)
c.scale(sx, sy[, px, py]) / c.rotate(degrees[, px, py]) / c.skew(sx, sy)
c.save() / c.restore() / c.clipRect(l, t, r, b)
c.getBitmap()                      // 视图画布返回 null
canvas.paint() / canvas.path()     // 工厂简写
```

Paint（`new Paint()`，附 `Paint.Style/Align/Cap/Join` 常量）：

```javascript
paint.setColor(intOrHex) / paint.setARGB(a, r, g, b) / paint.setAlpha(a)
paint.getColor() / paint.getAlpha()
paint.setStyle(Paint.Style.FILL)   // 接受常量、字符串或序号
paint.setStrokeWidth(w) / paint.getStrokeWidth()
paint.setStrokeCap(Paint.Cap.ROUND) / paint.setStrokeJoin(Paint.Join.BEVEL)
paint.setAntiAlias(bool) / paint.isAntiAlias() / paint.setDither(bool)
paint.setTextSize(px) / paint.getTextSize() / paint.measureText(text)
paint.setTextAlign(Paint.Align.CENTER)
paint.setTextScaleX(x) / paint.setTextSkewX(x) / paint.setLetterSpacing(em)
paint.setFakeBoldText(bool) / paint.setUnderlineText(bool) / paint.setStrikeThruText(bool)
paint.setShadowLayer(radius, dx, dy, color) / paint.clearShadowLayer()
```

Path（`new Path()`）：

```javascript
path.moveTo(x, y) / path.lineTo(x, y)
path.quadTo(x1, y1, x2, y2) / path.cubicTo(x1, y1, x2, y2, x3, y3)
path.arcTo(l, t, r, b, startAngle, sweepAngle, forceMoveTo)
path.addRect(l, t, r, b) / path.addRoundRect(l, t, r, b, rx, ry)
path.addOval(l, t, r, b) / path.addCircle(cx, cy, r)
path.close() / path.reset() / path.offset(dx, dy)
```

> 与 Pro 的差异：不支持 `drawPicture`（Picture 录制）、Paint 的 shader/maskFilter/xfermode/typeface 设置；
> 矩形对象用 JS 对象 `{left,top,right,bottom}` 代替 Java Rect/RectF。视图画布使用软件层渲染，全部绘制操作可用。

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

// 硬件按键监听（需开启无障碍服务，由 onKeyEvent 过滤）
events.observeKey()
events.onKeyDown("volume_up", function (code) { ... })
events.onKeyUp("back", fn)
events.onceKeyDown("enter", fn)
events.removeAllKeyListeners()
```

> 按键名支持：back/home/menu/power/volume_up/volume_down/enter/del/space/tab/camera/dpad_* 或直接传 keyCode 数字。
> 注意：仅真实按键事件会进入无障碍 onKeyEvent 过滤；adb `input keyevent` 注入与部分系统快捷键（如电源）不会触发。触摸事件监听（需 root）仍为待实现。

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
