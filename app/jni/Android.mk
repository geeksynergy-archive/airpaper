LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_MODULE := QPSK_Decoder
#VisualGDBAndroid: AutoUpdateSourcesInNextLine
LOCAL_SRC_FILES := filters.c hello.c rtl_redsea.c

include $(BUILD_SHARED_LIBRARY)
