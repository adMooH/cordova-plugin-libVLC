# cordova-plugin-libVLC
Cordova Plugin using libVLC

Tutorial to compile from source:
[https://wiki.videolan.org/LibVLC](https://wiki.videolan.org/LibVLC)

# Install
Latest stable version from npm:
```sh
$ cordova plugin add https://github.com/admooh/cordova-plugin-libVLC.git
```
Bleeding edge version from Github:
```sh
$ cordova plugin add https://github.com/admooh/cordova-plugin-libVLC
```

# Using the plugin
```sh
// returns {
	event_name: '',
	data: null
}
// events names: onPlayVlc, onPauseVlc, onStopVlc, onVideoEnd, onDestroyVlc, onError, getPosition, onPositionAndSizeChanged
// options: {autoPlay: true, hideControls: false, fullscreen: true,top: 0, left: number: 0 ,height: 0} 
// path-to-video: rtsp://170.93.143.139/rtplive/470011e600ef003a004ee33696235daa
libVLCPlayer.play('path-to-video', [options], [success], [failed]);
libVLCPlayer.stop([success], [failed]);
```

# Methods
```sh
libVLCPlayer.pause([success], [failed]);
libVLCPlayer.playNext('path-to-next-video', [options], [success], [failed]);
libVLCPlayer.stop([success], [failed]);
libVLCPlayer.close([success], [failed]);

//change VLC Player Position and Size on the screen
// options: { fullscreen: true, top: 0, left: number: 0, height: 0}
libVLCPlayer.changePositionAndSize(options,[success], [failed]);

// returns: {position, current_location (00:00), duration (00:00)}
libVLCPlayer.getPosition([success], [failed]);

// position must be 1 to 100 only
libVLCPlayer.seekPosition(1, [success], [failed]);
```

# License
Cordova Plugin using libVLC is licensed under the Apache License (ASL) license. For more information, see the LICENSE file in this repository.
