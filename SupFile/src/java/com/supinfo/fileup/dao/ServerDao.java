/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.dao;

import com.supinfo.fileup.entity.Server;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Siqi DU
 */
@Local
public interface ServerDao {
    
    void addServer(Server server);
    void updateServer(Server server);
    List<Server> getAllServers();
    Server getServerById(Long id);
    Server getServerByParh(String path);
    
}
