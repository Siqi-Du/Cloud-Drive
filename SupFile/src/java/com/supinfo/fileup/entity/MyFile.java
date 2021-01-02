/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.entity;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Siqi DU
 */
@Table
@Entity
public class MyFile implements Serializable {

    private Long id;
    
    private String myfilename;
    //private String myfilenameUUID;
    private String myfilepath;
    private String myfilerelativepath;  //  \\{username}\\upload\\{folder}\\{filename}
    private String myfileparent;
    private String type;
    private Boolean editable;
    private Long userId;
    private String dataAdded;
    private String fileType;
    private Long fileSize;
    private String sizeString;

    public MyFile() {
    }

    public MyFile(String myfilename, String myfilepath, String myfileparent, String type, Long userId) {
        this.myfilename = myfilename;
        //this.myfilenameUUID = myfilenameUUID;
        this.myfilepath = myfilepath;
        this.myfileparent = myfileparent;
        this.type = type;
        this.userId = userId;
//        this.fileSize = size;

        //this.editable = false;
        //this.dataAdded = dateadded;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotEmpty
    @Column(nullable = false)
    public String getMyfilename() {
        return myfilename;
    }

    public void setMyfilename(String myfilename) {
        this.myfilename = myfilename;
    }

    public String getMyfilepath() {
        return myfilepath;
    }

    public void setMyfilepath(String myfilepath) {
        this.myfilepath = myfilepath;
    }

    public String getMyfileparent() {
        return myfileparent;
    }

    public void setMyfileparent(String myfileparent) {
        this.myfileparent = myfileparent;
    }

    @NotEmpty
    @Column(nullable = false)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

//    @ManyToOne(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
//    @JoinColumn(name="user_fk",nullable=false)
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileType() {
        try {
            String[] strs = Files.probeContentType(Paths.get(getMyfilepath())).split("/");
            fileType = strs[0];
        } catch (Exception e) {
            fileType = null;
        }
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

//    @Temporal(TemporalType.DATE)
    public String getDataAdded() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dataAdded = df.format(new Date());
//        System.out.println(this.dataAdded);
        return dataAdded;
    }

    public void setDataAdded(String dataAdded) {
        this.dataAdded = dataAdded;
    }

    public String getMyfilerelativepath() {
        return myfilerelativepath;
    }

    public void setMyfilerelativepath(String myfilerelativepath) {
        this.myfilerelativepath = myfilerelativepath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        
        this.fileSize = fileSize;
    }

    public String getSizeString() {
        if(type.equals("folder")){
            sizeString = "--";
            return sizeString;
        }
        DecimalFormat df = new DecimalFormat("#.00"); 
         sizeString = ""; 
        if (fileSize < 1024) { 
            sizeString = df.format((double) fileSize) + "B"; 
        } else if (fileSize < 1048576) { 
            sizeString = df.format((double) fileSize / 1024) + "K"; 
        } else if (fileSize < 1073741824) { 
            sizeString = df.format((double) fileSize / 1048576) + "M"; 
        } else { 
            sizeString = df.format((double) fileSize / 1073741824) + "G"; 
        } 
        return sizeString;
    }

    public void setSizeString(String sizeString) {
        this.sizeString = sizeString;
    }
    
    
    @Override
    public String toString(){
        return  "\n fileId: " + this.getId()
                + "\n myfilename: " + this.myfilename 
                + "\n myfilePath: " + this.myfilepath
                + "\n myfileParent: " + this.myfileparent
                + "\n myfileType: " + this.fileType
                + "\n myfileSize: " + this.fileSize
                + "\n fileSizeeString:" + this.sizeString
                + "\n myfileDateAdded: " + this.dataAdded 
                + "\n userId: " + this.userId;
    }

}
