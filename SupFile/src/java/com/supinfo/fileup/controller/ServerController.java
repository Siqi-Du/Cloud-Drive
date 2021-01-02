/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.controller;

import com.supinfo.fileup.entity.Server;
import com.supinfo.fileup.service.ServerService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Startup;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Siqi DU
 */
@ManagedBean
@ApplicationScoped
@Startup
public class ServerController implements Serializable {

    @EJB
    private ServerService serverService;

    @PostConstruct
    public void connectStorageServers() {
        String serverIPandFolderString1 = ""; // \\\\192.168.133.128\\root
        String serverIPandFolderString2 = "";
        String serverIPandFolderString3 = "";
        
//        String serverPath5 = "D:\\Senior\\France\\4JVA\\Workspace_NetBeans\\FileUpV\\build\\web\\";
        String serverPath1 = "D:\\SupFile\\Server1\\" ;   //X:\\
        String serverPath2 = "D:\\SupFile\\Server2\\";
//        String serverPath3 = "D:\\SupFile\\Server3";

//        Server server5 = new Server("server5",0L);
        Server server1 = new Server("server1", 0L);
        Server server2 = new Server("server2", 0L);
//        Server server3 = new Server("server3", 0L);
        
        
//          server5.setServerPath(serverPath5);
        server1.setServerIPandFolderString(serverIPandFolderString1);
        server1.setServerPath(serverPath1);
        server2.setServerPath(serverPath2);
//        server3.setServerPath(serverPath3);

//          serverService.addServer(server5);
        serverService.addServer(server1);
        serverService.addServer(server2);
//        serverService.addServer(server3);

    }

}
