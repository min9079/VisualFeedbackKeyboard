LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := kenlm
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libkenlm.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := metaphone
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libmetaphone.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := mobile_word_corrector
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libmobile_word_corrector.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := mwc
LOCAL_SRC_FILES := kr_ac_snu_hcil_customkeyboard_WordCorrector.cpp
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := kenlm metaphone mobile_word_corrector

include $(BUILD_SHARED_LIBRARY)
