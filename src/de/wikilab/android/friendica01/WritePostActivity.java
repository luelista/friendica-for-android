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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WritePostActivity extends Activity {

	ImageButton sendBtn;
	TextView viewLatLon;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.poststatus);
        
        viewLatLon = (TextView) findViewById(R.id.viewLatLon);
        
        sendBtn = (ImageButton) findViewById(R.id.btn_upload);
        sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
        
        
        ToggleButton sendLatLon = (ToggleButton) findViewById(R.id.sendLatLon);
        sendLatLon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
					viewLatLon.setText(getString(R.string.viewLatLon)+"\n"+"Loading...");
				}
			}
		});
	}
	
	private final LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        double longitude = location.getLongitude();
	        double latitude = location.getLatitude();
	        viewLatLon.setText(getString(R.string.viewLatLon)+"\n"+"Lat="+String.valueOf(latitude)+"  Long="+String.valueOf(longitude));
	    }

		@Override
		public void onProviderDisabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nDisabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nEnabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			viewLatLon.setText(getString(R.string.viewLatLon)+"\nStatus="+String.valueOf(status));
		}
	};
	
	private void sendMessage() {
		EditText txt_status = (EditText) findViewById(R.id.maintb);
		ToggleButton geo_en = (ToggleButton) findViewById(R.id.sendLatLon);
		
		final ProgressDialog pd = ProgressDialog.show(this, "Posting status...", "Please wait", true, false);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(this, true, true);
		t.addPostData("status", txt_status.getText().toString());
		t.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
		if (geo_en.isChecked()) {
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
			Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null) {
				Toast.makeText(this, "Unable to get location info - please try again.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				return;
			}
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();

			t.addPostData("lat", String.valueOf(latitude));
			t.addPostData("long", String.valueOf(longitude));
		}
		t.postData("http://" + server + "/api/statuses/update.json", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				finish();
			}
		});
	}
	
}
