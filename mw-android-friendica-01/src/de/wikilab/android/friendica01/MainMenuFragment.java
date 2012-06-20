package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainMenuFragment extends Fragment implements LoginListener {
	private static final String TAG="Friendica/MainMenuFragment";
	
	
	View mainView;
	
	ListView lvw;

	private int selectedItemIndex=-1;
	
	public final ArrayList<String> MainList = new ArrayList<String>();
	
	
	private void appendNumber(ArrayList<String> listWithNotifications, int index, String number) {
		listWithNotifications.set(index, listWithNotifications.get(index) + " <b>[<font color=red>" + number + "</font>]</b>");
	}
	
	public void UpdateList() {
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlXmlDocument(Max.getServer(getActivity()) + "/ping", new Runnable() {
		//t.getUrlContent("http://" + server + "/ping", new Runnable() {
			@Override
			public void run() {
				ArrayList<String> listWithNotifications = (ArrayList<String>) MainList.clone();

				Document xd = t.getXmlDocumentResult();
				
				try {
					appendNumber(listWithNotifications, 0, xd.getElementsByTagName("net").item(0).getTextContent());
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 4, xd.getElementsByTagName("intro").item(0).getTextContent() + " intros");
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 2, xd.getElementsByTagName("home").item(0).getTextContent());
				} catch (Exception ingoreException) {}
				
				try {
					appendNumber(listWithNotifications, 1, xd.getElementsByTagName("notif").item(0).getAttributes().getNamedItem("count").getNodeValue());
				} catch (Exception ingoreException) {}
				
				lvw.setAdapter(new HtmlStringArrayAdapter(getActivity(), R.layout.mainmenuitem, android.R.id.text1, listWithNotifications));
				if (selectedItemIndex>-1)((HtmlStringArrayAdapter)lvw.getAdapter()).setSelectedItemIndex(selectedItemIndex);
			}
		});
		//lvw.setAdapter(new HtmlStringArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, MainList));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("selectedItemIndex")) {
				selectedItemIndex=savedInstanceState.getInt("selectedItemIndex");
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("selectedItemIndex", selectedItemIndex);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mainView = inflater.inflate(R.layout.mainmenu, container);
		
		lvw = (ListView) mainView.findViewById(R.id.listview);
		lvw.setAdapter(new HtmlStringArrayAdapter(getActivity(), R.layout.mainmenuitem, android.R.id.text1, MainList));
		if (selectedItemIndex>-1)((HtmlStringArrayAdapter)lvw.getAdapter()).setSelectedItemIndex(selectedItemIndex);
		
		MainList.add(getString(R.string.mm_timeline));
		MainList.add(getString(R.string.mm_notifications));
		MainList.add(getString(R.string.mm_mywall));
		MainList.add(getString(R.string.mm_myphotoalbums));
		MainList.add(getString(R.string.mm_friends));
		// MainList.add(getString(R.string.mm_friendrequests));
		MainList.add(getString(R.string.mm_updatemystatus));
		MainList.add(getString(R.string.mm_takephoto));
		MainList.add(getString(R.string.mm_selectphoto));
		MainList.add(getString(R.string.mm_preferences));
		MainList.add(getString(R.string.mm_logout));

		return mainView;
	}


	
	@Override
	public void OnLogin() {
		UpdateList();
		
		lvw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				((FragmentParentListener)getActivity()).OnFragmentMessage("Navigate Main Menu", MainList.get(index), null);
				((HtmlStringArrayAdapter)lvw.getAdapter()).setSelectedItemIndex(index);
				selectedItemIndex=index;
			}
		});
	}
	
	

}
