#include <android/log.h>
#include <cstring>
#include <iostream>

#include "kr_ac_snu_hcil_customkeyboard_WordCorrector.h"

#include "btem_cc_decoder.h"
#include "cwc_decoder.h"
#include "em_sc_decoder.h"

#define APPNAME "UniThumbN"

#ifdef LOGD
#undef LOGD
#endif
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, APPNAME, __VA_ARGS__)

static BTEMCcDecoder* g_btem_cc_decoder = NULL;
static CwcDecoder* g_cwc_decoder = NULL;
static Keyboard* g_keyboard = NULL;
static EMScDecoder* g_em_sc_decoder = NULL;
static EMScDecoder::InputSentence g_input_sentence;
static std::string g_last_keys = "";

#if 0
static int flag = 0;

JNIEXPORT jobjectArray JNICALL Java_com_min90_android_myspellcorrector_SpellCorrectionActivity_mySpellCorrect
  (JNIEnv *env, jobject thisObj, jstring input_word)
{
    const char *native_word = (*env)->GetStringUTFChars(env, input_word, 0);
    WORD_PROB_PAIR *word_list;
    int k = 5;
    int i;

    if(!flag)
    {
        init_decoder("oanc", "msr", 3, 0.02);
        flag = 1;
    }

    word_list = decode(native_word, k);

    jclass classStringFloatPair = (*env)->FindClass(env, "com/min90/android/myspellcorrector/SpellCorrectionActivity$StringFloatPair");
    jobjectArray ret = (jobjectArray) (*env)->NewObjectArray(env, k, classStringFloatPair, 0); 
    jmethodID cStringFloatPair = (*env)->GetMethodID(env, classStringFloatPair, "<init>", "(Lcom/min90/android/myspellcorrector/SpellCorrectionActivity;Ljava/lang/String;F)V");

    for(i=0; i<k; i++){
        (*env)->SetObjectArrayElement(env, ret, i, (*env)->NewObject(env, classStringFloatPair, cStringFloatPair, thisObj, (*env)->NewStringUTF(env, word_list[i].word), word_list[i].log_prob));
    }

    return ret;
}
#endif

using namespace std;

