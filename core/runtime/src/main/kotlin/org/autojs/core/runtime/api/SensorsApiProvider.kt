package org.autojs.core.runtime.api

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime
import org.autojs.core.runtime.callJsFunction
import org.mozilla.javascript.Function
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * sensors 命名空间：传感器读取与监听（对齐 AutoJS Pro 的 sensors 模块）。
 *
 * - 同步读取：sensors.has / getSensorList / get / 各便捷 getter（acceleration 等）
 * - 事件监听：sensors.on(type, cb) / register / unregister，回调经 callJsFunction 跨线程安全派发
 *
 * 说明：模拟器仅提供部分虚拟传感器，真机传感器更全；API 结构一致。
 */
class SensorsApiProvider : ApiProvider {
    override val name = "sensors"

    private val sm: SensorManager?
        get() = AppRuntime.context?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // 各类型最近一次读数
    private val latest = ConcurrentHashMap<Int, FloatArray>()
    private val latestAccuracy = ConcurrentHashMap<Int, Int>()
    private val latestTime = ConcurrentHashMap<Int, Long>()
    // 事件回调
    private val callbacks = ConcurrentHashMap<Int, CopyOnWriteArrayList<Function>>()

    private var handlerThread: HandlerThread? = null
    private val handler: Handler by lazy {
        val ht = HandlerThread("CcAutoSensors").also { it.start() }
        handlerThread = ht
        Handler(ht.looper)
    }

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val type = event.sensor.type
            val values = event.values.copyOf()
            latest[type] = values
            latestTime[type] = event.timestamp
            latestAccuracy[type] = event.accuracy
            val cbs = callbacks[type] ?: return
            if (cbs.isEmpty()) return
            // 以 (sensorType, values[], timestamp) 三参派发；FloatArray 在 Rhino 中即为 JS 数组
            for (fn in cbs) {
                callJsFunction(fn, arrayOf(type, values, event.timestamp))
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            latestAccuracy[sensor.type] = accuracy
        }
    }

    override fun install(scope: ScriptScope) {
        scope.defineObject("sensors") {
            function("has") { args ->
                val t = intOf(args.getOrNull(0))
                t != null && sm?.getDefaultSensor(t) != null
            }

            function("getSensorList") { args ->
                val t = intOf(args.getOrNull(0))
                val list = if (t != null) sm?.getSensorList(t) else sm?.getSensorList(Sensor.TYPE_ALL)
                // 用 createDynamicObject 生成真正的 JS 对象（Kotlin map 会被沙箱 ClassShutter 拦截）
                val result = ArrayList<Any?>()
                for (s in (list ?: emptyList())) {
                    result.add(
                        scope.createDynamicObject(
                            mutableMapOf(
                                "name" to s.name,
                                "vendor" to s.vendor,
                                "stringType" to s.stringType,
                                "type" to s.type,
                                "maxRange" to s.maximumRange,
                                "resolution" to s.resolution,
                                "power" to s.power,
                                "minDelay" to s.minDelay,
                                "maxDelay" to s.maxDelay,
                                "isWakeUp" to s.isWakeUpSensor
                            )
                        )
                    )
                }
                result
            }

            function("getDefaultSensorName") { args ->
                val t = intOf(args.getOrNull(0)) ?: return@function null
                sm?.getDefaultSensor(t)?.name
            }

            function("register") { args ->
                val t = intOf(args.getOrNull(0)) ?: return@function false
                val delay = intOf(args.getOrNull(1)) ?: SensorManager.SENSOR_DELAY_NORMAL
                registerType(t, delay)
            }

            function("enable") { args ->
                val t = intOf(args.getOrNull(0)) ?: return@function false
                val delay = intOf(args.getOrNull(1)) ?: SensorManager.SENSOR_DELAY_NORMAL
                registerType(t, delay)
            }

            function("on") { args ->
                val t = intOf(args.getOrNull(0)) ?: return@function false
                val fn = args.getOrNull(1) as? Function ?: return@function false
                val delay = intOf(args.getOrNull(2)) ?: SensorManager.SENSOR_DELAY_NORMAL
                if (!registerType(t, delay)) return@function false
                callbacks.getOrPut(t) { CopyOnWriteArrayList() }.add(fn)
                true
            }

            function("unregister") { args ->
                val t = intOf(args.getOrNull(0))
                if (t == null) {
                    sm?.unregisterListener(listener)
                    callbacks.clear()
                } else {
                    callbacks.remove(t)
                    sm?.getDefaultSensor(t)?.let { sm?.unregisterListener(listener, it) }
                }
                true
            }

            function("disable") { args ->
                val t = intOf(args.getOrNull(0))
                if (t == null) {
                    sm?.unregisterListener(listener); callbacks.clear()
                } else {
                    callbacks.remove(t)
                    sm?.getDefaultSensor(t)?.let { sm?.unregisterListener(listener, it) }
                }
                true
            }

            // 读取最近一次读数：{x, y, z, values[], timestamp, accuracy}
            function("get") { args ->
                val t = intOf(args.getOrNull(0)) ?: return@function null
                snapshot(scope, t)
            }

            function("acceleration") { _ -> lazySnapshot(scope, Sensor.TYPE_ACCELEROMETER) }
            function("gravity") { _ -> lazySnapshot(scope, Sensor.TYPE_GRAVITY) }
            function("linearAcceleration") { _ -> lazySnapshot(scope, Sensor.TYPE_LINEAR_ACCELERATION) }
            function("gyroscope") { _ -> lazySnapshot(scope, Sensor.TYPE_GYROSCOPE) }
            function("magneticField") { _ -> lazySnapshot(scope, Sensor.TYPE_MAGNETIC_FIELD) }
            function("light") { _ -> lazySnapshot(scope, Sensor.TYPE_LIGHT) }
            function("proximity") { _ -> lazySnapshot(scope, Sensor.TYPE_PROXIMITY) }
            function("pressure") { _ -> lazySnapshot(scope, Sensor.TYPE_PRESSURE) }
            function("rotationVector") { _ -> lazySnapshot(scope, Sensor.TYPE_ROTATION_VECTOR) }

            // 常量：传感器类型
            property("ACCELEROMETER", Sensor.TYPE_ACCELEROMETER)
            property("MAGNETIC_FIELD", Sensor.TYPE_MAGNETIC_FIELD)
            property("ORIENTATION", Sensor.TYPE_ORIENTATION)
            property("GYROSCOPE", Sensor.TYPE_GYROSCOPE)
            property("LIGHT", Sensor.TYPE_LIGHT)
            property("PRESSURE", Sensor.TYPE_PRESSURE)
            property("TEMPERATURE", Sensor.TYPE_TEMPERATURE)
            property("PROXIMITY", Sensor.TYPE_PROXIMITY)
            property("GRAVITY", Sensor.TYPE_GRAVITY)
            property("LINEAR_ACCELERATION", Sensor.TYPE_LINEAR_ACCELERATION)
            property("ROTATION_VECTOR", Sensor.TYPE_ROTATION_VECTOR)
            property("RELATIVE_HUMIDITY", Sensor.TYPE_RELATIVE_HUMIDITY)
            property("AMBIENT_TEMPERATURE", Sensor.TYPE_AMBIENT_TEMPERATURE)
            // 常量：采样延迟
            property("SENSOR_DELAY_NORMAL", SensorManager.SENSOR_DELAY_NORMAL)
            property("SENSOR_DELAY_UI", SensorManager.SENSOR_DELAY_UI)
            property("SENSOR_DELAY_GAME", SensorManager.SENSOR_DELAY_GAME)
            property("SENSOR_DELAY_FASTEST", SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    private fun registerType(type: Int, delayUs: Int): Boolean {
        val sensor = sm?.getDefaultSensor(type) ?: return false
        return runCatching { sm?.registerListener(listener, sensor, delayUs, handler); true }.getOrDefault(false)
    }

    private fun snapshot(scope: ScriptScope, type: Int): Any? {
        val v = latest[type] ?: return null
        return scope.createDynamicObject(
            mutableMapOf(
                "x" to v.getOrElse(0) { 0f },
                "y" to v.getOrElse(1) { 0f },
                "z" to v.getOrElse(2) { 0f },
                "values" to v.toList(),
                "timestamp" to (latestTime[type] ?: 0L),
                "accuracy" to (latestAccuracy[type] ?: 0)
            )
        )
    }

    /** 便捷 getter：首次访问自动注册，随后返回最近读数（无读数时返回 null）。 */
    private fun lazySnapshot(scope: ScriptScope, type: Int): Any? {
        if (!latest.containsKey(type)) registerType(type, SensorManager.SENSOR_DELAY_UI)
        return snapshot(scope, type)
    }

    private fun intOf(v: Any?): Int? = when (v) {
        is Number -> v.toInt()
        is String -> v.toIntOrNull()
        else -> null
    }
}
