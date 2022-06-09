package projekt.cloud.piece.force.screenshot.xposed

import android.app.Activity
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedHook: IXposedHookLoadPackage {

    companion object {
        private const val TAG = "ForceScreenshotHook"

        private const val ANDROID = "android"

        private const val WINDOW_ADD_FLAGS = "addFlags"
        private const val WINDOW_SET_FLAGS = "setFlags"
        private const val SURFACE_VIEW_SET_SECURE = "setSecure"
        private const val ACTIVITY_SET_CONTENT_VIEW = "setContentView"

        private const val IS_SECURE_LOCKED = "isSecureLocked"
        private const val WINDOW_STATE = "com.android.server.wm.WindowState"
        private const val WINDOW_MANAGER_SERVICE = "com.android.server.wm.WindowManagerService"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        when (val packageName = lpparam?.packageName) {
            ANDROID -> hookWindowState(lpparam)
            else -> {
                if (lpparam?.appInfo?.flags?.run { isSystemApplication(this) } != true) {
                    hookWindow(packageName)
                    hookSurfaceView(packageName)
                    hookActivity(packageName)
                }
            }
        }
    }

    /**
     * Check whether application is system application
     **/
    private fun isSystemApplication(flags: Int) =
        flags and FLAG_SYSTEM == 1 || flags and FLAG_UPDATED_SYSTEM_APP == 1

    /**
     * Hook class [Window]
     **/
    private fun hookWindow(packageName: String?) {
        /**
         * Hook [Window.addFlags]
         **/
        findAndHookMethod(Window::class.java, WINDOW_ADD_FLAGS, Int::class.java, object: XC_MethodHook() {
            /**
             * Call before [Window.addFlags], clear [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Window.$WINDOW_ADD_FLAGS(Int)")
                param?.args?.let {
                    val flags = it[0] as Int
                    if (flags == FLAG_SECURE) {
                        return param.setResult(null)
                    }
                    if (flags and FLAG_SECURE != 0) {
                        it[0] = flags and FLAG_SECURE.inv()
                    }
                }
            }
            /**
             * Make sure [FLAG_SECURE] is disabled
             **/
            override fun afterHookedMethod(param: MethodHookParam?) {
                (param?.thisObject as? Window)?.clearFlags(FLAG_SECURE)
            }
        })

        /**
         * Hook [Window.setFlags]
         **/
        findAndHookMethod(Window::class.java, WINDOW_SET_FLAGS, Int::class.java, Int::class.java, object : XC_MethodHook() {
            /**
             * Call before [Window.setFlags], clear [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Window.$WINDOW_SET_FLAGS(Int)")
                param?.args?.let {
                    var flags = it[1] as Int
                    if (flags == FLAG_SECURE) {
                        return param.setResult(null)
                    }
                    if (flags and FLAG_SECURE != 0) {
                        flags = flags and FLAG_SECURE.inv()
                        it[1] = flags
                        it[0] = flags
                    }
                }
            }
            /**
             * Make sure [FLAG_SECURE] is disabled
             **/
            override fun afterHookedMethod(param: MethodHookParam?) {
                (param?.thisObject as? Window)?.clearFlags(FLAG_SECURE)
            }
        })
    }

    /**
     * Hook class [SurfaceView]
     **/
    private fun hookSurfaceView(packageName: String?) {
        /**
         * Hook [SurfaceView.setSecure]
         **/
        findAndHookMethod(SurfaceView::class.java, SURFACE_VIEW_SET_SECURE, Boolean::class.java, object : XC_MethodHook() {
            /**
             * Call before [SurfaceView.setSecure], modify argument [Boolean] into false
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook SurfaceView.$SURFACE_VIEW_SET_SECURE(Boolean)")
                param?.args?.let {
                    if (it[0] as? Boolean == true) {
                        param.result = null
                    }
                }
            }
            /**
             * Call after [SurfaceView.setSecure],
             * make sure [SurfaceView.setSecure] is disabled
             **/
            override fun afterHookedMethod(param: MethodHookParam?) {
                if (param?.args?.get(0) as? Boolean == true) {
                    (param.thisObject as? SurfaceView)?.setSecure(false)
                }
            }
        })
    }

    /**
     * Hook class [Activity]
     **/
    private fun hookActivity(packageName: String?) {
        /**
         * Hook [Activity.setContentView]
         **/
        findAndHookMethod(Activity::class.java, ACTIVITY_SET_CONTENT_VIEW, Int::class.java, object : XC_MethodHook() {
            /**
             * Clear flag [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Activity.$ACTIVITY_SET_CONTENT_VIEW(Int)")
                (param?.thisObject as Activity?)?.window?.clearFlags(FLAG_SECURE)
            }
        })
        /**
         * Hook [Activity.setContentView]
         **/
        findAndHookMethod(Activity::class.java, ACTIVITY_SET_CONTENT_VIEW, View::class.java, object : XC_MethodHook() {
            /**
             * Clear flag [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Activity.$ACTIVITY_SET_CONTENT_VIEW(View)")
                (param?.thisObject as Activity?)?.window?.clearFlags(FLAG_SECURE)
            }
        })
        /**
         * Hook [Activity.setContentView]
         **/
        findAndHookMethod(Activity::class.java, ACTIVITY_SET_CONTENT_VIEW, View::class.java, ViewGroup.LayoutParams::class.java, object : XC_MethodHook() {
            /**
             * Clear flag [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Activity.$ACTIVITY_SET_CONTENT_VIEW(View, ViewGroup.LayoutParams)")
                (param?.thisObject as Activity?)?.window?.clearFlags(FLAG_SECURE)
            }
        })
    }

    /**
     * Hook window state
     **/
    private fun hookWindowState(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val windowStateClazz = findClass(WINDOW_STATE, lpparam?.classLoader)
        try {
            findAndHookMethod(windowStateClazz, IS_SECURE_LOCKED, returnConstant(false))
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
        try {
            findAndHookMethod(WINDOW_MANAGER_SERVICE, lpparam?.classLoader, IS_SECURE_LOCKED, windowStateClazz, returnConstant(false))
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }

}