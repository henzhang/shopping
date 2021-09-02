package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.utils.FastDFSUtils;
import entity.Result;
import entity.StatusCode;
import org.csource.common.MyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws Exception{
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),       // 文件名字
                file.getBytes(),                  // 文件内容
                StringUtils.getFilenameExtension(file.getOriginalFilename())); // 文件扩展名
        String[] uploads = FastDFSUtils.upload(fastDFSFile);
        // 拼接访问地址   url = http://192.168.211.132:8080/group1/M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpeg
        String url = FastDFSUtils.getTrackerInfo()+"/"+uploads[0]+"/" + uploads[1];
        return new Result (true, StatusCode.OK,"上传成功!",url);
    }
}
