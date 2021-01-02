/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao.jpa;

import com.supinfo.fileup.dao.UserDao;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class JpaUserDao implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void addUser(User user) {
        //System.out.println("@@@@@JpaUserDao.addUser");
        em.persist(user);
    }

    @Override
    public List<User> getAllUsers() {
        System.out.println("JpaUserDao getAllUsers");
        return em.createQuery("SELECT u FROM User u").getResultList();
    }

    // if the user is found or not by passing username and password
    @Override
    public User getUserByLogin(String username, String password) {
        System.out.println(".......................................................JpaUserDao.getUserByName");
        Query query = em.createQuery("SELECT u FROM User u WHERE u.username=?1 and u.password=?2");
        query.setParameter(1, username);
        query.setParameter(2, password);
        
        try {
            return (User) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no user found with username " + username + "and password" + password);
            return null;
        }
        
    }

    @Override
    public User getUserById(Long id) {

       System.out.println("........................................................JpaUserDao.getUserById");
        Query query = em.createQuery("SELECT u FROM User u WHERE u.id=?1");
        query.setParameter(1, id);
        try {
            return (User) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no user found with id " + id);
            return null;
        }
    }

    @Override
    public User getUserByName(String username) {

        System.out.println("********** JpaUserDao.getUserByName");
        Query query = em.createQuery("SELECT u FROM User u WHERE u.username=?1");
        query.setParameter(1, username);
        try {
            return (User) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no user found with username " + username);
            return null;
        }

    }

    @Override
    public User getUserByEmail(String email) {

        System.out.println(".....................................................JpaUserDao.getUserByEmail");
        Query query = em.createQuery("SELECT u FROM User u WHERE u.email=?1");
        query.setParameter(1, email);
        try {
            return (User) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no user found with email " + email);
            return null;
        }
    }
    
    @Override
    public void updateUser(User user) {
        if (!em.contains(user)) {
            System.out.println("........................JpaUserDao.updateUser" + "myFile " + user.getUsername() + "处于游离状态");
            user = em.merge(user);
        }
        else{
            user = em.merge(user);
        }
    }

    @Override
    public List<User> getUsersByServerId(Long id) {
        System.out.println("........................................................JpaUserDao.getUsersByServerId");
        Query query = em.createQuery("SELECT u FROM User u WHERE u.serverId=?1");
        query.setParameter(1, id);
        try {
            return  query.getResultList();
        } catch (Exception e) {
            System.out.println("***** no user found with ServerId " + id);
            return null;
        }
    }
    
    
}
