package com.luckyzyx.luckytool.service.base

import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import com.luckyzyx.luckytool.utils.LogUtils
import com.luckyzyx.luckytool.utils.bindRootService
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
@Suppress("PropertyName")
abstract class BaseControllerService<T : IInterface> {
    abstract val TAG: String
    abstract var controllerService: Class<*>
    open var controller: T? = null

    open fun init(context: Context) {
        get(context) {}
    }

    abstract fun getController(iBinder: IBinder?): T?

    open fun get(context: Context?, result: (T?) -> Unit) {
        if (context == null || controller != null) result(controller)
        else context.bindRootService(controllerService, { _: ComponentName?, iBinder: IBinder? ->
            controller = getController(iBinder)

            val uid = Binder.getCallingUid()

            LogUtils.d(
                TAG, "get ($uid)", "${controller != null}", true
            )
            result(controller)
        })
    }
}