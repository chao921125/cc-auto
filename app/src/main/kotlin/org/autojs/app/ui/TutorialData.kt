package org.autojs.app.ui

/** 内置教程条目。body 为纯文本讲解，sample 为 assets/scripts 下可运行示例文件名。 */
data class TutorialTopic(
    val category: String,
    val title: String,
    val summary: String,
    val body: String,
    val sample: String? = null
)

object TutorialData {
    val topics: List<TutorialTopic> = listOf(
        // ===== 入门 =====
        TutorialTopic(
            "入门", "快速开始", "认识 CcAuto 与第一个脚本",
            "CcAuto 是一款零 Root 的 Android 自动化引擎，使用 JavaScript 编写脚本。\n\n" +
                "1. 在「设置」中开启无障碍服务与悬浮窗权限。\n" +
                "2. 在「脚本」页新建脚本或项目。\n" +
                "3. 点击运行，在「管理」页查看运行状态与日志。\n\n" +
                "脚本通常以 \"auto\"; 开头请求无障碍权限。log()/console.log() 输出日志，" +
                "toast() 弹出提示，exit() 结束脚本。",
            "device_info.js"
        ),
        TutorialTopic(
            "入门", "内置函数", "console / toast / random / base64 / crypto / util",
            "无需 require 即可使用的全局能力：\n\n" +
                "log(a,b..) / console.log|info|warn|error  日志\n" +
                "toast(msg)   弹出提示\n" +
                "sleep(ms)    阻塞当前线程\n" +
                "exit()       结束当前脚本\n" +
                "random() / random(min,max)  随机数\n" +
                "base64.encode/decode        编解码\n" +
                "crypto.md5/sha1/sha256      哈希\n" +
                "util.format(fmt, ...)       字符串格式化\n" +
                "setClipBoard/getClipBoard   剪贴板（读写）",
            "builtins_demo.js"
        ),

        // ===== UI 自动化 =====
        TutorialTopic(
            "UI 自动化", "控件选择器", "auto.text/id/desc/clazz + 链式查找",
            "选择器返回一个查询对象 UiQuery，可继续链式过滤并查找：\n\n" +
                "auto.text(\"确定\")            精确文本\n" +
                "auto.textContains(\"登录\")    包含文本\n" +
                "auto.id(\"btn_ok\")            控件 id\n" +
                "auto.desc(\"...\") / auto.clazz(\"android.widget.Button\")\n" +
                "  .clickable(true).depth(3)   追加条件\n\n" +
                "查询动作：\n" +
                "  findOne(timeout) 单个控件  find()/findAll() 全部\n" +
                "  exists() 是否存在  waitFor() 等待出现\n" +
                "控件动作：\n" +
                "  node.click() / longClick() / setText(\"内容\") / bounds()",
            "auto_click.js"
        ),
        TutorialTopic(
            "UI 自动化", "坐标与手势", "click / longClick / swipe / gesture",
            "基于坐标的操作（无需控件）：\n\n" +
                "auto.click(x, y)          点击\n" +
                "auto.longClick(x, y)      长按\n" +
                "auto.swipe(x1,y1,x2,y2,duration)  滑动\n" +
                "auto.gesture(duration, [[x,y],[x,y]...])  多指路径\n\n" +
                "结合 device.width/device.height 做比例定位更适配不同分辨率。",
            "swipe_demo.js"
        ),
        TutorialTopic(
            "UI 自动化", "等待与滚动", "waitFor / scrollDown / scrollForward",
            "页面加载需要等待：\n\n" +
                "auto.waitFor()            等待无障碍就绪\n" +
                "auto.text(\"加载完成\").waitFor()  等待控件出现\n\n" +
                "滚动查找目标：\n" +
                "scrollDown() / scrollUp()\n" +
                "或控件级 query.scrollForward() / scrollBackward()",
            "wait_widget.js"
        ),
        TutorialTopic(
            "UI 自动化", "遍历与窗口", "遍历控件树 / 多窗口",
            "分析界面结构：\n\n" +
                "auto.textContains(\"\").find() 批量获取后 each 遍历\n" +
                "query.each(node => log(node.text()))\n\n" +
                "多窗口：\n" +
                "auto.windows() 返回当前所有窗口快照\n" +
                "auto.window(i) 取第 i 个窗口",
            "traverse_ui.js"
        ),

        // ===== 图像与视觉 =====
        TutorialTopic(
            "图像与视觉", "截图与找图", "images 模板匹配",
            "先申请截图权限：\n\n" +
                "images.requestScreenCapture()  弹出授权\n" +
                "var img = images.captureScreen()  截屏\n" +
                "images.findImage(img, \"tpl.png\", {threshold:0.9})\n\n" +
                "返回匹配区域 Region（含 bounds/center），可取其坐标点击。",
            "multiscale_match.js"
        ),
        TutorialTopic(
            "图像与视觉", "找色", "findColor / findMultiColors / detectsColor",
            "颜色相关：\n\n" +
                "images.findColor(img, 0xFF6F00, {region:[x,y,w,h], threshold:16})\n" +
                "images.findMultiColors(img, firstColor, offsets, {region})\n" +
                "images.detectsColor(img, color, x, y)  单点判断\n\n" +
                "找到后 auto.click(p.x, p.y)。",
            "find_color.js"
        ),
        TutorialTopic(
            "图像与视觉", "OCR 文字识别", "ocr.recognize",
            "对截图做文字识别（ML Kit 中文）：\n\n" +
                "images.requestScreenCapture()\n" +
                "var img = images.captureScreen()\n" +
                "var res = ocr.recognize(img)\n" +
                "res.texts / res.words  // 识别到的文本与位置\n\n" +
                "适合无 id 的纯文本场景定位。",
            "ocr_demo.js"
        ),
        TutorialTopic(
            "图像与视觉", "图像处理", "read / save / crop / resize / pixel",
            "图像工具：\n\n" +
                "images.read(path) / img.save(path)\n" +
                "images.crop(img, x, y, w, h)\n" +
                "images.resize(img, w, h)\n" +
                "images.pixel(img, x, y)  取色\n" +
                "img.close()  释放内存",
            "screenshot_save.js"
        ),

        // ===== 界面 =====
        TutorialTopic(
            "界面", "悬浮窗", "floaty.window + id 绑定",
            "floaty 创建置顶悬浮窗，XML 中 id 自动绑定到返回值：\n\n" +
                "var w = floaty.window('<frame><text id=\"t\" text=\"运行中\"/></frame>')\n" +
                "w.t.setText(\"计数: 1\")   // id 绑定更新\n" +
                "w.t.on(\"click\", fn)      // 控件事件\n" +
                "w.close() / floaty.close(w)\n" +
                "floaty.closeAll()\n" +
                "floaty.isOverlaysEnabled() / openOverlaySettings()",
            "floaty_demo.js"
        ),
        TutorialTopic(
            "界面", "脚本 UI", "ui.layout + id 绑定 + 事件",
            "以 \"ui\"; 开头进入脚本 UI 模式，ui.layout(xml) 全屏渲染原生界面：\n\n" +
                "XML 中 id=\"btnRun\" 的控件自动绑定为 ui.btnRun\n" +
                "ui.btnRun.on(\"click\") { ... }  事件回调（主线程执行）\n" +
                "ui.status.setText(\"...\") / ui.progress.setProgress(30)\n" +
                "ui.run(fn) 在主线程执行并取返回值；ui.finish() 关闭界面\n\n" +
                "支持 vertical/horizontal/text/button/input/progressbar/switch 等常用标签。\n" +
                "注意 Rhino 多行模板字符串不可用，XML 用字符串拼接书写。",
            "ui_demo.js"
        ),

        // ===== 网络 =====
        TutorialTopic(
            "网络", "HTTP 请求", "http.get/post/postJson",
            "基于 OkHttp：\n\n" +
                "var res = http.get(\"https://api/x\")\n" +
                "res.statusCode / res.body.string()\n\n" +
                "http.post(url, {body:...})\n" +
                "http.postJson(url, {id:1})\n" +
                "http.put / http.delete",
            "http_demo.js"
        ),
        TutorialTopic(
            "网络", "下载/上传/WebSocket", "download / uploadFile / newWebSocket",
            "文件传输与长连接：\n\n" +
                "http.download(url, \"/sdcard/a.zip\")\n" +
                "http.uploadFile(url, {file:\"/sdcard/a.txt\"})\n" +
                "http.newWebSocket(url, {\n" +
                "  onMessage(ws,msg){...}, onOpen(ws){...}\n" +
                "})",
            "download_demo.js"
        ),

        // ===== 文件与存储 =====
        TutorialTopic(
            "文件与存储", "文件读写", "files.*",
            "files 命名空间：\n\n" +
                "files.read/write/append(path, text)\n" +
                "files.readBytes/writeBytes\n" +
                "files.copy/move/remove/exists\n" +
                "files.isFile/isDir/size/list(path)\n" +
                "files.mkdir/create(path)\n" +
                "files.zip(src,dst) / files.unzip(zip,dst)",
            "file_ops.js"
        ),
        TutorialTopic(
            "文件与存储", "键值存储", "storages（MMKV）",
            "持久化键值：\n\n" +
                "var s = storages.create(\"config\")\n" +
                "s.put(\"token\", \"abc\")\n" +
                "s.get(\"token\", \"\")\n" +
                "storages.remove(\"config\")\n\n" +
                "基于 MMKV，跨脚本共享、重启不丢。",
            "storage_demo.js"
        ),

        // ===== 设备与应用 =====
        TutorialTopic(
            "设备与应用", "设备信息", "device.*",
            "只读设备属性：\n\n" +
                "device.width / height / density / densityDpi\n" +
                "device.brand / model / manufacturer\n" +
                "device.sdkInt / release\n" +
                "device.battery / isCharging / androidId\n" +
                "device.keepScreenOn()  保持屏幕常亮",
            "device_info.js"
        ),
        TutorialTopic(
            "设备与应用", "应用管理", "app.*",
            "应用启动与信息：\n\n" +
                "app.launch(\"com.tencent.mm\")  按包名启动\n" +
                "app.launchApp(\"微信\")\n" +
                "app.openUrl(\"https://...\")\n" +
                "app.viewFile(path)\n" +
                "app.isInstalled(pkg) / getInstalledPackages()\n" +
                "app.getAppName / getAppVersion / uninstall",
            "app_demo.js"
        ),

        // ===== 系统能力 =====
        TutorialTopic(
            "系统能力", "Shell 与 Root", "shell() / roots.exec / 无 root 降级",
            "全局 shell 可调用，也带成员：\n\n" +
                "var r = shell(\"echo hi\")        // r.code / r.result / r.error\n" +
                "shell(cmd, true)                // 经 su 执行\n" +
                "shell.exec(cmd[, root]) / shell.isRootAvailable()\n\n" +
                "roots 命名空间（对齐 AutoJS Pro）：\n" +
                "roots.available()               是否已 root\n" +
                "roots.exec(cmd)                 同步 root 执行\n" +
                "roots.shell(cmd, callback)      异步回调\n\n" +
                "无 root 设备全部安全降级、不崩溃。",
            "roots_demo.js"
        ),
        TutorialTopic(
            "系统能力", "Shizuku", "shizuku.exec",
            "免 root 的高级系统权限（需安装并激活 Shizuku）：\n\n" +
                "shizuku.isAvailable()\n" +
                "shizuku.checkPermission()\n" +
                "shizuku.exec(\"pm list packages\")",
            "shizuku_demo.js"
        ),
        TutorialTopic(
            "系统能力", "消息通知", "notifications.*",
            "读取/监听系统通知（需通知使用权）：\n\n" +
                "notifications.on(\"post\", n => log(n.title))\n" +
                "notifications.getActive()\n" +
                "notifications.cancel(key)\n" +
                "notifications.isRunning()",
            "notification_listen.js"
        ),

        // ===== AI 识别 =====
        TutorialTopic(
            "AI 识别", "目标检测", "ml.detect（ONNX）",
            "基于 ONNX Runtime 的目标检测：\n\n" +
                "var det = ml.createDetector(\"model.onnx\")\n" +
                "var objs = ml.detect(img)  // 返回带类别与置信度的框\n" +
                "ml.release(det)",
            "yolov8_detect.js"
        ),

        // ===== 定时器 =====
        TutorialTopic(
            "定时器", "setTimeout / setInterval", "浏览器式事件循环",
            "每个脚本线程自带事件循环：\n\n" +
                "setTimeout(()=>log(\"3 秒后\"), 3000)\n" +
                "var id = setInterval(()=>log(\"tick\"), 1000)\n" +
                "clearTimeout(id) / clearInterval(id)\n\n" +
                "脚本主逻辑结束后，事件循环会维持到无定时器/子任务。",
            "timer_demo.js"
        ),

        // ===== 多线程与引擎 =====
        TutorialTopic(
            "多线程与引擎", "多线程 threads", "threads.start / shutDownAll",
            "每个脚本线程自带 Rhino 上下文：\n\n" +
                "var t = threads.start(function(){ ... });\n" +
                "t.interrupt() / t.join() / t.isAlive()\n" +
                "threads.shutDownAll()  停止本脚本创建的子线程\n" +
                "threads.currentThreadName()\n\n" +
                "子线程共享脚本作用域变量，注意并发。",
            "threads_demo.js"
        ),
        TutorialTopic(
            "多线程与引擎", "脚本引擎 engines", "myEngine / stopAllScripts",
            "查询与运行控制：\n\n" +
                "engines.myScript()      当前脚本名\n" +
                "engines.myId()          当前引擎 id\n" +
                "engines.myEngine()      当前引擎对象\n" +
                "engines.runningScriptCount() / engines.all()\n" +
                "engines.stopAllScripts()  停止全部脚本",
            "engines_demo.js"
        ),

        // ===== 事件与对话框 =====
        TutorialTopic(
            "事件与对话框", "事件总线 events", "events.on / emit / emitter",
            "全局事件总线：\n\n" +
                "events.on(\"evt\", data => log(data))\n" +
                "events.once(evt, fn) / events.removeAllListeners(evt)\n" +
                "events.emit(\"evt\", payload)\n\n" +
                "独立发射器：\n" +
                "var e = events.emitter(); e.on(\"x\", fn); e.emit(\"x\", 1)\n\n" +
                "注：触摸事件监听（需 root）仍为待实现。",
            "events_demo.js"
        ),
        TutorialTopic(
            "事件与对话框", "按键监听", "events.observeKey / onKeyDown",
            "经无障碍服务的按键过滤监听硬件按键（需先在设置开启无障碍服务）：\n\n" +
                "events.observeKey()\n" +
                "events.onKeyDown(\"volume_up\", function(code){ ... })\n" +
                "events.onKeyUp(\"back\", fn)\n" +
                "events.onceKeyDown(\"enter\", fn)\n" +
                "events.removeAllKeyListeners()\n\n" +
                "键名：back/home/menu/volume_up/volume_down/enter/del/space/dpad_* 或数字 keyCode。\n" +
                "observeKey() 双通道：无障碍过滤 + root 设备上的 getevent 内核读取兜底。\n" +
                "注意：adb 注入的 keyevent 不经过无障碍过滤，验证需真实按键或 root。",
            "key_event_demo.js"
        ),
        TutorialTopic(
            "事件与对话框", "对话框 dialogs", "alert / confirm / rawInput / singleChoice",
            "脚本中弹出系统对话框（UI 在主线程执行）：\n\n" +
                "dialogs.alert(title, content[, callback])\n" +
                "var ok = dialogs.confirm(title, content)      返回 Boolean\n" +
                "var s  = dialogs.rawInput(title[, hint])      返回 String/null\n" +
                "var i  = dialogs.singleChoice(title, [..])    返回索引/-1\n\n" +
                "阻塞式调用会挂起当前脚本线程直到用户操作；回调式不阻塞。",
            "dialogs_demo.js"
        ),

        // ===== 顶层简写 =====
        TutorialTopic(
            "UI 自动化", "顶层简写", "text()/id()/click()/swipe() 直接调用",
            "对齐 AutoJS Pro：选择器与常用动作可直接顶层调用，无需 auto. 前缀：\n\n" +
                "text(\"登录\") / textContains / id / desc / clazz / className\n" +
                "click(x,y) / longClick(x,y) / swipe(...) / waitFor()\n" +
                "scrollDown() / scrollUp()\n\n" +
                "返回值同样是 UiQuery，可 findOne/find/click/setText 链式操作。",
            "selectors_demo.js"
        )
    )

    /** 按分类分组，保持插入顺序。 */
    val grouped: List<Pair<String, List<TutorialTopic>>> by lazy {
        val map = LinkedHashMap<String, MutableList<TutorialTopic>>()
        topics.forEach { map.getOrPut(it.category) { mutableListOf() }.add(it) }
        map.map { it.key to it.value }
    }
}
