package de.wikilab.android.friendica01;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Notification {
	
	public String href,name,url,photo,date,seen,content;
	
	public static Notification fromXmlNode(Node d) {
		Notification n = new Notification();
		
		n.href = d.getAttributes().getNamedItem("href").getNodeValue();
		n.name = d.getAttributes().getNamedItem("name").getNodeValue();
		n.url = d.getAttributes().getNamedItem("url").getNodeValue();
		n.photo = d.getAttributes().getNamedItem("photo").getNodeValue();
		n.date = d.getAttributes().getNamedItem("date").getNodeValue();
		n.seen = d.getAttributes().getNamedItem("seen").getNodeValue();
		
		n.content = d.getTextContent();
		return n;
	}
	
	public static class NotificationsListAdapter extends ArrayAdapter<Notification> {
		
		private static final String TAG = "friendica01.PostListAdapter";
		
		public NotificationsListAdapter(Context context, ArrayList<Notification> notifs) {
			super(context, R.layout.tl_listitem, notifs);
			
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				convertView = inf.inflate(R.layout.tl_listitem, null);
			}
			
			Notification post = (Notification) getItem(position);
	
			final ImageView profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
			profileImage.setImageResource(R.drawable.ic_launcher);
			try {
				String piurl = post.photo;
				Log.i(TAG, "Going to download profile img: " + piurl);
				final TwAjax pidl = new TwAjax(getContext(), true, false);
				
				//OLD: uncached, download every time, annoying when scrolling fast!
				/*pidl.getUrlBitmap(piurl, new Runnable() {
					@Override
					public void run() {
						
						profileImage.setImageBitmap(pidl.getBitmapResult());
					}
				});*/
				
				//NEW: download cached
				final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
				if (pifile.isFile()) {
					profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
				} else {
					pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
						@Override
						public void run() {
							profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
						}
					});
				}
			} catch (Exception e) {
			}
	
			TextView userName = (TextView) convertView.findViewById(R.id.userName);
			userName.setText(post.content);
	
			TextView htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
			htmlContent.setText(post.date);
			
			return convertView;
		}
		
		
	}
}