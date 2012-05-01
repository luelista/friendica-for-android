package de.wikilab.android.friendica01;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Max {

	//warum muss das in Android so kompliziert sein???
	public static void resizeImage(String pathOfInputImage, String pathOfOutputImage, int dstWidth, int dstHeight) {
		try
		{
		    int inWidth = 0;
		    int inHeight = 0;

		    InputStream in = new FileInputStream(pathOfInputImage);

		    // decode image size (decode metadata only, not the whole image)
		    BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(in, null, options);
		    in.close();
		    in = null;

		    // save width and height
		    inWidth = options.outWidth;
		    inHeight = options.outHeight;

		    // decode full image pre-resized
		    in = new FileInputStream(pathOfInputImage);
		    options = new BitmapFactory.Options();
		    // calc rought re-size (this is no exact resize)
		    options.inSampleSize = Math.max(inWidth/dstWidth, inHeight/dstHeight);
		    // decode full image
		    Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

		    // calc exact destination size
		    Matrix m = new Matrix();
		    RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
		    RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
		    m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
		    float[] values = new float[9];
		    m.getValues(values);

		    // resize bitmap
		    Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);

		    // save image
		    try
		    {
		        FileOutputStream out = new FileOutputStream(pathOfOutputImage);
		        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
		    }
		    catch (Exception e)
		    {
		        Log.e("Image", e.getMessage(), e);
		    }
		}
		catch (IOException e)
		{
		    Log.e("Image", e.getMessage(), e);
		}
	}
	
	
	
	/** 
     * Copy the content of the input stream into the output stream, using a temporary 
     * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}. 
     * 
     * @param in The input stream to copy from. 
     * @param out The output stream to copy to. 
     * 
     * @throws IOException If any error occurs during the copy. 
     */  
	public static void copy(InputStream in, OutputStream out) throws IOException {  
		final int IO_BUFFER_SIZE = 4 * 1024;  
		byte[] b = new byte[IO_BUFFER_SIZE];  
		int read;
		while ((read = in.read(b)) != -1) {  
			out.write(b, 0, read);
		}
	} 
	

	public static String getBaseName(String fileSpec) {
		if (fileSpec == null) return null;
		int pos = fileSpec.lastIndexOf("/");
		if (pos == -1) return fileSpec; else return fileSpec.substring(pos+1);
	}
	public static String getBaseNameWithoutExtension(String fileSpec) {
		if (fileSpec == null) return null;
		return getBaseName(fileSpec).replaceFirst("[.][^.]+$", "");
	}
	public static String getFileExtension(String fileSpec) {
		if (fileSpec == null) return null;
		int pos = fileSpec.lastIndexOf(".");
		if (pos == -1) return fileSpec; else return fileSpec.substring(pos+1);
	}
    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
    	Log.i("FileTypes", "URI = "+contentUri + ", scheme = "+contentUri.getScheme());
    	if (contentUri.getScheme().equals("file")) {
    		return contentUri.getPath();
    	} else {
	        String[] proj = { MediaStore.Images.Media.DATA };
	        Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        String res= cursor.getString(column_index);
	        cursor.close();
	        return res;
    	}
    }

}
