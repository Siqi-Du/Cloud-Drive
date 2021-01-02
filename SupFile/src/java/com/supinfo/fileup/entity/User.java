/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Siqi DU
 */
@Entity
@Table
public class User implements Serializable {
    
    
    private Long id; 
    private String username;   
    private String firstname;
    private String lastname;  
    private String email;
    private String password;
    private Boolean isLoggedin;
    private String description;
    private Long usedSpace;
    private Long serverId;
//    private String storageRootPath;
    
//    private List<MyFile> fileList;

    public User() {
    }
    
    public User(String username, String firstname, String lastname, String email, String password) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.isLoggedin = false;
        this.usedSpace = 0L;
//        this.serverId = serverId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @NotEmpty
    @Column(nullable=false)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    
    @NotEmpty
    @Column(nullable=false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username=" + username + ", firstname=" + firstname + ", lastname=" + lastname + ", email=" + email + ", password=" + password + ", isLoggedin=" + isLoggedin + ", description=" + description + ", usedSpace=" + usedSpace + ", serverId=" + serverId + '}';
    }

    
    
    //@OneToMany (mappedBy = "user")
//    public List<MyFile> getFileList() {
//        return fileList;
//    }
//
//    public void setFileList(List<MyFile> fileList) {
//        this.fileList = fileList;
//    }

    public Boolean getIsLoggedin() {
        return isLoggedin;
    }

    public void setIsLoggedin(Boolean isLoggedin) {
        this.isLoggedin = isLoggedin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(Long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

//    public String getStorageRootPath() {
//        return storageRootPath;
//    }
//
//    public void setStorageRootPath(String storageRootPath) {
//        this.storageRootPath = storageRootPath;
//    }
    
    
}
