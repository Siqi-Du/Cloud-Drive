/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Siqi DU
 */
@Entity
@Table
public class Server implements Serializable, Comparable<Server>{
    
    
    private Long id;
    private String servername;
    private String serverIPandFolderString;
    private Long size;
    private String serverPath;

    public Server() {
    }

    
    public Server(String servername, Long size) {
        this.servername = servername;
        this.size = size;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public String getServerIPandFolderString() {
        return serverIPandFolderString;
    }

    public void setServerIPandFolderString(String serverIPandFolderString) {
        this.serverIPandFolderString = serverIPandFolderString;
    }
    
    

    @Override
    public int compareTo(Server s) {
        int num = (int) (this.size - s.size);
        int num1 = (num == 0 ? this.servername.compareTo(s.servername) : num);
        return num1;
    }
    
    public String toString(){
        return "\n ServerId: " + this.id
                + "\n serverName: " + this.servername
                + "\n serverPath: " + this.serverPath
                +"\n serverSize: " + this.size;
    }
    
    
}
