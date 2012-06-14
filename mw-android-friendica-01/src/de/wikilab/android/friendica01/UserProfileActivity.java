package de.wikilab.android.friendica01;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

public class UserProfileActivity extends FragmentActivity implements FragmentParentListener {
	String userId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		userId= getIntent().getStringExtra("userId");
		
		setContentView(R.layout.userprofile);
		
		
		
		
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
					
				} catch (JSONException e) {
					e.printStackTrace();
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
		// TODO Auto-generated method stub
		
	}
	
}
