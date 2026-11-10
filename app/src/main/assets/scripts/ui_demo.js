// CcAuto 脚本 UI 示例 —— ui.layout + id 绑定 + 事件
// 注：Rhino 模板字符串不支持多行，XML 用字符串拼接书写
"ui";

var xml = '<vertical padding="16">' +
    '<text text="CcAuto UI 示例" textSize="20sp" textColor="#1976D2" marginBottom="16"/>' +
    '<text id="status" text="就绪" textSize="16sp"/>' +
    '<button id="btnRun" text="运行" marginTop="8"/>' +
    '<button id="btnStop" text="停止" marginTop="4"/>' +
    '<horizontal marginTop="16" w="*">' +
    '<text text="进度: " textSize="14sp"/>' +
    '<progressbar id="progress" max="100" progress="0" layout_weight="1"/>' +
    '</horizontal>' +
    '</vertical>';

ui.layout(xml);

ui.btnRun.on("click", function () {
    ui.status.setText("运行中...");
    var p = 0;
    var timer = setInterval(function () {
        p += 10;
        ui.run(function () { ui.progress.setProgress(p); });
        if (p >= 100) {
            clearInterval(timer);
            ui.status.setText("完成!");
        }
    }, 200);
});

ui.btnStop.on("click", function () {
    ui.status.setText("已停止");
});
