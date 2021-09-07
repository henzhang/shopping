package com.changgou.utils;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现
 * 1. 文件上传
 * 2. 文件下载
 * 3. 文件删除
 * 4. 文件获取
 * 5. tracker 信息获取
 * 6. storage 信息获取
 */
public class FastDFSUtils {

    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;

    private static StorageClient storageClient;

    /**
     * 加载 tracker链接信息
     */
    static {
        try {
            // 查找class path 文件下的路径
            String fileName = new ClassPathResource("fdfs_client.conf").getPath();
            ClientGlobal.init(fileName);

            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            storageClient = new StorageClient(trackerServer, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }


    /**
     * 上传文件
     *
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception {
        // 附加参数
        NameValuePair[] meta_list = new NameValuePair[2];
        meta_list[1] = new NameValuePair("拍摄地址", "上海");
        meta_list[0] = new NameValuePair("author", fastDFSFile.getAuthor());

        /**
         * 通过 storage client 访问 storage , 实现文件上传 , 并且获取文件上传后的存储信息
         * 1. 上传文件的字节数组
         * 2. 文件的扩展名
         * 3. 附加参数 比如：拍摄地址 北京 ...
         * return :
         * 1. uploads[0] :　storage group name       group1
         * 2.　uploads[１] :　storage file name      M00/02/44/XXX.jpg
         */
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploads;
    }


    /**
     * 获取文件
     *
     * @param groupName      文件组名    group1
     * @param remoteFileName 文件存储路径名字    M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpg
     * @return
     * @throws IOException
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
        FileInfo fileInfo = storageClient.get_file_info(groupName, remoteFileName);
        return fileInfo;
    }

    /**
     * 下载文件
     *
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception {
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 删除文件
     *
     * @param groupName
     * @param remoteFileName
     * @throws Exception
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        storageClient.delete_file(groupName, remoteFileName);
    }


    /**
     * 获取 storage 信息
     *
     * @return
     * @throws Exception
     */
    public static StorageServer getStorages() throws Exception {
        return trackerClient.getStoreStorage(trackerServer);
    }


    /**
     * 获取 tracker 的 ip 和 端口 信息
     *
     * @return String
     * @throws Exception
     */
    public static String getTrackerInfo() throws Exception {
        // tracker的ip 和 http端口
        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port(); // 8080
        String url = "http://" + ip + ":" + tracker_http_port;
        return url;
    }


    /**
     * 获取 storage 的 ip 和 端口 信息
     *
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception {
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }


    public static void main(String[] args) throws Exception {
        // 获取文件
        //FileInfo file = getFile("group1", "xxx");
        //System.out.println(file.getSourceIpAddr());
        //System.out.println(file.getFileSize());

        //文件下载
        //InputStream is = downloadFile("group1", "xxx");
        ////将文件写入本地磁盘
        //FileOutputStream os = new FileOutputStream("D:/1.jpg");
        ////定义一个缓冲区
        //byte[] buffer = new byte[1024];
        //while(is.read(buffer)!=-1){
        //    os.write(buffer);
        //}
        //os.flush();
        //os.close();
        //is.close();


    }


}
