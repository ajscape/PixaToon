package com.ajscape.pixatoon.ui.fragments;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.ajscape.pixatoon.R;
import com.ajscape.pixatoon.lib.Filter;
import com.ajscape.pixatoon.lib.FilterManager;
import com.ajscape.pixatoon.ui.MainActivity;
import com.ajscape.pixatoon.ui.Utils;
import com.ajscape.pixatoon.ui.interfaces.FilterPictureCallback;
import com.ajscape.pixatoon.ui.views.OpenCvCameraView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraViewerFragment extends Fragment implements CvCameraViewListener2 {

    private static final String TAG = "CameraViewer:";
    private OpenCvCameraView mCameraView;
    private FilterManager mFilterManager;
    private Mat mInputMat, mOutputMat;
    private boolean mCapturing = false;
    private boolean mStarting = false;

    public CameraViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =  inflater.inflate(R.layout.fragment_camera_viewer, container, false);
        mCameraView = (OpenCvCameraView)view.findViewById(R.id.cameraView);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
        mFilterManager = FilterManager.getInstance();

        Log.d(TAG,"Camera fragment created");
        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mCameraView.enableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mInputMat = new Mat(height, width, CvType.CV_8UC4);
        mOutputMat = new Mat(height, width, CvType.CV_8UC4);
        mStarting = true;
    }

    @Override
    public void onCameraViewStopped() {
        mInputMat.release();
        mOutputMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mInputMat = inputFrame.rgba();

        if(mCapturing || mStarting) {
            mOutputMat.setTo(new Scalar(0));
            mCapturing = false;
            mStarting = false;
            return mOutputMat;
        }

        Filter currentFilter = mFilterManager.getCurrentFilter();
        if(currentFilter != null) {
            if (mFilterManager.getFilterScaleFactor() != currentFilter.getDefaultScaleFactor())
                mFilterManager.setFilterScaleFactor(currentFilter.getDefaultScaleFactor());
            currentFilter.process(mInputMat, mOutputMat);

            return mOutputMat;
        }
        return mInputMat;
    }

    public void capturePicture(FilterPictureCallback pictureCallback) {
        Log.d(TAG, "take picture called");
        mCapturing = true;

        mFilterManager.setFilterScaleFactor(1.0);
        mFilterManager.getCurrentFilter().process(mInputMat, mOutputMat);
        Bitmap outputBitmap = Bitmap.createBitmap(mOutputMat.width(), mOutputMat.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mOutputMat, outputBitmap);

        if (((MainActivity)getActivity()).getOrientation() == Configuration.ORIENTATION_LANDSCAPE)
            outputBitmap = Utils.rotateBitmap(outputBitmap,-90);
        pictureCallback.onPictureCaptured(outputBitmap);
    }
    
    public boolean switchCamera() {
        if(mCameraView.switchCamera()) {
            mCameraView.disableView();
            mCameraView.enableView();
            Log.d(TAG, "camera switch successful");
            return true;
        }
        else {
            Log.d(TAG, "Unable to switch camera");
            return false;
        }
    }
}
