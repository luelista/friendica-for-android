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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WritePostFragment extends Fragment {

	Button sendBtn;
	TextView viewLatLon;
	
	private View myView;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.writepostinner, container, false);

        viewLatLon = (TextView) myView.findViewById(R.id.viewLatLon);
        
        sendBtn = (Button) myView.findViewById(R.id.btn_upload);
        sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
        
        
        ToggleButton sendLatLon = (ToggleButton) myView.findViewById(R.id.sendLatLon);
        sendLatLon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (!locationListenerAttached) {
						LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
						Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						
						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
						viewLatLon.setText(getString(R.string.viewLatLon)+"\n"+"Loading...");
						locationListenerAttached = true;
					}
				} else {
					detachLocationListener();
					
				}
			}
		});
		return myView;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		detachLocationListener();
	}
	
	void detachLocationListener() {
		if (locationListenerAttached) {
			LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(locationListener);
			this.locationListenerAttached = false;
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		((FragmentParentListener)activity).OnFragmentMessage("Set Header Text", getString(R.string.mm_updatemystatus), null);
	}
	
	private boolean locationListenerAttached;
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
		EditText txt_status = (EditText) myView.findViewById(R.id.maintb);
		ToggleButton geo_en = (ToggleButton) myView.findViewById(R.id.sendLatLon);
		
		final ProgressDialog pd = ProgressDialog.show(getActivity(), "Posting status...", "Please wait", true, false);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.addPostData("status", txt_status.getText().toString());
		t.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
		if (geo_en.isChecked()) {
			LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE); 
			Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null) {
				Toast.makeText(getActivity(), "Unable to get location info - please try again.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				return;
			}
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();

			t.addPostData("lat", String.valueOf(latitude));
			t.addPostData("long", String.valueOf(longitude));
		}
		t.postData(Max.getServer(getActivity()) + "/api/statuses/update.json", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				//getActivity().finish();
				((FragmentParentListener)getActivity()).OnFragmentMessage("Finished", null, null);
			}
		});
	}
	
}
