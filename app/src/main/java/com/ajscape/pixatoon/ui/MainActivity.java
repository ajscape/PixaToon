package com.ajscape.pixatoon.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ajscape.pixatoon.R;
import com.ajscape.pixatoon.lib.FilterManager;
import com.ajscape.pixatoon.lib.FilterType;
import com.ajscape.pixatoon.lib.Native;
import com.ajscape.pixatoon.ui.fragments.CameraViewerFragment;
import com.ajscape.pixatoon.ui.fragments.FilterConfigFragment;
import com.ajscape.pixatoon.ui.fragments.FilterSelectorFragment;
import com.ajscape.pixatoon.ui.fragments.PictureViewerFragment;
import com.ajscape.pixatoon.ui.interfaces.FilterConfigListener;
import com.ajscape.pixatoon.ui.interfaces.FilterPictureCallback;
import com.ajscape.pixatoon.ui.interfaces.FilterSelectorListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main Activity
 */
public class MainActivity extends Activity implements FilterSelectorListener, FilterConfigListener, View.OnClickListener {

    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private static final String TAG = "MainActivity";

    private static final int ORIENTATION_THRESH = 10;

    private CameraViewerFragment mCameraViewerFragment;
    private PictureViewerFragment mPictureViewerFragment;
    private FilterSelectorFragment mFilterSelectorFragment;
    private FilterConfigFragment mFilterConfigFragment;
    private ImageButton mOpenPictureBtn;
    private ImageButton mSelectFilterBtn;
    private ImageButton mOpenCameraBtn;
    private ImageButton mConfigFilterBtn;
    private ImageButton mCapturePictureBtn;

    private TextView mMsgTextView;
    private Handler mHandler;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private OrientationEventListener mOrientationListener;

    private FilterManager mFilterManager;

