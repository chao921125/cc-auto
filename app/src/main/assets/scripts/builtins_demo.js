// 内置工具函数示例 —— 演示 console / toast / random / base64 / crypto / util / exit
"auto";

// console 支持分级输出
console.log("普通日志");
console.info("信息");
console.warn("警告");
console.error("错误");

// toast 弹出提示（需要悬浮/通知权限时也会以日志输出）
toast("CcAuto 内置函数演示");

// random 随机数：无参返回 [0,1)，两参返回 [min,max] 的整数
log("随机小数: " + random());
log("随机整数 1-100: " + random(1, 100));

// base64 编解码
var enc = base64.encode("Hello CcAuto");
log("base64 编码: " + enc);
log("base64 解码: " + base64.decode(enc));

// crypto 哈希（十六进制）
log("md5: " + crypto.md5("abc"));
log("sha1: " + crypto.sha1("abc"));
log("sha256: " + crypto.sha256("abc"));

// util.format 字符串格式化
log(util.format("用户 %s 得分 %d", "张三", 100));

log("演示结束");
exit();
