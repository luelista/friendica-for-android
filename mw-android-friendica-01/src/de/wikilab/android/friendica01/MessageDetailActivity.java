package de.wikilab.android.friendica01;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

public class MessageDetailActivity extends Activity {

	String messageUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.msg_detail);
		
		Intent calling = getIntent();
		messageUri = calling.getStringExtra("message-uri");
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent(Max.getServer(this) + "/api/direct_messages?getText=html&uri=" + URLEncoder.encode(messageUri), new Runnable() {
			@Override public void run() {
				try {
					JSONObject msg = ((JSONArray) t.getJsonResult()).getJSONObject(0);
					ArrayList<String> list = new ArrayList<String>();

					((TextView) findViewById(R.id.subject)).setText(msg.getString("title"));
					((TextView) findViewById(R.id.userName)).setText(msg.getJSONObject("sender").getString("name") + " (" + msg.getString("sender_screen_name") + ")");
					((TextView) findViewById(R.id.userNameRecipient)).setText(msg.getJSONObject("recipient").getString("name") + " (" + msg.getString("recipient_screen_name") + ")");
					((WebView) findViewById(R.id.htmlContent)).loadDataWithBaseURL(Max.getServer(MessageDetailActivity.this), msg.getString("text"), "text/html", "utf-8", "");
					
					
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
}
