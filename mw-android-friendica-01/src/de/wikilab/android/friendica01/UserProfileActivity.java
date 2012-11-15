package de.wikilab.android.friendica01;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserProfileActivity extends FragmentActivity implements FragmentParentListener {
	private static final String TAG="Friendica/UserProfileActivity";
	
	String userId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		userId= getIntent().getStringExtra("userId");
		Log.i(TAG, "loading profile for userId="+userId);
		
		setContentView(R.layout.userprofile);
		
		
		Button btn;
		btn = (Button) findViewById(R.id.btn_nav_1);
		if (btn != null) {
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					findViewById(R.id.content_fragment_1).setVisibility(View.VISIBLE);
					findViewById(R.id.content_fragment_2).setVisibility(View.GONE);
				}
			});
			btn = (Button) findViewById(R.id.btn_nav_2);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					findViewById(R.id.content_fragment_1).setVisibility(View.GONE);
					findViewById(R.id.content_fragment_2).setVisibility(View.VISIBLE);
				}
			});
		}
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent(Max.getServer(this) + "/api/users/show/" + userId, new Runnable() {
			@Override public void run() {

				try {
					JSONObject j = (JSONObject) t.getJsonResult();
					
					((TextView) findViewById(R.id.profile_name)).setText(Html.fromHtml("<b>" + j.getString("name") + "</b><br>" + j.getString("screen_name")));
					((TextView) findViewById(R.id.header_text)).setText(j.getString("name") + "'s profile");
					
					((ImageView)findViewById(R.id.profile_image)).setImageURI(Uri.parse("file://"+Max.IMG_CACHE_DIR + "/friend_pi_" + j.getString("id") + "_.jpg"));
					
					String key,tx = ""; Iterator<String> iter = j.keys();
					while(iter.hasNext()) {
						key=iter.next(); tx+="<br><b>"+key+":</b> "+String.valueOf(j.get(key));
					}
					
					((TextView) findViewById(R.id.profile_content)).setText(Html.fromHtml(tx));
					
				} catch (Exception e) {

					((TextView) findViewById(R.id.profile_content)).setText(Html.fromHtml("<b>Error loading profile data!</b><br><br>"+e.toString()+"<br><br>"+Max.Hexdump(t.getResult().getBytes())));
					
				}
				
				
			}
		});
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		
		if (fragment instanceof PostListFragment) ((ContentFragment)fragment).navigate("userwall:" + userId);
		
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		
		if (message.equals("Navigate Conversation")) {
			Intent in = new Intent(this, GenericContentActivity.class);
			in.putExtra("target", "conversation:" + arg1);
			startActivity(in);
		}
	}
	
}
