package liking.com.video;


import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;


import liking.com.video.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    private final static String TAG = "VideoFragment";

    IjkVideoView mVideoView;

    TableLayout mHudView;

    Settings mSettings;

    private Uri mVideoUrl;

    boolean isInit = false;

    public static final long SLEEP_TIME_DIFF = 1000;
    public static final long MAX_SLEEP_TIME = 5 * 1000;

    public long sleepTime = 0;

    public VideoFragment() {
    }

    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_video, container, false);
        initView(inflate);
        initData();
        return inflate;
    }

    private void initData() {
        mVideoUrl = Uri.parse("http://baobab.wdjcdn.com/14564977406580.mp4");
        isInit = true;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postReload();
            }
        });
    }

    private void initView(View v) {
        mVideoView = (IjkVideoView)v.findViewById(R.id.ijk_video);
        mHudView = (TableLayout)v.findViewById(R.id.hud_view);
        mSettings = new Settings(getActivity());
        mSettings.setEnableBackgroundPlay(false);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mVideoView.setHudView(mHudView);

//        mHudView.setVisibility(View.VISIBLE);
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Log.e(TAG, "!->onError");
                postReload();
                return true;
            }
        });

        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                Log.e(TAG, "!->onCompletion");
                sleepTime = 0;
                mHandler.removeCallbacksAndMessages(null);
            }
        });

    }

    public Handler mHandler = new Handler();

    @Override
    public void onStop() {
        super.onStop();
        if (!mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

//    public void onEvent(final JVideoFragmentMessage message) {
//        switch (message.what) {
//            case JVideoFragmentMessage.PLAY_VIDEO:
//                mVideoUrl = Uri.parse(message.str);
//                isInit = true;
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        postReload();
//                    }
//                });
//                break;
//            case JVideoFragmentMessage.CHANGE_NETWORK:
//                if (!isInit) {
//                    return;
//                }
//                postReload();
//                break;
//        }
//    }

    /**
     * 1, 2, 3, 4, 5, 5, 5 ...
     *
     * @param
     */
    public void postReload() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadVideo();
                if (sleepTime < MAX_SLEEP_TIME) {
                    sleepTime += SLEEP_TIME_DIFF;
                }
            }
        }, sleepTime);
    }

    /**
     * @param
     */
    private void loadVideo() {
        Log.e(TAG,"loadVideo() 正在重新加载视频...");

        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }
        mVideoView.setVideoURI(mVideoUrl);
        mVideoView.requestFocus();
        mVideoView.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }
        IjkMediaPlayer.native_profileEnd();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        Log.e(TAG,"onDestroy(), 初始化播放器界面回收");
    }
}
