package com.changgou.search.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.utils.FastDFSUtils;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws Exception {
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),       // 文件名字 1.jpg
                file.getBytes(),                  // 文件内容
                StringUtils.getFilenameExtension(file.getOriginalFilename())); // 文件扩展名 jpg
        String[] uploads = FastDFSUtils.upload(fastDFSFile);
        /**
         * 拼接访问地址
         * url = http://192.168.211.132:8080/group1/M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpg
         *
         * 服务器ip : 192.168.211.132
         * nginx port : 8080
         *
         * 这里每次访问的端口是8080端口，访问的端口其实是storage容器的nginx端口，如果想修改该端口可以直接进入到storage容器，然后修改即可。
         * docker exec -it storage  /bin/bash
         * vi /etc/nginx/conf/nginx.conf
         * 修改后重启storage即可
         *
         */
        String url = FastDFSUtils.getTrackerInfo() + "/" + uploads[0] + "/" + uploads[1];
        return new Result(true, StatusCode.OK, "上传成功!", url);
    }
}
