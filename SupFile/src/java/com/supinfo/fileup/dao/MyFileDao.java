
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao;

import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author Siqi DU
 */
@Local
public interface MyFileDao {
    
    void addFile(MyFile myfile);
    
    List<MyFile> getAllFiles();
    
    MyFile getFileById(Long id);
    
    MyFile getFileByName(String name);
    
    MyFile getFileByAbsolutePath(String fileAbsolutePath);
    
    List<MyFile> getFilesByFileType(String fileType,User user);
    
    List<MyFile> getFilesByNameContains(String contains, User user);
    
    void updateFile(MyFile myFile);
    
    void deleteFileById(Long id);
    
    void deleteFile(MyFile myFile);
    
    List<MyFile> getFilesByDirectory(String File);
}
