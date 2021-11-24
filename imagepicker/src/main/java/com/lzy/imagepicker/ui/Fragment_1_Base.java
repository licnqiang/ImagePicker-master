package com.lzy.imagepicker.ui;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.lzy.imagepicker.ResourceTable;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sensor.agent.CategoryMotionAgent;
import ohos.sensor.bean.CategoryMotion;

/**
 * Created by pengan.li on 2020/5/8.
 * 展示饺子的一些自定义用法，修改jzvdStd暴露的函数和变量，不需要继承jzvdStd的用法
 */
public class Fragment_1_Base extends Fraction {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "Jzvd");

    private JzvdStd mJzvdStd;

    private Jzvd.JZAutoFullscreenListener mSensorEventListener;
    private CategoryMotion mSensorManager;
    private Component view;
    private CategoryMotionAgent categoryMotionAgent;

    private ComponentContainer mRootView;

    @Override
    protected Component onComponentAttached(LayoutScatter scatter, ComponentContainer container, Intent intent) {
        view = scatter.parse(ResourceTable.Layout_fragment_base, container, false);
        mJzvdStd = (JzvdStd) view.findComponentById(ResourceTable.Id_jz_video);

        categoryMotionAgent = new CategoryMotionAgent();
        mSensorManager = categoryMotionAgent.getSingleSensor(CategoryMotion.SENSOR_TYPE_ACCELEROMETER);
        mSensorEventListener = new Jzvd.JZAutoFullscreenListener();

        mJzvdStd .setUp("", "", Jzvd.SCREEN_NORMAL);

        Jzvd.PROGRESS_DRAG_RATE = 2f;//设置播放进度条手势滑动阻尼系数
        return view;
    }

    public void setDecorView(ComponentContainer rootView) {
        mRootView = rootView;
    }

    @Override
    protected void onActive() {
        super.onActive();
//        categoryMotionAgent.setSensorDataCallback(mSensorEventListener
//                , mSensorManager
//                , CategoryMotionAgent.SENSOR_SAMPLING_RATE_UI);
//        Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
//        categoryMotionAgent.releaseSensorDataCallback(
//                mSensorEventListener, mSensorManager);
//        JZUtils.clearSavedProgress(getContext(), null);
//        //home back
//        Jzvd.goOnPlayOnPause();
    }
}
