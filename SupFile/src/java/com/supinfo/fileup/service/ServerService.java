/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.service;

import com.supinfo.fileup.dao.ServerDao;
import com.supinfo.fileup.entity.Server;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Siqi DU
 */
@Stateless
public class ServerService {
    
    @EJB
    private ServerDao serverDao;
    
    public void addServer(Server server){
        serverDao.addServer(server);
    }
    
    public List<Server> getAllServers(){
        return serverDao.getAllServers();
    }
    
    public Server getServerById(Long id){
        return serverDao.getServerById(id);
    }
    
    public Server getServerByPath(String path){
        return serverDao.getServerByParh(path);
    }
    
    public void updateServer(Server serve){
        serverDao.updateServer(serve);
    }
}
