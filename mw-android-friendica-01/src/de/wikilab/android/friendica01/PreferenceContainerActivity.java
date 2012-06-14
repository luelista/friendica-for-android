/*package de.wikilab.android.friendica01;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class PreferenceContainerActivity extends ActivityGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.homeactivity);
		
	}
	

	public void test(Context c, ViewGroup mSomeContainer) {

		LocalActivityManager mgr = getLocalActivityManager();

		Intent i = new Intent(c, PreferencesActivity.class);

		Window w = mgr.startActivity("unique_per_activity_string", i);
		View wd = w != null ? w.getDecorView() : null;

		if(wd != null) {
		    mSomeContainer.addView(wd);
		}
	}
	
	
	
	
	
}*/