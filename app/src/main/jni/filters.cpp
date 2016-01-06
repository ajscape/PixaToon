#include "filters.h"

#define INPUT_MAX 100

#define CARTOON_THICK_MIN 3
#define CARTOON_THICK_MAX 51
#define CARTOON_THRESH_MIN 1
#define CARTOON_THRESH_MAX 10

#define SKETCH_BLEND_MIN 0.5
#define SKETCH_BLEND_MAX 1.0 
#define SKETCH_TEXSCALE_MIN 1.0
#define SKETCH_TEXSCALE_MAX 5.0

#define SKETCH2_BLUR_MIN 3
#define SKETCH2_BLUR_MAX 51
#define SKETCH2_CONTRAST_MIN 1.0
#define SKETCH2_CONTRAST_MAX 10.0 

#define OILPAINT_RADIUS_MIN 1
#define OILPAINT_RADIUS_MAX 10
#define OILPAINT_LEVELS_MIN 5
#define OILPAINT_LEVELS_MAX 30

#define PIXEL_SIZE_MIN 2
#define PIXEL_SIZE_MAX 30
#define PIXEL_COLORS_MIN 2
#define PIXEL_COLORS_MAX 20

#define CONVERT_RANGE(param, rangeMin, rangeMax)    (param*(rangeMax-rangeMin)/INPUT_MAX + rangeMin)
#define COLOR_DODGE_BLEND(srcPixel, blendPixel)     ((blendPixel >= 255)? 255:(min(255, (srcPixel*255)/(255-blendPixel) )))
#define LINEAR_DODGE_BLEND(srcPixel, blendPixel)    (min(255, srcPixel + blendPixel))
#define SCREEN_BLEND(srcPixel, blendPixel)          (255 - min(255, (255-srcPixel)*(255-blendPixel)/255))
#define LIGHTEN_BLEND(srcPixel, blendPixel)         ((srcPixel > blendPixel) ? srcPixel: blendPixel)


/* Singleton instance initialization */
ImageFilters* ImageFilters::sInstance = NULL;


/* Private Constructor to allow only single private instance */
ImageFilters::ImageFilters() 
{
    mScaleFactor = 1.0;
}


/* Static method to get singleton instance */
ImageFilters* ImageFilters::getInstance()
{
    if(sInstance == NULL) 
        sInstance = new ImageFilters();    
    return sInstance;
} 


/* Set common scale-factor for all filters*/
void ImageFilters::setScaleFactor(double scaleFactor)
{
    mScaleFactor = scaleFactor;
}


/* Set sketch texture for sketch-filters */
void ImageFilters::setSketchTexture(Mat& texture)
{
    mSketchTexture = texture;
    mSketchTexture = ~(1.2*~mSketchTexture);
}


/* Reverse sketch texture */
void ImageFilters::setSketchFlip(bool sketchFlip) {
    if(mSketchFlip != sketchFlip)
    {
        mSketchFlip = sketchFlip;
        flip(mSketchTexture, mSketchTexture, 0);
    }
}


