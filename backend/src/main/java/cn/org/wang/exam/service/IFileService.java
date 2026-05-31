package cn.org.wang.exam.service;

import org.springframework.web.multipart.MultipartFile;

import cn.org.wang.exam.common.result.Result;

/**
 * 说明：
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/3/21 10:43 PM
 */
public interface IFileService {

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 返回上传后的地址
     */
    Result<String> uploadImage(MultipartFile file);
}
