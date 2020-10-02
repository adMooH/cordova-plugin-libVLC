package com.webmons.disono.libVLC;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;

import com.webmons.disono.vlc.VlcListener;
import com.webmons.disono.vlc.VlcVideoLibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 * <p>
 * Created at: 1/09/2018
 */

public class VLCActivity extends Activity implements VlcListener, View.OnClickListener {
    private Activity activity;
    public static final String BROADCAST_LISTENER = "com.webmons.disono.libVLC.Listener";
    public final String TAG = "VLCActivity";

    SurfaceView surfaceView;
    RelativeLayout mediaPlayerContainer;
    LinearLayout mediaPlayerView;
    LinearLayout mediaPlayerControls;
    SeekBar mSeekBar;
    int mProgress = 0;
    boolean isSeeking = false;
    private VlcVideoLibrary vlcVideoLibrary;
    private ImageButton bStartStop;
    private Handler handlerSeekBar;
    private Runnable runnableSeekBar;
    private TextView videoCurrentLoc;
    private TextView videoDuration;

    private Handler handlerOverlay;
    private Runnable runnableOverlay;
    private int playingPos;

    private String _url;


    private boolean _isFullscreen = true;
    private int _top = 0;
    private int _left = 0;
    private int _width = 0;
    private int _height = 0;
    private boolean _autoPlay = false;
    private boolean _hideControls = false;

    private String currentLoc = "00:00";
    private String duration = "00:00";

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");

