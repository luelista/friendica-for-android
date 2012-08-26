package de.wikilab.android.friendica01;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<JSONObject> {
	private static final String TAG="Friendica/MessageListAdapter";
	SimpleDateFormat df2 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	protected static class ViewHolder {
		int Type;
		ImageView profileImage;
		TextView subject, userName, dateTime;
	}


	public MessageListAdapter(Context context, List<JSONObject> objects) {
		super(context, R.layout.msg_convlistitem, objects);

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
			convertView = inf.inflate(R.layout.msg_convlistitem, null);

			H.subject = (TextView) convertView.findViewById(R.id.subject);
			H.userName = (TextView) convertView.findViewById(R.id.userName);
			H.dateTime = (TextView) convertView.findViewById(R.id.date);
			H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);


			convertView.setTag(H);
		} else {
			H = (ViewHolder) convertView.getTag();
		}

		JSONObject post = (JSONObject) getItem(position);

		H.profileImage.setImageResource(R.drawable.ic_launcher);
		try {
			final String piurl = post.getString("thumb");
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

		try {
			H.userName.setText(post.getString("name"));
			H.subject.setText(post.getString("title"));
		} catch (Exception e) {
			H.userName.setText("xxInvalid Dataset!");
			H.subject.setText("yyInvalid Dataset!");
		}

		try {
			H.dateTime.setText(DateUtils.getRelativeDateTimeString(parent.getContext(), 
					df2.parse(post.getString("mailcreated")).getTime(),
					DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR));

		} catch (Exception e) {
			H.dateTime.setText("zzInvalid Dataset!");
		}


		return convertView;
	}


}
