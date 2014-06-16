package de.wikilab.android.friendica01;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

import de.wikilab.android.friendica01.activity.HomeActivity;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String TAG = "Friendica.GCMIntentService";
	
	public GCMIntentService() {
		super(HomeActivity.SENDER_ID);
		Log.i(TAG,"instance of GCMIntentService created");
	}
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e(TAG,"onError got called!!! "+arg1);
		Toast.makeText(arg0, arg1, Toast.LENGTH_LONG).show();
	}

	private void setNotificationProps(Intent i, Context ctx, Notification nb) {
		//String type = i.getStringExtra("cat")+"."+i.getStringExtra("type")+"."+i.getStringExtra("subtype");
		
		String contentTitle="",contentText="";
		
		//if (type.equals("USER.SHORTMESSAGE.")) {
		//	contentTitle=i.getStringExtra("actor_fullname") + " messaged you:"; contentText=i.getStringExtra("text");
		//} else if (type.equals("USER.POKE.")) {
		//	contentTitle=i.getStringExtra("actor_fullname") + " poked you"; contentText=null;
		//} else {
		//	contentTitle=i.getStringExtra("actor_fullname"); contentText=type;
		//}
		contentTitle = i.getStringExtra("name");

		contentText = "New Friendica Notification (" +  i.getStringExtra("otype") + ")";
		
		Intent notificationIntent = new Intent(ctx, HomeActivity.class);
		//for(String key : arg1.getExtras().keySet()) {
		//	notificationIntent.putExtra("MSG_" + key, arg1.getStringExtra(key));
		//}
		//notificationIntent.setFlags(Intent.FL)
		PendingIntent contentIntent = PendingIntent.getActivity(ctx,
				(int) (System.currentTimeMillis()/1000), notificationIntent, 0);
		//PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, null, 0);
		nb.setLatestEventInfo(ctx, contentTitle, contentText, contentIntent);
	}
	
	@Override
	protected void onMessage(Context ctx, Intent arg1) {
		Log.i(TAG, "Message received");
		Toast.makeText(ctx, "Test!!!", Toast.LENGTH_LONG).show();
		
		try {
			NotificationManager noti = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			Notification not = new Notification();
			//.setContentTitle(arg1.getStringExtra("cat")+"."+arg1.getStringExtra("type")+"."+arg1.getStringExtra("subtype"))
			//.setContentText(arg1.getStringExtra("actor_fullname"))
			setNotificationProps(arg1, ctx, not);
			not.vibrate = new long[]{0,200,100,400};
			not.icon = android.R.drawable.ic_popup_reminder;
			//.setAutoCancel(true)
			not.sound = Uri.parse("android.resource://de.wikilab.android.friendica01/" + R.raw.doorbell2);
			not.when = System.currentTimeMillis();
			
			noti.notify((int) (System.currentTimeMillis()/1000), not);
			
		} catch (Exception e) {
			Log.w(TAG, "exception in Message-receiver");
			e.printStackTrace();
		}
	}

	@Override
	protected void onRegistered(Context arg0, String registration_id) {
		Log.i(TAG, "onRegistered got called! :-)");
		Toast.makeText(arg0, "GCM Registration successful", Toast.LENGTH_SHORT).show();
		TwAjax regsvr = new TwAjax(arg0, true, true);
		regsvr.addPostData("registration_id", registration_id);
		regsvr.postData(Max.getServer(arg0) + "/api/gcm/register", null);
		Log.i(TAG, "gcmRegister returned "+regsvr.getResult());
	}

	@Override
	protected void onUnregistered(Context arg0, String registration_id) {
		Toast.makeText(arg0, "Unregistered from GCM", Toast.LENGTH_SHORT).show();
		TwAjax regsvr = new TwAjax(arg0, true, true);
		regsvr.addPostData("registration_id", registration_id);
		regsvr.postData(Max.getServer(arg0) + "/api/gcm/unregister", null);
		Log.i(TAG, "gcmUnregister returned "+regsvr.getResult());
	}

}
