package com.drcnet.highway.util;

import com.drcnet.highway.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/26 13:45
 * @Description: 文件下载
 */
@Slf4j
public class DownloadUtil {

    private DownloadUtil() {
    }


    /**
     * 通过输入流和文件名获得文件下载的实体，在restful接口上直接return
     *
     * @param inputStream 输入流
     * @param fileName    文件名
     * @return 响应实体
     * @throws IOException 读取成为byte[]可能发生异常
     */
    public static ResponseEntity<byte[]> download(InputStream inputStream, String fileName) throws IOException {
        byte[] bytes = input2byte(inputStream);
        return download(bytes,fileName);
    }

    /**
     * 通过输入流和文件名获得文件下载的实体，在restful接口上直接return
     *
     * @param fileName    文件名
     * @return 响应实体
     */
    public static ResponseEntity<byte[]> download(byte[] bytes, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            log.error("{}",e);
            throw new MyException();
        }
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * 输入流转换成byte数组
     *
     * @param inStream 输入流
     * @return
     * @throws IOException
     */
    public static byte[] input2byte(InputStream inStream)
            throws IOException {
        try (ByteArrayOutputStream swapStream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[4096];
            int rc;
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        }
    }

}
