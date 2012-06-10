package de.wikilab.android.friendica01;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WritePostActivity extends FragmentActivity implements FragmentParentListener {
	TextView header_text;
	WritePostFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.writepost);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		header_text = (TextView) findViewById(R.id.header_text);
		header_text.setText(getString(R.string.mm_updatemystatus));
		
		frag = (WritePostFragment) getSupportFragmentManager ().findFragmentById(R.id.pl_fragment);
		
	}
	

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		if (message.equals("Set Header Text")) {
			//setHeadertext((String) arg1);
		}
		if (message.equals("Finished")) {
			finish();
		}
	}
	

}
