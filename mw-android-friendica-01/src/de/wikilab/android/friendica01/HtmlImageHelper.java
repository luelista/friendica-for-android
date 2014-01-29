package de.wikilab.android.friendica01;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Helper class to display html code with <img>-Tags into android TextView elements
 *
 * Created by mw on 12.01.14. (was subclass of Max.java)
 */

class HtmlImageHelper extends AsyncTask<Void, Object, Void> {

    private static final String TAG = "friendica01.HtmlImageHelper";

    DisplayMetrics metrics;
    Spannable htmlSpannable;
    Resources resources;
    TextView htmlTextView;
    TwAjax t;

    public static void setHtmlWithImages(TextView t, String html) {
        // first parse the html
        // replace getHtmlCode() with whatever generates/fetches your html
        Spanned spanned = Html.fromHtml(html);
        Spannable htmlSpannable;

        // we need a SpannableStringBuilder for later use
        if (spanned instanceof SpannableStringBuilder) {
            // for now Html.fromHtml() returns a SpannableStringBuiler
            // so we can just cast it
            htmlSpannable = (SpannableStringBuilder) spanned;
        } else {
            // but we have a fallback just in case this will change later
            // or a custom subclass of Html is used
            htmlSpannable = new SpannableStringBuilder(spanned);
        }

        // now we can call setText() on the next view.
        // this won't show any images yet
        t.setText(htmlSpannable);

        // next we start a AsyncTask that loads the images
        new HtmlImageHelper(t.getContext(), htmlSpannable, t).execute();

    }


    private HtmlImageHelper(Context c, Spannable htmlSpannablep, TextView htmlTextViewp) {
        htmlSpannable = htmlSpannablep;
        htmlTextView = htmlTextViewp;

        // we need this to properly scale the images later
        //c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        resources = c.getResources();
        metrics = resources.getDisplayMetrics();
        t= new TwAjax(c, true, false);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... params) {

        // iterate over all images found in the html
        for (ImageSpan img : htmlSpannable.getSpans(0,
                htmlSpannable.length(), ImageSpan.class)) {

            Log.d(TAG, "Loading: "+img.getSource());

            File cachedFile = getImageFile(img);
            if (!cachedFile.isFile()) {
                t.urlDownloadToFile(img.getSource(), cachedFile.getAbsolutePath(), null);
                Log.d(TAG, "Download done");
            }

            if (cachedFile.isFile()) {
                Drawable d = new BitmapDrawable(resources, cachedFile.getAbsolutePath());

                // we use publishProgress to run some code on the
                // UI thread to actually show the image
                // -> onProgressUpdate()
                publishProgress(img, d);
            }

        }

        return null;

    }

    @Override
    protected void onProgressUpdate(Object... values) {

        // save ImageSpan to a local variable just for convenience
        ImageSpan img = (ImageSpan) values[0];
        Drawable d = (Drawable) values[1];

        // now we get the File object again. so remeber to always return
        // the same file for the same ImageSpan object
        File cache = getImageFile(img);

        // if the file exists, show it
        //if (cache.isFile()) {

        Log.d(TAG, "File OK");

        // first we need to get a Drawable object
        //Drawable d = new BitmapDrawable(resources, cache.getAbsolutePath());

        // next we do some scaling
        int width, height;
        int originalWidthScaled = (int) (d.getIntrinsicWidth() * metrics.density);
        int originalHeightScaled = (int) (d.getIntrinsicHeight() * metrics.density);
        if (originalWidthScaled > metrics.widthPixels) {
            height = d.getIntrinsicHeight() * metrics.widthPixels
                    / d.getIntrinsicWidth();
            width = metrics.widthPixels;
        } else {
            height = originalHeightScaled;
            width = originalWidthScaled;
        }

        // it's important to call setBounds otherwise the image will
        // have a size of 0px * 0px and won't show at all
        d.setBounds(0, 0, width, height);

        // now we create a new ImageSpan
        ImageSpan newImg = new ImageSpan(d, img.getSource());

        // find the position of the old ImageSpan
        int start = htmlSpannable.getSpanStart(img);
        int end = htmlSpannable.getSpanEnd(img);

        // remove the old ImageSpan
        htmlSpannable.removeSpan(img);

        // add the new ImageSpan
        htmlSpannable.setSpan(newImg, start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //finally we have to update the TextView with our
        // updates Spannable to display the image
        htmlTextView.setText(htmlSpannable);
        //} else {
        //	Log.e(TAG, "File NOT FOUND = Download error");

        //}
    }

    private File getImageFile(ImageSpan img) {
        return new File(Max.IMG_CACHE_DIR + "/url_" + Max.cleanFilename(img.getSource()));
    }

}
