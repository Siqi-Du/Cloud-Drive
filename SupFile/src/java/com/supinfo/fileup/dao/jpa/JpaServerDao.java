/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao.jpa;

import com.supinfo.fileup.dao.ServerDao;
import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.Server;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class JpaServerDao implements ServerDao{

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void addServer(Server server) {
        System.out.println(">>>>>>>>>>>>>>>JpaServerDao.addServer()");
        em.persist(server);
    }

    @Override
    public List<Server> getAllServers() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>JpaServerDao.getAllServers()");
        return em.createQuery("SELECT s FROM Server s").getResultList();
    }

    @Override
    public Server getServerById(Long id) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>JpaServerDao.getServerById()");
        Query query = em.createQuery("SELECT s FROM Server s WHERE s.id=?1");
        query.setParameter(1, id);
        try {
            return (Server) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no server found with id " + id);
            return null;
        }
    }

    @Override
    public Server getServerByParh(String path) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>JpaServerDao.getServerById()");
        Query query = em.createQuery("SELECT s FROM Server s WHERE s.serverPath=?1");
        query.setParameter(1, path);
        try {
            return (Server) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("***** no server found with path " + path);
            return null;
        }
    }

    @Override
    public void updateServer(Server server) {
        
            if (!em.contains(server)) {
            server = em.merge(server);
        }
        else{
            server = em.merge(server);
        }
        
    }
    
}
