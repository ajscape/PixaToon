package com.ajscape.pixatoon.ui.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ajscape.pixatoon.R;
import com.ajscape.pixatoon.lib.Filter;
import com.ajscape.pixatoon.lib.FilterManager;
import com.ajscape.pixatoon.ui.Utils;
import com.ajscape.pixatoon.ui.interfaces.FilterPictureCallback;
import com.ajscape.pixatoon.ui.views.PictureSurfaceView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gallery picture viewer implemented as fragment
 */
public class PictureViewerFragment extends Fragment {

    private static final String TAG="PictureViewer:";
    private PictureSurfaceView mPictureView;
    private Bitmap mScaledInputBitmap, mScaledOutputBitmap;
    private Mat mScaledInputMat, mScaledOutputMat;
    private FilterManager mFilterManager;
    private boolean mInputRotated = false;
    private PictureUpdateThread mUpdateThread;
    private AtomicBoolean mPendingUpdate = new AtomicBoolean(false);

    public PictureViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_picture_viewer, container, false);
        mPictureView = (PictureSurfaceView)view.findViewById(R.id.pictureView);
        mFilterManager = FilterManager.getInstance();
        Log.d(TAG, "Picture fragment view created");
        Bundle args = getArguments();
        if(args != null && args.containsKey("pictureFilePath")) {
            Log.d(TAG,"Picture path argument passed...loading picture");
            loadPicture(args.getString("pictureFilePath"));
        } else {
            mPictureView.clearView();
        }
        return view;
    }

    @Override
    public void onPause() {
        // Terminate picture update thread
        if(mUpdateThread != null && mUpdateThread.isAlive()) {
            boolean retry;
            do {
                try {
                    mUpdateThread.join();
                    retry = false;
                    Log.d(TAG,"Update thread terminated");
                } catch (InterruptedException e) {
                    Log.d(TAG,"Error while terminating update thread...retrying");
                    retry = true;
                }
            } while (retry);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mScaledInputBitmap.recycle();
        mScaledInputBitmap = null;
        mScaledOutputBitmap.recycle();
        mScaledOutputBitmap = null;
        mScaledInputMat.release();
        mScaledOutputMat.release();
        Log.d(TAG, "Picture fragment view destroyed");
        super.onDestroy();
    }

    public void loadPicture(String pictureFilePath) {
        Log.d(TAG,"load picture from path="+pictureFilePath);

        // Load mat from filepath
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap inputBitmap = BitmapFactory.decodeFile(pictureFilePath, options);

        // Get dimensions of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // If input is landscape, rotate for better screen coverage
        if(inputBitmap.getWidth()>inputBitmap.getHeight()) {
            inputBitmap = Utils.rotateBitmap(inputBitmap, 90);
            mInputRotated = true;
            mFilterManager.setSketchFlip(true);
        } else {
            mInputRotated = false;
            mFilterManager.setSketchFlip(false);
        }

        // Get scaled bitmap and mat fit to screen, for preview filter display
        mScaledInputBitmap = Utils.resizeBitmap(inputBitmap, width, height);
        mScaledOutputBitmap = mScaledInputBitmap.copy(mScaledInputBitmap.getConfig(), true);

        if(mScaledInputMat != null)
            mScaledInputMat.release();
        mScaledInputMat = new Mat(mScaledInputBitmap.getHeight(), mScaledInputBitmap.getWidth(), CvType.CV_8UC4);

        if(mScaledOutputMat != null)
            mScaledOutputMat.release();
        mScaledOutputMat = new Mat(mScaledInputBitmap.getHeight(), mScaledInputBitmap.getWidth(), CvType.CV_8UC4);
        org.opencv.android.Utils.bitmapToMat(mScaledInputBitmap, mScaledInputMat);
        mScaledInputMat.copyTo(mScaledOutputMat);

        // Set view with scaled bitmap
        updatePicture();
    }

    public void updatePicture() {
        if(mUpdateThread == null || !mUpdateThread.isAlive()) {
            mPendingUpdate.set(false);
            mUpdateThread = new PictureUpdateThread();
            mUpdateThread.start();
        } else {
            mPendingUpdate.set(true);
        }
    }

    public void capturePicture(FilterPictureCallback pictureCallback) {
        Filter currentFilter = mFilterManager.getCurrentFilter();
        if(currentFilter != null) {
            mPictureView.setVisibility(View.INVISIBLE);
            Bitmap outputBitmap = mScaledOutputBitmap;
            if(mInputRotated)
                outputBitmap = Utils.rotateBitmap(outputBitmap, -90);
            pictureCallback.onPictureCaptured(outputBitmap);
            mPictureView.setVisibility(View.VISIBLE);
        }
    }

    class PictureUpdateThread extends Thread {

        @Override
        public void run() {
            do {
                mPendingUpdate.set(false);
                Filter currentFilter = mFilterManager.getCurrentFilter();
                if (currentFilter != null) {
                    if(mFilterManager.getFilterScaleFactor() < 1.0)
                        mFilterManager.setFilterScaleFactor(1.0);
                    currentFilter.process(mScaledInputMat, mScaledOutputMat);
                    org.opencv.android.Utils.matToBitmap(mScaledOutputMat, mScaledOutputBitmap);
                }
                mPictureView.setImageBitmap(mScaledOutputBitmap);
            }while(mPendingUpdate.get() == true);
        }
    }
}
