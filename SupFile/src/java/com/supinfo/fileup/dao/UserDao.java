/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao;

import com.supinfo.fileup.entity.User;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Siqi DU
 */
@Local
public interface UserDao {
    
    void addUser(User user);
    
    List<User> getAllUsers();
    
    User getUserByLogin(String username, String password);
    
    User getUserById(Long id);
    
    User getUserByName(String name);
   
    User getUserByEmail(String email);
    
    List<User> getUsersByServerId(Long id);
    
    void updateUser(User user);
}
