LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_SRC_FILES := \
	D:\WorkSpace\My_krto\app\src\main\jni\Android.mk \

LOCAL_C_INCLUDES += D:\WorkSpace\My_krto\app\src\main\jni
LOCAL_C_INCLUDES += D:\WorkSpace\My_krto\app\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
