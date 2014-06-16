package de.wikilab.android.friendica01.activity;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.android.gcm.GCMRegistrar;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.R.xml;

public class PreferencesActivity  extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	private static final String TAG="Friendica/PreferencesActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_main);
        PreferenceManager.setDefaultValues(PreferencesActivity.this, R.xml.prefs_main, false);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
          initSummary(getPreferenceScreen().getPreference(i));
        }
        
        Preference p2 = findPreference("proxy_info");
        
        ProxySelector defaultProxySelector = ProxySelector.getDefault();
        List<Proxy> proxyList = defaultProxySelector.select(URI.create(Max.getServer(this)));
        
        //String ProxyIp = Proxy.getHost(PreferencesActivity.this);
		//int ProxyPort = Proxy.getPort(PreferencesActivity.this);
        if (proxyList.size() == 0 || proxyList.get(0).address() == null) {
        	p2.setSummary("Zur Zeit wird kein Proxy verwendet.");
		} else {
			p2.setSummary("Der Proxy " +  proxyList.get(0).address().toString() + " wird verwendet.");
		}
		
        p2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
	          	startActivity(new Intent(Settings.ACTION_WIFI_IP_SETTINGS));
				return true;
			}
        });

        final CheckBoxPreference p_notifPull = (CheckBoxPreference) findPreference("notification_pullenable");
        final CheckBoxPreference p_notifPush = (CheckBoxPreference) findPreference("notification_pushenable");
        
        p_notifPull.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				restartTimer(newValue.equals(true));
				if (newValue.equals(true)) p_notifPush.setChecked(false);
				return true;
			}
		});
        
        
        p_notifPush.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(true)) {
					GCMRegistrar.register(PreferencesActivity.this, HomeActivity.SENDER_ID);
					p_notifPull.setChecked(false);
				} else {
					GCMRegistrar.unregister(PreferencesActivity.this);
				}
				return true;
			}
		});
        
        Preference p_notifReregister = findPreference("notification_pushreregister");
        p_notifReregister.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	@Override
        	public boolean onPreferenceClick(Preference preference) {
				GCMRegistrar.register(PreferencesActivity.this, HomeActivity.SENDER_ID);
        		return true;
        	}
		});
    }

    private void restartTimer(boolean runMode) {
    	Max.cancelTimer(this);
        if (runMode) {
        	Max.runTimer(this);
        }
    }

    @Override 
    protected void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override 
    protected void onPause() { 
        super.onPause();
        // Unregister the listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);     
    }


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		updatePrefSummary(findPreference(key));

	} 
	
	@Override
	protected void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
	};

    private void initSummary(Preference p){
       if (p instanceof PreferenceCategory){
            PreferenceCategory pCat = (PreferenceCategory)p;
            for(int i=0;i<pCat.getPreferenceCount();i++){
                initSummary(pCat.getPreference(i));
            }
        }else{
            updatePrefSummary(p);
        }

    }

    private void updatePrefSummary(Preference p){
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p; 
            p.setSummary(listPref.getEntry()); 
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            String txt = editTextPref.getText();
            if (p.getKey().equals("login_password") && txt != null) txt = repeatChar('*', txt.length());
            p.setSummary(txt);
        }

    }
    
    String repeatChar(char s, int c) {
    	char[] a = new char[c];
    	Arrays.fill(a, s);
    	return new String(a);
    }

}
