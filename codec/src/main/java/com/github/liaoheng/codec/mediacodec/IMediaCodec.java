package com.github.liaoheng.codec.mediacodec;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.liaoheng.codec.model.VideoCodecConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 对硬编码({@link MediaCodec})封装，主要是维护对应状态
 *
 * @author liaoheng
 * @version 2018-05-17 12:50
 */
public interface IMediaCodec {

    /**
     * 硬编码({@link MediaCodec})使用Annex B格式，第一帧数据中包含pps,sps,vps等信息，这里做判断。
     */
    boolean isNalHeaderFrame(byte[] outData);

    /**
     * 判断关键帧
     */
    boolean isIFrame(byte[] outData);

    /**
     * 初始化编码器
     *
     * @throws IOException
     */
    void initEncoder() throws IOException;

    /**
     * 初始化解码器
     *
     * @throws IOException
     */
    void initDecoder() throws IOException;

    /**
     * 启动编码器
     *
     * @param config {@link VideoCodecConfig}
     */
    void startEncoder(VideoCodecConfig config);

    /**
     * 启动解码器
     *
     * @param surface {@link Surface}
     * @param config  {@link VideoCodecConfig}
     */
    void startDecoder(Surface surface, VideoCodecConfig config);

    /**
     * 停止编/解码器
     */
    void stop();

    /**
     * 释放编/解码器
     */
    void release();

    int getEncoderColorFormat();

    /**
     * Returns the index of an input buffer to be filled with valid data
     * or -1 if no such buffer is currently available.
     * This method will return immediately if timeoutUs == 0, wait indefinitely
     * for the availability of an input buffer if timeoutUs &lt; 0 or wait up
     * to "timeoutUs" microseconds if timeoutUs &gt; 0.
     *
     * @param timeoutUs The timeout in microseconds, a negative timeout indicates "infinite".
     * @throws IllegalStateException     if not in the Executing state,
     *                                   or codec is configured in asynchronous mode.
     * @throws MediaCodec.CodecException upon codec error.
     */
    int dequeueInputBuffer(long timeoutUs);

    /**
     * Dequeue an output buffer, block at most "timeoutUs" microseconds.
     * Returns the index of an output buffer that has been successfully
     * decoded or one of the INFO_* constants.
     *
     * @param info      Will be filled with buffer meta data.
     * @param timeoutUs The timeout in microseconds, a negative timeout indicates "infinite".
     * @throws IllegalStateException     if not in the Executing state,
     *                                   or codec is configured in asynchronous mode.
     * @throws MediaCodec.CodecException upon codec error.
     */
    int dequeueOutputBuffer(
            @NonNull MediaCodec.BufferInfo info, long timeoutUs);

    /**
     * If you are done with a buffer, use this call to return the buffer to the codec
     * or to render it on the output surface. If you configured the codec with an
     * output surface, setting {@code render} to {@code true} will first send the buffer
     * to that output surface. The surface will release the buffer back to the codec once
     * it is no longer used/displayed.
     * <p>
     * Once an output buffer is released to the codec, it MUST NOT
     * be used until it is later retrieved by {@link MediaCodec#getOutputBuffer} in response
     * to a {@link #dequeueOutputBuffer} return value or a
     * {@link MediaCodec.Callback#onOutputBufferAvailable} callback.
     *
     * @param index  The index of a client-owned output buffer previously returned
     *               from a call to {@link #dequeueOutputBuffer}.
     * @param render If a valid surface was specified when configuring the codec,
     *               passing true renders this output buffer to the surface.
     * @throws IllegalStateException     if not in the Executing state.
     * @throws MediaCodec.CodecException upon codec error.
     */
    void releaseOutputBuffer(int index, boolean render);

