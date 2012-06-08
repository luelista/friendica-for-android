package de.wikilab.android.friendica01;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


public class FileUploadService extends IntentService {
	
	/**
	 * Clipboard ID to upload to.
	 */
	public static final String EXTRA_CLIPBOARDID = "net.teamwiki.clip.FileUploadService.cbId";

	/**
	 * Delete the source file after successful upload.
	 */
	public static final String EXTRA_DELETE = "net.teamwiki.clip.FileUploadService.deleteAfterUpload";

	/**
	 * Target file name
	 */
	public static final String EXTRA_DESCTEXT = "net.teamwiki.clip.FileUploadService.EXTRA_DESCTEXT";
	
	
	private static final int UPLOAD_SUCCESS_ID = 1;
	private static final int UPLOAD_FAILED_ID = 2;
	private static final int UPLOAD_PROGRESS_ID = 2;
	
	int clipId, cbId;
	String descText;
	boolean deleteAfterUpload;
	Uri fileToUpload;
	String targetFilename;

	public FileUploadService() {
		super("Andfrnd_FileUploadService");
		Log.i("=== UPLOAD SERVICE ===", "on New()");
		// TODO Auto-generated constructor stub
	}

	public FileUploadService(String name) {
		super(name);
		Log.i("=== UPLOAD SERVICE ===", "on New(String)");
		// TODO Auto-generated constructor stub
	}

	private void showFailMsg(Context ctx, String txt) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Log.e("Andfrnd/UploadFile", "Upload FAILED: " + txt);
		
		//Instantiate the Notification:
		CharSequence tickerText = "Upload failed, please retry!";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		//Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		CharSequence contentTitle = "Upload failed, click to retry!";
		CharSequence contentText = txt;
		Intent notificationIntent = new Intent(this, FriendicaImgUploadActivity.class);
		Bundle b = new Bundle();
		b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
		b.putString(EXTRA_DESCTEXT, descText);
		
		notificationIntent.putExtras(b);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		//Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_FAILED_ID, notification);
		//Toast.makeText(ctx, "Upload failed, please retry:\n" + txt, Toast.LENGTH_LONG).show();
	}

	private void showSuccessMsg(Context ctx) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//Instantiate the Notification:
		int icon = R.drawable.arrow_up;
		CharSequence tickerText = "Upload succeeded!";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		/*
		//TODO!!!
		//Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		CharSequence contentTitle = "Upload successful, click to show details!";
		CharSequence contentText = targetFilename;
		Intent notificationIntent = new Intent(this, FilePreview.class);
		Bundle b = new Bundle();
		b.putInt("cid", clipId);
		notificationIntent.putExtras(b);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		 */
		
		//Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, tickerText, "Click to dismiss", nullIntent);
		
		
		//Pass the Notification to the NotificationManager:

		mNotificationManager.notify(UPLOAD_SUCCESS_ID, notification);
		//Toast.makeText(ctx, "Upload failed, please retry:\n" + txt, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("Andfrnd/UploadFile", "onHandleIntent exec");
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		//Instantiate the Notification:
		CharSequence tickerText = "Uploading...";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		//Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, "Upload in progress...", "You are notified here when it completes", nullIntent);
		
		//Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_PROGRESS_ID, notification);
		/*
		final TwLogin login = new TwLogin();
		login.initialize(FileUploadService.this);
		login.doLogin(FileUploadService.this, null, false);
		
		if (!login.isLoginOK()) {
			showFailMsg(FileUploadService.this, "Invalid login data or no network connection");
			return;
		}
		*/
		Bundle intentPara = intent.getExtras();
		fileToUpload = (Uri) intentPara.getParcelable(Intent.EXTRA_STREAM);
		descText = intentPara.getString(EXTRA_DESCTEXT);
		
		if (targetFilename == null || targetFilename.equals("")) targetFilename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt";
		
		String fileSpec = Max.getRealPathFromURI(FileUploadService.this, fileToUpload);
		

		String tempFile = Max.IMG_CACHE_DIR + "/imgUploadTemp_" + System.currentTimeMillis() + ".jpg";
		Max.resizeImage(fileSpec, tempFile, 1024,768);
		
		try {
			

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String server = prefs.getString("login_server", null);
			
			
			Log.i("Andfrnd/UploadFile", "before uploadFile");
			final TwAjax uploader = new TwAjax(FileUploadService.this, true, true);
			uploader.addPostFile(new TwAjax.PostFile("media", targetFilename, tempFile));
			uploader.addPostData("status", descText);
			uploader.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
			uploader.uploadFile("http://"+server+"/api/statuses/update", null);
			Log.i("Andfrnd/UploadFile", "after uploadFile");
			Log.i("Andfrnd/UploadFile", "isSuccess() = " + uploader.isSuccess() );
			Log.i("Andfrnd/UploadFile", "getError() = " + uploader.getError() );
			
			mNotificationManager.cancel(UPLOAD_PROGRESS_ID);
			if (uploader.isSuccess() && uploader.getError() == null) {
				JSONObject result = null;
				try {
					Log.i("Andfrnd/UploadFile","JSON RESULT: "+uploader.getHttpCode());
					result = (JSONObject) uploader.getJsonResult();
					
					String postedText = result.getString("text");
					showSuccessMsg(FileUploadService.this);
					
				} catch (Exception e) {
					String errMes = e.getMessage()+" | "+uploader.getResult();
					if (result != null) try {errMes = result.getString("error");} catch (JSONException fuuuuJava) {}
					
					showFailMsg(FileUploadService.this, errMes);
					
					e.printStackTrace();
				}
			} else if (uploader.getError() != null) {
				showFailMsg(FileUploadService.this, uploader.getError().toString());
			} else {
				showFailMsg(FileUploadService.this, uploader.getResult());
			}
			
		} finally {
			new File(tempFile).delete();
		}
	}

}
