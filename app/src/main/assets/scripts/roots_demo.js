// Root 能力示例 —— roots 命名空间 / 全局 shell / 剪贴板
// 无 root 设备上会安全降级（roots.available()=false），不会崩溃
log("R roots.available=" + roots.available());

if (roots.available()) {
    var r = roots.exec("id");
    log("R id=" + r.result + " (code=" + r.code + ")");

    roots.shell("getprop ro.product.brand", function (res) {
        log("R async brand=" + res.result.trim());
    });

    var keyOk = events.observeKey();
    log("R root 按键通道=" + (keyOk ? "可用" : "不可用"));
} else {
    log("R 当前设备无 root，root 能力降级为不可用（预期行为）");
}

// 全局 shell：可调用，也带 .exec/.isRootAvailable
var s1 = shell("echo hello-shell");
log("R shell()=" + s1.result + " code=" + s1.code);
var s2 = shell.exec("echo shell-exec");
log("R shell.exec()=" + s2.result);

// 剪贴板
setClipBoard("cc-clip-123");
log("R clipboard=" + getClipBoard());
