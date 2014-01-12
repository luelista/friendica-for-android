package de.wikilab.android.friendica01.adapter;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;
import de.wikilab.android.friendica01.R.drawable;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageContentAdapter extends ArrayAdapter<JSONObject> {
	private static final String TAG="Friendica/MessageContentAdapter";
	
	protected static class ViewHolder {
		int Type;
		ImageView profileImage;
		TextView subject, userName, htmlContent, dateTime;
	}
	
	
	public MessageContentAdapter(Context context, List<JSONObject> objects) {
		super(context, R.layout.msg_contentitem, objects);
		
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	@Override
	public long getItemId(int position) {
		try {
			return ((JSONObject) getItem(position)).getLong("id");
		} catch (JSONException e) {
			Log.e(TAG, "Item without ID!");
			return 0;
		}
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder H;
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			H = new ViewHolder();
			convertView = inf.inflate(R.layout.msg_contentitem, null);
			//H.subject = (TextView) convertView.findViewById(R.id.subject);
			H.userName = (TextView) convertView.findViewById(R.id.userName);
			H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
			H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
		
		
			H.htmlContent.setFocusable(true);
			H.htmlContent.setMovementMethod(LinkMovementMethod.getInstance());

			H.dateTime = (TextView) convertView.findViewById(R.id.date);
			
			
			convertView.setTag(H);
		} else {
			H = (ViewHolder) convertView.getTag();
		}
		
		JSONObject post = (JSONObject) getItem(position);
		
		if (H.profileImage != null) {
			H.profileImage.setImageResource(R.drawable.ic_launcher);
			try {
				final String piurl = post.getString("sender_profile_img");
				Log.i(TAG, "TRY Download profile img: " + piurl);
				final TwAjax pidl = new TwAjax(getContext(), true, false);
				pidl.ignoreSSLCerts = true;
				
				//NEW: download cached
				final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
				if (pifile.isFile()) {
					Log.i(TAG, "OK  Load cached profile Img: " + piurl);
					//profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
					H.profileImage.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
				} else {
					pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "OK  Download profile Img: " + piurl);
							H.profileImage.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
						}
					});
				}
			} catch (JSONException e) {
			}
		}
		
		if (H.userName != null) {
			try {
				String appendix = "";
				H.userName.setText(post.getString("sender_screen_name") + " --> " + post.getString("recipient_screen_name") );
			} catch (Exception e) {
				H.userName.setText("Invalid Dataset!");
			}
		}
		
		if (H.dateTime != null) {
			try {
				H.dateTime.setText(DateUtils.getRelativeDateTimeString(parent.getContext(), 
						java.util.Date.parse(post.getString("created_at")),
						DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR));
				
			} catch (Exception e) {
				H.dateTime.setText("Invalid Dataset!");
			}
		}
		
		try {
			
			//Max.setHtmlWithImages(H.htmlContent, post.getString("statusnet_html"));
			String filtered_html = post.getString("text");
			if (filtered_html.length()>500) filtered_html=filtered_html.substring(0,500)+"<br><br><b>click to read more...</b>";
			filtered_html = filtered_html.replace("\n", "<br>");
			
			Spanned spanned = Html.fromHtml(filtered_html);
			H.htmlContent.setText(spanned);
			//H.subject.setText(post.getString("title"));
			
		} catch (JSONException e) {
			H.htmlContent.setText("Invalid Dataset!");
		}
		
		return convertView;
	}
	
	
}
