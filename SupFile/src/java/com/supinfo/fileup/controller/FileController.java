/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.controller;

import com.sun.xml.rpc.processor.modeler.j2ee.xml.trueFalseType;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import com.supinfo.fileup.service.MyFileService;
import com.supinfo.fileup.service.ServerService;
import com.supinfo.fileup.service.UserService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.print.attribute.standard.Severity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Siqi DU
 */
@ManagedBean
@SessionScoped
public class FileController implements Serializable {

    @ManagedProperty(value = "#{userController}")
    UserController userController;

    @EJB
    private MyFileService myfileService;
    @EJB
    private UserService userService;
    @EJB
    private ServerService serverService;

    private SimpleDateFormat df;

    private User currentUser;
    private UploadedFile upfile;
    private StreamedContent downfile;
    private List<MyFile> fileList;

    private String addFolderName;
    private String re_name;
    private String re_name1;
    private String error;
    private String searchText;
    private String method; //copy or cut

    private MyFile currentFile;
    private MyFile fileToCopyOrCut;
    //    private String currentPage;
//    private String currentFileRelative; //  username/upload/filefolder/file.mp3
//    private String currentFileType;

    private String currentFolderRelative;
    private String currentFolderAbsolute;
    private String currentFolder;
    private String rootPath;  //  rootPath of storage servers
    private String webRootPath;
    private String shareString;

    private String userHomeUpload;  // /username/upload
    private String userHomeDownload; // /username/download
    private String userHome;  // /username

    private List<MyFile> pictureList;
    private List<MyFile> videoList;
    private List<MyFile> audioList;
    private List<MyFile> searchResult;

    private String showPage;  //could be : all_files  all_audios all_videos all_pics upload
    private Boolean showSharePage;
    
    
    @PostConstruct
    public void createUserFolder() {

        String username = getCurrentUser().getUsername();
        Long id = getCurrentUser().getId();

        userHome = getRootPath() + username;
        userHomeUpload = userHome + "\\" + "upload";
        userHomeDownload = userHome + "\\" + "download";

        if (myfileService.GetFileByAbsolutePath(userHome) == null) {

            File userFile = new File(userHome);
            File userHomeUp = new File(userHomeUpload);
            File userHomeDown = new File(userHomeDownload);

            userFile.mkdir();
            userHomeUp.mkdir();
            userHomeDown.mkdir();
            //System.out.println("新建两个用户文件夹：" + userHomeUp.getPath() + userHomeDown.getPath());

            MyFile myFileUser = new MyFile(userFile.getName(), userFile.getPath(), userFile.getParent(), "folder", id);
            myFileUser.setFileSize(0l);
            MyFile myFileuserHomeUp = new MyFile(userHomeUp.getName(), userHomeUp.getPath(), userHomeUp.getParent(), "folder", id);
            myFileuserHomeUp.setFileSize(0l);
            MyFile myFileuserHomeDown = new MyFile(userHomeDown.getName(), userHomeDown.getPath(), userHomeDown.getParent(), "folder", id);
            myFileuserHomeDown.setFileSize(0l);

            myfileService.addFile(myFileUser);
            myfileService.addFile(myFileuserHomeUp);
            myfileService.addFile(myFileuserHomeDown);
        }

        //在一开始将用户当前文件夹指向/upload
        currentFolderAbsolute = userHomeUpload;
        showPage = "all_files";
        getWebRootPath();

    }

    /**
     * //为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名 private String makeFileName(String
     * filename) { return UUID.randomUUID().toString() + "_" + filename; }
     */
    public Boolean addMessage(FacesMessage.Severity severity, String name, String details) {

        FacesMessage message = new FacesMessage(severity, name, details);
        FacesContext.getCurrentInstance().addMessage("messageUser", message);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        System.out.println("在addMessage（）输出message信息：" + message.getDetail());

        return true;
    }

    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    /**
     * 压缩文件
     *
     * @param srcfile : source folder full path
     * @parm targetFile : target file full path
     */
    public void zipFiles(File srcfile, File targetFile) {

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(targetFile));

            if (srcfile.isFile()) {
                zipFile(srcfile, out, "");
            } else {
                File[] list = srcfile.listFiles();
                for (int i = 0; i < list.length; i++) {
                    compress(list[i], out, "");
                }
            }

            System.out.println("压缩完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩文件夹里的文件 起初不知道是文件还是文件夹--- 统一调用该方法
     *
     * @param file
     * @param out
     * @param basedir
     */
    private void compress(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            this.zipDirectory(file, out, basedir);
        } else {
            this.zipFile(file, out, basedir);
        }
    }

