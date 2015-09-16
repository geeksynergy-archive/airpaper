#include<string.h>
#include <jni.h>
#include<stdio.h>
#include<filters.h>

int s_ButtonPressCounter = 0;

jstring
Java_com_geeksynergy_airpaper_MainActivity_stringFromJNI(JNIEnv* env, jobject this)
{
    char szBuf[512];
    // sprintf(szBuf, "%d", s_ButtonPressCounter++);
    sprintf(szBuf, "%d", sample_x(s_ButtonPressCounter));

    jstring str = (*env)->NewStringUTF(env, szBuf);
    return str;
}