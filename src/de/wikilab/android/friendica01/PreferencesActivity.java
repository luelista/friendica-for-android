package de.wikilab.android.friendica01;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class PreferencesActivity  extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_main);
        PreferenceManager.setDefaultValues(PreferencesActivity.this, R.xml.prefs_main, false);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
          initSummary(getPreferenceScreen().getPreference(i));
        }
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
        
        Preference p2 = findPreference("proxy_info");
        
        ProxySelector defaultProxySelector = ProxySelector.getDefault();
        List<Proxy> proxyList = defaultProxySelector.select(URI.create("http://" + server));
        
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
