package cn.jzvd;


import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.audio.AudioManager;
import ohos.media.common.Source;
import ohos.media.player.Player;
import ohos.utils.net.Uri;

/**
 * Created by Nathen on 2017/11/8.
 * 实现系统的播放引擎
 */
public class JZMediaSystem extends JZMediaInterface {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "PlayerView");
    public Player mediaPlayer;
    public Context mContext;

    public JZMediaSystem(Jzvd jzvd, Context context) {
        super(jzvd);
        mContext = context;
    }

    @Override
    public void prepare() {
        release();
        mMediaHandlerThread = EventRunner.getMainEventRunner();
        mMediaHandler = new EventHandler(EventRunner.create(true));//主线程还是非主线程，就在这里
        handler = new EventHandler(EventRunner.getMainEventRunner());
        mMediaHandler.postTask(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer = new Player(mContext);
                    mediaPlayer.setAudioStreamType(AudioManager.AudioVolumeType.STREAM_MUSIC.getValue());
                    mediaPlayer.setPlayerCallback(new Player.IPlayerCallback() {
                        @Override
                        public void onPrepared() {
                            handler.postTask(() -> jzvd.onPrepared());//如果是mp3音频，走这里
                        }

                        @Override
                        public void onMessage(int what, int extra) {
                            handler.postTask(() -> jzvd.onInfo(what, extra));
                        }

                        @Override
                        public void onError(int what, int extra) {
                            handler.postTask(() -> jzvd.onError(what, extra));
                        }

                        @Override
                        public void onResolutionChanged(int width, int height) {
                            handler.postTask(() -> jzvd.onVideoSizeChanged(width, height));
                        }

                        @Override
                        public void onPlayBackComplete() {
                            handler.postTask(() -> jzvd.onCompletion());
                        }

                        @Override
                        public void onRewindToComplete() {
                            handler.postTask(() -> jzvd.onSeekComplete());
                        }

                        @Override
                        public void onBufferingChange(int percent) {
                            handler.postTask(() -> jzvd.setBufferProgress(percent));
                        }

                        @Override
                        public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) {
                        }

                        @Override
                        public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) {
                        }
                    });
                    Source source = new Source("/storage/emulated/0/DCIM/Camera/VID_20211125_001413.mp4");
                    mediaPlayer.setSource(source);
                    mediaPlayer.prepare();
                    mediaPlayer.setVideoSurface(SAVED_SURFACE.getSurface());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void start() {
        if (mMediaHandler != null) {
            mMediaHandler.postTask(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        mediaPlayer.play();
                    }
                }
            });
        }
    }

    @Override
    public void pause() {
        if(mMediaHandler!=null){
            mMediaHandler.postTask(() -> {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            });
        }
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isNowPlaying();
    }

    @Override
    public void seekTo(long time) {
        mMediaHandler.postTask(() -> {
            try {
                mediaPlayer.rewindTo((int) time * 1000);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void release() {//not perfect change you later
        if (mMediaHandler != null && mMediaHandlerThread != null && mediaPlayer != null) {//不知道有没有妖孽
            EventRunner tmpHandlerThread = mMediaHandlerThread;
            Player tmpMediaPlayer = mediaPlayer;
            setSavedSurface(null);
            mMediaHandler.postTask(() -> {
                tmpMediaPlayer.setVideoSurface(null);
                tmpMediaPlayer.release();
            });
            mediaPlayer = null;
        }
    }

    //TODO 测试这种问题是否在threadHandler中是否正常，所有的操作mediaplayer是否不需要thread，挨个测试，是否有问题
    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime();
        } else {
            return 0;
        }
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaHandler == null) return;
        mMediaHandler.postTask(() -> {
            if (mediaPlayer != null) mediaPlayer.setVolume(rightVolume);
        });
    }

    @Override
    public void setSpeed(float speed) {
        mediaPlayer.setPlaybackSpeed(speed);
    }

    @Override
    public void setSurface(Surface surface) {
        if (surface != null) {
            mediaPlayer.setVideoSurface(surface);
        }
    }

    @Override
    public void surfaceCreated(SurfaceOps surface) {
        if (surface != null) {
            setSavedSurface(surface);
            prepare();
        }
    }

    @Override
    public void surfaceChanged(SurfaceOps surfaceOps, int info, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceOps surfaceOps) {
    }

}
