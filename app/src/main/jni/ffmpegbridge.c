#include "com_koolew_mars_VideoShootActivity.h"

#include "libavcodec/avcodec.h"


JNIEXPORT jint JNICALL Java_com_koolew_mars_VideoShootActivity_getAvcodecVersion
  (JNIEnv *env, jobject obj)
{
    return avcodec_version();
}