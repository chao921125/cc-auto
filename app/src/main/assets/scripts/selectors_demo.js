// 顶层选择器与动作简写示例 —— 对齐 AutoJS Pro，可直接使用 text()/id()/click()/swipe()
"auto";
waitFor();

// 顶层选择器：找到文本为"确定"的控件并点击
var btn = text("确定").findOne(3000);
if (btn) {
    btn.click();
    log("已点击 确定");
} else {
    log("未找到 确定，演示坐标操作");
    // 顶层动作简写
    click(device.width / 2, device.height / 2);
    swipe(device.width / 2, device.height * 0.7, device.width / 2, device.height * 0.3, 500);
}

// 链式过滤
var inputs = className("android.widget.EditText").find();
log("输入框数量: " + inputs.length);

log("顶层简写演示结束");