/* Color-Cartoon Filter Imaplementation */
void ImageFilters::applyColorCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold) 
{	
	edgeThickness = CONVERT_RANGE(edgeThickness, CARTOON_THICK_MIN, CARTOON_THICK_MAX);
	edgeThreshold = CONVERT_RANGE(edgeThreshold, CARTOON_THRESH_MIN, CARTOON_THRESH_MAX);
	
    edgeThickness *= mScaleFactor;
    if(edgeThickness%2 == 0) edgeThickness++;

    if(edgeThickness < CARTOON_THICK_MIN)
        edgeThickness = CARTOON_THICK_MIN;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    GaussianBlur(mSrcScaled, mSrcScaled, Size(5,5), 0);
    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
    
    getQuantizeSteps(mSrcGray, mStepNum, mStepProbArr, mStepValArr);
    quantize(mSrcGray, mDstGray, mStepValArr, mColorCartoonLevels);
	cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);
    mDstScaled = 0.7*mSrcScaled + 0.7*mDstScaled;
    
    adaptiveThreshold(mSrcGray, mEdgesGray, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, edgeThickness, edgeThreshold);
    cvtColor(mEdgesGray, mEdges, CV_GRAY2RGBA);
    mDstScaled = mDstScaled - ~mEdges;

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Gray-Cartoon Filter Implementation */
void ImageFilters::applyGrayCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold) 
{
    edgeThickness = CONVERT_RANGE(edgeThickness, CARTOON_THICK_MIN, CARTOON_THICK_MAX);
    edgeThreshold = CONVERT_RANGE(edgeThreshold, CARTOON_THRESH_MIN, CARTOON_THRESH_MAX);
    
    edgeThickness *= mScaleFactor;
    if(edgeThickness%2 == 0) edgeThickness++;

    if(edgeThickness < CARTOON_THICK_MIN)
        edgeThickness = CARTOON_THICK_MIN;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    GaussianBlur(mSrcScaled, mSrcScaled, Size(5,5), 0);
    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
    
    getQuantizeSteps(mSrcGray, mStepNum, mStepProbArr, mStepValArr);
    quantize(mSrcGray, mDstGray, mStepValArr, mGrayCartoonLevels);
    cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);
    
    adaptiveThreshold(mSrcGray, mEdgesGray, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, edgeThickness, edgeThreshold);
    cvtColor(mEdgesGray, mEdges, CV_GRAY2RGBA);
    mDstScaled = mDstScaled - ~mEdges;

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Color-Sketch Filter Implementation */
void ImageFilters::applyColorSketch(Mat& src, Mat& dst, int blend, int textureScale) 
{
    float blend1 = CONVERT_RANGE(blend, SKETCH_BLEND_MIN, SKETCH_BLEND_MAX);
    float textureScale1 = CONVERT_RANGE(textureScale, SKETCH_TEXSCALE_MIN, SKETCH_TEXSCALE_MAX);
    textureScale1 /= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    for(int i=0; i< mSrcScaled.rows; i++) 
        for(int j=0; j< mSrcScaled.cols; j++) {

            Vec4b srcPixel, dstPixel;
            srcPixel = mSrcScaled.at<Vec4b>(i,j);

            int texRow = (int)(i*textureScale1) % mSketchTexture.rows;
            int texCol = (int)(j*textureScale1) % mSketchTexture.cols;
            uchar texPixel = mSketchTexture.at<uchar>(texRow, texCol)*blend1;

            dstPixel.val[0] = COLOR_DODGE_BLEND(srcPixel.val[0], texPixel);
            dstPixel.val[1] = COLOR_DODGE_BLEND(srcPixel.val[1], texPixel);
            dstPixel.val[2] = COLOR_DODGE_BLEND(srcPixel.val[2], texPixel);
            dstPixel.val[3] = srcPixel.val[3];

            mDstScaled.at<Vec4b>(i,j) = dstPixel;
        }

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Pencil-Sketch Filter Implementation */
void ImageFilters::applyPencilSketch(Mat& src, Mat& dst, int blend, int textureScale) 
{
    float blend1 = CONVERT_RANGE(blend, SKETCH_BLEND_MIN, SKETCH_BLEND_MAX);
    float textureScale1 = CONVERT_RANGE(textureScale, SKETCH_TEXSCALE_MIN, SKETCH_TEXSCALE_MAX);
    textureScale1 /= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);

    if(mDstGray.size()!=mSrcGray.size() || mDstGray.type()!=mSrcGray.type())
        mDstGray.create(mSrcGray.size(), mSrcGray.type());

    for(int i=0; i< mSrcGray.rows; i++) 
        for(int j=0; j< mSrcGray.cols; j++) {     

            uchar srcPixel, dstPixel, texPixel;
            srcPixel = mSrcGray.at<uchar>(i,j);
            
            int texRow = (int)(i*textureScale1) % mSketchTexture.rows;
            int texCol = (int)(j*textureScale1) % mSketchTexture.cols;
            texPixel = mSketchTexture.at<uchar>(texRow, texCol)*blend1;

            dstPixel = COLOR_DODGE_BLEND(srcPixel, texPixel);

            mDstGray.at<uchar>(i,j) = dstPixel;
        }
    cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


// /* Pencil-Sketch2 Filter Implementation */
// void ImageFilters::applyPencilSketch2(Mat& src, Mat& dst, int blurRadius, int contrast)
// {
// 	blurRadius = CONVERT_RANGE(blurRadius, SKETCH2_BLUR_MIN, SKETCH2_BLUR_MAX);
// 	float contrast1 = CONVERT_RANGE(contrast, SKETCH_CONTRAST_MIN, SKETCH_CONTRAST_MAX);

//     blurRadius *= mScaleFactor;
//     if(blurRadius%2 == 0) blurRadius++;

//     if(blurRadius < SKETCH2_BLUR_MIN)
//         blurRadius = SKETCH2_BLUR_MIN;
	
//     resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);
//     cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
//     GaussianBlur(mSrcGray, mSrcGray, Size(3,3), 0);
//     GaussianBlur(~mSrcGray, mDstGray, Size(blurRadius,blurRadius), 0);

//     for(int i=0; i< mSrcGray.rows; i++) 
//         for(int j=0; j< mSrcGray.cols; j++) {     

//             uchar srcPixel, dstPixel, blendPixel;
//             srcPixel = mSrcGray.at<uchar>(i,j);
//             blendPixel = mDstGray.at<uchar>(i,j);

//             dstPixel = COLOR_DODGE_BLEND(srcPixel, blendPixel);

//             mDstGray.at<uchar>(i,j) = dstPixel;
//         }

//     mDstGray = ~(contrast1*(~mDstGray));
// 	cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);

//     resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
// }


/* Pixel-Art Filter Implementation */
void ImageFilters::applyPixelArt(Mat& src, Mat& dst, int pixelSize, int numColors) 
{
    pixelSize = CONVERT_RANGE(pixelSize, PIXEL_SIZE_MIN, PIXEL_SIZE_MAX);
    numColors = CONVERT_RANGE(numColors, PIXEL_COLORS_MIN, PIXEL_COLORS_MAX);

    pixelSize *= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    for(int i=0; i<mSrcScaled.rows; i+=pixelSize)
        for(int j=0; j<mSrcScaled.cols; j+=pixelSize) 
        {
            int sumRed=0, sumGreen=0, sumBlue=0, sumPix=0;
            for(int pi=i; pi<i+pixelSize; pi++)
                if(pi < mSrcScaled.rows) 
                    for(int pj=j; pj<j+pixelSize; pj++)
                        if(pj < mSrcScaled.cols)
                        {
                            Vec4b srcPixel = mSrcScaled.at<Vec4b>(pi,pj);
                            sumRed += srcPixel.val[0];
                            sumGreen += srcPixel.val[1];
                            sumBlue += srcPixel.val[2];
                            sumPix++;
                        }

            Vec4b dstPixel;
            dstPixel.val[0] = sumRed/sumPix;
            dstPixel.val[1] = sumGreen/sumPix;
            dstPixel.val[2] = sumBlue/sumPix;
            dstPixel.val[3] = 255;

            for(int pi=i; pi<i+pixelSize; pi++)
                if(pi < mSrcScaled.rows) 
                    for(int pj=j; pj<j+pixelSize; pj++)
                    if(pj < mSrcScaled.cols)
                        mDstScaled.at<Vec4b>(pi,pj) = dstPixel;
        }

    uchar qsteps[numColors];
    for(int i=0; i<numColors; i++)
        qsteps[i] = 255*(i+1)/numColors;

    //uchar qsteps[] = {30, 60, 90, 120, 150, 180, 210, 240, 255};
    //uchar qvals[]  = { 0, 45, 75, 105, 135, 165, 195, 225, 255};
    quantize(mDstScaled, mDstScaled, qsteps, qsteps);

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Oil-Paint Filter Implementation */
void ImageFilters::applyOilPaint(Mat& src, Mat& dst, int radius, int levels) 
{	
	radius = CONVERT_RANGE(radius, OILPAINT_RADIUS_MIN, OILPAINT_RADIUS_MAX);
	levels = CONVERT_RANGE(levels, OILPAINT_LEVELS_MIN, OILPAINT_LEVELS_MAX);
	
    int intensityHist[levels], totalRed[levels], totalGreen[levels], totalBlue[levels];
	
    radius *= mScaleFactor;
    if(radius == 0) radius=1;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    mSrcScaled = mSrcScaled*0.8+30;

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    for(int row=0; row<mSrcScaled.rows; ++row) 
    {
        for(int i=0; i<levels; i++)
            intensityHist[i] = totalRed[i] = totalGreen[i] = totalBlue[i] = 0;
    
		Vec4b *dstRowPtr = mDstScaled.ptr<Vec4b>(row);
        int winRowMin = ((row-radius) < 0) ? 0:(row-radius);
        int winRowMax = ((row+radius) >= mSrcScaled.rows) ? (mSrcScaled.rows-1):(row+radius);

        for(int winRow=winRowMin; winRow<=winRowMax; ++winRow) 
        {
            Vec4b *srcRowPtr = mSrcScaled.ptr<Vec4b>(winRow);
            for(int winCol=0; winCol<radius; ++winCol) 
            {
                Vec4b pix = srcRowPtr[winCol];
                int red = (int)pix.val[0];
                int green = (int)pix.val[1];
                int blue = (int)pix.val[2];

                int level = ((red+green+blue)*levels)/768;
                intensityHist[level]++;
                totalRed[level] += red;
                totalGreen[level] += green;
                totalBlue[level] += blue;
            }
        }
		
        for(int col=0; col<mSrcScaled.cols; ++col)
        {
            for(int winRow=winRowMin; winRow<=winRowMax; ++winRow) 
            {
                Vec4b *srcRowPtr = mSrcScaled.ptr<Vec4b>(winRow);

                if(col-radius >= 0) {
                    Vec4b pix = srcRowPtr[col-radius];
                    int red = (int)pix.val[0];
                    int green = (int)pix.val[1];
                    int blue = (int)pix.val[2];

                    int level = ((red+green+blue)*levels)/768;
                    intensityHist[level]--;
                    totalRed[level] -= red;
                    totalGreen[level] -= green;
                    totalBlue[level] -= blue;
                }

                if(col+radius < mSrcScaled.cols) {
                    Vec4b pix = srcRowPtr[col+radius];
                    int red = (int)pix.val[0];
                    int green = (int)pix.val[1];
                    int blue = (int)pix.val[2];

                    int level = ((red+green+blue)*levels)/768;
                    intensityHist[level]++;
                    totalRed[level] += red;
                    totalGreen[level] += green;
                    totalBlue[level] += blue;
                }
            }

            Vec4b dstPix;
            int maxLevel, maxIntensity = 0;
            for(int i=0; i<levels; i++)
                if(intensityHist[i]>maxIntensity) 
                {
                    maxIntensity = intensityHist[i];
                    maxLevel = i;
                }

            dstPix.val[0] = (uchar)(totalRed[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[1] = (uchar)(totalGreen[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[2] = (uchar)(totalBlue[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[3] = 255;
            dstRowPtr[col] = dstPix;
        }
    }
    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Get Quantization step levels based on given step probabilities */
void ImageFilters::getQuantizeSteps(Mat& src, int stepNum, float* stepProbArr, uchar* stepValArr) {
    Mat hist;
    int histSize = 256;

    float range[] = { 0, 256 } ;
    const float* histRange = { range };

    calcHist(&src, 1, 0, Mat(), hist, 1, &histSize, &histRange, true, false);

    float sumHist = 0.0f;
    for(int i=0; i<histSize; i++)
        sumHist += hist.at<float>(i,0);
    hist = hist/sumHist;

    float stepProb = 0.0f;
    int stepIndex = 0;
    for(int i=0; i<histSize; i++) {
        stepProb += hist.at<float>(i,0);
        if(stepProb >= stepProbArr[stepIndex]) {
            stepValArr[stepIndex++]=i;
            stepProb = 0.0f;
        }
    }
    for(int i=stepIndex; i<stepNum; i++)
        stepValArr[i] = 255;
}


/* Quantize the src image into dst, using given step boundaries and dst pixel values */
void ImageFilters::quantize(Mat& src, Mat& dst, uchar* stepValArr, uchar* dstValArr) 
{
    uchar buffer[256];
    int j=0;
    for(int i=0; i!=256; ++i) {
        if(i > stepValArr[j])
            j++;
        buffer[i] = dstValArr[j];
    } 
    Mat table(1, 256, CV_8U, buffer, sizeof(buffer));
    LUT(src, table, dst);
}