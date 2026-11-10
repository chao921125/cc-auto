// engines 命名空间示例 —— 查询与控制运行中的脚本引擎
"auto";

log("当前脚本: " + engines.myScript());
log("当前引擎 id: " + engines.myId());
log("运行中脚本数: " + engines.runningScriptCount());
log("运行中脚本列表: " + engines.all().join(", "));

// engines.myEngine() 返回引擎对象，可读取其信息
var eng = engines.myEngine();
if (eng) log("引擎对象可用");

log("engines 演示结束");
