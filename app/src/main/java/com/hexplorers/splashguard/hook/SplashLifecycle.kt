package com.hexplorers.splashguard.hook

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hexplorers.splashguard.SkipSettings
import java.util.Collections
import java.util.WeakHashMap

/**
 * One callback instance per process; scan activity View trees in the first
 * seconds after a foreground launch only, not arbitrary background screens.
 */
internal class SplashLifecycle private constructor(
    private val config: SkipSettings
) : Application.ActivityLifecycleCallbacks {

    private val handler = Handler(Looper.getMainLooper())
    private val pending = WeakHashMap<Activity, MutableList<Runnable>>()
    private val clicked = Collections.newSetFromMap(WeakHashMap<Activity, Boolean>())
    private var foregroundActivities = 0
    private var foregroundStart = 0L

    override fun onActivityStarted(activity: Activity) {
        if (foregroundActivities++ == 0) foregroundStart = SystemClock.elapsedRealtime()
    }

    override fun onActivityResumed(activity: Activity) {
        if (SkipMatcher.isSensitiveActivity(activity.javaClass.name)) return
        if (clicked.contains(activity)) return
        val now = SystemClock.elapsedRealtime()
        if (foregroundStart <= 0 || now - foregroundStart > config.windowSeconds * 1000L) return

        cancel(activity)
        val slots = mutableListOf<Runnable>()
        for (delay in longArrayOf(0L, 250L, 600L, 1100L, 1850L, 2750L)) {
            val task = Runnable {
                if (activity.isFinishing || activity.isDestroyed || clicked.contains(activity)) return@Runnable
                if (SystemClock.elapsedRealtime() - foregroundStart > config.windowSeconds * 1000L) return@Runnable
                val decor = activity.window?.decorView ?: return@Runnable
                val candidate = findCandidate(decor, activity)
                if (candidate != null && safelyClick(candidate)) {
                    clicked.add(activity)
                    cancel(activity)
                }
            }
            slots.add(task)
            handler.postDelayed(task, delay)
        }
        pending[activity] = slots
    }

    override fun onActivityPaused(activity: Activity) { cancel(activity) }
    override fun onActivityStopped(activity: Activity) {
        foregroundActivities = (foregroundActivities - 1).coerceAtLeast(0)
        cancel(activity)
    }
    override fun onActivityDestroyed(activity: Activity) {
        cancel(activity)
        clicked.remove(activity)
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private fun cancel(activity: Activity) {
        pending.remove(activity)?.forEach(handler::removeCallbacks)
    }

    private fun findCandidate(root: View, activity: Activity): View? {
        val decorWidth = root.resources.displayMetrics.widthPixels
        val decorHeight = root.resources.displayMetrics.heightPixels
        val activityHint = SkipMatcher.isSplashActivity(activity.javaClass.name)
        val queue = ArrayDeque<Pair<View, Int>>()
        queue.add(root to 0)
        var visited = 0
        while (queue.isNotEmpty() && visited++ < 250) {
            val (view, depth) = queue.removeFirst()
            if (!view.isShown || view.alpha < 0.4f) continue
            val text = (view as? TextView)?.text?.toString().orEmpty()
            val label = view.contentDescription?.toString().orEmpty()
            val matchText = listOf(text, label).firstOrNull(SkipMatcher::isPotentialAdSkip)
            if (matchText != null && view.isEnabled) {
                val bounds = Rect()
                if (view.getGlobalVisibleRect(bounds) && bounds.width() > 0 && bounds.height() > 0) {
                    val rightHalf = bounds.centerX() >= (decorWidth * 0.55f)
                    val upperArea = bounds.centerY() <= (decorHeight * 0.42f)
                    if (upperArea && (!config.topRight || rightHalf)) {
                        val idName = if (view.id != View.NO_ID) {
                            runCatching { view.resources.getResourceEntryName(view.id) }.getOrNull().orEmpty()
                        } else ""
                        val hint = activityHint || AdSdkProfiles.matches(idName, label) ||
                            SkipMatcher.isAdCountDown(matchText)
                        if (!config.strict || hint) return view
                    }
                }
            }
            if (view is ViewGroup && depth < 16) {
                for (i in 0 until view.childCount) queue.add(view.getChildAt(i) to depth + 1)
            }
        }
        return null
    }

    private fun safelyClick(view: View): Boolean {
        var target: View? = view
        repeat(3) {
            val current = target ?: return false
            if (current.isClickable && current.isEnabled && current.isShown) {
                return runCatching { current.performClick() }.getOrDefault(false)
            }
            target = current.parent as? View
        }
        return false
    }

    companion object {
        private val registered = Collections.newSetFromMap(WeakHashMap<Application, Boolean>())
        fun attach(app: Application, settings: SkipSettings) {
            synchronized(registered) {
                if (!registered.add(app)) return
            }
            app.registerActivityLifecycleCallbacks(SplashLifecycle(settings))
        }
    }
}
