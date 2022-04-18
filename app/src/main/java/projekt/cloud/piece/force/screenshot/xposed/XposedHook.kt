package projekt.cloud.piece.force.screenshot.xposed

import android.app.Activity
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedHook: IXposedHookLoadPackage {

    companion object {
        private const val TAG = "ForceScreenshotHook"

        private const val WINDOW_ADD_FLAGS = "addFlags"
        private const val WINDOW_SET_FLAGS = "setFlags"
        private const val SURFACE_VIEW_SET_SECURE = "setSecure"
        private const val ACTIVITY_SET_CONTENT_VIEW = "setContentView"
    }

    private open class WindowMethodHook: XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            param?.args?.let {
                var flags = it[0] as Int
                if (flags and FLAG_SECURE == FLAG_SECURE) {
                    flags = flags and FLAG_SECURE.inv()
                }
                it[0] = flags
            }
        }
        /**
         * Make sure [FLAG_SECURE] is disabled
         **/
        override fun afterHookedMethod(param: MethodHookParam?) {
            (param?.thisObject as Window?)?.clearFlags(FLAG_SECURE)
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpparam?.packageName
        hookWindow(packageName)
        hookSurfaceView(packageName)
        hookActivity(packageName)
    }

    /**
     * Hook class [Window]
     **/
    private fun hookWindow(packageName: String?) {
        /**
         * Hook [Window.addFlags]
         **/
        findAndHookMethod(Window::class.java, WINDOW_ADD_FLAGS, Int::class.java, object: WindowMethodHook() {
            /**
             * Call before [Window.addFlags], clear [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Window.$WINDOW_ADD_FLAGS(Int)")
                super.beforeHookedMethod(param)
            }
        })

        /**
         * Hook [Window.setFlags]
         **/
        findAndHookMethod(Window::class.java, WINDOW_SET_FLAGS, Int::class.java, Int::class.java, object : WindowMethodHook() {
            /**
             * Call before [Window.setFlags], clear [FLAG_SECURE]
             **/
            override fun beforeHookedMethod(param: MethodHookParam?) {
                XposedBridge.log("${TAG}: [$packageName] Hook Window.$WINDOW_SET_FLAGS(Int, Int)")
                super.beforeHookedMethod(param)
                param?.args?.let {
                    it[1] = it[0]
                }
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
                param?.args?.let { it[0] = false }
            }
            /**
             * Call after [SurfaceView.setSecure],
             * make sure [SurfaceView.setSecure] is disabled
             **/
            override fun afterHookedMethod(param: MethodHookParam?) {
                (param?.thisObject as SurfaceView?)?.setSecure(false)
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

}