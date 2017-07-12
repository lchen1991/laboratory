package com.laboratory.chenlei.module.media.music;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.laboratory.chenlei.R;
import com.laboratory.chenlei.module.media.music.model.MusicProvider;
import com.laboratory.chenlei.module.media.music.playback.LocalPlayback;
import com.laboratory.chenlei.module.media.music.playback.PlaybackManager;
import com.laboratory.chenlei.module.media.music.playback.QueueManager;
import com.laboratory.chenlei.utils.LogHelper;
import com.laboratory.chenlei.utils.MediaIDHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


 public class MusicService extends MediaBrowserServiceCompat implements
         PlaybackManager.PlaybackServiceCallback {

     private static final String TAG = LogHelper.makeLogTag(MusicService.class);

     // Extra on MediaSession that contains the Cast device name currently connected to
     public static final String EXTRA_CONNECTED_CAST = "com.laboratory.chenlei.CAST_NAME";
     // The action of the incoming Intent indicating that it contains a command
     // to be executed (see {@link #onStartCommand})
     public static final String ACTION_CMD = "com.laboratory.chenlei.ACTION_CMD";
     // The key in the extras of the incoming Intent indicating the command that
     // should be executed (see {@link #onStartCommand})
     public static final String CMD_NAME = "CMD_NAME";
     // A value of a CMD_NAME key in the extras of the incoming Intent that
     // indicates that the music playback should be paused (see {@link #onStartCommand})
     public static final String CMD_PAUSE = "CMD_PAUSE";
     // A value of a CMD_NAME key that indicates that the music playback should switch
     // to local playback from cast playback.
     public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
     // Delay stopSelf by using a handler.
     private static final int STOP_DELAY = 30000;

     private MusicProvider mMusicProvider;
     private PlaybackManager mPlaybackManager;

     private MediaSessionCompat mSession;
//     private MediaNotificationManager mMediaNotificationManager;
     private Bundle mSessionExtras;
     private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
//     private MediaRouter mMediaRouter;
//     private PackageValidator mPackageValidator;
//     private SessionManager mCastSessionManager;
//     private SessionManagerListener<CastSession> mCastSessionManagerListener;

//     private boolean mIsConnectedToCar;
//     private BroadcastReceiver mCarConnectionReceiver;

     /*
      * (non-Javadoc)
      * @see android.app.Service#onCreate()
      */
     @Override
     public void onCreate() {
         super.onCreate();
         LogHelper.d(TAG, "onCreate");

         mMusicProvider = new MusicProvider();

         // To make the app more responsive, fetch and cache catalog information now.
         // This can help improve the response time in the method
         // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
         mMusicProvider.retrieveMediaAsync(null /* Callback */);

//         mPackageValidator = new PackageValidator(this);

         QueueManager queueManager = new QueueManager(mMusicProvider, getResources(),
                 new QueueManager.MetadataUpdateListener() {
                     @Override
                     public void onMetadataChanged(MediaMetadataCompat metadata) {
                         mSession.setMetadata(metadata);
                     }

                     @Override
                     public void onMetadataRetrieveError() {
                         mPlaybackManager.updatePlaybackState(
                                 getString(R.string.error_no_metadata));
                     }

                     @Override
                     public void onCurrentQueueIndexUpdated(int queueIndex) {
                         mPlaybackManager.handlePlayRequest();
                     }

                     @Override
                     public void onQueueUpdated(String title,
                                                List<MediaSessionCompat.QueueItem> newQueue) {
                         mSession.setQueue(newQueue);
                         mSession.setQueueTitle(title);
                     }
                 });

         LocalPlayback playback = new LocalPlayback(this, mMusicProvider);
         mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager,
                 playback);

         // Start a new MediaSession
         mSession = new MediaSessionCompat(this, "MusicService");
         setSessionToken(mSession.getSessionToken());
         mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
         mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                 MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

         Context context = getApplicationContext();
//         Intent intent = new Intent(context, NowPlayingActivity.class);
//         PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
//                 intent, PendingIntent.FLAG_UPDATE_CURRENT);
//         mSession.setSessionActivity(pi);
//
//         mSessionExtras = new Bundle();
////         CarHelper.setSlotReservationFlags(mSessionExtras, true, true, true);
////         WearHelper.setSlotReservationFlags(mSessionExtras, true, true);
////         WearHelper.setUseBackgroundFromTheme(mSessionExtras, true);
//         mSession.setExtras(mSessionExtras);

         mPlaybackManager.updatePlaybackState(null);

//         try {
//             mMediaNotificationManager = new MediaNotificationManager(this);
//         } catch (RemoteException e) {
//             throw new IllegalStateException("Could not create a MediaNotificationManager", e);
//         }

//         int playServicesAvailable =
//                 GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//
//         if (!TvHelper.isTvUiMode(this) && playServicesAvailable == ConnectionResult.SUCCESS) {
//             mCastSessionManager = CastContext.getSharedInstance(this).getSessionManager();
//             mCastSessionManagerListener = new CastSessionManagerListener();
//             mCastSessionManager.addSessionManagerListener(mCastSessionManagerListener,
//                     CastSession.class);
//         }

//         mMediaRouter = MediaRouter.getInstance(getApplicationContext());

//         registerCarConnectionReceiver();
     }

     /**
      * (non-Javadoc)
      * @see android.app.Service#onStartCommand(Intent, int, int)
      */
     @Override
     public int onStartCommand(Intent startIntent, int flags, int startId) {
         if (startIntent != null) {
             String action = startIntent.getAction();
             String command = startIntent.getStringExtra(CMD_NAME);
             if (ACTION_CMD.equals(action)) {
                 if (CMD_PAUSE.equals(command)) {
                     mPlaybackManager.handlePauseRequest();
                 }
//                 else if (CMD_STOP_CASTING.equals(command)) {
//                     CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
//                 }
             } else {
                 // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                 MediaButtonReceiver.handleIntent(mSession, startIntent);
             }
         }
         // Reset the delay handler to enqueue a message to stop the service if
         // nothing is playing.
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
         return START_STICKY;
     }

     /**
      * (non-Javadoc)
      * @see android.app.Service#onDestroy()
      */
     @Override
     public void onDestroy() {
         LogHelper.d(TAG, "onDestroy");
//         unregisterCarConnectionReceiver();
         // Service is being killed, so make sure we release our resources
         mPlaybackManager.handleStopRequest(null);
//         mMediaNotificationManager.stopNotification();

//         if (mCastSessionManager != null) {
//             mCastSessionManager.removeSessionManagerListener(mCastSessionManagerListener,
//                     CastSession.class);
//         }

         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mSession.release();
     }

     @Override
     public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                  Bundle rootHints) {
         LogHelper.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName,
                 "; clientUid=" + clientUid + " ; rootHints=", rootHints);
         // To ensure you are not allowing any arbitrary app to browse your app's contents, you
         // need to check the origin:

