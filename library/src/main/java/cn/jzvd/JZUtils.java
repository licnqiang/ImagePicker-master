package cn.jzvd;

import ohos.aafwk.ability.Ability;
import ohos.agp.components.Component;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.StateElement;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.NotExistException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.NetCapabilities;
import ohos.net.NetManager;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import static ohos.agp.utils.LayoutAlignment.CENTER;

/**
 * Created by Nathen
 * On 2016/02/21 12:25
 */
public class JZUtils {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "JZUtils");

    public static final String TAG = "JZVD";
    public static int SYSTEM_UI = 0;
    private static final int ONE_SECONDS_MS = 1000;
    private static final int ONE_MINS_MINUTES = 60;
    private static final int NUMBER = 16;
    private static final String TIME_FORMAT = "%02d";
    private static final String SEMICOLON = ":";

    public static String stringForTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * This method requires the caller to hold the permission ACCESS_NETWORK_STATE.
     *
     * @param context context
     * @return if wifi is connected,return true
     */
    public static boolean isWifiConnected(Context context) {
//        // 获取网络管理对象
//        NetManager netManager = NetManager.getInstance(context);
//        // 获取NetCapabilities对象
//        NetCapabilities netCapabilities = netManager.getNetCapabilities(netManager.getDefaultNet());
//        // NetCapabilities.NET_CAPABILITY_VALIDATED表示连接了网络，并且可以上网
        return true;
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Ability scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Ability) {
            return (Ability) context;
        } else if (context instanceof AbilityContext) {
            return scanForActivity(((AbilityContext) context).getContext());
        }

        return null;
    }

    public static void setRequestedOrientation(Context context, AbilityInfo.DisplayOrientation orientation) {
        if (JZUtils.scanForActivity(context) != null) {
            JZUtils.scanForActivity(context).setDisplayOrientation(
                    orientation);
        } else {
            JZUtils.scanForActivity(context).setDisplayOrientation(
                    orientation);
        }
    }

    public static Window getWindow(Context context) {
        if (JZUtils.scanForActivity(context) != null) {
            return JZUtils.scanForActivity(context).getWindow();
        } else {
            return JZUtils.scanForActivity(context).getWindow();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResourceManager().getDeviceCapability().screenDensity / DeviceCapability.SCREEN_MDPI;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void saveProgress(Context context, Object url, long progress) {
        HiLog.info(label, "saveProgress: " + progress);
        if (progress < 5000) {
            progress = 0;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Preferences preferences = databaseHelper.getPreferences("JZVD_PROGRESS");
        String key;
        if (url.toString().length() > 50) {
            key=url.toString().substring(0, 50);
        }else {
            key=url.toString();
        }
        preferences.putLong("newVersion:" + key, progress).flush();
    }

    public static long getSavedProgress(Context context, Object url) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Preferences preferences = databaseHelper.getPreferences("JZVD_PROGRESS");
        String key;
        if (url.toString().length() > 50) {
            key=url.toString().substring(0, 50);
        }else {
            key=url.toString();
        }
        return preferences.getLong("newVersion:" + key, 0);
    }

    /**
     * if url == null, clear all progress
     *
     * @param context context
     * @param url     if url!=null clear this url progress
     */
    public static void clearSavedProgress(Context context, Object url) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Preferences preferences = databaseHelper.getPreferences("JZVD_PROGRESS");
        if (url == null) {
            preferences.clear().flush();
        } else {
            String key;
            if (url.toString().length() > 50) {
                key=url.toString().substring(0, 50);
            }else {
                key=url.toString();
            }
            preferences.putLong("newVersion:" + key, 0).flush();
        }
    }

    public static void showStatusBar(Context context) {
        if (Jzvd.TOOL_BAR_EXIST) {
            JZUtils.getWindow(context).clearFlags(WindowManager.LayoutConfig.MARK_FULL_SCREEN);
        }
    }

    //如果是沉浸式的，全屏前就没有状态栏
    public static void hideStatusBar(Context context) {
        if (Jzvd.TOOL_BAR_EXIST) {
            JZUtils.getWindow(context).addFlags(WindowManager.LayoutConfig.MARK_FULL_SCREEN);
        }
    }

    public static void hideSystemUI(Context context) {
//        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        ;
//        if (os.Build.VERSION.SDK_INT >= os.Build.VERSION_CODES.KITKAT) {
//            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        }
//        SYSTEM_UI = JZUtils.getWindow(context).getDecorView().getSystemUiVisibility();
//        JZUtils.getWindow(context).getDecorView().setSystemUiVisibility(uiOptions);
    }

    public static void showSystemUI(Context context) {
//        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//        JZUtils.getWindow(context).getDecorView().setSystemUiVisibility(SYSTEM_UI);
    }

    //获取状态栏的高度
//    public static int getStatusBarHeight(Context context) {
//        ResourceManager resources = context.getResourceManager();
//        int resourceId = resources.getIdentifier("status_bar_height", "dimen");
//        int height = resources.getDimensionPixelSize(resourceId);
//        return height;
//    }

    //获取NavigationBar的高度
//    public static int getNavigationBarHeight(Context context) {
//        boolean var1 = ScrollHelper.get(context).hasPermanentMenuKey();
//        int var2;
//        return (var2 = context.getResourceManager().getIdentifier("navigation_bar_height", "dimen", )) > 0 && !var1 ? context.getResources().getDimensionPixelSize(var2) : 0;
//    }

    //获取屏幕的宽度
    public static int getScreenWidth(Context context) {
        DeviceCapability dm = context.getResourceManager().getDeviceCapability();
        return dm.width;
    }

    //获取屏幕的高度
    public static int getScreenHeight(Context context) {
        DeviceCapability dm = context.getResourceManager().getDeviceCapability();
        return dm.height;
    }

    public static CommonDialog createDialogWithView(Context context, Component localView) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setContentCustomComponent(localView);
        Window window = dialog.getWindow();
        window.setWindowLayout(-2, -2);
        WindowManager.LayoutConfig localLayoutParams = window.getLayoutConfig().get();
        localLayoutParams.alignment = CENTER;
        window.setLayoutConfig(localLayoutParams);
        return dialog;
    }


    public static StateElement getStateElement(Context context, int resourceId1, int resourceId2, int state1, int state2) {
        PixelMapElement element = null;
        PixelMapElement element1 = null;
        try {
            element = new PixelMapElement(context.getResourceManager().getResource(resourceId1));
            element1 = new PixelMapElement(context.getResourceManager().getResource(resourceId2));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }

        StateElement stateElement = new StateElement();
        stateElement.addState(new int[]{state1}, element);
        stateElement.addState(new int[]{state2}, element1);
        return stateElement;
    }
}
