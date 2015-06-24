LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := ffmpegbridge
LOCAL_LDLIBS := \
	-llog \
	-lz \
	-lm \
	-ljnigraphics \
	-landroid \

LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libavfilter libswresample


LOCAL_SRC_FILES := \
	/Users/jinchangzhu/AndroidStudioProjects/Mars/app/src/main/jni/ffmpegbridge.c \

LOCAL_C_INCLUDES += /Users/jinchangzhu/AndroidStudioProjects/Mars/app/src/main/jni

include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH      := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE    := imageblurtool
LOCAL_SRC_FILES := \
	/Users/jinchangzhu/AndroidStudioProjects/Mars/app/src/main/jni/com_koolew_mars_blur_ImageBlurTool.cpp \
	/Users/jinchangzhu/AndroidStudioProjects/Mars/app/src/main/jni/blur/ImageBlurTool.c
LOCAL_LDLIBS    := -lm -llog -ljnigraphics
LOCAL_C_INCLUDES += /Users/jinchangzhu/AndroidStudioProjects/Mars/app/src/main/jni

include $(BUILD_SHARED_LIBRARY)


$(call import-module,ffmpeg-2.4.9/android/arm)
