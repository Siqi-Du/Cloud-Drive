/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.service;

import com.supinfo.fileup.dao.UserDao;
import com.supinfo.fileup.entity.User;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class UserService {
    
    @EJB
    private UserDao userDao;
    
    public void addUser(User user){
        userDao.addUser(user);
    }
    
    public List<User> getAllUsers(){
        return userDao.getAllUsers();
    }
    
    public User getUserByLogin(String username, String password){
        return userDao.getUserByLogin(username, password);
    }
    
    public User getUserById(Long id){
        return userDao.getUserById(id);
    }
    
    public User getUserByName(String username){
        return userDao.getUserByName(username);
    }
    
    public User getUserByEmail(String email){
        return userDao.getUserByEmail(email);
    }
    
    public void updateUser(User user){
        userDao.updateUser(user);
    }
    
    public List<User> getUsersByServerId(Long id){
        return userDao.getUsersByServerId(id);
    }
}
