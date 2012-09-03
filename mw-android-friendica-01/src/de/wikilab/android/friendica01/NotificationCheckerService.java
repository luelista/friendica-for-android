package de.wikilab.android.friendica01;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationCheckerService extends Service {
	private static final String TAG="Friendica/NotificationCheckerService";
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.e(TAG, "Friendica timer fired");
		Toast.makeText(this, "Friendica timer fired", Toast.LENGTH_LONG).show();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
