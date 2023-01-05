package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 上传文件
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;
    /**
     * MultipartFile是spring中输入流的封装类  file名需与前端发送的数据的名一直   name="file"
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());
        //获取上传图片的文件名
        String originalFilename = file.getOriginalFilename();
        //获取原文件名的后缀.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //为防止存入到指定位置重名，UUID生成新ID
        String filename = UUID.randomUUID().toString() + suffix;
        //判断basePath中的文件夹是否存在
        File dir = new File(basePath);
        if (!dir.exists()){
            dir.mkdir();
        }
        //将文件转存到指定位置
        try {
            file.transferTo(new File(basePath +filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }

    /**
     * 从浏览器端访问服务器端的图片
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //创建输入流，通过输入流读取
            FileInputStream fileInputStream = new FileInputStream(basePath + name);
            //创建输出流，通过输出流写回到浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //设置响应内容为图片
            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){//读取输入流读取到bytes中
                outputStream.write(bytes,0,len);      //输出流写响应给前端
            }
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