    /**
     * 压缩文件夹
     *
     * @param dir
     * @param out
     * @param basedir
     */
    public void zipDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            /* 递归 */
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /**
     * 压缩单个文件
     *
     * @param srcfile
     */
    public void zipFile(File srcfile, ZipOutputStream out, String basedir) {
        if (!srcfile.exists()) {
            return;
        }

        byte[] buf = new byte[1024];
        FileInputStream in = null;

        try {
            int len;
            in = new FileInputStream(srcfile);
            out.putNextEntry(new ZipEntry(basedir + srcfile.getName()));

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.closeEntry();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

//    private void copyFileUsingIterator(File source, File dest) {
//
//        try {
//            int i = 0;
//            LineIterator it = FileUtils.lineIterator(source, "UTF-8");
//            while (it.hasNext()) {
//                System.out.println(i++);
//                String line = it.nextLine();
//                /// 进行处理
//                FileUtils.write(dest, line, "UTF-8");
//
//            }
//            LineIterator.closeQuietly(it);
//        } catch (Exception e) {
//            System.out.println("yayayyayyayayayyayayyaa");
//        }
//    }
//    private boolean deleteDir(String dirPath) {
//        Boolean flag = true;
//        File folder = new File(dirPath);
//        File[] files = folder.listFiles();
////        List<MyFile> myfiles = myfileService.getFilesByDirectory(dirPath);
//        for (File f : files) {
//            if (f.isFile()) {
//                myfileService.deleteFile(myfileService.GetFileByAbsolutePath(f.getPath()));
//                flag = f.delete();
//                if (!flag) {
//                    break;
//                }
//            }
//            if (f.isDirectory()) {
//                myfileService.deleteFile(myfileService.GetFileByAbsolutePath(f.getPath()));
//                flag = deleteDir(f.getAbsolutePath());
//                if (!flag) {
//                    break;
//                }
//            }
//        }
//        if (!flag) {
//            return false;
//        }
//        //删除当前目录  
//        if (folder.delete()) {
//            myfileService.deleteFile(myfileService.GetFileByAbsolutePath(dirPath));
//            return true;
//        } else {
//            return false;
//        }
//    }
    private boolean deleteDir(String dirPath) {
        System.out.println("当前函数为：deleteDir..................");
        Boolean flag = true;
//        File folder = new File(dirPath);
        List<MyFile> files = myfileService.getFilesByDirectory(dirPath);

        for (MyFile mf : files) {
            if (mf.getType().equals("file")) {
                System.out.println("deletefile::::::");
                System.out.println(mf.getMyfilepath());
//                System.out.println("delete dir: 删除file前用户size" + mf.getMyfilename() + currentUser.getUsedSpace());
                currentUser.setUsedSpace(currentUser.getUsedSpace() - mf.getFileSize());
                userService.updateUser(currentUser);
//                System.out.println("delete dir: 删除file后用户size" + mf.getMyfilename() + currentUser.getUsedSpace());
                myfileService.deleteFile(mf);
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
                flag = deleteDir(mf.getMyfilepath());

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
                myfileService.deleteFile(myfileService.GetFileByAbsolutePath(dirPath));
                return true;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("在3出return");
                return false;
            }

        }
        return false;

    }

//    /**
//     * 程序自动打包
//     *
//     * @param zipFileName : 压缩后的文件名
//     * @param inputFolderPath 待压缩的folder路径
//     * @param newFolderPath 压缩后文件存放folder路径
//     * @return zipFileFullPath : 压缩后文件路径
//     */
//    public String createZip(String zipFileName, String inputFolderPath, String newFolderPath
//    ) {
//
//        ZipOutputStream zos = null;
//        FileOutputStream fos = null;
//        DataOutputStream dos = null;
//
//        String zipFileFullPath = myfileService.GetFileByAbsolutePath(inputFolderPath).getMyfileparent() + "\\" + zipFileName + ".zip";
//        System.out.println("打印zupFileFullPath " + zipFileFullPath);
////System.out.println("压缩后文件路径为 ：" + zipFileFullPath);
//
//        try {
//
//            File folder = new File(inputFolderPath);
//            File[] files = folder.listFiles(); //--得到指定目录下所有文件名
//            if (null != files) {
//                System.out.println("进入createZip的if");
//                int fileLen = files.length;
//                fos = new FileOutputStream(zipFileFullPath); //--打包后路径 + 文件名
//                dos = new DataOutputStream(fos);
//                zos = new ZipOutputStream(dos); //--定义zip输流
//
//                for (int i = 0; i < fileLen; i++) {
//                    System.out.println("进入createZip的for循环中");
//                    FileInputStream fis;
//                    DataInputStream dis;
//                    String tempfilePath = files[i].getAbsolutePath();  //--得到每个文件绝对路径 + 文件名
//                    System.out.println("此文件的临时路径为： " + tempfilePath);
//                    fis = new FileInputStream(tempfilePath);
//                    dis = new DataInputStream(fis);
//                    ZipEntry ze = new ZipEntry(files[i].getName()); //--解压文件后直接得到文件，没有目录结构
//                    zos.putNextEntry(ze);
//                    zos.setEncoding("UTF-8");    //--指定编码,否则文件名乱码
//
//                    int c = 0;
//                    while ((c = dis.read()) != -1) {
//                        //System.out.println("进入createZip的while循环中");
//                        zos.write(c);
//                    }
//
//                    zos.closeEntry();
//                    fis.close();
//                    dis.close();
//
//                }
//            }
//            zos.close();
//            dos.close();
//            fos.close();
//            System.out.println("完成reateZip");
//
//        } catch (Exception e) {
//            System.out.println("***************************createZip exception!!");
//        }
//        return zipFileFullPath;
//    }
//    public String zipAFolder(MyFile folderToZipFile, String newZipFolderPath) {
//        try {
//            Files.copy(Paths.get(folderToZipFile.getMyfilepath()), Paths.get(userHomeDownload));
//            List<MyFile> files = myfileService.getFilesByDirectory(folderToZipFile.getMyfilepath());
//            for(MyFile f: files){
//                if(f.getType().equals("file")){
//                    //输入到流里
//                }
//                if(f.getType().equals("folder")){
//                    
//                }
//            }
//        }catch(Exception e){
//            
//        }
//
//     }
//    public void copyFolderToDownload(MyFile folder) {
//        try {
//        } catch (Exception e) {
//        }
//    }
    
    public String share(MyFile myFile){
        
        shareString = serverService.getServerById(userService.getUserById(myFile.getUserId()).getServerId()).getServerIPandFolderString()
                + "\\" + myFile.getMyfilerelativepath()
                + "\n username: root"
                + "\n password: root";
        
        System.out.println("输出共享路径：   " + shareString);
        showSharePage = true;
        FacesContext.getCurrentInstance().addMessage("share", new FacesMessage("Share Path: ",shareString));
//        addMessage(FacesMessage.SEVERITY_INFO, "Share Path ", shareString);
        
        return "/all_files";
    }
    //每次点击上方niv:Files 
    public String gotoAll_FilesPage(String pageToShow) {
        if (pageToShow.equals("all_files")) {
            //将当前文件夹导入/upload
            currentFolderAbsolute = userHomeUpload;
            System.out.println("在从header进入all_files时的currentfolderAbsolute为：" + currentFolderAbsolute);
        }

        showPage = pageToShow;
        return "/all_files?faces-redirect=true";
    }

    public String goto_upload() {
        showPage = "upload";
        return "/all_files?faces-redirect=true";
    }

    public String uploadFile() {
        System.out.println(".............................filecontroller.uploadFile()");
        showPage = "all_files";
        //上传文件已在当前folder中存在
        for (MyFile f : fileList) {
            if (upfile.getFileName().equals(f.getMyfilename())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", upfile.getFileName() + " is Existed!");
                return "all_files?faces-redirect=true";
            }
        }

        //上传文件为空
        if (upfile == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", upfile.getFileName() + "is null!");
            return "/all_files?faces-redirect=true";
        }

        try {
            HttpServletRequest request = getHttpServletRequest();
            HttpServletResponse response = getHttpServletResponse();
//            HttpSession session = getHttpSession();

            //说明输入的请求信息采用UTF-8编码方式 
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=UTF-8");

            //上传到共享文件夹
            //将上传的文件名和路径以MyFile保存到数据库中
//            MyFile myfile = new MyFile(upfile.getFileName(), outputFile.getName(), outputFile.getPath(), outputFile.getParent(), "file");
            MyFile myfile = new MyFile(upfile.getFileName(), currentFolderAbsolute + "\\" + upfile.getFileName(), currentFolderAbsolute, "file", currentUser.getId());
            myfile.setMyfilerelativepath(myfile.getMyfilepath().replace(rootPath, ""));
            myfile.setFileSize(upfile.getSize());

            System.out.println("输出待上传文件信息："
                    + "\nid:" + myfile.getId()
                    + "\nfilename:" + myfile.getMyfilename()
                    + "\nfilePath:" + myfile.getMyfilepath()
                    + "\nfileRelativePath:" + myfile.getMyfilerelativepath()
                    + "\nfileSize:" + myfile.getFileSize());

            //设定用户额定容量  30G : 30 * Math.pow(2, 30)
            Long size = currentUser.getUsedSpace() + upfile.getSize();
            if (size <= 20 * Math.pow(2, 30)) {
                currentUser.setUsedSpace(size);
                userService.updateUser(currentUser);
                myfileService.addFile(myfile);

                //根据文件名创建路径 
//                File outputPath = new File(uploadPath);
                //在存储上创建文件
                File outputFile = new File(currentFolderAbsolute, upfile.getFileName());
                System.out.println("在存储上创建文件 输出outputFile的信息：" + outputFile.getPath() + outputFile.getParent());
                System.out.println(outputFile.getName());
//                if (!outputPath.exists()) {
//                    outputPath.mkdir();
//                }
//            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
//            wrapper.
                InputStream in = upfile.getInputstream();
                System.out.println((int) upfile.getSize());
                byte[] buffer = new byte[1024];
                FileOutputStream outStream = new FileOutputStream(outputFile);
                int len = 0;
                while ((len = in.read(buffer)) != -1) { //每次读取1024个字节
                    outStream.write(buffer, 0, len);
                }

                System.out.println("上传完成！！");
//                outStream.flush();
                in.close();
                outStream.close();
                //将文件拷贝到共享文件夹
//                Files.copy(Paths.get(myfile.getMyfilepath()), Paths.get("Z:\\" + myfile.getMyfilename()), StandardCopyOption.REPLACE_EXISTING);
                //C:\\Users\\Siqi DU\\AppData\\Roaming\\Microsoft\\Windows\\Network Shortcuts\\server2
                addMessage(FacesMessage.SEVERITY_INFO, "Succesful", upfile.getFileName() + " is uploaded.");
                return "/all_files?faces-redirect=true";

            } else {
                addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", currentUser.getUsername() + "has reached the maximam storage space");
                return "/all_files?faces-redirect=true";
            }

        } catch (Exception ex) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Unsucessful", "Invalid File Name!");
            System.out.println("********** FIleController.upload exception");
            System.out.println(ex);
            return null;
        }

    }

    public String cancelUpload() {
        showPage = "all_files";
        return "/all_files?faces-redirect=true";
    }

    public String addFolder() {
        System.out.println("........................filecontroller.addFolder()");

        for (MyFile f : fileList) {
            if (addFolderName.equals(f.getMyfilename())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", addFolderName + " is Existed!");
                return "all_files";
            }
        }

        try {
            HttpServletRequest request = getHttpServletRequest();
            HttpServletResponse response = getHttpServletResponse();
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=UTF-8");

            //根据文件名创建路径 
            File outputPath = new File(currentFolderAbsolute);
            //File outputFile = new File(currentFolderAbsolute, makeFileName(addFolderName));
            File outputFile = new File(currentFolderAbsolute, addFolderName);
            outputFile.mkdir();
            //System.out.println("新建的文件（夹）名字：" + outputFile.getName() + "\n 对话框输入的文件夹的名字:" + addFolderName);

            //如果upload文件夹不存在就创建
            if (!outputPath.exists()) {
                outputPath.mkdir();
            }

            MyFile myFolder = new MyFile(addFolderName, outputFile.getPath(), outputFile.getParent(), "folder", currentUser.getId());
            myFolder.setMyfilerelativepath(myFolder.getMyfilepath().replace(rootPath, ""));
            myFolder.setFileSize(0l);
            myfileService.addFile(myFolder);

            //将新建成的文件夹添加到fileList再传回View
            fileList.add(myFolder);

            addMessage(FacesMessage.SEVERITY_INFO, "Succesful", "New Folder " + outputFile.getName() + " added.");
//            currentPage = "all_files";
            return "all_files";

        } catch (Exception ex) {
            System.out.println("********** FIleController.upload exception");
//            System.out.println(ex);
            return null;
        }
    }

    public String delete(MyFile fileToDelete) {

        System.out.println("......................fileController.delete()");
        try {
//            System.out.println("输出待删除文件信息：\n"
//                    + "文件名" + fileToDelete.getMyfilename()
//                    + "\n 类型" + fileToDelete.getType()
//                    // + "\n UUID 文件名 " + fileToDelete.getMyfilenameUUID()
//                    + "\n文件路径" + fileToDelete.getMyfilepath()
//                    + "\n 属于文件夹" + fileToDelete.getMyfileparent());
            if (fileToDelete.getType().equals("file")) {
                Files.delete(Paths.get(fileToDelete.getMyfilepath()));
                currentUser.setUsedSpace(currentUser.getUsedSpace() - fileToDelete.getFileSize());
                userService.updateUser(currentUser);
                myfileService.deleteFile(fileToDelete);
                System.out.println("文件成功在文件夹删除");
            }

            if (fileToDelete.getType().equals("folder")) {
                if (deleteDir(fileToDelete.getMyfilepath()) == false) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "UnSuccessful", fileToDelete.getMyfilename() + " is not Deleted!");
                } else {
                    addMessage(FacesMessage.SEVERITY_INFO, "Successful", fileToDelete.getMyfilename() + " is Deleted!");
                }
            }

//            fileList.remove(fileToDelete);
            return "/all_files?faces-redirect=true";

        } catch (Exception e) {
            System.out.println("***************FileController.delete.exception");
            addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", fileToDelete.getMyfilename() + " is NOT Deleted!");
            return null;
        }
    }

    public String enter(MyFile fileToEnter) {
        System.out.println("................................fileController.enter()");
        if (fileToEnter.getType().equals("file")) {
            //online viewer  audio,video,txt,pictures
            currentFile = fileToEnter;
            if (fileToEnter.getFileSize() > 100000000) {
                addMessage(FacesMessage.SEVERITY_WARN, "WARNING", "File is too big, Please download to view");
                return "/all_files?faces-redirect=true";
            }
            try {
                System.out.println("将要拷贝到server端的文件夹： " + webRootPath + fileToEnter.getMyfilename());
                Files.copy(Paths.get(fileToEnter.getMyfilepath()), Paths.get(webRootPath + fileToEnter.getMyfilename()), StandardCopyOption.REPLACE_EXISTING);
//                File source = new File(fileToEnter.getMyfilepath());
//                File dest = new File(webRootPath + fileToEnter.getMyfilename());
                //copyFileUsingFileStreams(new File(fileToEnter.getMyfilepath()), new File(webRootPath + fileToEnter.getMyfilename()));

            } catch (Exception e) {
                System.out.println("在enter函数中不能拷贝文件");
            }
//            currentPage = page;
            return "view_online?faces-redirect=true";
        }
        if (fileToEnter.getType().equals("folder")) {
            this.currentFolderAbsolute = fileToEnter.getMyfilepath();
        }
//        currentPage = page;
        return "all_files";
    }

    public String backFromViewer() {

//        if (currentPage.equals("all_files")) {
//            System.out.println("............................从viewer返回all files 页面");
        File file = new File(webRootPath + currentFile.getMyfilename());
        file.delete();
        return "/all_files?faces-redirect=true";
//        }
//        if (currentPage.equals("all_pics")) {
//            return "/all_pics?faces-redirect=true";
//        }
//        if (currentPage.equals("all_videos")) {
//            return "/all_videos?faces-redirect=true";
//        }
//        if (currentPage.equals("all_audios")) {
//            return "/all_audios?faces-redirect=true";
//        }
//        if (currentPage.equals("search_result")) {
//            return "/search_result?faces-redirect=true";
//        }

//        return null;
    }

    public String back() {
        System.out.println("................返回到当前文件夹： " + currentFolderAbsolute);
        String parent = myfileService.GetFileByAbsolutePath(currentFolderAbsolute).getMyfileparent();
        String ParentOfParent = myfileService.GetFileByAbsolutePath(parent).getMyfileparent() + "\\";
        System.out.println("parentofparent 是" + ParentOfParent);
        if (!ParentOfParent.equals(getRootPath())) {
            currentFolderAbsolute = parent;
            System.out.println("。。。。。。。。。。。。。。的父文件夹" + currentFolderAbsolute);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Note ", "It is the First Page!");
        }
        return "/all_files?faces-redirect=true";
    }

    /**
     * 和下边的confirmRename一起表示文件/夹的重命名 rename：得到原文件名，设置inputtext默认显示原文件名
     * confirmRename ： 重命名文件/夹
     *
     * @param myFile
     * @return
     */
    public String rename(MyFile myFile) {

        re_name = myFile.getMyfilename();
        re_name1 = myFile.getMyfilename();
        System.out.println(".............................fileController.rename()" + myFile.getMyfilename());
        myFile.setEditable(Boolean.TRUE);
        myfileService.update(myFile);

        return "/all_files?faces-redirect=true";
    }

    public String confirmRanme(MyFile myFile) {
        System.out.println("..........................filecontroller.confirmRename()" + re_name + "\n rename1的值:" + re_name1 + "\nmyFile Name:" + myFile.getMyfilename());

        if (re_name.equals(re_name1)) {
            //System.out.println("相同名字是本来的值");
            myFile.setEditable(Boolean.FALSE);
            myfileService.update(myFile);
            return "/all_files?faces-redirect=true";
        }

        for (MyFile f : fileList) {
            if (re_name.equals(f.getMyfilename())) {
                if (f.getMyfilename().equals(myFile.getMyfilename())) {
                    continue;
                }
                myFile.setEditable(Boolean.FALSE);
                myfileService.update(myFile);

                addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", re_name + " is Existed!");
                return "/all_files?faces-redirect=true";
            }
        }

        if (myFile.getType().equals("file")) {
            System.out.println("--------------------fileController.comfirmrename().iffile");

            //重命名文件
            String newFullPath = myFile.getMyfileparent() + "\\" + re_name;
            File f = new File(myFile.getMyfilepath());
            f.renameTo(new File(newFullPath));

            //在数据库里更改并merge
            myFile.setMyfilename(re_name);
            myFile.setMyfilepath(newFullPath);
            myFile.setEditable(Boolean.FALSE);
            myfileService.update(myFile);
            return "/all_files?faces-redirect=true";
        }

        if (myFile.getType().equals("folder")) {
            System.out.println("--------------------fileController.confirmrename().iffolder");

            //在folder没有rename前得到folder内的文件列表
            List<MyFile> filesToRename = myfileService.getFilesByDirectory(myFile.getMyfilepath());

            //文件夹重命名   ：  需要将文件夹下的文件也重命名
            String newFullPath = myFile.getMyfileparent() + "\\" + re_name;
            File f = new File(myFile.getMyfilepath());
            f.renameTo(new File(newFullPath));

            //在数据库里更改并merge folder
            myFile.setMyfilename(re_name);
            myFile.setMyfilepath(newFullPath);
            myFile.setEditable(Boolean.FALSE);
            myfileService.update(myFile);

            //对 filesToRename 所有文件在数据库中进行重命名并merge
            String parentOfFolder = myFile.getMyfileparent();
            String renameParentPath = parentOfFolder + "\\" + re_name;

            for (MyFile mf : filesToRename) {

                List<MyFile> filessToRename = myfileService.getFilesByDirectory(mf.getMyfilepath());
                File f1 = new File(mf.getMyfilepath());
                String renameFilePath = renameParentPath + "\\" + mf.getMyfilename();
                mf.setMyfileparent(renameParentPath);
                mf.setMyfilepath(renameFilePath);

                mf.setEditable(Boolean.FALSE);
                myfileService.update(mf);
                f1.renameTo(new File(renameFilePath));
                if (mf.getType().equals("folder")) {
                    renameFilesInFolder(mf, filessToRename);
                }
            }
        }

        return "/all_files?faces-redirect=true";
    }

    public void renameFilesInFolder(MyFile folderFile, List<MyFile> filesToRename) {

        for (MyFile file : filesToRename) {
            String renameFilePath = folderFile.getMyfilepath() + "\\" + file.getMyfilename();
            List<MyFile> filessToRename = myfileService.getFilesByDirectory(file.getMyfilepath());
            File f1 = new File(file.getMyfilepath());
            file.setMyfileparent(folderFile.getMyfilepath());
            file.setMyfilepath(renameFilePath);
            file.setEditable(Boolean.FALSE);
            myfileService.update(file);
            f1.renameTo(new File(renameFilePath));
            if (file.getType().equals("folder")) {
                renameFilesInFolder(file, filessToRename);
            }
        }
    }

    //以原文件名（.zip）的形式下载
    @Asynchronous
    public void downloadFile(MyFile myFile) {

        System.out.println("..................................fileController.downloadFile()");

        try {
            HttpServletRequest request = getHttpServletRequest();
            HttpServletResponse response = getHttpServletResponse();
            request.setCharacterEncoding("utf-8");

            //以zip形式下载文件夹
            if (myFile.getType().equals("folder")) {
                response.reset();
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.addHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(myFile.getMyfilename() + ".zip", "UTF-8"));//new String( ().getBytes("GB2312"),"ISO8859-1") 
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
                response.setHeader("Pragma", "public");

                File srcFile = new File(myFile.getMyfilepath());
                File targetFile = new File(userHomeDownload + "\\" + myFile.getMyfilename() + ".zip");
//                zipFiles(srcFile,targetFile);
                createZip(srcFile.getAbsolutePath(), targetFile.getAbsolutePath());
                System.out.println("得到的下载路径为" + targetFile.getAbsolutePath());

                if (!targetFile.exists()) {
                    System.out.println("文件不存在~！");
                    PrintWriter out = response.getWriter();
                    out.println("文件 " + targetFile.getAbsolutePath() + "不存在");
                }
                FileInputStream in = new FileInputStream(targetFile.getAbsolutePath());
                int zipfileSize = in.available();
                System.out.println("文件的长度为：" + zipfileSize);
                response.addHeader("content-Length", String.valueOf(zipfileSize));
                OutputStream out = response.getOutputStream();
//                HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
//                wrapper.setBufferSize(zipfileSize);
                byte buffer[] = new byte[1024];
                int len = 0;
                //循环将输入流中的内容读取到缓冲区当中
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                in.close();
                out.flush();
                out.close();
                FacesContext.getCurrentInstance().responseComplete(); //--非常关键，否则提示文件损坏
                //return "/all_files?faces-redirect=true";
                Files.delete(Paths.get(targetFile.getAbsolutePath()));

            }

            //下载文件
            if (myFile.getType().equals("file")) {

                String saveFullPath = myFile.getMyfilepath();
                System.out.println("得到的下载路径为" + saveFullPath);
                File file = new File(saveFullPath);
                //file.createNewFile();
                response.reset();
                //response.setContentType("text/html; charset=UTF-8");
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.addHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(myFile.getMyfilename(), "UTF-8"));//new String( ().getBytes("GB2312"),"ISO8859-1") 
//                response.setHeader("Content-Transfer-Encoding", "binary");
//                response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
//                response.setHeader("Pragma", "public");

                if (!file.exists()) {
                    System.out.println("文件不存在~！");
                    PrintWriter out = response.getWriter();
                    out.println("文件 " + saveFullPath + " 不存在");
                } else {
                    System.out.println("//读取要下载的文件并保存到文件输出流");
                    FileInputStream in = new FileInputStream(saveFullPath);
                    int fileSize = in.available();
                    System.out.println(fileSize);
                    response.addHeader("content-Length", String.valueOf(fileSize));
                    System.out.println("创建文件输出流 response.getOutputStream()");
                    OutputStream out = response.getOutputStream();
                    System.out.println("判断response是否已被commited::::" + response.isCommitted());
//                    HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
//                    wrapper.setBufferSize(fileSize);

                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }

                    out.flush();
                    in.close();
                    out.close();
                    FacesContext.getCurrentInstance().responseComplete(); //--非常关键，否则提示文件损坏

                    addMessage(FacesMessage.SEVERITY_INFO, "Successful", myFile.getMyfilename() + " is Downloaded!");
                    //return "/all_files?faces-redirect=true";
                }
            }

        } catch (Exception e) {
            System.out.println("************************************download exception");
//            e.printStackTrace();
        }
    }

    public String copy(MyFile fileToCopy) {
        fileToCopyOrCut = fileToCopy;
        method = "copy";
        return "all_files";
    }

    public String cut(MyFile fileToCopy) {
        fileToCopyOrCut = fileToCopy;
        method = "cut";
        return "all_files";
    }

    public String paste() {
        try {
            MyFile fileToCopy = new MyFile(
                    fileToCopyOrCut.getMyfilename(),
                    currentFolderAbsolute + "\\" + fileToCopyOrCut.getMyfilename(),
                    currentFolderAbsolute,
                    fileToCopyOrCut.getType(),
                    currentUser.getId());
            Long size = fileToCopyOrCut.getFileSize();
            fileToCopy.setFileSize(size);
            fileToCopy.setMyfilerelativepath(fileToCopy.getMyfilepath().replace(rootPath, ""));
            System.out.println("输出待paste文件信息：" + fileToCopy.toString());

            if (myfileService.GetFileByAbsolutePath(fileToCopy.getMyfilepath()) != null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Unsuccessful", fileToCopyOrCut.getType() + " Already Exists!");
                return "/all_files?faces-redirect=true";
            }

            myfileService.addFile(fileToCopy);
            Files.copy(Paths.get(fileToCopyOrCut.getMyfilepath()), Paths.get(fileToCopy.getMyfilepath()));
            currentUser.setUsedSpace(currentUser.getUsedSpace() + size);
            userService.updateUser(currentUser);

            if (fileToCopyOrCut.getType().equals("file") && method.equals("cut")) {
                myfileService.deleteFile(fileToCopyOrCut);
                Files.delete(Paths.get(fileToCopyOrCut.getMyfilepath()));
                currentUser.setUsedSpace(currentUser.getUsedSpace() - size);
                userService.updateUser(currentUser);
            }

            if (fileToCopyOrCut.getType().equals("folder")) {
                copyFilesInFolder(fileToCopyOrCut, fileToCopy);

                //删除文件夹
                if (method.equals("cut")) {
                    deleteDir(fileToCopyOrCut.getMyfilepath());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(FileController.class.getName()).log(Level.SEVERE, null, ex);
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No File to Paste!");
        }

        fileToCopyOrCut = null;
        method = null;
        return "/all_files?faces-redirect=true";
    }

    public void copyFilesInFolder(MyFile fromFolder, MyFile toFolder) {
        try {

            List<MyFile> filesToCopy = myfileService.getFilesByDirectory(fromFolder.getMyfilepath());

            for (MyFile f : filesToCopy) {

                MyFile fToCopy = new MyFile(
                        f.getMyfilename(),
                        toFolder.getMyfilepath() + "\\" + f.getMyfilename(),
                        toFolder.getMyfilepath(),
                        f.getType(),
                        currentUser.getId());
                Long fSize = f.getFileSize();
                fToCopy.setFileSize(fSize);
                fToCopy.setMyfilerelativepath(fToCopy.getMyfilepath().replace(rootPath, ""));
                System.out.println("输出paste folder后的文件信息" + fToCopy.toString());
                currentUser.setUsedSpace(currentUser.getUsedSpace() + fSize);
                userService.updateUser(currentUser);
                myfileService.addFile(fToCopy);
                Files.copy(Paths.get(f.getMyfilepath()), Paths.get(fToCopy.getMyfilepath()));

                if (f.getType().equals("folder")) {
                    copyFilesInFolder(f, fToCopy);
                }

            }
        } catch (Exception e) {
            System.out.println("出错了在copyFilesInFolder里！！");
        }
    }

    public void pics() {
        this.getPictureList();
    }

    public String search() {
        searchResult = myfileService.getFilesByNameContains(searchText, currentUser);
        return "/search_result?faces-redirect=true";
    }

    public HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    public HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    }

    public HttpSession getHttpSession() {
        return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    }

    public UploadedFile getUpfile() {
        return upfile;
    }

    public void setUpfile(UploadedFile upfile) {
        this.upfile = upfile;
    }

    public List<MyFile> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<MyFile> searchResult) {
        this.searchResult = searchResult;
    }

    public String getSearchText() {
        System.out.println("在getSearchText 中得到的" + searchText);
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getShowPage() {
        return showPage;
    }

    public void setShowPage(String showPage) {
        this.showPage = showPage;
    }

    public Boolean getShowSharePage() {
        return showSharePage;
    }

    public void setShowSharePage(Boolean showSharePage) {
        this.showSharePage = showSharePage;
    }
    
    public String getAddFolderName() {
        if (addFolderName == null) {
            addFolderName = "New Folder";
        }
        return addFolderName;
    }

    public void setAddFolderName(String addFolderName) {
        this.addFolderName = addFolderName;
    }

    public String getRe_name() {
        System.out.println("得到的rename的值为： " + re_name);
        return re_name;
    }

    public void setRe_name(String re_name) {
        this.re_name = re_name;
    }

    public StreamedContent getDownfile() {
        return downfile;
    }

    public void setDownfile(StreamedContent downfile) {
        this.downfile = downfile;
    }

    public String getUserHomeUpload() {
        return userHomeUpload;
    }

    public void setUserHomeUpload(String userHomeUpload) {
        this.userHomeUpload = userHomeUpload;
    }

    public String getUserHomeDownload() {
        return userHomeDownload;
    }

    public void setUserHomeDownload(String userHomeDownload) {
        this.userHomeDownload = userHomeDownload;
    }

    public String getUserHome() {
        return userHome;
    }

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }

    public MyFile getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(MyFile currentFile) {
        this.currentFile = currentFile;
    }

