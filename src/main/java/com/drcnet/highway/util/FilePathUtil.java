package com.drcnet.highway.util;

public class FilePathUtil {

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

}
