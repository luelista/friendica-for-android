package de.wikilab.android.friendica01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements FragmentParentListener, LoginListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.homeactivity);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, null);
		} else {
			Max.tryLogin(this);
		}
		
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {

		if (message.equals("Set Header Text")) {
			setHeadertext((String) arg1);
		}
		
		if (message.equals("Navigate Main Menu")) {
			
			if (arg1.equals("Timeline")) {
				navigatePostList("timeline");
			}
			
			if (arg1.equals("Notifications")) {
				navigatePostList("notifications");
			}

			if (arg1.equals("My Wall")) {
				navigatePostList("mywall");
			}
			
			if (arg1.equals("Preferences")) {
				startActivity(new Intent(HomeActivity.this, PreferencesActivity.class));
			}
			
			if (arg1.equals("Log Out")) {
				SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit();
				//prefs.putString("login_server", null); //keep server and user ...
				//prefs.putString("login_user", null);
				prefs.putString("login_password", null); //...only remove password
				prefs.commit();
				
				finish();
			}
			
		}
	}
	
	void setHeadertext(String ht) {
		TextView subheading = (TextView) findViewById(R.id.header_text);
		
		if (subheading != null) subheading.setText(ht);
	}
	
	private void navigatePostList(String listTarget) {
		Fragment viewer = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment);
		
	    if (viewer == null || !viewer.isInLayout()) {
	        Intent showContent = new Intent(getApplicationContext(), PostListActivity.class);
	        showContent.putExtra("target", listTarget);
	        startActivity(showContent);
	    } else {
	    	if (viewer instanceof PostListFragment) {
	    		((PostListFragment)viewer).navigateList(listTarget);
	    	} else {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		PostListFragment plfrag = new PostListFragment();
	    		t.replace(R.id.view_fragment, plfrag);
	    		t.commit();
	    		plfrag.navigateList(listTarget);
	    	}
	    }
	}
	
	
	@Override
	public void OnLogin() {
		LoginListener target = (LoginListener) getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		target.OnLogin();
		
	}
	
}
