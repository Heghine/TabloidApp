package com.science.inc.tabloid.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.science.inc.tabloid.manager.TabloidAppManager;

import android.R;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;

public class Util {
	
	
	public static Bitmap getRoundedCornerBitmap(final Bitmap source, final int radius) {
	    final Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
	    final Canvas canvas = new Canvas(output);
	    final BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

	    final Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    paint.setShader(shader);

	    // rect contains the bounds of the shape
	    // radius is the radius in pixels of the rounded corners
	    // paint contains the shader that will texture the shape
	    final RectF rect = new RectF(0.0f, 0.0f, source.getWidth(), source.getHeight());
	    canvas.drawRoundRect(rect, radius, radius, paint);

	    return output;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromStream(InputStream res, String url,  int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    Rect r = new Rect();
	    BitmapFactory.decodeStream(res, r, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    
	    try {
	    	res.close();
			res = (InputStream) new URL(url).getContent();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(res, r, options);
	}
	
	public static StateListDrawable createStateListDrawable(int size) {
	    StateListDrawable stateListDrawable = new StateListDrawable();

	    OvalShape ovalShape = new OvalShape();
	    ovalShape.resize(size, size);
	    ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
	    shapeDrawable.getPaint().setColor(TabloidAppManager.getInstance().mainActivity.getResources().getColor(R.color.black));

	    stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, shapeDrawable);
	    stateListDrawable.addState(new int[]{android.R.attr.state_focused}, shapeDrawable);
	    stateListDrawable.addState(new int[]{}, null);

	    return stateListDrawable;
	}
}
