package com.psgod.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UploadCrashRequest;


/**
 * 上传crash 信息
 * @author ZouMengyuan
 *
 */
public class UploadCrashService extends Service{
	
	private static final String TAG = UploadCrashService.class.getSimpleName();
	private String mCrashinfo;
	
	
	public static final String ACTION = "com.psgod.service.UploadCrashServiceT";

	private OnCrashEndListener onCrashEndListener;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new CrashBinder();
	}
	
	public class CrashBinder extends Binder {
		
		public void doThing(String crashInfo) {
			UploadCrashService.this.doThing(crashInfo);
		}
		
		public void setOnCrashEndListener(OnCrashEndListener onCrashListener) {
			UploadCrashService.this.onCrashEndListener = onCrashListener;
		}
		
	}
	
	public interface OnCrashEndListener{
		public void onCrashEnd(Boolean response);
	}
	
	public void doThing(String crashInfo) {
		
        //发送错误信息到服务器
        UploadCrashRequest.Builder builder = new UploadCrashRequest.Builder()
    		.setCrashInfo(crashInfo);
        UploadCrashRequest request = builder.setListener(new Listener<Boolean>() {
			@Override
			public void onResponse(Boolean response) {
				if(onCrashEndListener != null){
					onCrashEndListener.onCrashEnd(response);
				}
			}
		}).build();
        request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				this).getRequestQueue();
		requestQueue.add(request);
		
	}

    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) { 
		return super.onStartCommand(intent, flags, startId);  
    }  
}
