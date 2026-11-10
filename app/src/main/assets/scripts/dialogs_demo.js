// dialogs 命名空间示例 —— 脚本中弹出对话框（回调式，不阻塞）
"auto";

// 提示框（回调）
dialogs.alert("CcAuto", "这是一个提示框", function () {
    log("用户点了确定");

    // 确认框（阻塞式，返回 true/false；在脚本线程等待用户）
    var ok = dialogs.confirm("确认", "继续吗？");
    log("confirm 返回: " + ok);

    // 输入框（阻塞式，返回字符串或 null）
    var name = dialogs.rawInput("请输入名字");
    log("输入内容: " + name);

    // 单选列表（阻塞式，返回索引，取消为 -1）
    var idx = dialogs.singleChoice("选择水果", ["苹果", "香蕉", "橙子"]);
    log("选择了索引: " + idx);
});
