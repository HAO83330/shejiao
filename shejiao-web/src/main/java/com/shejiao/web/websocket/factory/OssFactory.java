package com.shejiao.web.websocket.factory;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author shejiao
 */
public interface OssFactory {

    String save(MultipartFile file);

    Boolean delete(String path);
}
