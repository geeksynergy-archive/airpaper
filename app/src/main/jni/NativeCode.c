#include <string.h>
#include <jni.h>
/*
 * Class:     com_inde_ndksupport_app_MainActivity
 * Method:    getStringFromNative
 * Signature: ()Ljava/lang/String;
jstring Java_com_geeksynergy_airpaper_MainActivity_getStringFromNative
  (JNIEnv *env , jobject obj)
  {
        return (*env)->NewStringUTF(env,"Hello From JNI");
  }
 */


#ifndef _Included_com_geeksynergy_airpaper_AudioBufferProcessor
#define _Included_com_geeksynergy_airpaper_AudioBufferProcessor
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_geeksynergy_airpaper_AudioBufferProcessor
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_geeksynergy_airpaper_AudioBufferProcessor_init
(JNIEnv *, jobject);

/*
 * Class:     com_geeksynergy_airpaper_AudioBufferProcessor
 * Method:    processBuffer
 * Signature: ([FI)V
 */
JNIEXPORT void JNICALL Java_com_geeksynergy_airpaper_AudioBufferProcessor_processBuffer
(JNIEnv *, jobject, jfloatArray, jint);

/*
 * Class:     com_geeksynergy_airpaper_AudioBufferProcessor
 * Method:    processBuffer2
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_geeksynergy_airpaper_AudioBufferProcessor_processBuffer2
(JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif



