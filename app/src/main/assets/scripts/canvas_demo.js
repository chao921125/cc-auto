// CcAuto canvas 示例 —— 位图画布 + ui <canvas> 控件 draw 事件
// 对齐 AutoJS Pro 一代 canvas 模块；Paint/Path 为全局构造器
"ui";

// ---------- 1. 位图画布：绘制并保存 PNG ----------
var c = canvas.create(360, 240);
c.drawColor(colors.rgb(255, 255, 255));

var fill = new Paint();
fill.setColor(colors.rgb(25, 118, 210));
fill.setStyle(Paint.Style.FILL);
fill.setAntiAlias(true);
c.drawRoundRect(10, 10, 170, 110, 16, 16, fill);
c.drawCircle(280, 60, 45, fill);

var stroke = new Paint();
stroke.setColor(colors.rgb(211, 47, 47));
stroke.setStyle(Paint.Style.STROKE);
stroke.setStrokeWidth(4);
stroke.setAntiAlias(true);
c.drawRect(10, 130, 170, 230, stroke);

var path = new Path();
path.moveTo(200, 230);
path.lineTo(280, 130);
path.lineTo(350, 230);
path.close();
c.drawPath(path, stroke);

var text = new Paint();
text.setColor(colors.rgb(33, 33, 33));
text.setTextSize(28);
text.setAntiAlias(true);
c.drawText("CcAuto canvas", 200, 180, text);

var out = "/sdcard/canvas_demo_" + Date.now() + ".png";
if (images.save(c.getBitmap(), out)) {
    log("位图画布已保存: " + out);
}

// ---------- 2. ui <canvas> 控件：draw 回调 + invalidate 重绘 ----------
var xml = '<vertical padding="16" bg="#FFFFFF">' +
    '<text text="canvas 控件示例（点按钮重绘）" textSize="18sp" textColor="#1976D2" marginBottom="8"/>' +
    '<canvas id="board" w="*" h="300" bg="#F5F5F5"/>' +
    '<button id="btnRedraw" text="随机重绘" marginTop="8"/>' +
    '</vertical>';
ui.layout(xml);

var seed = 1;
function rnd() {
    seed = (seed * 1103515245 + 12345) % 2147483648;
    return seed / 2147483648;
}

ui.board.on("draw", function (cv) {
    cv.drawColor(colors.rgb(245, 245, 245));
    var p = new Paint();
    p.setStyle(Paint.Style.FILL);
    p.setAntiAlias(true);
    var w = cv.getWidth(), h = cv.getHeight();
    for (var i = 0; i < 12; i++) {
        p.setColor(colors.rgba(
            Math.floor(rnd() * 256), Math.floor(rnd() * 256), Math.floor(rnd() * 256), 200));
        cv.drawCircle(rnd() * w, rnd() * h, 10 + rnd() * 40, p);
    }
    var t = new Paint();
    t.setColor(colors.rgb(33, 33, 33));
    t.setTextSize(36);
    t.setTextAlign(Paint.Align.CENTER);
    cv.drawText("draw 回调绘制", w / 2, h / 2, t);
});

ui.btnRedraw.on("click", function () {
    seed = Date.now() % 2147483648;
    ui.board.invalidate();
});