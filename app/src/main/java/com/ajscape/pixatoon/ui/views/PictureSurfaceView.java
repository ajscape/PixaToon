package com.ajscape.pixatoon.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ajscape.pixatoon.ui.Utils;

/**
 * Picture View implementation using SurfaceView, for rendering gallery pictures with filter effects
 */
public class PictureSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG="PictureView";
    private Bitmap mBitmap;
    private boolean isSurfaceAvailable = false;

    public PictureSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public PictureSurfaceView(Context context, AttributeSet attr) {
        super(context,attr);
        getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface Created");
        isSurfaceAvailable = true;
        if(mBitmap != null)
            drawBitmap();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"Surface Changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"Surface Destroyed");
        isSurfaceAvailable = false;
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if(isSurfaceAvailable)
            drawBitmap();
    }

    private void drawBitmap() {
        Canvas canvas = getHolder().lockCanvas();
        if(canvas != null) {
            synchronized (getHolder()) {
                if(canvas.getWidth()<mBitmap.getWidth() || canvas.getHeight()<mBitmap.getHeight()) {
                    Log.w(TAG,"Bitmap size larger than canvas, resizing");
                    mBitmap = Utils.resizeBitmap(mBitmap, canvas.getWidth(), canvas.getHeight());
                }
                int top = (canvas.getHeight() - mBitmap.getHeight())/2;
                int left = (canvas.getWidth() - mBitmap.getWidth())/2;
                canvas.drawBitmap(mBitmap, left, top, null);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void clearView() {
        Canvas canvas = getHolder().lockCanvas();
        if(canvas != null) {
            canvas.drawColor( 0, PorterDuff.Mode.CLEAR );
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
}