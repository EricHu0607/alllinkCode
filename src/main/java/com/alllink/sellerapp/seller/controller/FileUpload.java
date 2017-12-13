package com.alllink.sellerapp.seller.controller;

import com.alllink.commons.utils.RandomNumberUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/file")
public class FileUpload {

    //头像上传
    @RequestMapping(value = "/image", method = RequestMethod.POST)
    @ResponseBody
    public HashMap<String, String> imageUpload(MultipartFile file,String sellerId, HttpServletRequest request) throws IOException {
       //String contextPath = request.getSession().getServletContext().getRealPath("/");//("/WEB-INF/update");
System.out.println(sellerId);
        String path = "D:\\image\\";
        System.out.println("path:" + path);

        //获取上传文件名称
        String fileName = file.getOriginalFilename();
        System.out.println("fileName:" + fileName);

        //获取文件名hashCode
        int hashCode = fileName.hashCode();
        //获取hashCode的低4位，并转换成16进制字符串
        String dir1 = Integer.toHexString(hashCode & 0xF);
        //获取hashCode的低5~8位，并转换成16进制字符串
        String dir2 = Integer.toHexString(hashCode >>> 4 & 0xF);
        //与文件保存目录连接成完整路径
        path = path + "/" + dir1 + "/" + dir2;
        //文件名重命名，UUID_fileName方式
        fileName = RandomNumberUtil.CreateUUID() + "_" + fileName;
        System.out.println("path:" + path + "fileName: " +fileName);

        File dir = new File(path, fileName);
        System.out.println("dir.exists():" + dir);

        if (!dir.exists()) {// 判断目录是否存在
            dir.mkdirs();//创建文件夹
        }
        file.transferTo(dir);

//        JSONObject jsonObj = new JSONObject();
//        jsonObj.put("url",dir.toString());
//        return jsonObj;
        HashMap<String, String> map = new HashMap<>();
        System.out.println(dir.toString().substring(10));
        map.put("url","\\pic\\" + dir.toString().substring(10));
        return map;
    }

    //多文件上传
    @RequestMapping(value = "/filesUpload", method = RequestMethod.POST)
    @ResponseBody
    public List<String> FilesUpload(MultipartFile multipartFiles[], HttpServletRequest request) throws IOException {

        List<String> newFileNames = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            //文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            String newFileName = null;
            if (multipartFile != null && originalFilename != null && originalFilename.length() > 0) {
                newFileName = RandomNumberUtil.CreateUUID() + "_" + originalFilename;
                //存储图片的物理路径
                String path = "D:\\image\\";
                //获取文件名hashCode
                int hashCode = originalFilename.hashCode();
                //获取hashCode的低4位，并转换成16进制字符串
                String dir1 = Integer.toHexString(hashCode & 0xF);
                //获取hashCode的低5~8位，并转换成16进制字符串
                String dir2 = Integer.toHexString(hashCode >>> 4 & 0xF);
                //与文件保存目录连接成完整路径
                path = path + "/" + dir1 + "/" + dir2;
                File dir = new File(path, newFileName);
                if (!dir.exists()) {// 判断目录是否存在
                    dir.mkdirs();//创建文件夹
                }
                multipartFile.transferTo(dir);
                newFileNames.add("\\pic\\" + dir.toString().substring(10));
            }
        }
        return newFileNames;
    }

}