/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.ws.rest;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import com.supinfo.fileup.service.MyFileService;
import com.supinfo.fileup.service.ServerService;
import com.supinfo.fileup.service.UserService;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jms.Message;
import javax.mail.internet.ContentDisposition;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 *
 * @author Siqi DU
 */
@Stateless
@Path("/files")
public class FileResourceREST {

    @EJB
    private MyFileService myFileService;

    @EJB
    private UserService userService;

    @EJB
    private ServerService serverService;

    @Resource
    private WebServiceContext webServiceContext;

    private String webFolder = "D:\\Senior\\France\\4JVA\\Workspace_NetBeans\\FileUpV\\build\\web\\";

    private boolean deleteDir(String dirPath, User user) {
        System.out.println("当前函数为：deleteDir..................");
        Boolean flag = true;
        List<MyFile> files = myFileService.getFilesByDirectory(dirPath);

        for (MyFile mf : files) {
            if (mf.getType().equals("file")) {
                System.out.println("deletefile::::::");
                System.out.println(mf.getMyfilepath());
//                System.out.println("delete dir: 删除file前用户size" + mf.getMyfilename() + currentUser.getUsedSpace());
                user.setUsedSpace(user.getUsedSpace() - mf.getFileSize());
                userService.updateUser(user);
//                System.out.println("delete dir: 删除file后用户size" + mf.getMyfilename() + currentUser.getUsedSpace());
                myFileService.deleteFile(mf);
                File f = new File(mf.getMyfilepath());
                flag = f.delete();

                if (!flag) {
                    System.out.println("在1出return");
                    return false;
                }
            }
            if (mf.getType().equals("folder")) {
                System.out.println("Try To deletefolder::::::");
                System.out.println(mf.getMyfilepath());
                flag = deleteDir(mf.getMyfilepath(), user);

                if (!flag) {
                    System.out.println("在2出return");
                    return false;
                }
            }
        }

        //删除当前目录  
        if (true) {
            try {
                Files.delete(Paths.get(dirPath));
                System.out.println("deletefolder:::::::::::::::::::::::::::::::::::::::::");
                System.out.println(dirPath);
                myFileService.deleteFile(myFileService.GetFileByAbsolutePath(dirPath));
                return true;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("在3出return");
                return false;
            }

        }
        return false;

    }

