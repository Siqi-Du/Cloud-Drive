/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.util;

import java.io.IOException;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author Siqi DU
 */
public class FtpClientUtil {

    public static final String hostname = "192.168.1.111";
    public static final int port = 21;
//    public static final String username = "testing";
//    public static final String password = "testing";

    public static final String SPLIT = "/";
    public static String encoding = System.getProperty("file.encoding");  

    private static FtpClientUtil singleFtp;
    private static FTPClient ftpClient;
    
    private String username;
    private String password;

//    FtpClientUtil() {
//        System.out.println("进行ftpclient的初始化");
//        ftpClient = new FTPClient();
//    }

//    //单例模式：懒汉模式  
//    public static FtpClientUtil getInstance() {
//        if (singleFtp == null) {
//            singleFtp = new FtpClientUtil();
//        }
//        return singleFtp;
//    }

    //连接FTP服务器  
    @SuppressWarnings("finally")
    public FTPClient connectFtpServer() {
        ftpClient = new FTPClient();
        try {
            //如果存在连接，先断开，再重新连接  
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
                //return true;  
            }

            // 下面三行代码必须要，而且不能改变编码格式，否则不能正确下载中文文件  
            ftpClient.setControlEncoding("utf-8");
//            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);  
//            conf.setServerLanguageCode("zh");  
            //连接FTP服务器  
            ftpClient.connect(hostname, port);
            ftpClient.login(username,password);
//            ftpClient.enterLocalPassiveMode();  
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setDataTimeout(1000 * 60 * 30);   //设置传输超时（30分钟）  

            //判断连接是否成功  
            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                System.out.println("FTP 连接成功");
//                if(!ftpClient.changeWorkingDirectory("/home/")){
//                    System.out.println("cd to home/username 失败！！！！");
//                }
            } else {
                System.out.println("FTP 连接失败");
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("出错啦！！！！！！！");
        }
        
        return ftpClient;
    }

    /**
     * 断开ftp连接
     *
     * @throws Exception
     */
    public void disconnect() throws Exception {
        try {
            FTPClient ftpClient = connectFtpServer();
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
                ftpClient = null;
            }
        } catch (IOException e) {
            System.out.println("不能退出ftp服务器");
            throw new Exception("Could not disconnect from server.", e);
        }
    }

    //创建目录
    public Boolean createDirectory(String dir) {
        if (dir == null) {
            System.out.println("没有接收到dir");
            return false;
        }
        if(ftpClient == null){
            System.out.println("ftpClient没有连接到ftp server");
            return false;
        }
        
        try{
            //设置编码，处理乱码问题  
        dir = new String(dir.getBytes(encoding), FTP.DEFAULT_CONTROL_ENCODING);
        ftpClient.makeDirectory(dir);
        
        } catch (Exception e){
            System.out.println("出错啦！！！！！！");
        }
        return true;
        
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    

}
