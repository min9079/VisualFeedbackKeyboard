APP_ABI := armeabi-v7a x86
APP_PLATFORM := android-24
#APP_CFLAGS := -DNDEBUG -DKENLM_MAX_ORDER=12 -DOS_ANDROID -fexceptions -march=armv7-a -mfloat-abi=softfp -mfpu=neon -I./include -std=c++11
APP_CFLAGS := -DNDEBUG -DKENLM_MAX_ORDER=12 -DOS_ANDROID -I./include
APP_CPPFLAGS += -frtti -fexceptions -std=c++11 '-I../../CustomKeyboard/app/src/main/jni/include', '-std=c++11'
APP_STL := c++_shared
#APP_GNUSTL_FORCE_CPP_FEATURES := exceptions rtti
