package cn.itcast.core.controller.upload;

import cn.itcast.core.entity.Result;
import cn.itcast.core.fastdfs.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file){

        //通过工具类,将文件上传到FastDFS
        try {
            String conf = "classpath:FastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);

            //获取文件名
            String originalFilename = file.getOriginalFilename();//xxx.jpg

            //获取文件扩展名
            String extName = FilenameUtils.getExtension(originalFilename);

            //文件上传
            String path = fastDFSClient.uploadFile(file.getBytes(), extName, null);

            //设置url
            String url = FILE_SERVER_URL+path;
            return new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }

    }
}
