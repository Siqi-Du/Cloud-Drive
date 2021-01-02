/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.service;

import com.supinfo.fileup.dao.MyFileDao;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class MyFileService {
    
    @EJB
    private MyFileDao fileDao;
    
    public void addFile(MyFile file){
        fileDao.addFile(file);
    }
    
    public List<MyFile> getAllFiles(){
        return fileDao.getAllFiles();
    }
    
    public MyFile getFileById(Long id){
        return fileDao.getFileById(id);
    }
    
    public MyFile getFileByName(String name){
        return fileDao.getFileByName(name);
    }
    
    public MyFile GetFileByAbsolutePath(String fileAbsolutePath){
        return fileDao.getFileByAbsolutePath(fileAbsolutePath);
    }
    
    public void deleteFileById(Long id){
        fileDao.deleteFileById(id);
    }
    
    public void deleteFile(MyFile myFile){
        fileDao.deleteFile(myFile);
    }
    public List<MyFile> getFilesByDirectory(String directoryPath){
        return fileDao.getFilesByDirectory(directoryPath);
    }
    
    public void update(MyFile myFile){
        fileDao.updateFile(myFile);
    }
    
    public List<MyFile> getFilesByFileType (String fileType, User user){
        return fileDao.getFilesByFileType(fileType,user);
    }
    
    public List<MyFile> getFilesByNameContains(String contains, User user){
        return fileDao.getFilesByNameContains(contains, user);
    }
}
