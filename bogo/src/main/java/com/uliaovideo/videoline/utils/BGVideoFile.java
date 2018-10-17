package com.uliaovideo.videoline.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

/**
 * Created by weipeng on 2018/2/26.
 */

public class BGVideoFile {
    // 获取视频缩略图
    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap b=null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            b=retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();

        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return b;
    }
}
