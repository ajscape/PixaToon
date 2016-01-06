#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "filters.h"

extern "C" {

ImageFilters* filters = ImageFilters::getInstance();

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_setScaleFactor(JNIEnv* env, jobject, jdouble scaleFactor)
{ 
	filters->setScaleFactor((double)scaleFactor);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_setSketchTexture(JNIEnv* env, jobject, jlong sketchTexture)
{ 
	filters->setSketchTexture(*(Mat*)sketchTexture);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_setSketchFlip(JNIEnv* env, jobject, jboolean sketchFlip)
{ 
	filters->setSketchFlip((bool)sketchFlip);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_colorCartoonFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint thickness, jint threshold)
{ 
	filters->applyColorCartoon(*(Mat*)addrSrc, *(Mat*)addrDst, (int)thickness, (int)threshold);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_grayCartoonFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint thickness, jint threshold)
{ 
	filters->applyGrayCartoon(*(Mat*)addrSrc, *(Mat*)addrDst, (int)thickness, (int)threshold);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_pencilSketchFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint sketchBlend, jint contrast)
{ 
	filters->applyPencilSketch(*(Mat*)addrSrc, *(Mat*)addrDst, (int)sketchBlend, (int)contrast);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_colorSketchFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint sketchBlend, jint contrast)
{ 
	filters->applyColorSketch(*(Mat*)addrSrc, *(Mat*)addrDst, (int)sketchBlend, (int)contrast);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_pixelArtFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint pixelSize, jint numColors)
{ 
	filters->applyPixelArt(*(Mat*)addrSrc, *(Mat*)addrDst, (int)pixelSize, (int)numColors);
}

JNIEXPORT void JNICALL Java_com_ajscape_pixatoon_lib_Native_oilPaintFilter(JNIEnv* env, jobject, jlong addrSrc, jlong addrDst, jint radius, jint levels)
{ 
	filters->applyOilPaint(*(Mat*)addrSrc, *(Mat*)addrDst, (int)radius, (int)levels);
}

}