//    public String getCurrentFileRelative() {
//        currentFileRelative = currentFile.getMyfilepath().replace(rootPath, "");
//        System.out.println("当前文件相关路径为： " + currentFileRelative);
//        return currentFileRelative;
//    }
//
//    public void setCurrentFileRelative(String currentFileRelative) {
//        this.currentFileRelative = currentFileRelative;
//    }
//    public String getCurrentFileType() {
//
//        try {
//            String[] strs = Files.probeContentType(Paths.get(currentFile.getMyfilepath())).split("/");
//            currentFileType = strs[0];
//        } catch (IOException ex) {
//            System.out.println("不能得到当前文件的类型");
//        }
//        return currentFileType;
//    }
//
//    public void setCurrentFileType(String currentFileType) {
//        this.currentFileType = currentFileType;
//    }
    public String getRe_name1() {
        System.out.println("得到的rename1的值为： " + re_name1);
        return re_name1;
    }

    public void setRe_name1(String re_name1) {
        this.re_name1 = re_name1;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public List<MyFile> getPictureList() {
        System.out.println("输出所有的picutures:");
        pictureList = new ArrayList<>();
        pictureList = myfileService.getFilesByFileType("image", getCurrentUser());

        return pictureList;
    }

    public void setPictureList(List<MyFile> pictureList) {
        this.pictureList = pictureList;
    }

    public List<MyFile> getVideoList() {
        System.out.println("输出所有的picutures:");
        videoList = new ArrayList<>();
        videoList = myfileService.getFilesByFileType("video", getCurrentUser());
        if (!videoList.isEmpty()) {
            for (MyFile f : videoList) {
                System.out.println(f.getMyfilepath());
            }
        }

        return videoList;
    }

    public void setVideoList(List<MyFile> videoList) {
        this.videoList = videoList;
    }

    public List<MyFile> getAudioList() {
        System.out.println("输出所有的songs:");
        audioList = new ArrayList<>();
        audioList = myfileService.getFilesByFileType("audio", getCurrentUser());
        if (!audioList.isEmpty()) {
            for (MyFile f : audioList) {
                System.out.println(f.getMyfilepath());
            }
        }

        return audioList;
    }

    public String getShareString() {
        shareString = null;
        return shareString;
    }

    public void setShareString(String shareString) {
        this.shareString = shareString;
    }
    
    

    public void setAudioList(List<MyFile> audioList) {
        this.audioList = audioList;
    }

    public User getCurrentUser() {
        System.out.println("得到当前User函数");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        currentUser = (User) session.getAttribute("user");
        System.out.println(currentUser.getUsername());
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public SimpleDateFormat getDf() {
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df;
    }

    public void setDf(SimpleDateFormat df) {
        this.df = df;
    }

    public List<MyFile> getFileList() {
        fileList = myfileService.getFilesByDirectory(currentFolderAbsolute);
        return fileList;
    }

    public void setFileList(List<MyFile> fileList) {
        this.fileList = fileList;
    }

    public MyFile getFileToCopyOrCut() {
        return fileToCopyOrCut;
    }

    public void setFileToCopyOrCut(MyFile fileToCopyOrCut) {
        this.fileToCopyOrCut = fileToCopyOrCut;
    }

    public String getWebRootPath() {
        HttpSession session = getHttpSession();
        webRootPath = session.getServletContext().getRealPath("/");
        System.out.println("此项目的web rootPath为" + webRootPath);
        return webRootPath;
    }

    public void setWebRootPath(String webRootPath) {
        this.webRootPath = webRootPath;
    }

    public String getRootPath() {
        rootPath = serverService.getServerById(currentUser.getServerId()).getServerPath();
        System.out.println("在getRootPath得到的rootPath为:" + rootPath);
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getCurrentFolderRelative() {
        currentFolderRelative = currentFolderAbsolute.replace(rootPath, "");
        System.out.println("当前所在的相关路径为： " + currentFolderRelative);
        return currentFolderRelative;
    }

    public void setCurrentFolderRelative(String currentFolderRelative) {
        this.currentFolderRelative = currentFolderRelative;
    }

    public String getCurrentFolderAbsolute() {

        return currentFolderAbsolute;
    }

    public void setCurrentFolderAbsolute(String currentFolderAbsolute) {
        this.currentFolderAbsolute = currentFolderAbsolute;
    }

    public String getCurrentFolder() {

//        if (myfileService.GetFileByAbsolutePath(currentFolderAbsolute) == null) {
//            currentFolderAbsolute = "D:\\Senior\\France\\4JVA\\Workspace_NetBeans\\FileUpV\\build\\web\\" + userController.getUsername() + "\\upload";
//        }
        System.out.println("当前的currentFolderAbsolute为" + currentFolderAbsolute);
        currentFolder = myfileService.GetFileByAbsolutePath(currentFolderAbsolute).getMyfilename();
        System.out.println("得到的属于文件夹为：" + currentFolder);
        return currentFolder;
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

}
