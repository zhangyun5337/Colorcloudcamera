package com.zhangyun.colorcloudcamera.utils;

/**
 * Created by ZhangYun on 2018/2/21.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class AlbumUtil {
    public static List<String> getImagesInFolder(String photoFolder, final boolean isOnlyAlex) {
        List<String> images = new LinkedList<>();
        File file = new File(photoFolder);
        if (file.exists()) {
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    boolean isValidImage = file != null && file.isFile() && file.length() > 1000;
                    if (isValidImage && isOnlyAlex) {
                        return file.getName().contains("CMCC");
                    }
                    return isValidImage;
                }
            });
            if (files != null && files.length > 0) {
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File file, File t1) {
                        return file.lastModified() <= t1.lastModified() ? 1 : -1;
                    }
                });
                for (File f : files) {
                    images.add(f.getAbsolutePath());
                }
            }
        }
        return images;
    }
}
