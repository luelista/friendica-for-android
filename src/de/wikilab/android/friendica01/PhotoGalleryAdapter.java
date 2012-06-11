package de.wikilab.android.friendica01;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private Pic[] mPics ;
    private int viewId;
    
    public static class Pic {
    	public String url,caption,data1,data2;
    	public Pic(String purl, String pcaption, String pdata1, String pdata2) { url=purl; caption=pcaption; data1=pdata1; data2=pdata2; }
    }
    
    public PhotoGalleryAdapter(Context c, int pviewId, Pic[] pics) {
        mContext = c; mPics = pics; viewId = pviewId;
    }

    public int getCount() {
        return mPics.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = inf.inflate(viewId, null);
        	//imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(145, 145));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
        }
        imageView = (ImageView) convertView.findViewById(R.id.profile_image);
        
        ((TextView) convertView.findViewById(R.id.caption)).setText(mPics[position].caption);
        
        final TwAjax t = new TwAjax(mContext, true, false);
        
		//NEW: download cached
		final File cachefile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(mPics[position].url));
		if (cachefile.isFile()) {
			imageView.setImageURI(Uri.parse("file://" + cachefile.getAbsolutePath()));
		} else {
			imageView.setImageBitmap(null);
			t.urlDownloadToFile(mPics[position].url, cachefile.getAbsolutePath(), new Runnable() {
				@Override
				public void run() {
					imageView.setImageURI(Uri.parse("file://" + cachefile.getAbsolutePath()));
				}
			});
		}
		
        return convertView;
    }

}