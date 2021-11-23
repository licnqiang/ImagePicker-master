package cn.jzvd;


import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

/**
 * Created by Nathen on 2017/11/7.
 * 自定义播放器
 */
public abstract class JZMediaInterface implements SurfaceOps.Callback {

    public static SurfaceOps SAVED_SURFACE;
    public EventRunner mMediaHandlerThread;
    public EventHandler mMediaHandler;
    public EventHandler handler;
    public Jzvd jzvd;

    public static void setSavedSurface(SurfaceOps savedSurface) {
        SAVED_SURFACE = savedSurface;
    }

    public JZMediaInterface(Jzvd jzvd) {
        this.jzvd = jzvd;
    }

    public abstract void start();

    public abstract void prepare();

    public abstract void pause();

    public abstract boolean isPlaying();

    public abstract void seekTo(long time);

    public abstract void release();

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public abstract void setVolume(float leftVolume, float rightVolume);

    public abstract void setSpeed(float speed);

    public abstract void setSurface(Surface surface);
}
