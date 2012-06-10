package de.wikilab.android.friendica01;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private Pic[] mPics ;
    public static class Pic {
    	public String url,caption,data1,data2;
    	public Pic(String purl, String pcaption, String pdata1, String pdata2) { url=purl; caption=pcaption; data1=pdata1; data2=pdata2; }
    }
    
    public PhotoGalleryAdapter(Context c, Pic[] pics) {
        mContext = c; mPics = pics;
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
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        
        final TwAjax t = new TwAjax(mContext, true, false);
        t.getUrlBitmap(mPics[position].url, new Runnable() {
			@Override
			public void run() {
		        imageView.setImageBitmap(t.getBitmapResult());
			}
		});
        return imageView;
    }

}