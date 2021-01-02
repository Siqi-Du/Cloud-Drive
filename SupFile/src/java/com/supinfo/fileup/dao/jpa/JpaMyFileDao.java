/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao.jpa;

import com.supinfo.fileup.dao.MyFileDao;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.User;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.persistence.Query;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class JpaMyFileDao implements MyFileDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void addFile(MyFile myfile) {
        System.out.println("********** JpaFileDao.addFile");
        em.persist(myfile);
    }

    @Override
    public List<MyFile> getAllFiles() {
        System.out.println("********** JpaFileDao.getAllFiles");
        return em.createQuery("SELECT f FROM MyFile f ORDER BY f.id DESC").getResultList();

    }

    @Override
    public MyFile getFileById(Long id) {
        System.out.println("********** JpaFileDao.getFileById");
        Query query = em.createQuery("SELECT f FROM MyFile f WHERE f.id=?1");
        query.setParameter(1, id);
        try {
            return (MyFile) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no file found with id " + id);
            return null;
        }
    }

    @Override
    public MyFile getFileByName(String name) {
        System.out.println("********** JpaFileDao.getFileByName");
        Query query = em.createQuery("SELECT f FROM MyFile f WHERE f.myfilename=?1");
        query.setParameter(1, name);
        try {
            return (MyFile) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no file found with name " + name);
            return null;
        }
    }

    @Override
    public List<MyFile> getFilesByDirectory(String directoryPath) {

        System.out.println("********** JpaFileDao.getFilesByFolder");
        Query query = em.createQuery("SELECT f FROM MyFile f WHERE f.myfileparent=?1 ORDER BY f.id DESC");
        query.setParameter(1, directoryPath);

        try {
            return (List<MyFile>) query.getResultList();
        } catch (Exception e) {
            System.out.println("*****no files found by directoryPath" + directoryPath);
            return null;
        }
    }

    @Override
    public void deleteFileById(Long id) {
        if (!em.contains(getFileById(id))){
            em.merge(getFileById(id));
        }
        em.remove(getFileById(id));
    }

    @Override
    public void deleteFile(MyFile myFile) {

        if (!em.contains(myFile)) {
            myFile = em.merge(myFile);
        }
        em.remove(myFile);

    }

    @Override
    public MyFile getFileByAbsolutePath(String fileAbsolutePath) {
        System.out.println("********** JpaFileDao.getFileByAbsolutePath()" + fileAbsolutePath);
        Query query = em.createQuery("SELECT f FROM MyFile f WHERE f.myfilepath=?1");
        query.setParameter(1, fileAbsolutePath);
        try {
            return (MyFile) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no file found with file absolute path " + fileAbsolutePath);
            return null;
        }
    }

    @Override
    public void updateFile(MyFile myFile) {
        if (!em.contains(myFile)) {
            System.out.println("........................JpaMyFileDao.updateFIle" + "myFile " + myFile.getMyfilename() + "处于游离状态");
            myFile = em.merge(myFile);
        }
        else{
            myFile = em.merge(myFile);
        }
    }

    @Override
    public List<MyFile> getFilesByFileType(String fileType, User user) {
        System.out.println("********** JpaFileDao.getFilesByFileType");
        Query query = em.createQuery("SELECT f FROM MyFile f WHERE f.fileType=?1 and f.userId=?2");
        query.setParameter(1, fileType);
        query.setParameter(2, user.getId());

        try {
            return (List<MyFile>) query.getResultList();
        } catch (Exception e) {
            System.out.println("*****no files found by FileType" + fileType);
            return null;
        }
    }

    @Override
    public List<MyFile> getFilesByNameContains(String contains, User user) {
        System.out.println("********** JpaFileDao.getFilesByNameContains");
        Query query;
        query = em.createQuery("SELECT f FROM MyFile f WHERE f.myfilename LIKE :contains and f.userId= :id");
        query.setParameter("contains", "%"+contains+"%");
        query.setParameter("id", user.getId());

        try {
            return (List<MyFile>) query.getResultList();
        } catch (Exception e) {
            System.out.println("*****no files found by contains" + contains);
            return null;
        }
    }

}
