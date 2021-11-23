package cn.jzvd;


import es.dmoral.toasty.Toasty;
import ohos.aafwk.content.Intent;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.PopupDialog;
import ohos.app.Context;
import ohos.event.commonevent.*;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;
import ohos.rpc.RemoteException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.utils.LayoutAlignment.END;

/**
 * Created by Nathen
 * On 2016/04/18 16:15
 */
public class JzvdStd extends Jzvd {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "JzvdStd");

    private FullScreenListener fullScreenListener;

    public static long LAST_GET_BATTERYLEVEL_TIME = 0;
    public static int LAST_GET_BATTERYLEVEL_PERCENT = 70;
    protected  Timer DISMISS_CONTROL_VIEW_TIMER;
    private MYListeners listeners = new MYListeners();
    public Image backButton;
    public ProgressBar bottomProgressBar;
    public RoundProgressBar loadingProgressBar;
    public Text titleTextView;
    public Image posterImageView;
    public Image tinyBackImageView;
    public DirectionalLayout batteryTimeLayout;
    public Image batteryLevel;
    public Text videoCurrentTime;
    public Text replayTextView;
    public Text clarity;
    public PopupDialog clarityPopWindow;
    public Text mRetryBtn;
    public DirectionalLayout mRetryLayout;
    private CommonEventSubscriber commonEventSubscriber;
    private AnimatorValue value;

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;
    protected CommonDialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected Text mDialogSeekTime;
    protected Text mDialogTotalTime;
    protected Image mDialogIcon;
    protected CommonDialog mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;
    protected Text mDialogVolumeTextView;
    protected Image mDialogVolumeImageView;
    protected CommonDialog mBrightnessDialog;
    protected ProgressBar mDialogBrightnessProgressBar;
    protected Text mDialogBrightnessTextView;
    protected boolean mIsWifi;

    protected ArrayDeque<Runnable> delayTask = new ArrayDeque<>();


    public JzvdStd(Context context) {
        super(context);
    }

    public JzvdStd(Context context, AttrSet attrs) {
        super(context, attrs);
    }

    @Override
    public void clickFullScreen(int id) {
        if (fullScreenListener != null) {
            fullScreenListener.onFullScreen(id);
        }
    }

    public void setFullScreenListener(FullScreenListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

    @Override
    public void getView(Component component) {
        initView(component, getContext());
    }

    public void initView(Component component, Context context) {
        batteryTimeLayout = (DirectionalLayout) component.findComponentById(ResourceTable.Id_battery_time_layout);
        bottomProgressBar = (ProgressBar) component.findComponentById(ResourceTable.Id_bottom_progress);
        titleTextView = (Text) component.findComponentById(ResourceTable.Id_title);
        backButton = (Image) component.findComponentById(ResourceTable.Id_back);
        posterImageView = (Image) component.findComponentById(ResourceTable.Id_poster);
        loadingProgressBar = (RoundProgressBar) component.findComponentById(ResourceTable.Id_loading);
        tinyBackImageView = (Image) component.findComponentById(ResourceTable.Id_back_tiny);
        batteryLevel = (Image) component.findComponentById(ResourceTable.Id_battery_level);
        videoCurrentTime = (Text) component.findComponentById(ResourceTable.Id_video_current_time);
        replayTextView = (Text) component.findComponentById(ResourceTable.Id_replay_text);
        clarity = (Text) component.findComponentById(ResourceTable.Id_clarity);
        mRetryBtn = (Text) component.findComponentById(ResourceTable.Id_retry_btn);
        mRetryLayout = (DirectionalLayout) component.findComponentById(ResourceTable.Id_retry_layout);

        progressBar.setTouchEventListener(this);
        progressBar.setValueChangedListener(this);
        posterImageView.setClickedListener(listeners);
        backButton.setClickedListener(listeners);
        tinyBackImageView.setClickedListener(this);
        clarity.setClickedListener(listeners);
        mRetryBtn.setClickedListener(new ClickedListener() {
            @Override
            public void onClick(Component component) {
                clickRetryBtn();
            }
        });
        textureViewContainer.setTouchEventListener(this);
        value = new AnimatorValue();
        value.setLoopedCount(-1);
        value.setDuration(2000);
        value.setValueUpdateListener((animatorValue, v) -> loadingProgressBar.setRotation(360 * v));
    }


    private void subscribeWifiChange() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_WIFI_CONN_STATE);
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscriber = new CommonEventSubscriber(subscribeInfo) {
            @Override
            public void onReceiveEvent(CommonEventData commonEventData) {
                boolean isWifi = JZUtils.isWifiConnected(jzvdContext);
                if (mIsWifi == isWifi) return;
                mIsWifi = isWifi;
                if (!mIsWifi && !WIFI_TIP_DIALOG_SHOWED && state == STATE_PLAYING) {
                    startButton.callOnClick();
                    showWifiDialog();
                }
            }
        };
        try {
            CommonEventManager.subscribeCommonEvent(commonEventSubscriber);
        } catch (RemoteException exception) {
            HiLog.error(label, "%{public}s", "Subscribe error!");
        }
    }

    private void unsubscribeWifiChange() {
        if (commonEventSubscriber == null) {
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(commonEventSubscriber);
        } catch (RemoteException exception) {
            HiLog.error(label, "%{public}s", "unsubscribe error!");
        }
    }

    private void subscribeBatteryChange() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_BATTERY_CHANGED);
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscriber = new CommonEventSubscriber(subscribeInfo) {
            @Override
            public void onReceiveEvent(CommonEventData commonEventData) {
                Intent intent = commonEventData.getIntent();
                int level = intent.getIntParam("level", 0);
                int scale = intent.getIntParam("scale", 100);
                int percent = level * 100 / scale;
                LAST_GET_BATTERYLEVEL_PERCENT = percent;
                setBatteryLevel();
                try {
                    unsubscribeBatteryChange();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        try {
            CommonEventManager.subscribeCommonEvent(commonEventSubscriber);
        } catch (RemoteException exception) {
            HiLog.error(label, "%{public}s", "Subscribe error!");
        }
    }

    private void unsubscribeBatteryChange() {
        if (commonEventSubscriber == null) {
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(commonEventSubscriber);
        } catch (RemoteException exception) {
            HiLog.error(label, "%{public}s", "unsubscribe error!");
        }
    }

    public void setUp(JZDataSource jzDataSource, int screen, JZMediaSystem mediaInterfaceClass) {
        if ((System.currentTimeMillis() - gobakFullscreenTime) < 200) {
            return;
        }

        if ((System.currentTimeMillis() - gotoFullscreenTime) < 200) {
            return;
        }

        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        if (titleTextView != null) {
            titleTextView.setText(jzDataSource.title);
        }
        setScreen(screen);
    }

    @Override
    public void changeUrl(JZDataSource jzDataSource, long seekToInAdvance) {
        super.changeUrl(jzDataSource, seekToInAdvance);
        if (titleTextView != null) {
            titleTextView.setText(jzDataSource.title);
        }
    }

    public void changeStartButtonSize(int size) {
        ComponentContainer.LayoutConfig lp = startButton.getLayoutConfig();
        lp.height = size;
        lp.width = size;
        lp = loadingProgressBar.getLayoutConfig();
        lp.height = size;
        lp.width = size;
    }

    @Override
    public int getLayoutId() {
        return ResourceTable.Layout_jz_layout_std;
    }

    @Override
    public void onStateNormal() {
        super.onStateNormal();
        changeUiToNormal();
    }

    @Override
    public void onStatePreparing() {
        super.onStatePreparing();
        changeUiToPreparing();
    }

    public void onStatePreparingPlaying() {
        super.onStatePreparingPlaying();
        changeUIToPreparingPlaying();
    }

    public void onStatePreparingChangeUrl() {
        super.onStatePreparingChangeUrl();
        changeUIToPreparingChangeUrl();
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        changeUiToPlayingClear();
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        changeUiToPauseShow();
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStateError() {
        super.onStateError();
//        changeUiToError();
//        mRetryBtn.callOnClick();
        startVideo();
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        changeUiToComplete();
        cancelDismissControlViewTimer();
        bottomProgressBar.setProgressValue(100);
    }

    @Override
    public void startVideo() {
        super.startVideo();
        registerWifiListener(getApplicationContext());
    }

    @Override
    public boolean onTouchEvent(Component v, TouchEvent event) {
        int id = v.getId();
        if (id == ResourceTable.Id_bottom_seek_progress) {
            switch (event.getAction()) {
                case TouchEvent.PRIMARY_POINT_DOWN:
                    cancelDismissControlViewTimer();
                    break;
                case TouchEvent.PRIMARY_POINT_UP:
                    startDismissControlViewTimer();
                    break;
            }
        }
        return super.onTouchEvent(v, event);
    }

    @Override
    public void onTouch(TouchEvent event, int flag) {
        switch (event.getAction()) {
            case TouchEvent.PRIMARY_POINT_DOWN:
            case TouchEvent.HOVER_POINTER_MOVE:
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                startDismissControlViewTimer();
                if (mChangePosition) {
                    long duration = getDuration();
                    int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
                    bottomProgressBar.setProgressValue(progress);
                }
                break;
        }
        if (flag == 1) {
            if (!mChangePosition && !mChangeVolume) {
                onClickUiToggle();
            }
        } else if (flag == 2) {
            if (state == STATE_PLAYING || state == STATE_PAUSE) {
                startButton.callOnClick();
            }
        }
    }


    private class MYListeners implements Component.ClickedListener {
        @Override
        public void onClick(Component v) {
            int i = v.getId();
            if (i == ResourceTable.Id_poster) {
                clickPoster();
            } else if (i == ResourceTable.Id_back) {
                clickBack();
            } else if (i == ResourceTable.Id_back_tiny) {
                clickBackTiny();
            } else if (i == ResourceTable.Id_clarity) {
                clickClarity();
            } else if (i == ResourceTable.Id_retry_btn) {

            }
        }
    }

    @Override
    public void click(int i) {
        clickSurfaceContainer();
        if (clarityPopWindow != null) {
            clarityPopWindow.hide();
        }
    }


//    @Override
//    public void onClick(Component v) {
//        int i = v.getId();
//        if (i == ResourceTable.Id_poster) {
//            HiLog.info(label,"909090");
//            clickPoster();
//        } else if (i == ResourceTable.Id_surface_container) {
//            clickSurfaceContainer();
//            if (clarityPopWindow != null) {
//                clarityPopWindow.hide();
//            }
//        } else if (i == ResourceTable.Id_back) {
//            clickBack();
//        } else if (i == ResourceTable.Id_back_tiny) {
//            clickBackTiny();
//        } else if (i == ResourceTable.Id_clarity) {
//            clickClarity();
//        } else if (i == ResourceTable.Id_retry_btn) {
//            clickRetryBtn();
//        }
//    }

    protected void clickRetryBtn() {
        if (jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            try {
                Toasty.warning(jzvdContext, getResourceManager().getElement(ResourceTable.String_no_url).getString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            } catch (WrongTypeException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_DIALOG_SHOWED) {
            showWifiDialog();
            return;
        }
        seekToInAdvance = mCurrentPosition;
        startVideo();
    }

    protected void clickClarity() {
        onCLickUiToggleToClear();
        LayoutScatter inflater = LayoutScatter.getInstance(jzvdContext);
        final DirectionalLayout layout = (DirectionalLayout) inflater.parse(ResourceTable.Layout_jz_layout_clarity, null, false);

        Component.ClickedListener mQualityListener = v1 -> {
            int index = (int) v1.getTag();
//                this.seekToInAdvance = getCurrentPositionWhenPlaying();
            jzDataSource.currentUrlIndex = index;
//                onStatePreparingChangeUrl();
            changeUrl(jzDataSource, getCurrentPositionWhenPlaying());
            clarity.setText(jzDataSource.getCurrentKey().toString());
            for (int j = 0; j < layout.getChildCount(); j++) {//设置点击之后的颜色
                if (j == jzDataSource.currentUrlIndex) {
                    ((Text) layout.getComponentAt(j)).setTextColor(new Color(Color.getIntColor("#fff85959")));
                } else {
                    ((Text) layout.getComponentAt(j)).setTextColor(new Color(Color.getIntColor("#ffffff")));
                }
            }
            if (clarityPopWindow != null) {
                clarityPopWindow.hide();
            }
        };

        for (int j = 0; j < jzDataSource.urlsMap.size(); j++) {
            String key = jzDataSource.getKeyFromDataSource(j);
            Text clarityItem = (Text) LayoutScatter.getInstance(jzvdContext).parse(ResourceTable.Layout_jz_layout_clarity_item, null, false);
            clarityItem.setText(key);
            clarityItem.setTag(j);
            layout.addComponent(clarityItem, j);
            clarityItem.setClickedListener(mQualityListener);
            if (j == jzDataSource.currentUrlIndex) {
                clarityItem.setTextColor(new Color(Color.getIntColor("#fff85959")));
            }
        }
        clarityPopWindow = new PopupDialog(jzvdContext, layout, JZUtils.dip2px(jzvdContext, 240), StackLayout.LayoutConfig.MATCH_PARENT);
        clarityPopWindow.setCustomComponent(layout);
//        clarityPopWindow.setAnimationStyle(R.style.pop_animation);
        clarityPopWindow.showOnCertainPosition(END, 0, 0);
//            int offsetX = clarity.getMeasuredWidth() / 3;
//            int offsetY = clarity.getMeasuredHeight() / 3;
//            clarityPopWindow.update(clarity, -offsetX, -offsetY, Math.round(layout.getMeasuredWidth() * 2), layout.getMeasuredHeight());
    }

    protected void clickBackTiny() {
        clearFloatScreen();
    }

    protected void clickBack() {
        backPress();
    }

    protected void clickSurfaceContainer() {
        startDismissControlViewTimer();
    }

    protected void clickPoster() {
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            try {
                Toasty.info(jzvdContext, getResourceManager().getElement(ResourceTable.String_no_url).getString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            } catch (WrongTypeException e) {
                e.printStackTrace();
            }
            return;
        }
        if (state == STATE_NORMAL) {
            if (!jzDataSource.getCurrentUrl().toString().startsWith("file") &&
                    !jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                    !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_DIALOG_SHOWED) {
                showWifiDialog();
                return;
            }
            startVideo();
        } else if (state == STATE_AUTO_COMPLETE) {
            onClickUiToggle();
        }
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
        try {
            fullscreenButton.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_enlarge)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
        backButton.setVisibility(Component.HIDE);
        tinyBackImageView.setVisibility(Component.INVISIBLE);
        changeStartButtonSize(45);
        batteryTimeLayout.setVisibility(Component.HIDE);
        clarity.setVisibility(Component.HIDE);
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        //进入全屏之后要保证原来的播放状态和ui状态不变，改变个别的ui
        try {
            fullscreenButton.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_shrink)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
        backButton.setVisibility(Component.VISIBLE);
        tinyBackImageView.setVisibility(Component.INVISIBLE);
        batteryTimeLayout.setVisibility(Component.VISIBLE);
        if (jzDataSource.urlsMap.size() == 1) {
            clarity.setVisibility(Component.HIDE);
        } else {
            clarity.setText(jzDataSource.getCurrentKey().toString());
            clarity.setVisibility(Component.VISIBLE);
        }
        changeStartButtonSize(62);
        setSystemTimeAndBattery();
    }

    @Override
    public void setScreenTiny() {
        super.setScreenTiny();
        tinyBackImageView.setVisibility(Component.VISIBLE);
        setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE,
                Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE);
        batteryTimeLayout.setVisibility(Component.HIDE);
        clarity.setVisibility(Component.HIDE);
    }

    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        CommonDialog dialog = new CommonDialog(getContext());
        Component container = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_jz_dialog_wifi, null, false);
        dialog.setContentCustomComponent(container);
        dialog.setSize(MATCH_CONTENT, MATCH_CONTENT);
        Button resume = (Button) container.findComponentById(ResourceTable.Id_resume);
        resume.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                dialog.hide();
                WIFI_TIP_DIALOG_SHOWED = true;
                if (state == STATE_PAUSE) {
                    startButton.callOnClick();
                } else {
                    startVideo();
                }
            }
        });
        Button cancel = (Button) container.findComponentById(ResourceTable.Id_cancel);
        cancel.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                dialog.hide();
                releaseAllVideos();
                clearFloatScreen();
            }
        });
        dialog.show();
    }

    @Override
    public void onTouchStart(Slider slider) {
        super.onTouchStart(slider);
        cancelDismissControlViewTimer();
    }

    @Override
    public void onTouchEnd(Slider seekBar) {
        super.onTouchEnd(seekBar);
        HiLog.info(label, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        startDismissControlViewTimer();
    }

    public void onClickUiToggle() {//这是事件

        if (bottomContainer.getVisibility() != Component.VISIBLE) {
            setSystemTimeAndBattery();
            clarity.setText(jzDataSource.getCurrentKey().toString());
        }
        if (state == STATE_PREPARING) {
            changeUiToPreparing();
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
            } else {
                setSystemTimeAndBattery();
            }
        } else if (state == STATE_PLAYING) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        } else if (state == STATE_PAUSE) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToPauseClear();
            } else {
                changeUiToPauseShow();
            }
        }
    }

    public void setSystemTimeAndBattery() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        videoCurrentTime.setText(dateFormater.format(date));
        if ((System.currentTimeMillis() - LAST_GET_BATTERYLEVEL_TIME) > 30000) {
            LAST_GET_BATTERYLEVEL_TIME = System.currentTimeMillis();
            subscribeBatteryChange();
        } else {
            setBatteryLevel();
        }
    }

    public void setBatteryLevel() {
        int percent = LAST_GET_BATTERYLEVEL_PERCENT;
        try {
            if (percent < 15) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_10)));
            } else if (percent >= 15 && percent < 40) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_30)));
            } else if (percent >= 40 && percent < 60) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_50)));
            } else if (percent >= 60 && percent < 80) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_70)));
            } else if (percent >= 80 && percent < 95) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_90)));
            } else if (percent >= 95 && percent <= 100) {
                batteryLevel.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_battery_level_100)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
    }

    //** 和onClickUiToggle重复，要干掉
    public void onCLickUiToggleToClear() {
        if (state == STATE_PREPARING) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToPreparing();
            } else {
            }
        } else if (state == STATE_PLAYING) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToPlayingClear();
            } else {
            }
        } else if (state == STATE_PAUSE) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToPauseClear();
            } else {
            }
        } else if (state == STATE_AUTO_COMPLETE) {
            if (bottomContainer.getVisibility() == Component.VISIBLE) {
                changeUiToComplete();
            } else {
            }
        }
    }

    @Override
    public void onProgress(int progress, long position, long duration) {
        super.onProgress(progress, position, duration);
        bottomProgressBar.setProgressValue(progress);
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        bottomProgressBar.setViceProgress(bufferProgress);
    }

    @Override
    public void resetProgressAndTime() {
        super.resetProgressAndTime();
        bottomProgressBar.setProgressValue(0);
        bottomProgressBar.setViceProgress(0);
    }

    public void changeUiToNormal() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.INVISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.VISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPreparing() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE,
                        Component.VISIBLE, Component.VISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUIToPreparingPlaying() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.VISIBLE, Component.INVISIBLE,
                        Component.VISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUIToPreparingChangeUrl() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE,
                        Component.VISIBLE, Component.VISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPlayingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.VISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPlayingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.VISIBLE, Component.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPauseShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.VISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPauseClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.VISIBLE, Component.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToComplete() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.INVISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.VISIBLE, Component.INVISIBLE, Component.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToError() {
        switch (screen) {
            case SCREEN_NORMAL:
                setAllControlsVisiblity(Component.INVISIBLE, Component.INVISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(Component.VISIBLE, Component.INVISIBLE, Component.VISIBLE,
                        Component.INVISIBLE, Component.INVISIBLE, Component.INVISIBLE, Component.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }


    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro, int posterImg, int bottomPro, int retryLayout) {
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        if (loadingPro == Component.VISIBLE) {
            EventHandler handler = new EventHandler(EventRunner.create(true));
            handler.postTask(new Runnable() {
                @Override
                public void run() {
                    if (value != null && !value.isRunning()) {
                        value.start();
                    }
                }
            });
        } else {
            if (value != null && value.isRunning()) {
                value.cancel();
            }
        }
        posterImageView.setVisibility(posterImg);
        bottomProgressBar.setVisibility(bottomPro);
        mRetryLayout.setVisibility(retryLayout);
    }

    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(VISIBLE);
            try {
                startButton.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_pause_normal)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
            replayTextView.setVisibility(Component.HIDE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(Component.HIDE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            try {
                startButton.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_restart_normal)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
            replayTextView.setVisibility(VISIBLE);
        } else {
            try {
                startButton.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_play_normal)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
            replayTextView.setVisibility(Component.HIDE);
        }
    }

    @Override
    public void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        if (mProgressDialog == null) {
            Component localView = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_jz_dialog_progress, null, false);
            mDialogProgressBar = (ProgressBar) localView.findComponentById(ResourceTable.Id_duration_progressbar);
            mDialogSeekTime = (Text) localView.findComponentById(ResourceTable.Id_tv_current);
            mDialogTotalTime = (Text) localView.findComponentById(ResourceTable.Id_tv_duration);
            mDialogIcon = (Image) localView.findComponentById(ResourceTable.Id_duration_image_tip);
            mProgressDialog = new CommonDialog(getContext());
            mProgressDialog.setContentCustomComponent(localView);
            mProgressDialog.setSize(MATCH_CONTENT, MATCH_CONTENT);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        mDialogProgressBar.setProgressValue(totalTimeDuration <= 0 ? 0 : (int) (seekTimePosition * 100 / totalTimeDuration));
        if (deltaX > 0) {
            try {
                mDialogIcon.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_forward_icon)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mDialogIcon.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_backward_icon)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
        }
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissProgressDialog() {
        super.dismissProgressDialog();
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    int prePercent;

    @Override
    public void showVolumeDialog(float deltaY, int volumePercent) {
        super.showVolumeDialog(deltaY, volumePercent);
        if (mVolumeDialog == null) {
            Component localView = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_jz_dialog_volume, null, false);
            mDialogVolumeImageView = (Image) localView.findComponentById(ResourceTable.Id_volume_image_tip);
            mDialogVolumeTextView = (Text) localView.findComponentById(ResourceTable.Id_tv_volume);
            mDialogVolumeProgressBar = (ProgressBar) localView.findComponentById(ResourceTable.Id_volume_progressbar);
            mVolumeDialog = new CommonDialog(getContext());
            mVolumeDialog.setCornerRadius(18);
            mVolumeDialog.setContentCustomComponent(localView);
            mVolumeDialog.setSize(MATCH_CONTENT, MATCH_CONTENT);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        if (volumePercent <= 0 && prePercent > 0) {
            try {
                mDialogVolumeImageView.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_close_volume)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
        } else if (volumePercent > 0 && prePercent < 0) {
            try {
                mDialogVolumeImageView.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_add_volume)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
        }else if(volumePercent > 0 && prePercent == 0) {
            try {
                mDialogVolumeImageView.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_add_volume)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }

        }else if(volumePercent <= 0 && prePercent == 0){
            try {
                mDialogVolumeImageView.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_jz_close_volume)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotExistException e) {
                e.printStackTrace();
            }
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        mDialogVolumeTextView.setText(volumePercent + "%");
        mDialogVolumeProgressBar.setProgressValue(volumePercent);
        prePercent = volumePercent;
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissVolumeDialog() {
        super.dismissVolumeDialog();
        if (mVolumeDialog != null) {
            mVolumeDialog.hide();
        }
    }

    @Override
    public void showBrightnessDialog(int brightnessPercent) {
        super.showBrightnessDialog(brightnessPercent);
        if (mBrightnessDialog == null) {
            Component localView = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_jz_dialog_brightness, null, false);
            mDialogBrightnessTextView = (Text) localView.findComponentById(ResourceTable.Id_tv_brightness);
            mDialogBrightnessProgressBar = (ProgressBar) localView.findComponentById(ResourceTable.Id_brightness_progressbar);
            mBrightnessDialog = new CommonDialog(getContext());
            mBrightnessDialog.setContentCustomComponent(localView);
            mBrightnessDialog.setSize(MATCH_CONTENT, MATCH_CONTENT);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        mDialogBrightnessTextView.setText(brightnessPercent + "%");
        mDialogBrightnessProgressBar.setProgressValue(brightnessPercent);
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissBrightnessDialog() {
        super.dismissBrightnessDialog();
        if (mBrightnessDialog != null) {
            mBrightnessDialog.hide();
        }
    }


    public void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 2500);
    }

    public void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        cancelDismissControlViewTimer();
    }

    @Override
    public void reset() {
        super.reset();
        cancelDismissControlViewTimer();
        unregisterWifiListener(getApplicationContext());
    }

    public void dissmissControlView() {
        if (state != STATE_NORMAL
                && state != STATE_ERROR
                && state != STATE_AUTO_COMPLETE) {
            getContext().getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    bottomContainer.setVisibility(Component.INVISIBLE);
                    topContainer.setVisibility(Component.INVISIBLE);
                    startButton.setVisibility(Component.INVISIBLE);

                    if (screen != SCREEN_TINY) {
                        bottomProgressBar.setVisibility(Component.VISIBLE);
                    }
                }
            });
        }
    }

    public void registerWifiListener(Context context) {
        if (context == null) return;
        mIsWifi = JZUtils.isWifiConnected(context);
        subscribeWifiChange();
    }

    public void unregisterWifiListener(Context context) {
        if (context == null) return;
        try {
            unsubscribeWifiChange();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public class DismissControlViewTimerTask extends TimerTask {
        @Override
        public void run() {
            EventHandler handler = new EventHandler(EventRunner.getMainEventRunner());
            handler.postSyncTask(new Runnable() {
                @Override
                public void run() {
                    dissmissControlView();
                }
            });
        }
    }

}
