#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>

using namespace cv;
using namespace std;

class ImageFilters  
{
private:

    static ImageFilters* sInstance;
    Mat mSrcGray, mDstGray, mEdges, mEdgesGray, mSrcScaled, mDstScaled, mSketchTexture;
    int mStepNum = 4;
    float mStepProbArr[4] = { 0.1f, 0.2f, 0.2f, 0.5f };
    uchar mColorCartoonLevels[4] = {0, 30, 50, 100};
    uchar mGrayCartoonLevels[4] = {10, 50, 100, 255};
    uchar mStepValArr[4];
    double mScaleFactor;
    bool mSketchFlip = false;

    ImageFilters();
    static void getQuantizeSteps(Mat& src, int stepNum, float* stepProbArr, uchar* stepValArr);
    static void quantize(Mat& src, Mat& dst, uchar* stepValArr, uchar* dstValArr);

public:

    static ImageFilters* getInstance();
    void setScaleFactor(double scaleFactor);
    void setSketchTexture(Mat& texture);
    void setSketchFlip(bool flip);
    void applyColorCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold);
    void applyGrayCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold);
    void applyColorSketch(Mat& src, Mat& dst, int sketchBlend, int contrast);
    void applyPencilSketch(Mat& src, Mat& dst, int sketchBlend, int contrast);
    void applyPencilSketch2(Mat& src, Mat& dst, int blurRadius, int contrast);
    void applyOilPaint(Mat& src, Mat& dst, int radius, int levels);
    void applyPixelArt(Mat& src, Mat& dst, int pixelSize, int numColors);
};