//         if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
//             // If the request comes from an untrusted package, return an empty browser root.
//             // If you return null, then the media browser will not be able to connect and
//             // no further calls will be made to other media browsing methods.
//             LogHelper.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
//                     + "Returning empty browser root so all apps can use MediaController."
//                     + clientPackageName);
//             return new MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_EMPTY_ROOT, null);
//         }

         return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
     }

     @Override
     public void onLoadChildren(@NonNull final String parentMediaId,
                                @NonNull final Result<List<MediaItem>> result) {
         LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
         if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
             result.sendResult(new ArrayList<MediaItem>());
         } else if (mMusicProvider.isInitialized()) {
             // if music library is ready, return immediately
             result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
         } else {
             // otherwise, only return results when the music library is retrieved
             result.detach();
             mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                 @Override
                 public void onMusicCatalogReady(boolean success) {
                     result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                 }
             });
         }
     }

     /**
      * Callback method called from PlaybackManager whenever the music is about to play.
      */
     @Override
     public void onPlaybackStart() {
         mSession.setActive(true);

         mDelayedStopHandler.removeCallbacksAndMessages(null);

         // The service needs to continue running even after the bound client (usually a
         // MediaController) disconnects, otherwise the music playback will stop.
         // Calling startService(Intent) will keep the service running until it is explicitly killed.
         startService(new Intent(getApplicationContext(), MusicService.class));
     }


     /**
      * Callback method called from PlaybackManager whenever the music stops playing.
      */
     @Override
     public void onPlaybackStop() {
         mSession.setActive(false);
         // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
         // potentially stopping the service.
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
         stopForeground(true);
     }

     @Override
     public void onNotificationRequired() {
//         mMediaNotificationManager.startNotification();
     }

     @Override
     public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
         mSession.setPlaybackState(newState);
     }

     /**
      * A simple handler that stops the service if playback is not active (playing)
      */
     private static class DelayedStopHandler extends Handler {
         private final WeakReference<MusicService> mWeakReference;

         private DelayedStopHandler(MusicService service) {
             mWeakReference = new WeakReference<>(service);
         }

         @Override
         public void handleMessage(Message msg) {
             MusicService service = mWeakReference.get();
             if (service != null && service.mPlaybackManager.getPlayback() != null) {
                 if (service.mPlaybackManager.getPlayback().isPlaying()) {
                     LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.");
                     return;
                 }
                 LogHelper.d(TAG, "Stopping service with delay handler.");
                 service.stopSelf();
             }
         }
     }
 }
