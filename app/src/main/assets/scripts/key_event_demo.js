// 按键事件示例 —— 经无障碍服务监听硬件按键（需在设置中开启无障碍服务）
"auto";

if (events.observeKey()) {
    log("K 按键监听已开启（试试音量键/返回键）");
}

events.onKeyDown("volume_up", function (code) {
    log("K 音量+ 按下, code=" + code);
});

events.onKeyUp("volume_up", function (code) {
    log("K 音量+ 松开");
});

events.onKeyDown("back", function (code) {
    log("K 返回键 按下, code=" + code);
});

// 30 秒后自动结束
setTimeout(function () {
    events.removeAllKeyListeners();
    log("K 按键演示结束");
}, 30000);
