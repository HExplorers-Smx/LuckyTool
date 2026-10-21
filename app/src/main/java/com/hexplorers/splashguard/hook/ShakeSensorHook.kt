package com.hexplorers.splashguard.hook

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.hexplorers.splashguard.SkipSettings
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

/**
 * Prevents registered advertising SDKs from receiving motion sensor samples
 * by rejecting registration at the public SensorManager overloads.
 * Normal app/game/pedometer listeners are not modified.
 */
internal class ShakeSensorHook(private val config: SkipSettings) : YukiBaseHooker() {

    override fun onHook() {
        SensorManager::class.java.declaredMethods.asSequence()
            .filter { it.name == "registerListener" }
            .filter { it.returnType == java.lang.Boolean.TYPE }
            .filter { method ->
                val parameters = method.parameterTypes
                parameters.size >= 3 &&
                    SensorEventListener::class.java.isAssignableFrom(parameters[0]) &&
                    Sensor::class.java.isAssignableFrom(parameters[1])
            }
            .forEach { method ->
                method.hook {
                    before {
                        val listener = args.getOrNull(0) as? SensorEventListener ?: return@before
                        val sensor = args.getOrNull(1) as? Sensor ?: return@before
                        val relevantSensor = sensor.type == Sensor.TYPE_ACCELEROMETER ||
                            (config.includeGyroscope && sensor.type == Sensor.TYPE_GYROSCOPE)
                        if (!relevantSensor) return@before

                        val listenerClass = listener.javaClass.name
                        val callerClasses = if (ShakeAdClassifier.isKnownAdSdkClass(listenerClass)) {
                            emptyList()
                        } else {
                            Throwable().stackTrace.asSequence()
                                .drop(2)
                                .take(24)
                                .map { it.className }
                                .toList()
                        }
                        if (!ShakeAdClassifier.fromListenerOrStack(listenerClass, callerClasses)) {
                            return@before
                        }
                        result = false
                        if (config.debugLog) {
                            Log.i("SplashGuard", "Blocked advertising motion sensor: " +
                                "type=${sensor.type}, listener=$listenerClass")
                        }
                    }
                }
            }
    }
}