string do_decode(const string input_sentence)
{
    vector<pair<AbstractDecoder::DString, float>> ret_list;
    Keyboard keyboard("Nexus5x_s");
    EMCcDecoder em_cc_decoder("twitter_c", "all", 4, 0.02, 1.2, false);
    CwcDecoder cwc_decoder(em_cc_decoder, "twitter", "all", keyboard, 4, 0.02, 1.2, false);

    {
        std::pair<std::string, EMWcDecoder::InputWord> context_vword = cwc_decoder.parseInputSentence(input_sentence);
        cwc_decoder.setContext(context_vword.first);
        string v_word = context_vword.second;
        for (string::iterator it = v_word.begin(); it != v_word.end(); ++it) {
            EMCcDecoder::CharInputKey key = *it;

            cout << cwc_decoder.decodeKey(key, 10) << endl;
        }
        cout << endl;
        ret_list = cwc_decoder.decode(context_vword.first, context_vword.second);

        for (vector<pair<AbstractDecoder::DString, float>>::iterator it = ret_list.begin(); it != ret_list.end(); it++) {
            cout << it->first.toString() << ": " << it->second << endl;
        }
        cout << ret_list.size() << endl;
    }

    return (ret_list.begin())->first.toString();
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    correctWord
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_correctWord(JNIEnv* env, jobject thisObj, jstring inJNIStr)
{
    LOGD(__func__);
    const char* native_string = env->GetStringUTFChars(inJNIStr, 0);
    LOGD("input arguemt=%s", native_string);
    string w_word = do_decode(native_string);
    return env->NewStringUTF(w_word.c_str());
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    setContext
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_setContext(JNIEnv* env, jobject thisObj, jstring inJNIStr)
{
    LOGD(__func__);
    if (inJNIStr != NULL) {
        const char* native_string = env->GetStringUTFChars(inJNIStr, 0);
        LOGD("input argument=%s", native_string);
        g_cwc_decoder->setContext(native_string);
    }

    return;
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    decodeKey
 * Signature: (C)C
 */
JNIEXPORT jstring JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_decodeKey(JNIEnv* env, jobject thisObj, jchar inJNIChar)
{

    std::string ret_string;

    LOGD(__func__);
    const unsigned short wchar = inJNIChar;
    Touch::Point<float> pt = g_keyboard->getPositionMM((char)wchar);
    LOGD("input argument: %c (%f,%f)", wchar, pt.x, pt.y);
    BTEMCcDecoder::TouchInputKey key(g_keyboard->getPositionMM(wchar), *g_btem_cc_decoder);
    ret_string = g_cwc_decoder->decodeKey(key, 50).toString();
    g_last_keys = ret_string;
    LOGD("ret string: %s", ret_string.c_str());

    return env->NewStringUTF(ret_string.c_str());
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    decodeTouchKey
 * Signature: (FF)Lkr/ac/snu/hcil/customkeyboard/WordCorrector/TouchString;
 */

JNIEXPORT jobject JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_decodeTouchKey(JNIEnv* env, jobject thisObj, jfloat x, jfloat y)
{
    float mm_x = g_keyboard->pixelsToMM(x);
    float mm_y = g_keyboard->pixelsToMM(y);

    AbstractDecoder::DString ret_string;
    Touch::Point<float> pt(mm_x, mm_y);

    LOGD(__func__);
    LOGD("input argument:(%f, %f) -> (%f, %f)", x, y, mm_x, mm_y);
    BTEMCcDecoder::TouchInputKey key(pt, *g_btem_cc_decoder);
    ret_string = g_cwc_decoder->decodeKey(key, 10);
    g_last_keys = ret_string.toString();

    LOGD("decoded keys:%s", ret_string.toString().c_str());

    jclass jTouchStringClass = env->FindClass("kr/ac/snu/hcil/customkeyboard/WordCorrector$TouchString");
    jobject jTouchStringObj = env->AllocObject(jTouchStringClass);
    jfieldID jfid = env->GetFieldID(jTouchStringClass, "mStr", "Ljava/lang/String;");
    env->SetObjectField(jTouchStringObj, jfid, env->NewStringUTF(ret_string.toString().c_str()));

    string ori_string;

    for (int i = 0; i < ret_string.size(); i++) {
        auto input_key = ret_string[i].second;
        if (TOUCH_INPUT_KEY == input_key->getClassId()) {
            auto touch_input_key = static_pointer_cast<BTEMCcDecoder::TouchInputKey>(input_key);
            ori_string.append(1, g_keyboard->getShortestKey(touch_input_key->getPoint()));
        } else if (CHAR_INPUT_KEY == input_key->getClassId()) {
            auto char_input_key = static_pointer_cast<EMCcDecoder::CharInputKey>(input_key);
            ori_string.append(1, (char)*char_input_key);
        }
    }

    jfid = env->GetFieldID(jTouchStringClass, "mOriStr", "Ljava/lang/String;");
    env->SetObjectField(jTouchStringObj, jfid, env->NewStringUTF(ori_string.c_str()));

    LOGD("raw keys:%s", ori_string.c_str());

    return jTouchStringObj;
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    decodeWord
 * Signature: ()[Lkr/ac/snu/hcil/customkeyboard/WordCorrector/PredictEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_decodeWord(JNIEnv* env, jobject thisObj)
{
    jobjectArray ret;
    vector<pair<AbstractDecoder::DString, float>> ret_list;

    LOGD(__func__);
    ret_list = g_cwc_decoder->decodeWord();

    vector<string> word;
    int size = std::min(3, (int) ret_list.size());
    for (int i = 0; i < size; ++i) {
        word.push_back(ret_list[i].first.toString());
        if(i>0 && ret_list[i-1].second - ret_list[i].second > 1)
            break; // skip if the probability is over 10 times smaller.
        if(g_input_sentence.getWords().size() > 5)
            i++;
        if(g_input_sentence.getWords().size() > 10)
            break;
    }
    if(0 == word.size())
        word.push_back(g_last_keys);
    g_input_sentence.pushWord(word);

    jclass jPredictEntryClass = env->FindClass("kr/ac/snu/hcil/customkeyboard/WordCorrector$PredictEntry");

    ret = (jobjectArray)env->NewObjectArray(ret_list.size(), jPredictEntryClass, NULL);

    for (int i = 0; i < ret_list.size(); i++) {
        // create PredicEntry object
        jobject jPredictEntryObj = env->AllocObject(jPredictEntryClass);

        // set fields (String, float)
        jfieldID jfid = env->GetFieldID(jPredictEntryClass, "mStr", "Ljava/lang/String;");
        env->SetObjectField(jPredictEntryObj, jfid, env->NewStringUTF(ret_list[i].first.toString().c_str()));

        jfid = env->GetFieldID(jPredictEntryClass, "mProb", "F");
        env->SetFloatField(jPredictEntryObj, jfid, std::pow(10, ret_list[i].second));

        // add object to array.
        env->SetObjectArrayElement(ret, i, jPredictEntryObj);
    }

    return ret;
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    decodeSentence
 * Signature: ()[Lkr/ac/snu/hcil/customkeyboard/WordCorrector/PredictEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_decodeSentence(JNIEnv* env, jobject thisObj)
{
    jobjectArray ret;
    vector<pair<AbstractDecoder::DString, float>> ret_list;

    LOGD(__func__);
    ret_list = g_em_sc_decoder->decode("", g_input_sentence);

    jclass jPredictEntryClass = env->FindClass("kr/ac/snu/hcil/customkeyboard/WordCorrector$PredictEntry");

    ret = (jobjectArray)env->NewObjectArray(ret_list.size(), jPredictEntryClass, NULL);

    for (int i = 0; i < ret_list.size(); i++) {
        // create PredicEntry object
        jobject jPredictEntryObj = env->AllocObject(jPredictEntryClass);

        // set fields (String, float)
        jfieldID jfid = env->GetFieldID(jPredictEntryClass, "mStr", "Ljava/lang/String;");
        env->SetObjectField(jPredictEntryObj, jfid, env->NewStringUTF(ret_list[i].first.toString().c_str()));

        jfid = env->GetFieldID(jPredictEntryClass, "mProb", "F");
        env->SetFloatField(jPredictEntryObj, jfid, std::pow(10, ret_list[i].second));

        // add object to array.
        env->SetObjectArrayElement(ret, i, jPredictEntryObj);
    }

    return ret;
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    backKey
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_backKey(JNIEnv* env, jobject thisObj)
{
    std::string ret_string;

    ret_string = g_cwc_decoder->backKey();
    LOGD("ret string: %s", ret_string.c_str());

    return env->NewStringUTF(ret_string.c_str());
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    backWord
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_backWord(JNIEnv* env, jobject thisObj)
{
    return g_input_sentence.popWord();
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    pushWord
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_pushWord (JNIEnv* env, jobject thisObj, jstring inJNIStr)
{
    LOGD(__func__);
    if (inJNIStr != NULL) {
        const char* native_string = env->GetStringUTFChars(inJNIStr, 0);
        LOGD("input argument=%s", native_string);
        vector<string> word;
        word.push_back(native_string);
        g_input_sentence.pushWord(word);
    }
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    getChar
 * Signature: (II)C
 */
JNIEXPORT jchar JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_getChar (JNIEnv* env, jobject thisObj, jint x, jint y)
{
    return g_cwc_decoder->getChar(x,y);
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    clearSentence
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_clearSentence(JNIEnv* env, jobject thisObj)
{
    g_input_sentence.clear();
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    predictChar
 * Signature: (IF)[Lkr/ac/snu/hcil/customkeyboard/WordCorrector/PredictEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_predictChar(JNIEnv* env, jobject thisObj, jint n_of_chars, jfloat threshold)
{
    jobjectArray ret;
    float threshold_likeli = 1.0;
    int prediction_size = n_of_chars;

    LOGD(__func__);
    auto pc = g_cwc_decoder->predictChar(n_of_chars);
    LOGD("char prediction: %d", pc.size());

    if (threshold > 0) {
        threshold_likeli = std::log10(threshold);

        for (prediction_size = 0; prediction_size < pc.size(); prediction_size++) {
            // Ignore predictions under threshold probability.
            if (pc[prediction_size].second < threshold_likeli)
                break;
        }
    }

    jclass jPredictEntryClass = env->FindClass("kr/ac/snu/hcil/customkeyboard/WordCorrector$PredictEntry");

    ret = (jobjectArray)env->NewObjectArray(prediction_size, jPredictEntryClass, NULL);

    for (int i = 0; i < prediction_size; i++) {
        // create PredicEntry object
        jobject jPredictEntryObj = env->AllocObject(jPredictEntryClass);

        // set fields (String, float)
        jfieldID jfid = env->GetFieldID(jPredictEntryClass, "mStr", "Ljava/lang/String;");
        env->SetObjectField(jPredictEntryObj, jfid, env->NewStringUTF(pc[i].first.c_str()));

        jfid = env->GetFieldID(jPredictEntryClass, "mProb", "F");
        env->SetFloatField(jPredictEntryObj, jfid, std::pow(10, pc[i].second));

        // add object to array.
        env->SetObjectArrayElement(ret, i, jPredictEntryObj);
    }

    return (ret);
}

jobjectArray predictWordInternal(JNIEnv* env, jobject thisObj, jint n_of_candidates, jfloat threshold, jchar inJNIChar)
{
    jobjectArray ret;
    float threshold_likeli = 1.0;
    int prediction_size = 0;
    const unsigned short wchar = inJNIChar;
    vector<pair<string, float>> pw;

    LOGD("[%s] inJNIChar: %c", __func__, inJNIChar);
    if (0 == inJNIChar)
        pw = g_cwc_decoder->predictWord(n_of_candidates);
    else
        pw = g_cwc_decoder->predictWord(n_of_candidates, string(1, inJNIChar));
    prediction_size = pw.size();
    LOGD("word prediction: %d", pw.size());

    if (threshold > 0) {
        threshold_likeli = std::log10(threshold);

        for (prediction_size = 0; prediction_size < pw.size(); prediction_size++) {
            // Ignore predictions under threshold probability.
            if (pw[prediction_size].second < threshold_likeli)
                break;
        }
    }

    jclass jPredictEntryClass = env->FindClass("kr/ac/snu/hcil/customkeyboard/WordCorrector$PredictEntry");

    ret = (jobjectArray)env->NewObjectArray(prediction_size, jPredictEntryClass, NULL);

    for (int i = 0; i < prediction_size; i++) {
        // create PredicEntry object
        jobject jPredictEntryObj = env->AllocObject(jPredictEntryClass);

        // set fields (String, float)
        jfieldID jfid = env->GetFieldID(jPredictEntryClass, "mStr", "Ljava/lang/String;");
        env->SetObjectField(jPredictEntryObj, jfid, env->NewStringUTF(pw[i].first.c_str()));

        jfid = env->GetFieldID(jPredictEntryClass, "mProb", "F");
        env->SetFloatField(jPredictEntryObj, jfid, std::pow(10, pw[i].second));

        // add object to array.
        env->SetObjectArrayElement(ret, i, jPredictEntryObj);
    }

    return (ret);
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    predictWord
 * Signature: (IF)[Lkr/ac/snu/hcil/customkeyboard/WordCorrector/PredictEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_predictWord__IF(JNIEnv* env, jobject thisObj, jint n_of_candidates, jfloat threshold)
{
    return predictWordInternal(env, thisObj, n_of_candidates, threshold, 0);
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    predictWord
 * Signature: (IFC)[Lkr/ac/snu/hcil/customkeyboard/WordCorrector/PredictEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_predictWord__IFC(JNIEnv* env, jobject thisObj, jint n_of_candidates, jfloat threshold, jchar inJNIChar)
{
    return predictWordInternal(env, thisObj, n_of_candidates, threshold, inJNIChar);
}

const bool getBoolean(JNIEnv* env, jobject obj, char const* field_name)
{
    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "Z");
    if (NULL == fid)
        return -1;

    return env->GetBooleanField(obj, fid);
}

const float getFloat(JNIEnv* env, jobject obj, char const* field_name)
{
    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "F");
    if (NULL == fid)
        return -1;

    return env->GetFloatField(obj, fid);
}

const int getInt(JNIEnv* env, jobject obj, char const* field_name)
{
    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "I");
    if (NULL == fid)
        return -1;

    return env->GetIntField(obj, fid);
}

const char* getString(JNIEnv* env, jobject obj, char const* field_name)
{
    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "Ljava/lang/String;");
    if (NULL == fid)
        return NULL;

    jstring retString = static_cast<jstring>(env->GetObjectField(obj, fid));
    return env->GetStringUTFChars(retString, NULL);
}

const int getFingerType(JNIEnv* env, jobject obj, char const* field_name)
{
    LOGD(__func__);

    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "Lkr/ac/snu/hcil/customkeyboard/WordCorrector$ConfBuilder$FingerType;");
    if (NULL == fid)
        return 0;

    jobject enum_obj = env->GetObjectField(obj, fid);
    jclass enum_class = env->GetObjectClass(enum_obj);
    jmethodID mid = env->GetMethodID(enum_class, "ordinal", "()I");
    jint value = env->CallIntMethod(enum_obj, mid);

    LOGD("enum val: %d", value);

    return value;
}

void getFloatArray(JNIEnv* env, jobject obj, char const* field_name, float left_margin[4])
{
    jclass class_ = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(class_, field_name, "[F");

    jfloatArray arr = static_cast<jfloatArray>(env->GetObjectField(obj, fid));

    jsize len = env->GetArrayLength(arr);
    jfloat* body = env->GetFloatArrayElements(arr, 0);

    for (int i = 0; i < len; i++) {
        LOGD("%f", body[i]);
        left_margin[i] = body[i];
    }
    env->ReleaseFloatArrayElements(arr, body, 0);
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    init
 * Signature: (Lkr/ac/snu/hcil/customkeyboard/WordCorrector/Configuration;)V
 */
JNIEXPORT void JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_init(JNIEnv* env, jobject thisObj, jobject configuration)
{
    LOGD(__func__);

    const char* lm_name = getString(env, configuration, "mLMName");
    LOGD("ln_name: %s", lm_name);
    const char* em_name = getString(env, configuration, "mEMName");
    LOGD("em_name: %s", em_name);
    const int em_order = getInt(env, configuration, "mEMOrder");
    LOGD("em_order: %d", em_order);
    const float cer = getFloat(env, configuration, "mCER");
    LOGD("cer: %f", cer);
    const float lambda = getFloat(env, configuration, "mLambda");
    LOGD("lambda: %f", lambda);
    const char* keyboard = getString(env, configuration, "mKeyboard");
    LOGD("keyboard: %s", keyboard);
    const bool is_verbose = getBoolean(env, configuration, "mIsVerbose");
    LOGD("is_verbose: %d", is_verbose);
    const float btn_w = getFloat(env, configuration, "mBtnW");
    LOGD("btn_w: %f", btn_w);
    const float btn_h = getFloat(env, configuration, "mBtnH");
    LOGD("btn_h: %f", btn_h);
    const int dpi = getInt(env, configuration, "mDpi");
    LOGD("dpi: %d", dpi);
    const int finger_type = getFingerType(env, configuration, "mFingerType");
    LOGD("finger_type: %d", finger_type);
    float left_margin[4];
    getFloatArray(env, configuration, "mLeftMargin", left_margin);
    LOGD("left_margin: %f, %f, %f", left_margin[0], left_margin[1], left_margin[2]);

    if (strcmp(keyboard, "custom") == 0) {
        g_keyboard = new Keyboard(btn_w, btn_h, left_margin, dpi);
    } else {
        g_keyboard = new Keyboard(keyboard);
    }

    g_btem_cc_decoder = new BTEMCcDecoder(string(lm_name) + "_c", em_name, *g_keyboard, em_order, cer, lambda, Btd2D::NORMAL, is_verbose);
    g_cwc_decoder = new CwcDecoder(*g_btem_cc_decoder, lm_name, em_name, *g_keyboard, em_order, cer, lambda, is_verbose);
    g_em_sc_decoder = new EMScDecoder(lm_name, false);
}

/*
 * Class:     kr_ac_snu_hcil_customkeyboard_WordCorrector
 * Method:    cleanup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_kr_ac_snu_hcil_customkeyboard_WordCorrector_cleanup(JNIEnv* env, jobject thisObj)
{
    LOGD(__func__);
    if (g_cwc_decoder) {
        delete g_cwc_decoder;
        g_cwc_decoder = NULL;
    }
    if (g_btem_cc_decoder) {
        delete g_btem_cc_decoder;
        g_btem_cc_decoder = NULL;
    }
    if (g_keyboard) {
        delete g_keyboard;
        g_keyboard = NULL;
    }
    if (g_em_sc_decoder) {
        delete g_em_sc_decoder;
        g_em_sc_decoder = NULL;
    }
}