    /**
     * 创建ZIP文件
     *
     * @param sourcePath 文件或文件夹路径
     * @param zipPath 生成的zip文件存在路径（包括文件名）
     */
    public static void createZip(String sourcePath, String zipPath) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipPath);
            zos = new ZipOutputStream(fos);
            zos.setEncoding("utf-8");//此处修改字节码方式。  
            writeZip(new File(sourcePath), "", zos);
        } catch (FileNotFoundException e) {
            System.out.println("create zip file failed!");;
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                System.out.println("create zip file failed!");;
            }

        }
    }

    private static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.exists()) {
            if (file.isDirectory()) {//处理文件夹  
                parentPath += file.getName() + File.separator;
                File[] files = file.listFiles();
                if (files.length != 0) {
                    for (File f : files) {
                        writeZip(f, parentPath, zos);
                    }
                } else {       //空目录则创建当前目录  
                    try {
                        zos.putNextEntry(new ZipEntry(parentPath));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block  
                        e.printStackTrace();
                    }
                }
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    ZipEntry ze = new ZipEntry(parentPath + file.getName());
                    zos.putNextEntry(ze);
                    byte[] content = new byte[1024];
                    int len;
                    while ((len = fis.read(content)) != -1) {
                        zos.write(content, 0, len);
                        zos.flush();
                    }

                } catch (FileNotFoundException e) {
                    System.out.println("create zip file failed!");;
                } catch (IOException e) {
                    System.out.println("create zip file failed!");;
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException e) {
                        System.out.println("create zip file failed!");;
                    }
                }
            }
        }
    }

    @GET
    public String sayHello() {
        return "hello FileResourceREST";
    }

    //得到目标file的Id
    @GET
    @Path("/{username}/getFile")
    @Produces(MediaType.APPLICATION_JSON)
    public MyFile getFileByFilePath(
            @PathParam("username") String username,
            @QueryParam("filePath") String filePath) {

        try {
            User user = userService.getUserByName(username);
            if (user.getIsLoggedin() != true) {
                System.out.println("用户已登陆");
                return null;
            }

            String userHome = serverService.getServerById(user.getServerId()).getServerPath() + username;
            System.out.println("踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩" + userHome);
            //filePath 为空，返回userHome 
            String fileFullPath = userHome + filePath;
            System.out.println("输出fileFullPath REST：" + fileFullPath);

            return myFileService.GetFileByAbsolutePath(fileFullPath);

        } catch (Exception e) {
            System.out.println("用户不存在");
            return null;
        }

    }

    @GET
    @Path("/{username}/getFiles")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MyFile> getFilesByFolderId(
            @PathParam("username") String username,
            @QueryParam("folderId") String folderId) {

        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户已登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        return myFileService.getFilesByDirectory(myFileService.getFileById(Long.parseLong(folderId)).getMyfilepath());
    }

    @POST
    @Path("/{username}/addFolder")
    @Produces(MediaType.APPLICATION_JSON)
    public MyFile addNewFolder(
            @PathParam("username") String username,
            @FormParam("currentFolderId") String currentFolderId,
            @FormParam("addFolderName") String addFolderName) {

        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户已登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        File outputFile = new File(myFileService.getFileById(Long.parseLong(currentFolderId)).getMyfilepath(), addFolderName);
        if (myFileService.GetFileByAbsolutePath(outputFile.getPath()) != null) {
            System.out.println("新添加的folder已存在");
            return null;
        }

        outputFile.mkdir();
        MyFile myFolder = new MyFile(addFolderName, outputFile.getPath(), outputFile.getParent(), "folder", userService.getUserByName(username).getId());
        myFolder.setMyfilerelativepath(myFolder.getMyfilepath().replace(serverService.getServerById(user.getServerId()).getServerPath(), ""));
        myFolder.setFileSize(0l);
        myFileService.addFile(myFolder);

        return myFolder;
    }

    @POST
    @Path("/{username}/rename")
    @Produces(MediaType.APPLICATION_JSON)
    public MyFile rename(@PathParam("username") String username, @FormParam("fileId") String fileId, @FormParam("renameFileName") String renameFileName) {
        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户已登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        MyFile fileToRename = myFileService.getFileById(Long.parseLong(fileId));
        String newFullPath = fileToRename.getMyfileparent() + "\\" + renameFileName;

        if (myFileService.GetFileByAbsolutePath(newFullPath) != null) {
            System.out.println("重命名的文件在此文件夹中已存在");
            return null;
        }

        if (fileToRename.getType().equals("file")) {
            System.out.println("得到的待重命名是文件在 web service里");
            File f = new File(fileToRename.getMyfilepath());
            f.renameTo(new File(newFullPath));
            //在数据库中对文件重命名
            fileToRename.setMyfilename(renameFileName);
            fileToRename.setMyfilepath(newFullPath);
            fileToRename.setEditable(Boolean.FALSE);

            myFileService.update(fileToRename);

            return fileToRename;
        }

        if (fileToRename.getType().equals("folder")) {
            System.out.println("得到的待重命名的是文件夹  在web service里");
            //重命名文件夹
            //在folder没有rename前得到folder内的文件列表
            List<MyFile> filesToRename = myFileService.getFilesByDirectory(fileToRename.getMyfilepath());
            //对 filesToRename 所有文件在数据库中进行重命名并merge
            File f = new File(fileToRename.getMyfilepath());
            f.renameTo(new File(newFullPath));

            //在数据库里更改并merge folder
            fileToRename.setMyfilename(renameFileName);
            fileToRename.setMyfilepath(newFullPath);
            fileToRename.setEditable(Boolean.FALSE);
            myFileService.update(fileToRename);

            String parentOfFolder = fileToRename.getMyfileparent();
            String renameParentPath = parentOfFolder + "\\" + renameFileName;

            for (MyFile mf : filesToRename) {
                List<MyFile> filessToRename = myFileService.getFilesByDirectory(mf.getMyfilepath());

                File f1 = new File(mf.getMyfilepath());

                String renameFilePath = renameParentPath + "\\" + mf.getMyfilename();
                mf.setMyfileparent(renameParentPath);
                mf.setMyfilepath(renameFilePath);
                mf.setEditable(Boolean.FALSE);
                myFileService.update(mf);
                f1.renameTo(new File(renameFilePath));

                if (mf.getType().equals("folder")) {
                    renameFilesInFolder(mf, filessToRename);
                }
            }
        }
        return fileToRename;
    }

    private void renameFilesInFolder(MyFile folderFile, List<MyFile> filesToRename) {

        for (MyFile file : filesToRename) {
            String renameFilePath = folderFile.getMyfilepath() + "\\" + file.getMyfilename();
            List<MyFile> filessToRename = myFileService.getFilesByDirectory(file.getMyfilepath());
            File f1 = new File(file.getMyfilepath());
            file.setMyfileparent(folderFile.getMyfilepath());
            file.setMyfilepath(renameFilePath);
            file.setEditable(Boolean.FALSE);
            myFileService.update(file);
            f1.renameTo(new File(renameFilePath));
            if (file.getType().equals("folder")) {
                renameFilesInFolder(file, filessToRename);
            }
        }
    }

    @POST
    @Path("/{username}/share")
    @Produces(MediaType.APPLICATION_JSON)
    public String share(@PathParam("username") String username, @FormParam("shareId") String shareFileId) {
        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户已登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        MyFile myFile = myFileService.getFileById(Long.parseLong(shareFileId));
        //共享文件路径
        String shareString = serverService.getServerById(userService.getUserById(myFile.getUserId()).getServerId()).getServerIPandFolderString()
                + "\\" + myFile.getMyfilerelativepath()
                + "\n username: root"
                + "\n password: root";

        System.out.println("输出共享路径：   " + shareString);

        return shareString;
    }

    @GET
    @Path("/{username}/delete/{fileId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MyFile delete(@PathParam("username") String username, @PathParam("fileId") String fileId) {
        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户未登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        try {
            MyFile fileToDelete = myFileService.getFileById(Long.parseLong(fileId));
            
            if (fileToDelete.getType().equals("file")) {
                Files.delete(Paths.get(fileToDelete.getMyfilepath()));
                user.setUsedSpace(user.getUsedSpace() - fileToDelete.getFileSize());
                userService.updateUser(user);
                myFileService.deleteFile(fileToDelete);
                System.out.println("文件成功在文件夹删除");
                return fileToDelete;
            }

            if (fileToDelete.getType().equals("folder")) {
                if (deleteDir(fileToDelete.getMyfilepath(), user) == false) {
                    return null;
                } else {
                    return fileToDelete;
                }
            }

        } catch (Exception e) {
            System.out.println("***************FileController.delete.exception");
            return null;
        }
        return null;
    }

    @GET
    @Path("/{username}/download/{fileId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpServletResponse download(@PathParam("username") String username, @PathParam("fileId") String fileId, @Context HttpServletResponse response) throws FileNotFoundException {

        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户未登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }
        
        MyFile myFile = myFileService.getFileById(Long.parseLong(fileId));

        if (myFile.getType().equals("folder")) {

            String userHomeDownload = webFolder + username + "\\download";
            File srcFile = new File(myFile.getMyfilepath());
            File targetFile = new File(userHomeDownload + "\\" + myFile.getMyfilename() + ".zip");
            System.out.println("得到的下载路径为" + targetFile.getAbsolutePath());
            createZip(srcFile.getAbsolutePath(), targetFile.getAbsolutePath());

            try {
                response.reset();
                response.setContentType("APPLICATION/OCTET-STREAM");
                //response.setContentType("multipart/form-data"); 
                response.addHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(myFile.getMyfilename() + ".zip", "UTF-8"));//new String( ().getBytes("GB2312"),"ISO8859-1") 
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
                response.setHeader("Pragma", "public");

                if (!targetFile.exists()) {
                    System.out.println("文件不存在~！");
                    PrintWriter out = response.getWriter();
                    out.println("文件 " + targetFile.getAbsolutePath() + " 不存在");
                }
                FileInputStream in = new FileInputStream(targetFile.getAbsolutePath());
                int zipfileSize = in.available();
                System.out.println("文件的长度为：" + zipfileSize);
                response.addHeader("content-Length", String.valueOf(zipfileSize));

                //创建输出流
                OutputStream out = response.getOutputStream();
                //创建缓冲区
                HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
                wrapper.setBufferSize(zipfileSize);
                byte buffer[] = new byte[1024];

                int len = 0;
//                in.read(buffer);
                //循环将输入流中的内容读取到缓冲区当中
                while ((len = in.read(buffer)) > 0) {
                    //输出缓冲区的内容到浏览器，实现文件下载
                    out.write(buffer, 0, len);
                }
//                //System.out.println(Arrays.toString(buffer));
                //关闭输入流  
                in.close();
                //刷新输出缓冲  
                out.flush();
                out.close();
                FacesContext.getCurrentInstance().responseComplete(); //--非常关键，否则提示文件损坏
                Files.delete(Paths.get(targetFile.getAbsolutePath()));

                return response; //return (Response)response;

            } catch (Exception e) {
                //printStackTrace();
                System.out.println("Something went wrong");
            }

        }

        //下载文件
        if (myFile.getType().equals("file")) {

            //得到要下载的文件
            String saveFullPath = myFile.getMyfilepath();
            System.out.println("得到的下载路径为" + saveFullPath);
            File file = new File(saveFullPath);
            //file.createNewFile();
            try {
                response.reset();
                response.setContentType("APPLICATION/OCTET-STREAM");
                //response.setContentType("multipart/form-data"); 
                response.addHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(myFile.getMyfilename(), "UTF-8"));//new String( ().getBytes("GB2312"),"ISO8859-1") 

                if (!file.exists()) {
                    System.out.println("文件不存在~！");
                    PrintWriter out = response.getWriter();
                    out.println("文件 " + saveFullPath + " 不存在");
                } else {
                    System.out.println("//读取要下载的文件并保存到文件输出流");

                    FileInputStream in = new FileInputStream(saveFullPath);
                    //FileOutputStream fout = new FileOutputStream(userHomeDownload + "\\" + myFile.getName());
                    int fileSize = in.available();
                    System.out.println(fileSize);
                    response.addHeader("content-Length", String.valueOf(fileSize));
                    System.out.println("创建文件输出流 response.getOutputStream()");
                    //创建输出流
                    OutputStream out = response.getOutputStream();
                    System.out.println("判断response是否已被commited::::" + response.isCommitted());
                    //创建缓冲区

                    HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
                    wrapper.setBufferSize(fileSize);

                    byte[] buffer = new byte[1024];
                    System.out.println("创建buffer成功， 输出buffer" + buffer.toString());
//                    in.read(buffer, 0, fileSize);
//                    out.write(buffer);
//                    System.out.println("创建buffer成功， 输出buffer" + buffer.toString());
                    int len = 0;
                    //循环将输入流中的内容读取到/doload中
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    System.out.println(out.toString());

                    //刷新输出缓冲  
                    out.flush();
                    //关闭输入流  
                    in.close();
                    out.close();
                    //fout.close();
                    FacesContext.getCurrentInstance().responseComplete(); //--非常关键，否则提示文件损坏
                    return response;
                }
            } catch (Exception e) {
                //printStackTrace();
                System.out.println("something went wrong");
            }
        }
        return null;
    }

    @POST
    @Path("/{username}/uploadTo/{currentFolderId}")
    @Produces("application/json;charset=UTF-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public MyFile upload(
            @PathParam("username") String username,
            @PathParam("currentFolderId") String currentFolderId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @FormDataParam("file") InputStream fileInputStream
    //            @FormDataParam("file") FormDataContentDisposition fileInputDetails
    ) throws IOException, ServletException, FileUploadException {

        User user = userService.getUserByName(username);
        try {
            if (user.getIsLoggedin() == false) {
                System.out.println("用户未登陆");
                return null;
            }
        } catch (Exception e) {
            System.out.println("用户不存在");
        }

        System.out.println(request.getRemoteAddr());  //clientIP
        System.out.println(request.getRemotePort());  //55608

//        request.setCharacterEncoding("utf-8");
//        response.setContentType("text/html; charset=UTF-8");
        String fileName = request.getHeader("Filename");
        System.out.println(fileName);
        int fileSize = request.getContentLength();
//        String headerInfo = request.getHeader("Content-Disposition");
//        //从HTTP头信息中获取文件名fileName=（文件名）  
//        String fileNamew = headerInfo.substring(headerInfo.lastIndexOf("=") + 2, headerInfo.length() - 1);
        String currentFolderPath = myFileService.getFileById(Long.parseLong(currentFolderId)).getMyfilepath();
        System.out.println(currentFolderPath);
        File outputFile = new File(currentFolderPath, fileName);
        String saveFileFullPath = outputFile.getAbsolutePath();
        System.out.println(saveFileFullPath);
        outputFile.createNewFile();

//        OutputStream out = new FileOutputStream(new File(saveFileFullPath));
//        byte[] buffer = new byte[1024];
//        int bytes = 0;
//        while ((bytes = fileInputStream.read(buffer)) != -1) {
//            out.write(buffer, 0, bytes);
//        }
//        //        out.flush();
//        out.close();
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String str = null;
        List<String> lines = new ArrayList<>();
        while ((str = bufferedReader.readLine()) != null) {
            System.out.println(str);
            lines.add(str);
        }
        int tt = lines.size();
        System.out.println(tt);
        lines.remove(0);
        lines.remove(0);
        lines.remove(0);
        lines.remove(0);
        lines.remove(tt - 5);
        FileOutputStream fos = new FileOutputStream(outputFile);
        for (String line2 : lines) {
            fos.write((line2 + "\r\n").getBytes());
            System.out.println(line2);
        }
        fos.close();
//        } catch (Exception e) {
        MyFile myfile = new MyFile(fileName, outputFile.getPath(), outputFile.getParent(), "file", user.getId());
        myfile.setFileSize(Long.parseLong("" + outputFile.getTotalSpace()));
        myFileService.addFile(myfile);
        return myfile;
//        }

//        MyFile myfile = new MyFile(fileName, outputFile.getPath(), outputFile.getParent(), "file", user.getId());
//        myfile.setFileSize(outputFile.getTotalSpace());
//        myFileService.addFile(myfile);
//        return myfile;
    }

}
