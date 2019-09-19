package com.drcnet.highway.util;

import com.drcnet.highway.enums.FilePathEnum;

import java.io.File;
import java.io.IOException;

public class FilePathUtil {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    public static String getFilePath(String path, FilePathEnum pathEnum) {
        String pathPrev;
        if (isWindows()) {
            pathPrev = pathEnum.windows;
        } else {
            pathPrev = pathEnum.linux;
        }
        return pathPrev + path;
    }

    public static void createPath(String ... paths) throws IOException {
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()){
                dir.mkdirs();
            }
            if (!dir.canWrite() && !FilePathUtil.isWindows()){
                Runtime.getRuntime().exec("chmod 777 -R " + path);
            }
        }
    }
}