                if (method != null) {
                    switch (method) {
                        case "playNext":
                            _url = intent.getStringExtra("url");
                            _autoPlay = intent.getBooleanExtra("autoPlay", false);
                            _hideControls = intent.getBooleanExtra("hideControls", false);

                            _initPlayer();

                            break;
                        case "changePositionAndSize":
                                getAndSetPositionAndSizeProps(intent);
                                setCustomPositionAndSize();
                            break;
                        case "pause":
							pause();

                            break;
                        case "resume":
                            if (vlcVideoLibrary.isPlaying()) {
                                vlcVideoLibrary.getPlayer().play();
                            }

                            break;
                        case "stop":
                            stop();

                            break;
                        case "getPosition":
                            if (vlcVideoLibrary.isPlaying()) {
                                JSONObject obj = new JSONObject();
                                try {
                                    obj.put("position", playingPos);
                                    obj.put("current_location", currentLoc);
                                    obj.put("duration", duration);
                                    _sendBroadCast("getPosition", obj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;
                        case "seekPosition":
                            Log.d(TAG, "Seek: " + intent.getFloatExtra("position", 0));
                            if (vlcVideoLibrary.isPlaying()) {
							isSeeking = true;
							_changePosition(intent.getFloatExtra("position", 0));
						}

                            break;

                        case "close":
                            onClose();
                            break;
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activity = this;
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        getWindow().setBackgroundDrawable(new 	android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        Utils.convertActivityToTranslucent(this);

        setContentView(_getResource("vlc_player", "layout"));
        _UIListener();
        _broadcastRCV();

        Intent intent = getIntent();
        _url = intent.getStringExtra("url");
        // do not show the controls
        _hideControls = intent.getBooleanExtra("hideControls", false);
        // auto play the video after launching
        _autoPlay = intent.getBooleanExtra("autoPlay", false);

        getAndSetPositionAndSizeProps(intent);
        setCustomPositionAndSize();

        _handlerSeekBar();
        _handlerMediaControl();

        // play
        _initPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();

        pause();
    }

    @Override
    public void onResume() {
        super.onResume();

            if (vlcVideoLibrary.isPlaying()) {
                vlcVideoLibrary.getPlayer().play();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.unregisterReceiver(br);

        stop();
        _sendBroadCast("onDestroyVlc");
    }

    public void onClose() {
        Log.d(TAG, "onClose");
        finish();
        _sendBroadCast("onCloseVlc");
    }

    @Override
    public void onClick(View v) {
        if (!vlcVideoLibrary.isPlaying()) {
            vlcVideoLibrary.play(_url);
        } else {
            pause();
        }
    }

    @Override
    public void onPlayVlc() {
        _sendBroadCast("onPlayVlc");

        Drawable drawableIcon = getResources().getDrawable(_getResource("ic_pause_white_24dp", "drawable"));
        bStartStop.setImageDrawable(drawableIcon);
    }

    @Override
    public void onPauseVlc() {
        _sendBroadCast("onPauseVlc");

        Drawable drawableIcon = getResources().getDrawable(_getResource("ic_play_arrow_white_24dp", "drawable"));
        bStartStop.setImageDrawable(drawableIcon);
    }

    @Override
    public void onStopVlc() {
        _sendBroadCast("onStopVlc");

        Drawable drawableIcon = getResources().getDrawable(_getResource("ic_play_arrow_white_24dp", "drawable"));
        bStartStop.setImageDrawable(drawableIcon);
    }

    @Override
    public void onVideoEnd() {
        _sendBroadCast("onVideoEnd");

        Drawable drawableIcon = getResources().getDrawable(_getResource("ic_play_arrow_white_24dp", "drawable"));
        bStartStop.setImageDrawable(drawableIcon);
    }

    @Override
    public void onError() {
        _sendBroadCast("onError");

        stop();

        Drawable drawableIcon = getResources().getDrawable(_getResource("ic_play_arrow_white_24dp", "drawable"));
        bStartStop.setImageDrawable(drawableIcon);
    }

	private void pause() {
		try {
			if (vlcVideoLibrary.isPlaying()) {
				vlcVideoLibrary.pause();
			}
		} catch (Exception error) {
			Log.e(TAG, "pause", error);
			_sendBroadCast("onError");
		}
	}

	private void stop() {
		try {
			if (vlcVideoLibrary.isPlaying()) {
				vlcVideoLibrary.stop();
			}
		} catch (Exception error) {
			Log.e(TAG, "stop", error);
			_sendBroadCast("onError");
		}
	}

    private void getAndSetPositionAndSizeProps(Intent intent) {
        _isFullscreen = intent.getBooleanExtra("fullscreen", true);
        _top = intent.getIntExtra("top", 0);
        _left = intent.getIntExtra("left", 0);
        _width = intent.getIntExtra("width", 320);
        _height = intent.getIntExtra("height", 240);
    }

    private void setCustomPositionAndSize() {
        if (!_isFullscreen) {
            setCustomPosition();
            setCustomSize();
        } else {
            setCustomPositionAndSizeToFullscreen();
        }
        _sendBroadCast("onPositionAndSizeChanged");
    }

    private void setCustomPositionAndSizeToFullscreen() {
		setMediaPlayerContainerToFullscreen();
		setVlcToFullscreen();
    }

    private void setMediaPlayerContainerToFullscreen() {
		setMargins(mediaPlayerContainer,0,0,0,0);
		mediaPlayerContainer.getLayoutParams().height =  ViewGroup.LayoutParams.MATCH_PARENT;
		mediaPlayerContainer.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
	}

    private void setVlcToFullscreen() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;
		vlcVideoLibrary.setSize(screenWidth,screenHeight);
	}

    private void setCustomSize() {
        mediaPlayerContainer.getLayoutParams().height = _height;
        mediaPlayerContainer.getLayoutParams().width = _width;
        vlcVideoLibrary.setSize(_width,_height);
    }

    private void setCustomPosition() {
        setMargins(mediaPlayerContainer,_left,_top,0,0);
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private void _initPlayer() {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (_hideControls) {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    runOnUiThread(() -> mediaPlayerControls.setVisibility(View.GONE));
                                }
                            };

                            thread.start();
                        }

                        if (_autoPlay && vlcVideoLibrary != null && _url != null) {
                            stop();

                            vlcVideoLibrary.play(_url);
                        }
                    }
                },
                300
        );
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(VideoPlayerVLC.BROADCAST_METHODS);
        activity.registerReceiver(br, filter);
    }

    private void _UIListener() {
        mSeekBar = findViewById(_getResource("videoSeekBar", "id"));

        surfaceView = findViewById(_getResource("vlc_surfaceView", "id"));
        bStartStop = findViewById(_getResource("vlc_start_stop", "id"));

        videoCurrentLoc = findViewById(_getResource("videoCurrentLoc", "id"));
        videoDuration = findViewById(_getResource("videoDuration", "id"));

        mediaPlayerContainer = findViewById(_getResource("mediaPlayerContainer", "id"));

        mediaPlayerView = findViewById(_getResource("mediaPlayerView", "id"));
        mediaPlayerControls = findViewById(_getResource("mediaPlayerControls", "id"));
        mediaPlayerControls.bringToFront();

        bStartStop.setOnClickListener(this);
        vlcVideoLibrary = new VlcVideoLibrary(this, this, surfaceView);
    }

    private void _handlerSeekBar() {
        // SEEK BAR
        handlerSeekBar = new Handler();
        runnableSeekBar = () -> {
            try {
                if (vlcVideoLibrary.getPlayer() != null && vlcVideoLibrary.isPlaying()) {
                    long curTime = vlcVideoLibrary.getPlayer().getTime();
                    long totalTime = (long) (curTime / vlcVideoLibrary.getPlayer().getPosition());
                    int minutes = (int) (curTime / (60 * 1000));
                    int seconds = (int) ((curTime / 1000) % 60);
                    int endMinutes = (int) (totalTime / (60 * 1000));
                    int endSeconds = (int) ((totalTime / 1000) % 60);
                    currentLoc = String.format(Locale.US, "%02d:%02d", minutes, seconds);
                    duration = String.format(Locale.US, "%02d:%02d", endMinutes, endSeconds);

                    videoCurrentLoc.setText(currentLoc);
                    videoDuration.setText(duration);

                    if (!isSeeking) {
                        playingPos = (int) (vlcVideoLibrary.getPlayer().getPosition() * 100);
                        mSeekBar.setProgress(playingPos);
                    }
                }

                handlerSeekBar.postDelayed(runnableSeekBar, 1000);
            } catch (Exception ignored) {

            }
        };
        runnableSeekBar.run();
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                _changePosition((float) mProgress);
            }
        });
    }

    private void _changePosition(float progress) {
        // progress
        if (vlcVideoLibrary.getPlayer() != null && vlcVideoLibrary.getPlayer().getTime() > 0 && progress > 0 && isSeeking) {
            vlcVideoLibrary.getPlayer().pause();
            vlcVideoLibrary.getPlayer().setPosition((progress / 100.0f));

            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            vlcVideoLibrary.getPlayer().play();
                        }
                    },
                    600
            );
        }

        isSeeking = false;
    }

    private void _handlerMediaControl() {
        // OVERLAY
        handlerOverlay = new Handler();
        runnableOverlay = () -> mediaPlayerControls.setVisibility(View.GONE);
        final long timeToDisappear = 3000;
        handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
        mediaPlayerView.setOnClickListener(view -> {
            if (!_hideControls) {
                mediaPlayerControls.setVisibility(View.VISIBLE);
            }

            handlerOverlay.removeCallbacks(runnableOverlay);
            handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
        });
    }

    /**
     * Resource ID
     *
     * @param name
     * @param type layout, drawable, id
     * @return
     */
    private int _getResource(String name, String type) {
        String package_name = getApplication().getPackageName();
        Resources resources = getApplication().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

    private void _sendBroadCast(String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    private void _sendBroadCast(String methodName, JSONObject object) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        intent.putExtra("data", object.toString());
        activity.sendBroadcast(intent);
    }
}
