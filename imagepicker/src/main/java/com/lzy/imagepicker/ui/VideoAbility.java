package com.lzy.imagepicker.ui;

import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.lzy.imagepicker.ResourceTable;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.sensor.agent.CategoryMotionAgent;
import ohos.sensor.bean.CategoryMotion;

public class VideoAbility extends Ability {

    private JzvdStd mJzvdStd;
    private Jzvd.JZAutoFullscreenListener mSensorEventListener;
    private CategoryMotion mSensorManager;
    private CategoryMotionAgent categoryMotionAgent;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_video_slice);

        mJzvdStd = (JzvdStd) findComponentById(ResourceTable.Id_jz_video);

        categoryMotionAgent = new CategoryMotionAgent();
        mSensorManager = categoryMotionAgent.getSingleSensor(CategoryMotion.SENSOR_TYPE_ACCELEROMETER);
        mSensorEventListener = new Jzvd.JZAutoFullscreenListener();

        mJzvdStd .setUp(intent.getStringParam("video"), "", Jzvd.SCREEN_NORMAL);

        Jzvd.PROGRESS_DRAG_RATE = 2f;//设置播放进度条手势滑动阻尼系数
    }

    @Override
    protected void onActive() {
        super.onActive();
        categoryMotionAgent.setSensorDataCallback(mSensorEventListener
                , mSensorManager
                , CategoryMotionAgent.SENSOR_SAMPLING_RATE_UI);
        Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        categoryMotionAgent.releaseSensorDataCallback(
                mSensorEventListener, mSensorManager);
        JZUtils.clearSavedProgress(getContext(), null);
        //home back
        Jzvd.goOnPlayOnPause();
    }
}
