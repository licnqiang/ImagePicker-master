package com.lzy.imagepicker.util;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.data.usage.DataUsage;
import ohos.data.usage.MountState;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class Utils {

    /**
//     * 获得状态栏的高度
//     */
//    public static int getStatusHeight(Context context) {
//        int statusHeight = -1;
//        try {
//            Class<?> clazz = Class.forName("com.ohos.internal.R$dimen");
//            Object object = clazz.newInstance();
//            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
//            statusHeight = context.getResources().getDimensionPixelSize(height);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return statusHeight;
//    }

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列，并获取Item宽度
     * @param ability 上下文对象
     * @return 图片宽度
     */
    public static int getImageItemWidth(Ability ability) {

        DisplayManager displayManager = DisplayManager.getInstance();
        Display display = displayManager.getDefaultDisplay(ability).get();

        int screenWidth = display.getAttributes().width;
        int densityDpi = display.getAttributes().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * display.getAttributes().densityPixels);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    /**
     * 判断SDCard是否可用
     * @return 是否有sd卡
     */
    public static boolean existSDCard() {
        return DataUsage.getDiskMountedStatus().equals(MountState.DISK_MOUNTED);
    }

//    /**
//     * 获取手机大小（分辨率）
//     */
//    public static DisplayMetrics getScreenPix(Activity ability) {
//        DisplayMetrics displaysMetrics = new DisplayMetrics();
//        ability.getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
//        return displaysMetrics;
//    }

    /**
     * vp转px
     */
//    public static int vp2px(Context context, float vpVal) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, vpVal, context.getResources().getDisplayMetrics());
//    }

    /**
     * 判断手机是否含有虚拟按键  99%
     */
//    public static boolean hasVirtualNavigationBar(Context context) {
//        boolean hasSoftwareKeys = true;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//
//            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
//            d.getRealMetrics(realDisplayMetrics);
//
//            int realHeight = realDisplayMetrics.heightPixels;
//            int realWidth = realDisplayMetrics.widthPixels;
//
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            d.getMetrics(displayMetrics);
//
//            int displayHeight = displayMetrics.heightPixels;
//            int displayWidth = displayMetrics.widthPixels;
//
//            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
//            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
//        }
//
//        return hasSoftwareKeys;
//    }

    /**
     * 获取导航栏高度，有些没有虚拟导航栏的手机也能获取到，建议先判断是否有虚拟按键
     * @param mContext 上下文对象
     * @param vp 需要转换的vp值
     * @return 转换完成的vp
     */
    public static int vp2px(int vp, Context mContext) {

        return (int)(mContext.getResourceManager().getDeviceCapability().screenDensity / 160 * vp);
    }


    public static int[] getScreenSize(Context context) {
        DisplayManager displayManager = DisplayManager.getInstance();
        Display display = displayManager.getDefaultDisplay(context).get();

        int screenWidth = display.getAttributes().width;
        int screenHeith = display.getAttributes().height;
        return new int[]{screenWidth, screenHeith};
    }
}
