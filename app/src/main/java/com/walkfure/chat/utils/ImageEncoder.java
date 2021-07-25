package com.walkfure.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;

import org.webrtc.EglBase;
import org.webrtc.GlRectDrawer;
import org.webrtc.JavaI420Buffer;
import org.webrtc.Logging;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrameDrawer;
import org.webrtc.VideoSink;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageEncoder implements VideoSink {
    Context mContext;
    boolean down=false;
    String outputString;
    java.io.File newFile;
    FileOutputStream fileOutputStream = null;
    public ImageEncoder(Context mContext,String outputFile){
        down=false;
        this.mContext=mContext;
        outputString=outputFile;
    }



    @Override
    public void onFrame(VideoFrame frame) {
        Log.e("的实力开发软件","sdlkfjdskl大师傅士大夫大师傅vaca");
        if(!down){
            down=true;
            I420toNV21Conversion( frame.getBuffer().toI420());
            frame.getBuffer().toI420().release();
            mContext.sendBroadcast(new Intent("photoOk"));
        }
    }



    public void release() {

    }

    private synchronized void  I420toNV21Conversion(VideoFrame.I420Buffer i420Buffer) {
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        //convert to nv21, this is the same as byte[] from onPreviewCallback
        byte[] nv21Data = createNV21Data(i420Buffer);

        //let's test the conversion by converting the NV21 data to jpg and showing it in a bitmap.
        YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21,width,height,null);
        newFile= new File(outputString);
        try {
            fileOutputStream = new FileOutputStream(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, fileOutputStream);
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static byte[] createNV21Data(VideoFrame.I420Buffer i420Buffer) {
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        final int chromaStride = width;
        final int chromaWidth = (width + 1) / 2;
        final int chromaHeight = (height + 1) / 2;
        final int ySize = width * height;
        final ByteBuffer nv21Buffer = ByteBuffer.allocateDirect(ySize + chromaStride * chromaHeight);
        // We don't care what the array offset is since we only want an array that is direct.
        @SuppressWarnings("ByteBufferBackingArray") final byte[] nv21Data = nv21Buffer.array();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final byte yValue = i420Buffer.getDataY().get(y * i420Buffer.getStrideY() + x);
                nv21Data[y * width + x] = yValue;
            }
        }
        for (int y = 0; y < chromaHeight; ++y) {
            for (int x = 0; x < chromaWidth; ++x) {
                final byte uValue = i420Buffer.getDataU().get(y * i420Buffer.getStrideU() + x);
                final byte vValue = i420Buffer.getDataV().get(y * i420Buffer.getStrideV() + x);
                nv21Data[ySize + y * chromaStride + 2 * x + 0] = vValue;
                nv21Data[ySize + y * chromaStride + 2 * x + 1] = uValue;
            }
        }
        return nv21Data;
    }

    /** Convert a byte array to a direct ByteBuffer. */
    private static ByteBuffer toByteBuffer(int[] array) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length);
        buffer.put(toByteArray(array));
        buffer.rewind();
        return buffer;
    }


    /**
     * Convert an int array to a byte array and make sure the values are within the range [0, 255].
     */
    private static byte[] toByteArray(int[] array) {
        final byte[] res = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            final int value = array[i];
            res[i] = (byte) value;
        }
        return res;
    }






}