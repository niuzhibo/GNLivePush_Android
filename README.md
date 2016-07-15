# 极牛Android 推流 SDK 使用手册



### 功能特性
☑AAC 音频编码
  
☑H.264 视频编码
  
☑多分辨率编码支持
  
☑摄像头控制（朝向,闪光灯,前后摄像头）

☑摄像头控制（可以调用原生的系统api）
  
☑动态设定音视频码率

☑视频分辨率：支持360P,480P,540P和720P
  
☑根据网络带宽自适应调整视频的码率
  
☑支持 RTMP 协议直播推流

	  
## 内容摘要
---
- [工程环境](#1)
	* [运行环境](#1.1)
	* [添加工程](#1.2)
	* [权限列表](#1.3)
- [SDK使用示例](#2)
    * [xml设置](#2.0)
    * [初始化](#2.1)
    * [配置参数](#2.2)
    * [推流操作](#2.4)
    * [推流状态](#2.5)
- [其他](#3)
    * [美颜](#3.1)
    * [流量获取](#3.2)
	
    
	



	  
## 工程环境<h2 id="1">
***

### 运行环境<h3 id = "1.1">

- 最低支持版本：Android 3.0
- 支持CPU架构：armeabi-v7a，armeabi-v8a,x86

### 添加工程<h3 id = "1.2">

- libs/gnpush1.0.0.jar


### 权限列表<h3 id = "1.3">
           <!-- 使用权限 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_SINTERNETWIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <!-- 硬件特性 -->
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />


## SDK使用示例<h2 id="2">

### xml设置<h3 id = "2.0">

      <android.opengl.GLSurfaceView
    android:id="@+id/camera_preview"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_alignParentTop="true" 
    android:layout_alignParentBottom="true"/>
    
    
### 初始化<h3 id = "2.1">

``` 
GnPushLive gnPushLive = GnPushLive.getInstance();
gnPushLive.init(this,"appkey","room_id");
```

### 参数配置<h3 id = "2.3">
 		
 		gnPushLive.setFrameRate(15);
        //设置最高码率，即目标码率
        gnPushLive.setMaxAverageVideoBitrate(800);
        //设置最低码率
        gnPushLive.setMinAverageVideoBitrate(800 * 2 / 8);
        //设置初始码率
        gnPushLive.setInitAverageVideoBitrate(800 * 5 / 8);
        gnPushLive.setAudioBitrate(32);
        gnPushLive.setVideoResolution(GnPushConstants.VIDEO_RESOLUTION_540P);
        gnPushLive.setEncodeMethod(GnPushConstants.SOFTWARE);
        gnPushLive.setSampleAudioRateInHz(44100);

        landscape = true;
        gnPushLive.setDefaultLandscape(landscape);
        if (landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        gnPushLive.setEnableStreamStatModule(true);
        gnPushLive.setFrontCameraMirror(false);

        gnPushLive.setConfig();
        gnPushLive.setDisplayPreview(mCameraPreview);
        gnPushLive.setOnStatusListener(mOnErrorListener);
        gnPushLive.setOnLogListener(mOnLogListener);
        gnPushLive.setOnAudioRawDataListener(mOnAudioRawDataListener);
        gnPushLive.enableDebugLog(true);
        gnPushLive.setMuteAudio(mute_audio);
        gnPushLive.setEnableEarMirror(earMirror);
        gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DENOISE);      


### 推流操作<h3 id = "2.4">
	
- 开始推流
    
        gnPushLive.startStream()
        
- 停止推流

        gnPushLive.stopStream() 
        
- 闪关灯 

		gnPushLive.toggleTorch(true);


*注意* ：只有后置摄像头才能开启闪关灯

- 摄像头

      gnPushLive.switchCamera();
      
- 其他方法

   在`Activity`的`onResume()`|`onPause()`|`onDestroy()`里面分别调用以下方法：
  
  - gnPushLive.onResume();
  - gnPushLive.onPause();
  - gnPushLive.onDestroy();  

       
       
		
		
		
### 推流状态<h3 id = "2.5">
          
          
        public OnStatusListener mOnErrorListener = new OnStatusListener() {.....}

        gnPushLive.setOnStatusListener(mOnErrorListener)//请参考demo
        
 - 状态码如下
 
 |        名称    	 |       数值      |       含义      |
|:------------------:|:----------:|:-------------------:|
|GNVIDEO_OPEN_STREAM_SUCC|0|推流成功|
|GNVIDEO_INIT_DONE|1000|首次开启预览完成初始化的通知,表示可以进行推流，默认整个GNPushLive生命周期只会回调一次|
|GNVIDEO_AUTH_FAILED|-1001|鉴权失败|
|GNVIDEO_ENCODED_FRAMES_THRESHOLD|-1002|鉴权失败后编码帧数达上限|
|GNVIDEO_ENCODED_FRAMES_FAILED|-1003|编码失败|
|GNVIDEO_CODEC_OPEN_FAILED|-1004|推流失败|
|GNVIDEO_CODEC_GUESS_FORMAT_FAILED|-1005|推流失败|
|GNVIDEO_OPEN_FILE_FAILED|-1006|推流失败|
|GNVIDEO_WRITE_FRAME_FAILED|-1007|推流过程中断网|
|GNVIDEO_OPEN_CAMERA_FAIL|-2001|打开摄像头失败|
|GNVIDEO_CAMERA_DISABLED|-2002|打开摄像头失败|
|GNVIDEO_NETWORK_NOT_GOOD|-3001|网络状况不佳|
|GNVIDEO_EST_BW_RAISE|-3002|码率开始上调的通知|
|GNVIDEO_EST_BW_DROP|-3003|码率开始下调的通知|   

## 其他<h2 id="3">    

### 美颜<h3 id = "3.1">

		gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DENOISE);//美颜
		
		gnPushLive.setBeautyFilter(GnPushConstants.FILTER_BEAUTY_DISABLE);//无美颜

	
### 流量获取<h3 id = "3.2">

    gnPushLive.getUploadedKBytes()



     

