package com.shejiao.common.utils.file;

import com.shejiao.common.config.shejiaoConfig;
import com.shejiao.common.constant.Constants;
import com.shejiao.common.utils.StringUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * 图片处理工具类
 *
 * @Author shejiao
 */
public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    public static byte[] getImage(String imagePath) {
        InputStream is = getFile(imagePath);
        try {
            if (is != null) {
                return IOUtils.toByteArray(is);
            }
        } catch (Exception e) {
            log.error("图片加载异常 {}", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return null;
    }

    public static InputStream getFile(String imagePath) {
        try {
            byte[] result = readFile(imagePath);
            if (result != null) {
                result = Arrays.copyOf(result, result.length);
                return new ByteArrayInputStream(result);
            }
        } catch (Exception e) {
            log.error("获取图片异常 {}", e);
        }
        return null;
    }

    /**
     * 读取文件为字节数据
     *
     * @param url 地址
     * @return 字节数据
     */
    public static byte[] readFile(String url) {
        InputStream in = null;
        try {
            if (url.startsWith("http") || url.startsWith("https")) {
                // 网络地址
                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                urlConnection.setConnectTimeout(30 * 1000);
                urlConnection.setReadTimeout(60 * 1000);
                urlConnection.setDoInput(true);
                in = urlConnection.getInputStream();
            } else if (url.startsWith("data:image")) {
                // Base64编码的图片数据
                String base64Data = url.split(",")[1];
                byte[] data = java.util.Base64.getDecoder().decode(base64Data);
                in = new ByteArrayInputStream(data);
            } else {
                // 本机地址
                String uploadPath = shejiaoConfig.getUploadPath();
                String downloadPath;
                if (url.startsWith(Constants.RESOURCE_PREFIX)) {
                    // 包含资源前缀，移除前缀后拼接
                    String path = StringUtils.substringAfter(url, Constants.RESOURCE_PREFIX);
                    // 避免重复的upload路径
                    if (path.startsWith("/upload")) {
                        path = path.substring(7); // 移除 "/upload"
                    }
                    downloadPath = uploadPath + path;
                } else if (url.startsWith("/")) {
                    // 以斜杠开头，直接拼接
                    downloadPath = uploadPath + url;
                } else {
                    // 相对路径，直接拼接
                    downloadPath = uploadPath + "/" + url;
                }
                in = new FileInputStream(downloadPath);
            }
            return IOUtils.toByteArray(in);
        } catch (Exception e) {
            log.error("获取文件路径异常 {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
