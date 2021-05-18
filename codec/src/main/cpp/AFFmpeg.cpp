#include <jni.h>
#include "AFFmpeg.h"

unsigned char frame[FRAME_LENGTH];
int frame_length = 0;

static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl)
{
    static int print_prefix = 1;
    static char prev[1024];
    char line[1024];

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);

    if (level <= AV_LOG_WARNING)
    {
        LOGE("%s", line);
    }
    else
    {
        LOGD("%s", line);
    }
}

extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_init(
        JNIEnv *env,
        jobject /* this */)
{
    if (A_DEBUG)
    {
        av_log_set_level(AV_LOG_DEBUG);
        av_log_set_callback(log_callback_null);
    }
    av_register_all();
//    avformat_network_init();
    LOGD("Init FFmpeg %s", av_version_info());
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_initRecord(
        JNIEnv *env,
        jobject /* this */, jstring fileName, jint width, jint height, jint bitRate, jint gop,
        jint frameRate, jint videoCodecId)
{
    frame_length = 0;
    jboolean tRet = JNI_FALSE;
    jboolean isCopy;
    const char *outFileName = env->GetStringUTFChars(fileName, &isCopy);
    mRecord = new FFmpegRecord2();
    if (mRecord->init(outFileName, width, height, bitRate, gop, frameRate, videoCodecId))
    {
        tRet = JNI_TRUE;
    }
    alaw_pcm16_tableinit();
    env->ReleaseStringUTFChars(fileName, outFileName);
    return tRet;
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_releaseRecord(
        JNIEnv *env,
        jobject /* this */)
{
    mRecord->release();
    delete mRecord;
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_writeAudioData(
        JNIEnv *env,
        jobject /* this */, jbyteArray data, jint length)
{
    if (mRecord != nullptr && mRecord->isInit && mRecord->have_audio)
    {

        jbyte g711[length];
        env->GetByteArrayRegion(data, 0, length, g711);
        int pcm_length = length * 2;
        char pcm[pcm_length];
        alaw_to_pcm16(length, reinterpret_cast<const char *>(g711), pcm);

        for (int i = 0; i < pcm_length; ++i)
        {
            frame[frame_length++] = static_cast<unsigned char>(pcm[i]);

            if (frame_length >= FRAME_LENGTH)
            {
                frame_length = 0;
                unsigned char *buf = reinterpret_cast<unsigned char *>(new char[FRAME_LENGTH]);
                memcpy(buf, frame, FRAME_LENGTH);
                if (mRecord != nullptr && mRecord->isInit)
                {
                    mRecord->write_audio_frame(buf, FRAME_LENGTH);
                }
            }
        }
    }
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_writeVideoData(
        JNIEnv *env,
        jobject /* this */, jbyteArray data, jint length)
{
    if (mRecord != nullptr && mRecord->isInit && mRecord->have_video)
    {
        jbyte video[length];
        env->GetByteArrayRegion(data, 0, length, video);
        mRecord->write_video_frame(reinterpret_cast<unsigned char *>(video), length);
    }
}

extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_screenshot(
        JNIEnv *env,
        jobject /* this */, jstring fileName, jint width, jint height, jbyteArray data,
        jint length, jint videoCodecId)
{

    jboolean tRet = JNI_FALSE;
    jboolean isCopy;
    const char *outFileName = env->GetStringUTFChars(fileName, &isCopy);

    jbyte image[length];
    env->GetByteArrayRegion(data, 0, length, image);

    if (videoCodecId == 1)
    {
        if (mCaptureImage.captureH265(outFileName, width, height,
                                      reinterpret_cast<unsigned char *>(image),
                                      length))
        {
            tRet = JNI_TRUE;
        }
    }
    else
    {
        if (mCaptureImage.captureH264(outFileName, width, height,
                                      reinterpret_cast<unsigned char *>(image),
                                      length))
        {
            tRet = JNI_TRUE;
        }
    }

    env->ReleaseStringUTFChars(fileName, outFileName);
    return tRet;
}

extern "C"
JNIEXPORT jbyteArray
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_imageToYUV(
        JNIEnv *env,
        jobject /* this */, jbyteArray data, jint width, jint height)
{
    int length = env->GetArrayLength(data);
    jbyte image[length];
    env->GetByteArrayRegion(data, 0, length, image);

    unsigned char *yuv = imageToYUV.decode(reinterpret_cast<unsigned char *>(image),
                                           static_cast<size_t>(length),
                                           width, height);
    if (!yuv)
    {
        return nullptr;
    }

    const int yuvLength = width * height + (width / 2) * (height / 2) * 2;
    jbyteArray array = env->NewByteArray(yuvLength);
    env->SetByteArrayRegion(array, 0, yuvLength, reinterpret_cast<const jbyte *>(yuv));
    free(yuv);
    return array;
}

extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_imageToYUVInit(
        JNIEnv *env,
        jobject /* this */)
{
    jboolean tRet = JNI_FALSE;
    if (imageToYUV.init())
    {
        tRet = JNI_TRUE;
    }
    return tRet;
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_github_liaoheng_codec_core_AFFmpeg_imageToYUVRelease(
        JNIEnv *env,
        jobject /* this */)
{
    imageToYUV.release();
}