    /**
     * After filling a range of the input buffer at the specified index
     * submit it to the component. Once an input buffer is queued to
     * the codec, it MUST NOT be used until it is later retrieved by
     * {@link MediaCodec#getInputBuffer} in response to a {@link #dequeueInputBuffer}
     * return value or a {@link MediaCodec.Callback#onInputBufferAvailable}
     * callback.
     * <p>
     * Many decoders require the actual compressed data stream to be
     * preceded by "codec specific data", i.e. setup data used to initialize
     * the codec such as PPS/SPS in the case of AVC video or code tables
     * in the case of vorbis audio.
     * The class {@link android.media.MediaExtractor} provides codec
     * specific data as part of
     * the returned track format in entries named "csd-0", "csd-1" ...
     * <p>
     * These buffers can be submitted directly after {@link MediaCodec#start} or
     * {@link MediaCodec#flush} by specifying the flag {@link
     * MediaCodec#BUFFER_FLAG_CODEC_CONFIG}.  However, if you configure the
     * codec with a {@link MediaFormat} containing these keys, they
     * will be automatically submitted by MediaCodec directly after
     * start.  Therefore, the use of {@link
     * MediaCodec#BUFFER_FLAG_CODEC_CONFIG} flag is discouraged and is
     * recommended only for advanced users.
     * <p>
     * To indicate that this is the final piece of input data (or rather that
     * no more input data follows unless the decoder is subsequently flushed)
     * specify the flag {@link MediaCodec#BUFFER_FLAG_END_OF_STREAM}.
     * <p class=note>
     * <strong>Note:</strong> Prior to {@link android.os.Build.VERSION_CODES#M},
     * {@code presentationTimeUs} was not propagated to the frame timestamp of (rendered)
     * Surface output buffers, and the resulting frame timestamp was undefined.
     * Use {@link MediaCodec#releaseOutputBuffer(int, long)} to ensure a specific frame timestamp is set.
     * Similarly, since frame timestamps can be used by the destination surface for rendering
     * synchronization, <strong>care must be taken to normalize presentationTimeUs so as to not be
     * mistaken for a system time. (See {@linkplain MediaCodec#releaseOutputBuffer(int, long)
     * SurfaceView specifics}).</strong>
     *
     * @param index              The index of a client-owned input buffer previously returned
     *                           in a call to {@link #dequeueInputBuffer}.
     * @param offset             The byte offset into the input buffer at which the data starts.
     * @param size               The number of bytes of valid input data.
     * @param presentationTimeUs The presentation timestamp in microseconds for this
     *                           buffer. This is normally the media time at which this
     *                           buffer should be presented (rendered). When using an output
     *                           surface, this will be propagated as the {@link
     *                           SurfaceTexture#getTimestamp timestamp} for the frame (after
     *                           conversion to nanoseconds).
     * @param flags              A bitmask of flags
     *                           {@link MediaCodec#BUFFER_FLAG_CODEC_CONFIG} and {@link MediaCodec#BUFFER_FLAG_END_OF_STREAM}.
     *                           While not prohibited, most codecs do not use the
     *                           {@link MediaCodec#BUFFER_FLAG_KEY_FRAME} flag for input buffers.
     * @throws IllegalStateException      if not in the Executing state.
     * @throws MediaCodec.CodecException  upon codec error.
     * @throws MediaCodec.CryptoException if a crypto object has been specified in
     *                                    {@link MediaCodec#configure}
     */
    void queueInputBuffer(
            int index,
            int offset, int size, long presentationTimeUs, int flags)
            throws MediaCodec.CryptoException;

    /**
     * Returns a {@link java.nio.Buffer#clear cleared}, writable ByteBuffer
     * object for a dequeued input buffer index to contain the input data.
     * <p>
     * After calling this method any ByteBuffer or Image object
     * previously returned for the same input index MUST no longer
     * be used.
     *
     * @param index The index of a client-owned input buffer previously
     *              returned from a call to {@link #dequeueInputBuffer},
     *              or received via an onInputBufferAvailable callback.
     * @return the input buffer, or null if the index is not a dequeued
     * input buffer, or if the codec is configured for surface input.
     * @throws IllegalStateException     if not in the Executing state.
     * @throws MediaCodec.CodecException upon codec error.
     */
    @Nullable
    ByteBuffer getInputBuffer(int index);

    /**
     * Returns a read-only ByteBuffer for a dequeued output buffer
     * index. The position and limit of the returned buffer are set
     * to the valid output data.
     * <p>
     * After calling this method, any ByteBuffer or Image object
     * previously returned for the same output index MUST no longer
     * be used.
     *
     * @param index The index of a client-owned output buffer previously
     *              returned from a call to {@link #dequeueOutputBuffer},
     *              or received via an onOutputBufferAvailable callback.
     * @return the output buffer, or null if the index is not a dequeued
     * output buffer, or the codec is configured with an output surface.
     * @throws IllegalStateException     if not in the Executing state.
     * @throws MediaCodec.CodecException upon codec error.
     */
    @Nullable
    ByteBuffer getOutputBuffer(int index);

    MediaCodec getMediaCodec();

    /**
     * 是否运行中
     */
    boolean isRunning();

    boolean isRelease();

    int getVideoCodecId();
}
