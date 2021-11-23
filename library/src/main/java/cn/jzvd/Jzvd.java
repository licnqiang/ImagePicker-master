package cn.jzvd;

import cn.jzvd.gesture.GestureDetector;
import es.dmoral.toasty.Toasty;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.agp.components.*;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.window.service.WindowManager;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.audio.AudioManager;
import ohos.media.audio.AudioRemoteException;
import ohos.media.player.Player;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import ohos.sensor.bean.CategoryMotion;
import ohos.sensor.data.CategoryMotionData;
import ohos.sensor.listener.ICategoryMotionDataCallback;
import ohos.sysappcomponents.settings.SystemSettings;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Created by Nathen on 16/7/30.
 */
public abstract class Jzvd extends StackLayout implements Slider.ValueChangedListener, Component.TouchEventListener, Component.ClickedListener, Component.EstimateSizeListener {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "Jzvd");

    public static final int SCREEN_NORMAL = 0;
    public static final int SCREEN_FULLSCREEN = 1;
    public static final int SCREEN_TINY = 2;
    public static final int STATE_IDLE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARING_CHANGE_URL = 2;
    public static final int STATE_PREPARING_PLAYING = 3;
    public static final int STATE_PREPARED = 4;
    public static final int STATE_PLAYING = 5;
    public static final int STATE_PAUSE = 6;
    public static final int STATE_AUTO_COMPLETE = 7;
    public static final int STATE_ERROR = 8;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER = 0;//DEFAULT
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;
    public static final int THRESHOLD = 80;
    public static Jzvd CURRENT_JZVD;
    public static LinkedList<ComponentContainer> CONTAINER_LIST;
    public static boolean TOOL_BAR_EXIST = true;
    public static AbilityInfo.DisplayOrientation FULLSCREEN_ORIENTATION = AbilityInfo.DisplayOrientation.LANDSCAPE;
    public static AbilityInfo.DisplayOrientation NORMAL_ORIENTATION = AbilityInfo.DisplayOrientation.PORTRAIT;
    public static boolean SAVE_PROGRESS = true;
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long lastAutoFullscreenTime = 0;
    public static int ON_PLAY_PAUSE_TMP_STATE = 0;//这个考虑不放到库里，去自定义
    public static int backUpBufferState = -1;
    public static float PROGRESS_DRAG_RATE = 1f;//进度条滑动阻尼系数 越大播放进度条滑动越慢
    private static ComponentContainer mDecorView;
    public int state = -1;
    public int screen = -1;
    public JZDataSource jzDataSource;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public JZMediaSystem mediaInterface;
    public int positionInList = -1;//很想干掉它
    public int videoRotation = 0;
    public int seekToManulPosition = -1;
    public long seekToInAdvance = 0;

    protected GestureDetector gestureDetector;

    public abstract int getLayoutId();

    public abstract void click(int resourceId);

    public Image startButton;
    public Slider progressBar;
    public Image fullscreenButton;
    public Text currentTimeTextView, totalTimeTextView;
    public ComponentContainer textureViewContainer;
    public ComponentContainer topContainer, bottomContainer;
    public JZTextureView textureView;
    public boolean preloading = false;
    protected long gobakFullscreenTime = 0;//这个应该重写一下，刷新列表，新增列表的刷新，不打断播放，应该是个flag
    protected long gotoFullscreenTime = 0;
    protected Timer UPDATE_PROGRESS_TIMER;
    public int mScreenWidth;
    public int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;
    protected float mDownX;
    protected float mDownY;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected boolean mChangeBrightness;
    protected long mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;
    protected long mSeekTimePosition;
    protected Context jzvdContext;
    protected long mCurrentPosition;
    private Component component;
    /**
     * 如果不在列表中可以不加block
     */
    protected ComponentContainer.LayoutConfig blockLayoutParams;
    protected int blockIndex;
    protected int blockWidth;
    protected int blockHeight;

    public abstract void getView(Component component);

    public Jzvd(Context context) {
        super(context);
        init(context);
    }

    public Jzvd(Context context, AttrSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        component = LayoutScatter.getInstance(context).parse(getLayoutId(), null, true);
        jzvdContext = context;
        setEstimateSizeListener(this);
        startButton = (Image) component.findComponentById(ResourceTable.Id_start);
        fullscreenButton = (Image) component.findComponentById(ResourceTable.Id_fullscreen);
        progressBar = (Slider) component.findComponentById(ResourceTable.Id_bottom_seek_progress);
        currentTimeTextView = (Text) component.findComponentById(ResourceTable.Id_current);
        totalTimeTextView = (Text) component.findComponentById(ResourceTable.Id_total);
        bottomContainer = (ComponentContainer) component.findComponentById(ResourceTable.Id_layout_bottom);
        textureViewContainer = (ComponentContainer) component.findComponentById(ResourceTable.Id_surface_container);
        topContainer = (ComponentContainer) component.findComponentById(ResourceTable.Id_layout_top);
        getView(component);
        DependentLayout.LayoutConfig config = new DependentLayout.LayoutConfig();
        config.width = ComponentContainer.LayoutConfig.MATCH_PARENT;
        config.height = ComponentContainer.LayoutConfig.MATCH_PARENT;
        component.setLayoutConfig(config);
        addComponent(component);
        mScreenWidth = getContext().getResourceManager().getDeviceCapability().width;
        mScreenHeight = getContext().getResourceManager().getDeviceCapability().height;
        state = STATE_IDLE;
        initListener();
        CONTAINER_LIST = new LinkedList<>();
    }

    private void initListener() {
        startButton.setClickedListener(this);
        fullscreenButton.setClickedListener(this);
        progressBar.setValueChangedListener(this);
        bottomContainer.setClickedListener(this);
        textureViewContainer.setClickedListener(this);
        textureViewContainer.setTouchEventListener(this);
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(TouchEvent e) {
                onTouch(e, 2);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(TouchEvent e) {
                onTouch(e, 1);
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(TouchEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                return false;
            }
        });
    }

    @Override
    public void onClick(Component v) {
        int i = v.getId();
        if (i == ResourceTable.Id_start) {
            clickStart();
        } else if (i == ResourceTable.Id_fullscreen) {
            clickFullscreen();
        } else if (i == ResourceTable.Id_surface_container) {
            click(i);
        }
    }

    abstract void clickFullScreen(int id);

    @Override
    public boolean onEstimateSize(int widthMeasureSpec, int heightMeasureSpec) {
        if (screen == SCREEN_FULLSCREEN || screen == SCREEN_TINY) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = MeasureSpec.getSize(heightMeasureSpec);
            setEstimatedSize(specWidth, specHeight);
            int childWidthMeasureSpec = MeasureSpec.getMeasureSpec(specWidth, MeasureSpec.PRECISE);
            int childHeightMeasureSpec = MeasureSpec.getMeasureSpec(specHeight, MeasureSpec.PRECISE);
            getComponentAt(0).estimateSize(childWidthMeasureSpec, childHeightMeasureSpec);
            return true;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setEstimatedSize(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.getMeasureSpec(specWidth, MeasureSpec.PRECISE);
            int childHeightMeasureSpec = MeasureSpec.getMeasureSpec(specHeight, MeasureSpec.PRECISE);
            getComponentAt(0).estimateSize(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {

            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = MeasureSpec.getSize(heightMeasureSpec);
            setEstimatedSize(specWidth, specHeight);
            int childWidthMeasureSpec = MeasureSpec.getMeasureSpec(specWidth, MeasureSpec.PRECISE);
            int childHeightMeasureSpec = MeasureSpec.getMeasureSpec(specHeight, MeasureSpec.PRECISE);
            getComponentAt(0).estimateSize(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        return true;
    }

    public void setUp(String url, String title) {
        setUp(new JZDataSource(url, title), SCREEN_NORMAL);
    }

    public void setUp(String url, String title, int screen) {
        setUp(new JZDataSource(url, title), screen);
    }

    public void setUp(JZDataSource jzDataSource, int screen) {
        setUp(jzDataSource, screen, new JZMediaSystem(this, getContext()));
    }

    public void setUp(String url, String title, int screen, JZMediaSystem mediaInterface) {
        setUp(new JZDataSource(url, title), screen, mediaInterface);
    }

    public void setUp(JZDataSource jzDataSource, int screen, JZMediaSystem mediaInterface) {
        this.jzDataSource = jzDataSource;
        this.screen = screen;
        onStateNormal();
        this.mediaInterface = mediaInterface;
    }

    /**
     * 增加准备状态逻辑
     */
    public static void goOnPlayOnResume() {
        if (CURRENT_JZVD != null) {
            if (CURRENT_JZVD.state == Jzvd.STATE_PAUSE) {
                if (ON_PLAY_PAUSE_TMP_STATE == STATE_PAUSE) {
                    CURRENT_JZVD.onStatePause();
//                    CURRENT_JZVD.mediaInterface.pause();
                } else {
                    CURRENT_JZVD.onStatePlaying();
                    CURRENT_JZVD.mediaInterface.start();
                }
                ON_PLAY_PAUSE_TMP_STATE = 0;
            } else if (CURRENT_JZVD.state == Jzvd.STATE_PREPARING) {
                //准备状态暂停后的
                CURRENT_JZVD.startVideo();
            }
            if (CURRENT_JZVD.screen == Jzvd.SCREEN_FULLSCREEN) {
                JZUtils.hideStatusBar(CURRENT_JZVD.jzvdContext);
                JZUtils.hideSystemUI(CURRENT_JZVD.jzvdContext);
            }
        }
    }

    /**
     * 增加准备状态逻辑
     */
    public static void goOnPlayOnPause() {
        if (CURRENT_JZVD != null) {
            if (CURRENT_JZVD.state == Jzvd.STATE_AUTO_COMPLETE ||
                    CURRENT_JZVD.state == Jzvd.STATE_NORMAL ||
                    CURRENT_JZVD.state == Jzvd.STATE_ERROR) {
                Jzvd.releaseAllVideos();
            } else if (CURRENT_JZVD.state == Jzvd.STATE_PREPARING) {
                //准备状态暂停的逻辑
                Jzvd.setCurrentJzvd(CURRENT_JZVD);
                CURRENT_JZVD.state = STATE_PREPARING;
            } else {
                ON_PLAY_PAUSE_TMP_STATE = CURRENT_JZVD.state;
                CURRENT_JZVD.onStatePause();
                CURRENT_JZVD.mediaInterface.pause();
            }
        }
    }

    public static void startFullscreenDirectly(Context context, Class _class, String url, String title) {
        startFullscreenDirectly(context, _class, new JZDataSource(url, title));
    }

    public static void startFullscreenDirectly(Context context, Class _class, JZDataSource jzDataSource) {
        JZUtils.hideStatusBar(context);
        JZUtils.setRequestedOrientation(context, FULLSCREEN_ORIENTATION);
        JZUtils.hideSystemUI(context);
        try {
            Constructor<Jzvd> constructor = _class.getConstructor(Context.class);
            final Jzvd jzvd = constructor.newInstance(context);
            StackLayout.LayoutConfig lp = new StackLayout.LayoutConfig(
                    StackLayout.LayoutConfig.MATCH_PARENT, StackLayout.LayoutConfig.MATCH_PARENT);
            mDecorView.addComponent(jzvd, lp);
            jzvd.setUp(jzDataSource, JzvdStd.SCREEN_FULLSCREEN);
            jzvd.startVideo();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void releaseAllVideos() {
        HiLog.debug(label, "releaseAllVideos");
        if (CURRENT_JZVD != null) {
            CURRENT_JZVD.reset();
            CURRENT_JZVD = null;
        }
        CONTAINER_LIST.clear();
    }

    public static boolean backPress() {
        HiLog.info(label, "backPress");
        if (CONTAINER_LIST.size() != 0 && CURRENT_JZVD != null) {//判断条件，因为当前所有goBack都是回到普通窗口
            CURRENT_JZVD.gotoNormalScreen();
            return true;
        } else if (CONTAINER_LIST.size() == 0 && CURRENT_JZVD != null && CURRENT_JZVD.screen != SCREEN_NORMAL) {//退出直接进入的全屏
            CURRENT_JZVD.clearFloatScreen();
            return true;
        }
        return false;
    }

    public static void setCurrentJzvd(Jzvd jzvd) {
        if (CURRENT_JZVD != null) CURRENT_JZVD.reset();
        CURRENT_JZVD = jzvd;
    }

    public static void setTextureViewRotation(int rotation) {
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.setRotation(rotation);
        }
    }

    public static void setVideoImageDisplayType(int type) {
        Jzvd.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.getContext().getUITaskDispatcher().delayDispatch(new Runnable() {
                @Override
                public void run() {
                    CURRENT_JZVD.textureView.postLayout();
                }
            }, 100);
        }
    }

    public void setMediaInterface(JZMediaSystem mediaInterfaceClass) {
        reset();
        this.mediaInterface = mediaInterfaceClass;
    }

    protected void clickFullscreen() {
        HiLog.info(label, "onClick fullscreen [" + this.hashCode() + "] ");
        if (state == STATE_AUTO_COMPLETE) return;
        if (screen == SCREEN_FULLSCREEN) {
            //quit fullscreen
            backPress();
        } else {
            HiLog.debug(label, "toFullscreenActivity [" + this.hashCode() + "] ");
            gotoFullscreen();
        }
    }

    protected void clickStart() {
        HiLog.info(label, "onClick start [" + this.hashCode() + "] ");
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            try {
                Toasty.warning(getContext(), getResourceManager().getElement(ResourceTable.String_no_url).getString());
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
            if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                    jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                    !JZUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {//这个可以放到std中
                showWifiDialog();
                return;
            }
            startVideo();
        } else if (state == STATE_PLAYING) {
            HiLog.debug(label, "pauseVideo [" + this.hashCode() + "] ");
            mediaInterface.pause();
            onStatePause();
        } else if (state == STATE_PAUSE) {
            mediaInterface.start();
            onStatePlaying();
        } else if (state == STATE_AUTO_COMPLETE) {
            startVideo();
        }
    }

    @Override
    public boolean onTouchEvent(Component v, TouchEvent event) {
        int id = v.getId();
        if (id == ResourceTable.Id_surface_container) {
            gestureDetector.onTouchEvent(event, textureViewContainer);
        }
        if (id == ResourceTable.Id_surface_container) {
            MmiPoint mmiPoint = event.getPointerScreenPosition(0);
            float x = mmiPoint.getX();
            float y = mmiPoint.getY();
            switch (event.getAction()) {
                case TouchEvent.PRIMARY_POINT_DOWN:
                    touchActionDown(x, y);
                    break;
                case TouchEvent.POINT_MOVE:
                    touchActionMove(x, y);
                    break;
                case TouchEvent.PRIMARY_POINT_UP:
                    touchActionUp();
                    break;
            }
        }
        return true;
    }


    public abstract void onTouch(TouchEvent event, int flag);


    protected void touchActionUp() {
        HiLog.info(label, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
        mTouchingProgressBar = false;
        dismissProgressDialog();
        dismissVolumeDialog();
        dismissBrightnessDialog();
        if (mChangePosition) {
            mediaInterface.seekTo(mSeekTimePosition);
            long duration = getDuration();
            int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
            progressBar.setProgressValue(progress);
        }
        if (mChangeVolume) {
            //change volume event
        }
        startProgressTimer();
    }

    protected void touchActionMove(float x, float y) {
        HiLog.info(label, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
        float deltaX = x - mDownX;
        float deltaY = y - mDownY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        if (screen == SCREEN_FULLSCREEN) {
//            //拖动的是NavigationBar和状态栏
//            if (mDownX > JZUtils.getScreenWidth(getContext()) || mDownY < JZUtils.getStatusBarHeight(getContext())) {
//                return;
//            }
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                    cancelProgressTimer();
                    if (absDeltaX >= THRESHOLD) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true;
                            mGestureDownPosition = getCurrentPositionWhenPlaying();
                        }
                    } else {
                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                        if (mDownX < mScreenHeight * 0.5f) {//左侧改变亮度
                            mChangeBrightness = true;
                            WindowManager.LayoutConfig lp = JZUtils.getWindow(getContext()).getLayoutConfig().get();
                            if (lp.windowBrightness < 0) {
                                mGestureDownBrightness = Integer.parseInt(SystemSettings.getValue(DataAbilityHelper.creator(jzvdContext), SystemSettings.Display.AUTO_SCREEN_BRIGHTNESS));
//                              mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                HiLog.info(label, "current system brightness: " + mGestureDownBrightness);
                            } else {
                                mGestureDownBrightness = lp.windowBrightness * 255;
                                HiLog.info(label, "current activity brightness: " + mGestureDownBrightness);
                            }
                        } else {//右侧改变声音
                            mChangeVolume = true;
                            try {
                                if (mAudioManager != null) {
                                    mGestureDownVolume = mAudioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
                                }
                            } catch (AudioRemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if (mChangePosition) {
            long totalTimeDuration = getDuration();
            if (PROGRESS_DRAG_RATE <= 0) {
                HiLog.debug(label, "error PROGRESS_DRAG_RATE value");
                PROGRESS_DRAG_RATE = 1f;
            }
            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * PROGRESS_DRAG_RATE));
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = JZUtils.stringForTime(mSeekTimePosition);
            String totalTime = JZUtils.stringForTime(totalTimeDuration);

            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        }
        if (mChangeVolume) {
            deltaY = -deltaY;
            int max = 0;
            try {
                max = mAudioManager.getMaxVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            } catch (AudioRemoteException e) {
                e.printStackTrace();
            }
            int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
            mAudioManager.setVolume(AudioManager.AudioVolumeType.STREAM_MUSIC, mGestureDownVolume + deltaV);
            //dialog中显示百分比
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
            showVolumeDialog(-deltaY, volumePercent);
        }

        if (mChangeBrightness) {
            deltaY = -deltaY;
            int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
            WindowManager.LayoutConfig params = JZUtils.getWindow(getContext()).getLayoutConfig().get();
            if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                params.windowBrightness = 1;
            } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                params.windowBrightness = 0.01f;
            } else {
                params.windowBrightness = (mGestureDownBrightness + deltaV) / 255;
            }
            JZUtils.getWindow(getContext()).setLayoutConfig(params);
            //dialog中显示百分比
            int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
            showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
        }
    }

    protected void touchActionDown(float x, float y) {
        HiLog.info(label, "onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
        mTouchingProgressBar = true;
        mDownX = x;
        mDownY = y;
        mChangeVolume = false;
        mChangePosition = false;
        mChangeBrightness = false;
    }

    public void onStateNormal() {
        HiLog.info(label, "onStateNormal " + " [" + this.hashCode() + "] ");
        state = STATE_NORMAL;
        cancelProgressTimer();
        if (mediaInterface != null) mediaInterface.release();
    }

    public void onStatePreparing() {
        HiLog.info(label, "onStatePreparing " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING;
        resetProgressAndTime();
    }

    public void onStatePreparingPlaying() {
        HiLog.info(label, "onStatePreparingPlaying " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING_PLAYING;
    }

    public void onStatePreparingChangeUrl() {
        HiLog.info(label, "onStatePreparingChangeUrl " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING_CHANGE_URL;
        releaseAllVideos();
        startVideo();
        mediaInterface.prepare();
    }

    public void changeUrl(JZDataSource jzDataSource, long seekToInAdvance) {
        this.jzDataSource = jzDataSource;
        this.seekToInAdvance = seekToInAdvance;
        onStatePreparingChangeUrl();
    }

    public void onPrepared() {
        HiLog.info(label, "onPrepared " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARED;
        if (!preloading) {
            mediaInterface.start();//这里原来是非县城
            preloading = false;
        }
        if (jzDataSource.getCurrentUrl().toString().toLowerCase().contains("mp3") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("wma") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("aac") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("m4a") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("wav")) {
            onStatePlaying();
        }
    }

    public void startPreloading() {
        preloading = true;
        startVideo();
    }

    /**
     * 如果STATE_PREPARED就播放，如果没准备完成就走正常的播放函数startVideo();
     */
    public void startVideoAfterPreloading() {
        if (state == STATE_PREPARED) {
            mediaInterface.start();
        } else {
            preloading = false;
            startVideo();
        }
    }

    public void onStatePlaying() {
        HiLog.info(label, "onStatePlaying " + " [" + this.hashCode() + "] ");
        if (state == STATE_PREPARED) {//如果是准备完成视频后第一次播放，先判断是否需要跳转进度。
            HiLog.debug(label, "onStatePlaying:STATE_PREPARED ");
            mAudioManager = new AudioManager(jzvdContext);
//            mAudioManager.activateAudioInterrupt((AudioInterrupt) onAudioFocusChangeListener);
            if (seekToInAdvance != 0) {
                mediaInterface.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            } else {

                long position = JZUtils.getSavedProgress(getContext(), jzDataSource.getCurrentUrl());
                HiLog.info(label,"getProgress-----"+position);
                if (position != 0) {
                    mediaInterface.seekTo(position);//这里为什么区分开呢，第一次的播放和resume播放是不一样的。 这里怎么区分是一个问题。然后
                }
            }
        }
        state = STATE_PLAYING;
        startProgressTimer();
    }

    public void onStatePause() {
        HiLog.info(label, "onStatePause " + " [" + this.hashCode() + "] ");
        state = STATE_PAUSE;
        JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), getCurrentPositionWhenPlaying());
        HiLog.info(label, "save--- " + getCurrentPositionWhenPlaying());
        startProgressTimer();
    }

    public void onStateError() {
        HiLog.info(label, "onStateError " + " [" + this.hashCode() + "] ");
        state = STATE_ERROR;
        cancelProgressTimer();
    }

    public void onStateAutoComplete() {
        HiLog.info(label, "onStateAutoComplete " + " [" + this.hashCode() + "] ");
        state = STATE_AUTO_COMPLETE;
        cancelProgressTimer();
        progressBar.setProgressValue(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    public void onInfo(int what, int extra) {
        HiLog.debug(label, "onInfo what - " + what + " extra - " + extra);
        if (what == Player.PLAYER_INFO_VIDEO_RENDERING_START) {
            HiLog.debug(label, "MEDIA_INFO_VIDEO_RENDERING_START");
            if (state == Jzvd.STATE_PREPARED
                    || state == Jzvd.STATE_PREPARING_CHANGE_URL
                    || state == Jzvd.STATE_PREPARING_PLAYING) {
                onStatePlaying();//开始渲染图像，真正进入playing状态
            }
        } else if (what == Player.PLAYER_INFO_BUFFERING_START) {
            HiLog.debug(label, "MEDIA_INFO_BUFFERING_START");
            backUpBufferState = state;
            setState(STATE_PREPARING_PLAYING);
        } else if (what == Player.PLAYER_INFO_BUFFERING_END) {
            HiLog.debug(label, "MEDIA_INFO_BUFFERING_END");
            if (backUpBufferState != -1) {
                setState(backUpBufferState);
                backUpBufferState = -1;
            }
        }
    }

    public void onError(int what, int extra) {
        HiLog.error(label, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            onStateError();
            mediaInterface.release();
        }
    }

    public void onCompletion() {
        HiLog.info(label, "onAutoCompletion " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateAutoComplete();
        mediaInterface.release();
//        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutConfig.MARK_LOCK_AS_SCREEN_ON);
        JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), 0);

        if (screen == SCREEN_FULLSCREEN) {
            if (CONTAINER_LIST.size() == 0) {
                clearFloatScreen();//直接进入全屏
            } else {
                gotoNormalCompletion();
            }
        }
    }

    public void gotoNormalCompletion() {
        gobakFullscreenTime = System.currentTimeMillis();//退出全屏
        if (mDecorView != null) {
            mDecorView.removeComponent(this);
        }
        textureViewContainer.removeComponent(textureView);
        CONTAINER_LIST.getLast().removeComponentAt(blockIndex);//remove block
        CONTAINER_LIST.getLast().addComponent(this, blockIndex, blockLayoutParams);
        CONTAINER_LIST.pop();
        setScreenNormal();
        JZUtils.showStatusBar(jzvdContext);
        JZUtils.setRequestedOrientation(jzvdContext, NORMAL_ORIENTATION);
        JZUtils.showSystemUI(jzvdContext);
    }

    /**
     * 多数表现为中断当前播放
     */
    public void reset() {
        HiLog.info(label, "reset " + " [" + this.hashCode() + "] ");
        if (state == STATE_PLAYING || state == STATE_PAUSE) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), position);
        }
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateNormal();
        textureViewContainer.removeAllComponents();
//        mAudioManager.activateAudioInterrupt((AudioInterrupt) onAudioFocusChangeListener);
        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutConfig.MARK_LOCK_AS_SCREEN_ON);
        if (mediaInterface != null) mediaInterface.release();
    }

    /**
     * 里面的的onState...()其实就是setState...()，因为要可以被复写，所以参考Activity的onCreate(),onState..()的方式看着舒服一些，老铁们有何高见。
     *
     * @param state stateId
     */
    public void setState(int state) {
        switch (state) {
            case STATE_NORMAL:
                onStateNormal();
                break;
            case STATE_PREPARING:
                onStatePreparing();
                break;
            case STATE_PREPARING_PLAYING:
                onStatePreparingPlaying();
                break;
            case STATE_PREPARING_CHANGE_URL:
                onStatePreparingChangeUrl();
                break;
            case STATE_PLAYING:
                onStatePlaying();
                break;
            case STATE_PAUSE:
                onStatePause();
                break;
            case STATE_ERROR:
                onStateError();
                break;
            case STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public void setScreen(int screen) {//特殊的个别的进入全屏的按钮在这里设置  只有setup的时候能用上
        switch (screen) {
            case SCREEN_NORMAL:
                setScreenNormal();
                break;
            case SCREEN_FULLSCREEN:
                setScreenFullscreen();
                break;
            case SCREEN_TINY:
                setScreenTiny();
                break;
        }
    }

    public void startVideo() {
        HiLog.debug(label, "startVideo [" + this.hashCode() + "] ");
        setCurrentJzvd(this);
        addTextureView();
        JZUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutConfig.MARK_LOCK_AS_SCREEN_ON);
        onStatePreparing();
    }

    public void addTextureView() {
        HiLog.debug(label, "addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) {
            textureViewContainer.removeComponent(textureView);
        }
        textureView = new JZTextureView(mContext);
        textureView.getSurfaceOps().ifPresent(new Consumer<SurfaceOps>() {
            @Override
            public void accept(SurfaceOps surfaceOps) {
                surfaceOps.addCallback(mediaInterface);
            }
        });
        DependentLayout.LayoutConfig layoutConfig = new DependentLayout.LayoutConfig(ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);
        layoutConfig.addRule(DependentLayout.LayoutConfig.CENTER_IN_PARENT);
        textureView.setLayoutConfig(layoutConfig);
        textureViewContainer.addComponent(textureView);
    }

    public void clearFloatScreen() {
        JZUtils.showStatusBar(getContext());
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        if (mDecorView != null) {
            mDecorView.removeComponent(this);
        }
        if (mediaInterface != null) mediaInterface.release();
        CURRENT_JZVD = null;
    }

    public void onVideoSizeChanged(int width, int height) {
        HiLog.info(label, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (textureView != null) {
            if (videoRotation != 0) {
                textureView.setRotation(videoRotation);
            }
            textureView.setVideoSize(width, height);
        }
    }

    public void startProgressTimer() {
        HiLog.info(label, "startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void onProgress(int progress, long position, long duration) {
//        HiLog.debug(label, "onProgress: progress=" + progress + " position=" + position + " duration=" + duration);
        mCurrentPosition = position;
        if (!mTouchingProgressBar) {
            if (seekToManulPosition != -1) {
                if (seekToManulPosition > progress) {
                    return;
                } else {
                    seekToManulPosition = -1;//这个关键帧有没有必要做
                }
            } else {
                progressBar.setProgressValue(progress);
            }
        }
        if (position != 0) currentTimeTextView.setText(JZUtils.stringForTime(position));
        totalTimeTextView.setText(JZUtils.stringForTime(duration));
    }

    public void setBufferProgress(int bufferProgress) {
        progressBar.setViceProgress(bufferProgress);
    }

    public void resetProgressAndTime() {
        mCurrentPosition = 0;
        progressBar.setProgressValue(0);
        progressBar.setViceProgress(0);
        currentTimeTextView.setText(JZUtils.stringForTime(0));
        totalTimeTextView.setText(JZUtils.stringForTime(0));
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (state == STATE_PLAYING || state == STATE_PAUSE || state == STATE_PREPARING_PLAYING) {
            try {
                position = mediaInterface.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        try {
            duration = mediaInterface.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    @Override
    public void onProgressUpdated(Slider slider, int progress, boolean fromUser) {
        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();
            currentTimeTextView.setText(JZUtils.stringForTime(progress * duration / 100));
        }
    }

    @Override
    public void onTouchStart(Slider slider) {
        HiLog.info(label, "bottomProgress onStartTrackingTouch [" + this.hashCode() + "] ");
        cancelProgressTimer();
        ComponentParent vpdown = getComponentParent();
        while (vpdown != null) {
//            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getComponentParent();
        }
    }

    @Override
    public void onTouchEnd(Slider seekBar) {
        HiLog.info(label, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        startProgressTimer();
        ComponentParent vpup = getComponentParent();
        if (state != STATE_PLAYING &&
                state != STATE_PAUSE) return;
        long time = seekBar.getProgress() * getDuration() / 100;
        seekToManulPosition = seekBar.getProgress();
        mediaInterface.seekTo(time);
        HiLog.info(label, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    public void cloneAJzvd(ComponentContainer vg) {
        try {
            Constructor<Jzvd> constructor = (Constructor<Jzvd>) Jzvd.this.getClass().getConstructor(Context.class);
            Jzvd jzvd = constructor.newInstance(getContext());
            jzvd.setId(getId());
            jzvd.setMinWidth(blockWidth);
            jzvd.setMinHeight(blockHeight);
            vg.addComponent(jzvd, blockIndex, blockLayoutParams);
            jzvd.setUp(jzDataSource.cloneMe(), SCREEN_NORMAL, mediaInterface);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void setDecorView(ComponentContainer decorView) {
        mDecorView = decorView;
    }

    /**
     * 如果全屏或者返回全屏的视图有问题，复写这两个函数gotoScreenNormal(),根据自己布局的情况重新布局。
     */
    public void gotoFullscreen() {
//        clickFullScreen(getId());
        gotoFullscreenTime = System.currentTimeMillis();
        ComponentContainer vg = (ComponentContainer) getComponentParent();
        jzvdContext = vg.getContext();
        blockLayoutParams = getLayoutConfig();
        blockIndex = vg.getChildIndex(this);
        blockWidth = getWidth();
        blockHeight = getHeight();
        vg.removeComponent(this);
        cloneAJzvd(vg);
        CONTAINER_LIST.add(vg);
        if (mDecorView != null) {
            vg = mDecorView;
        }
        StackLayout.LayoutConfig fullLayout = new StackLayout.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);
        vg.addComponent(this, fullLayout);
        setScreenFullscreen();
        JZUtils.hideStatusBar(jzvdContext);
        JZUtils.setRequestedOrientation(jzvdContext, FULLSCREEN_ORIENTATION);
        JZUtils.hideSystemUI(jzvdContext);//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326
    }

    public void gotoNormalScreen() {//goback本质上是goto
        gobakFullscreenTime = System.currentTimeMillis();//退出全屏
        if (mDecorView != null) {
            mDecorView.removeComponent(this);
        }
//        CONTAINER_LIST.getLast().removeAllComponents();
        CONTAINER_LIST.getLast().removeComponentAt(blockIndex);//remove block
        CONTAINER_LIST.getLast().addComponent(this, blockIndex, blockLayoutParams);
        CONTAINER_LIST.pop();
        setScreenNormal();//这块可以放到jzvd中
        JZUtils.showStatusBar(jzvdContext);
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
    }

    public void setScreenNormal() {//TODO 这块不对呀，还需要改进，设置flag之后要设置ui，不设置ui这么写没意义呀
        screen = SCREEN_NORMAL;
    }

    public void setScreenFullscreen() {
        screen = SCREEN_FULLSCREEN;
    }

    public void setScreenTiny() {
        screen = SCREEN_TINY;
    }

    //重力感应的时候调用的函数，、、这里有重力感应的参数，暂时不能删除
    public void autoFullscreen(float x) {//TODO写道demo中
        if (CURRENT_JZVD != null
                && (state == STATE_PLAYING || state == STATE_PAUSE)
                && screen != SCREEN_FULLSCREEN
                && screen != SCREEN_TINY) {
            if (x > 0) {
                JZUtils.setRequestedOrientation(getContext(), AbilityInfo.DisplayOrientation.LANDSCAPE);
            } else {
                JZUtils.setRequestedOrientation(getContext(), AbilityInfo.DisplayOrientation.LANDSCAPE);
            }
            gotoFullscreen();
        }
    }

    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
//                && CURRENT_JZVD != null
                && state == STATE_PLAYING
                && screen == SCREEN_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            backPress();
        }
    }

    public void onSeekComplete() {

    }

    public void showWifiDialog() {
    }

    public void showProgressDialog(float deltaX,
                                   String seekTime, long seekTimePosition,
                                   String totalTime, long totalTimeDuration) {
    }

    public void dismissProgressDialog() {
    }

    public void showVolumeDialog(float deltaY, int volumePercent) {
    }

    public void dismissVolumeDialog() {
    }

    public void showBrightnessDialog(int brightnessPercent) {
    }

    public void dismissBrightnessDialog() {
    }

    public Context getApplicationContext() {//这个函数必要吗
        Context context = getContext();
        if (context != null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }
        return context;
    }

    public static class JZAutoFullscreenListener implements ICategoryMotionDataCallback {
        @Override
        public void onSensorDataModified(CategoryMotionData data) {
            final float x = data.values[0];
            float y = data.values[1];
            float z = data.values[2];
            //过滤掉用力过猛会有一个反向的大数值
            if (x < -12 || x > 12) {
                if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000) {
                    if (Jzvd.CURRENT_JZVD != null) Jzvd.CURRENT_JZVD.autoFullscreen(x);
                    lastAutoFullscreenTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void onAccuracyDataModified(CategoryMotion categoryMotion, int i) {

        }

        @Override
        public void onCommandCompleted(CategoryMotion categoryMotion) {

        }
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            EventHandler handler = new EventHandler(EventRunner.getMainEventRunner());
            handler.postSyncTask(new Runnable() {
                @Override
                public void run() {
                    if (state == STATE_PLAYING || state == STATE_PAUSE || state == STATE_PREPARING_PLAYING) {
//                        HiLog.info(label, "onProgressUpdate " + "[" + this.hashCode() + "] ");
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                        onProgress(progress, position, duration);
                    }
                }
            });
        }
    }

}
