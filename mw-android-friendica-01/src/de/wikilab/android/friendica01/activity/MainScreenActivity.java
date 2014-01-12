package de.wikilab.android.friendica01.activity;
/*package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainScreenActivity extends Activity implements LoginListener {

	private static final String TAG = "friendica01.MainScreenActivity"; 
			
	ListView lvw;
	
	static final ArrayList<String> MainList = new ArrayList<String>();
	
	{
		MainList.add("Timeline");
		MainList.add("Notifications");
		MainList.add("My Wall");
		MainList.add("Update My Status");
		MainList.add("Friends");
		MainList.add("Friend Requests");
		MainList.add("My Photo Albums");
		MainList.add("Take Photo And Upload");
		MainList.add("Select Photo And Upload");
		MainList.add("Preferences");
		MainList.add("Log Out");
	}
	
	private void appendNumber(ArrayList<String> listWithNotifications, int index, String number) {
		listWithNotifications.set(index, listWithNotifications.get(index) + " <b>[<font color=red>" + number + "</font>]</b>");
	}
	
	public void UpdateList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlXmlDocument("http://" + server + "/ping", new Runnable() {
		//t.getUrlContent("http://" + server + "/ping", new Runnable() {
			@Override
			public void run() {
				ArrayList<String> listWithNotifications = (ArrayList<String>) MainList.clone();

				Document xd = t.getXmlDocumentResult();
				
				try {
					appendNumber(listWithNotifications, 0, xd.getElementsByTagName("net").item(0).getTextContent());
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 5, xd.getElementsByTagName("intro").item(0).getTextContent());
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 2, xd.getElementsByTagName("home").item(0).getTextContent());
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 1, xd.getElementsByTagName("notif").item(0).getAttributes().getNamedItem("count").getNodeValue());
				} catch (Exception ingoreException) {}
				
				lvw.setAdapter(new HtmlStringArrayAdapter(MainScreenActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, listWithNotifications));
				
			}
		});
		//lvw.setAdapter(new HtmlStringArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, MainList));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Max.initDataDirs();
		
		setContentView(R.layout.mainscreen);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, null);
		} else {
			Max.tryLogin(this);
		}
        
	}

	@Override
	public void OnLogin() {

		lvw = (ListView) findViewById(R.id.listview);
		lvw.setAdapter(new HtmlStringArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, MainList));
		
		UpdateList();
		
		lvw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {

				if (MainList.get(index).equals("Timeline")) {
					startActivity(new Intent(MainScreenActivity.this, TimelineActivity.class));
				}

				if (MainList.get(index).equals("Notifications")) {
					startActivity(new Intent(MainScreenActivity.this, NotificationsActivity.class));
				}

				if (MainList.get(index).equals("My Wall")) {
					startActivity(new Intent(MainScreenActivity.this, PostListActivity.class));
				}

				if (MainList.get(index).equals("Update My Status")) {
					startActivity(new Intent(MainScreenActivity.this, WritePostActivity.class));
				}
				
				if (MainList.get(index).equals("Preferences")) {
					startActivity(new Intent(MainScreenActivity.this, PreferencesActivity.class));
				}
				
				if (MainList.get(index).equals("Log Out")) {
					SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(MainScreenActivity.this).edit();
					//prefs.putString("login_server", null); //keep server and user ...
					//prefs.putString("login_user", null);
					prefs.putString("login_password", null); //...only remove password
					prefs.commit();
					
					finish();
				}
				
				
			}
		});
	}
	
}
*/