    // Statically Load native OpenCV and image filter implementation libraries
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("image_filters");
    }

    /**
     * Picture capture callback implementation
     */
    private FilterPictureCallback mPictureCallback = new FilterPictureCallback() {
        @Override
        public void onPictureCaptured(Bitmap pictureBitmap) {
            saveBitmap(pictureBitmap);
            displayMessage("Picture Saved");
        }
    };

    /**
     * OnCreate method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        checkAndRequestPermissions();

//      //  Hide navigation buttons and go full-screen, for devices without hardware navigation buttons
//      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_main);

        mFilterManager = FilterManager.getInstance();
        mCameraViewerFragment = new CameraViewerFragment();
        mPictureViewerFragment = new PictureViewerFragment();
        mFilterSelectorFragment = new FilterSelectorFragment();

        mOpenPictureBtn = (ImageButton) findViewById(R.id.openPictureBtn);
        mSelectFilterBtn = (ImageButton) findViewById(R.id.selectFilterBtn);
        mOpenCameraBtn = (ImageButton) findViewById(R.id.openCameraBtn);
        mConfigFilterBtn = (ImageButton) findViewById(R.id.configFilterBtn);
        mCapturePictureBtn = (ImageButton) findViewById(R.id.capturePictureBtn);

        mMsgTextView = (TextView) findViewById(R.id.msgTextView);
        mHandler = new Handler();

        // Register onClick listeners
        mOpenPictureBtn.setOnClickListener(this);
        mSelectFilterBtn.setOnClickListener(this);
        mOpenCameraBtn.setOnClickListener(this);
        mConfigFilterBtn.setOnClickListener(this);
        mCapturePictureBtn.setOnClickListener(this);
        findViewById(R.id.filterViewer).setOnClickListener(this);

        // Load sketch texture
        loadSketchTexture(getApplicationContext().getResources(),
                R.drawable.sketch_texture);

        // Set camera viewer as default
        getFragmentManager()
                .beginTransaction()
                .add(R.id.filterViewer, mCameraViewerFragment)
                .commit();

        /**
         * Device orientation listener implementation to appropriately rotate button and filter icons on orientation change
         */
        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                if(isOrientationLandscape(orientation) && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    mOrientation = Configuration.ORIENTATION_LANDSCAPE;
                    mCapturePictureBtn.setRotation(90);
                    mOpenPictureBtn.setRotation(90);
                    mOpenCameraBtn.setRotation(90);
                    mSelectFilterBtn.setRotation(90);
                    mConfigFilterBtn.setRotation(90);
                    mMsgTextView.setRotation(90);
                    if(mFilterSelectorFragment.isVisible())
                        mFilterSelectorFragment.changeOrientation(mOrientation);
                }
                else if(isOrientationPortrait(orientation) && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mOrientation = Configuration.ORIENTATION_PORTRAIT;
                    mCapturePictureBtn.setRotation(0);
                    mOpenPictureBtn.setRotation(0);
                    mOpenCameraBtn.setRotation(0);
                    mSelectFilterBtn.setRotation(0);
                    mConfigFilterBtn.setRotation(0);
                    mMsgTextView.setRotation(0);
                    if(mFilterSelectorFragment.isVisible())
                        mFilterSelectorFragment.changeOrientation(mOrientation);
                }
            }
        };
    }

    /**
     * OnResume method
     */
    @Override
    public void onResume() {
        super.onResume();
        mOrientationListener.enable();
    }

    /**
     * OnPause method
     */
    @Override
    public void onPause() {
        super.onPause();
        mOrientationListener.disable();
    }

    /**
     * OnClick method, triggered whenever click event happens on the registered UI views
     * @param v
     */
    @Override
    public void onClick(View v) {
        // Detect clicked view, and execute actions accordingly
        switch(v.getId()) {
            case R.id.openPictureBtn:
                closeCurrentFilterConfig();
                closeFilterSelector();
                openPicture();
                break;
            case R.id.selectFilterBtn:
                closeCurrentFilterConfig();
                if(!mFilterSelectorFragment.isVisible())
                    openFilterSelector();
                else
                    closeFilterSelector();
                break;
            case R.id.openCameraBtn:
                closeCurrentFilterConfig();
                closeFilterSelector();
                openCameraFilterViewer();
                break;
            case R.id.configFilterBtn:
                closeFilterSelector();
                if(!isFilterConfigVisible())
                    openCurrentFilterConfig();
                else
                    closeCurrentFilterConfig();
                break;
            case R.id.capturePictureBtn:
                if(mFilterManager.getCurrentFilter()!=null) {
                    if (mCameraViewerFragment.isVisible())
                        mCameraViewerFragment.capturePicture(mPictureCallback);
                    else
                        mPictureViewerFragment.capturePicture(mPictureCallback);
                    break;
                }
            case R.id.filterViewer:
                closeCurrentFilterConfig();
                closeFilterSelector();
        }
    }

    public void checkAndRequestPermissions() {
        if(Build.VERSION.SDK_INT > 22) {
            String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions,REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    finish();
                }
            }
        }
    }

    /**
     * Get device orientation (Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE)
     * @return
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * Switch to Gallery Picture View mode, and display picture from the given filepath
     * @param pictureFilePath
     */
    private void openPictureFilterViewer(String pictureFilePath) {
        mFilterManager.reset();
        if(!mPictureViewerFragment.isVisible()) {
            Bundle args = new Bundle();
            args.putString("pictureFilePath", pictureFilePath);
            mPictureViewerFragment.setArguments(args);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filterViewer, mPictureViewerFragment)
                    .commit();
            mCapturePictureBtn.setImageResource(R.drawable.icon_btn_save);
            mOpenCameraBtn.setImageResource(R.drawable.btn_opencamera);
        } else {
            mPictureViewerFragment.loadPicture(pictureFilePath);
        }
    }

    /**
     * Switch to Camera View mode
     */
    private void openCameraFilterViewer() {
        if(!mCameraViewerFragment.isVisible()) {

            mFilterManager.reset();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filterViewer, mCameraViewerFragment)
                    .commit();
            mCapturePictureBtn.setImageResource(R.drawable.icon_btn_camera);
            mOpenCameraBtn.setImageResource(R.drawable.btn_switchcamera);
        }
        else {
            boolean switched = mCameraViewerFragment.switchCamera();
            if(!switched) {
                displayMessage("Front camera not detected");
            }
        }
    }

    /**
     * Open picture from gallery, by using 'Select Picture' intent
     */
    private void openPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Callback for 'Select Picture' intent result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                // Get selected picture filepath
                String pictureFilePath = cursor.getString(columnIndex);
                cursor.close();
                Log.d(TAG, "Picture picked- " + pictureFilePath);

                // Switch to picture view mode, loading the selected picture
                openPictureFilterViewer(pictureFilePath);
            }
        }
    }

    /**
     * Open filter selector panel, to choose between different image filters
     */
    private void openFilterSelector() {
        if(!mFilterSelectorFragment.isVisible()) {
            mSelectFilterBtn.setImageResource(R.drawable.icon_btn_filters_on);

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.filterSelectorPanel, mFilterSelectorFragment)
                    .commit();

            Log.d(TAG, "filter selector opened");
        }
    }

    /**
     * Close filter selector panel, if opened
     */
    private void closeFilterSelector() {
        if(mFilterSelectorFragment.isVisible()) {
            mSelectFilterBtn.setImageResource(R.drawable.icon_btn_filters_off);
            getFragmentManager()
                    .beginTransaction()
                    .remove(mFilterSelectorFragment)
                    .commit();
            Log.d(TAG, "filter selector closed");
        }
    }

    /**
     * FilterSelectorListener callback implementation, triggered on selecting any new filter
     * @param filterType
     */
    @Override
    public void onFilterSelect(FilterType filterType) {
        if(mFilterManager.getCurrentFilter()==null || filterType != mFilterManager.getCurrentFilter().getType()) {
            mFilterManager.setCurrentFilter(filterType);
            Log.d(TAG, "current filter set to " + filterType.toString());
            if (mPictureViewerFragment.isVisible()) {
                mPictureViewerFragment.updatePicture();
            }
            // Display selected filter name as Toast
            displayMessage(filterType.toString());
        }
    }

    /**
     * Returns true if filter configuration panel is opened, else returns false
     * @return
     */
    private boolean isFilterConfigVisible() {
        if(mFilterConfigFragment!=null && mFilterConfigFragment.isVisible())
            return true;
        else
            return false;
    }

    /**
     * Open filter configuration panel with current filter specific settings
     */
    private void openCurrentFilterConfig() {
        if (mFilterManager.getCurrentFilter()!=null && !isFilterConfigVisible()) {

            mFilterConfigFragment = new FilterConfigFragment();
            mFilterConfigFragment.setFilter(mFilterManager.getCurrentFilter());

            mConfigFilterBtn.setImageResource(R.drawable.icon_btn_settings_on);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.filterConfigPanel, mFilterConfigFragment)
                    .commit();
            Log.d(TAG, "filter config opened");
        }
    }

    /**
     * Close filter configuration panel
     */
    private void closeCurrentFilterConfig() {
        if (isFilterConfigVisible()) {
            mConfigFilterBtn.setImageResource(R.drawable.icon_btn_settings_off);
            getFragmentManager()
                    .beginTransaction()
                    .remove(mFilterConfigFragment)
                    .commit();
            Log.d(TAG,"filter config closed");
        }
    }

    /**
     * FilterConfigListener callback implementation triggered whenever filter config sliders are moved
     */
    @Override
    public void onFilterConfigChanged() {
        if(mPictureViewerFragment.isVisible())
            mPictureViewerFragment.updatePicture();
    }

    /**
     * Save given bitmap to memory
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        try {
            String savedPicturePath = Utils.saveBitmap(this, bitmap);
            Log.d(TAG, "Saved picture at "+savedPicturePath);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: Unable to save picture", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display message text on screen for short interval
     * @param msg
     */
    private void displayMessage(String msg) {
        mMsgTextView.setText(msg);
        mMsgTextView.setAlpha(1);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMsgTextView.setAlpha(0);
                    }
                });
            }
        }, 1000);
    }

    /**
     * Returns true if device orientation is Landscape
     * @param orientation
     * @return
     */
    private boolean isOrientationLandscape(int orientation) {
        return orientation >= (270 - ORIENTATION_THRESH) && orientation <= (270 + ORIENTATION_THRESH);
    }

    /**
     * Returns true if deveice orientation is Portrait
     * @param orientation
     * @return
     */
    private boolean isOrientationPortrait(int orientation) {
        return (orientation >= (360 - ORIENTATION_THRESH) && orientation <= 360) || (orientation >= 0 && orientation <= ORIENTATION_THRESH);
    }

    /**
     * Load sketch texture resource and pass it to native image filters library
     * @param res
     * @param sketchTexRes
     */
    private void loadSketchTexture(Resources res, int sketchTexRes) {
        Mat mat, tempMat;
        Bitmap bmp = BitmapFactory.decodeResource(res, sketchTexRes);
        tempMat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        org.opencv.android.Utils.bitmapToMat(bmp, tempMat);
        mat = new Mat(tempMat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(tempMat, mat, Imgproc.COLOR_RGBA2GRAY);
        Native.setSketchTexture(mat.getNativeObjAddr());
    }
}

