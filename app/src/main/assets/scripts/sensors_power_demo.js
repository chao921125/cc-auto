// 传感器与电源示例 —— sensors / power_manager

// ===== power_manager：电量与屏幕 =====
log("P batteryLevel=" + power_manager.batteryLevel() + "%");
log("P isCharging=" + power_manager.isCharging() + " plugType=" + power_manager.plugType());
log("P isScreenOn=" + power_manager.isScreenOn());
var info = power_manager.batteryInfo();
log("P batteryInfo: level=" + info.level + " status=" + info.status + " temp=" + info.temperature);

// ===== sensors：能力探测 =====
log("S has(accelerometer)=" + sensors.has(sensors.ACCELEROMETER));
log("S has(gyroscope)=" + sensors.has(sensors.GYROSCOPE));
log("S has(light)=" + sensors.has(sensors.LIGHT));
var all = sensors.getSensorList();
log("S 传感器总数=" + all.size());
for (var i = 0; i < all.size() && i < 5; i++) {
    var s = all.get(i);
    log("  - " + s.name + " [" + s.vendor + "]");
}

// ===== sensors：开启采集并读取最近值 =====
if (sensors.has(sensors.ACCELEROMETER)) {
    sensors.register(sensors.ACCELEROMETER, sensors.SENSOR_DELAY_UI);
    sleep(500);
    var a = sensors.acceleration();
    if (a !== null) {
        log("S 加速度 x=" + a.x + " y=" + a.y + " z=" + a.z);
    } else {
        log("S 暂无加速度读数（模拟器可能未推送）");
    }
    sensors.unregister(sensors.ACCELEROMETER);
}

// ===== sensors：事件回调（2 秒演示）=====
log("S 监听陀螺仪 2 秒…");
var hits = 0;
sensors.on(sensors.GYROSCOPE, function (type, values, ts) {
    hits++;
    if (hits <= 3) log("  陀螺仪回调 #" + hits + ": " + values[0] + "," + values[1] + "," + values[2]);
});
sleep(2000);
sensors.unregister(sensors.GYROSCOPE);
log("S 陀螺仪回调次数=" + hits);

log("完成");
