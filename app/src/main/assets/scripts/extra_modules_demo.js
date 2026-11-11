// 补充模块示例 —— colors / keys / media / settings
// colors：颜色运算
var red = colors.parse("#FF0000");
log("C parse(#FF0000)=" + red);
log("C red(green)=" + colors.green(red) + " blue=" + colors.blue(red));
log("C hex(rgb(255,128,0))=" + colors.hex(colors.rgb(255, 128, 0)));
log("C mix(黑,白,0.5)=" + colors.hex(colors.mix(colors.parse("#000000"), colors.parse("#FFFFFF"), 0.5)));
log("C isSimilar(红,略红,10)=" + colors.isSimilar(colors.parse("#FF0000"), colors.parse("#F50000"), 10));

// keys：按键模拟（音量键走 AudioManager，免 root 可靠）
var v0 = settings.get("volume_music");
var ok = keys.press("volume_up");
sleep(400);
var v1 = settings.get("volume_music");
log("K keys.press(volume_up)=" + ok + " 音量 " + v0 + " → " + v1);

// settings：系统设置读取
log("S brightness=" + settings.get("screen_brightness"));
log("S canWrite=" + settings.canWrite());

// media：音频播放（文件不存在时安全返回 false）
log("M playMusic(不存在文件)=" + media.playMusic("/sdcard/__none__.mp3"));
log("M isMusicPlaying=" + media.isMusicPlaying());
media.stopMusic();
