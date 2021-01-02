/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.ws.rest;

import com.supinfo.fileup.entity.MyFile;
import com.supinfo.fileup.entity.Server;
import com.supinfo.fileup.entity.User;
import com.supinfo.fileup.service.MyFileService;
import com.supinfo.fileup.service.ServerService;
import com.supinfo.fileup.service.UserService;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Siqi DU
 */
@Stateless
@Path("/auth")
public class UserResourceREST {

    @EJB
    private UserService userService;

    @EJB
    private MyFileService myFileService;
    
    @EJB
    private ServerService serverService;

    @GET
    public String sayHello() {
        return "Hello UserResourceREST!";
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public User register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("passwordConfirm") String passwordConfirm,
            @FormParam("firstname") String firstname,
            @FormParam("lastname") String lastname,
            @FormParam("email") String email
    ) {
        System.out.println("username:" + username
                + "\n password:" + password
                + "\n passwordConfirm" + passwordConfirm
                + "\n firstname:" + firstname
                + "\n lastname:" + lastname
                + "\n email:" + email
        );

        String registerError;

        //while (userService.getUserByName(username) != null || userService.getUserByEmail(email) != null || !(password.equals(passwordConfirm)) || password == null) {
        if (password.isEmpty()) {
            registerError = "Password can not be NULL!";
            System.out.println(registerError);
            return null;
        }
        if (userService.getUserByName(username) != null) {
            registerError = "User Exists, Please enter another username!";
            System.out.println(registerError);
            return null;
        }

        if (userService.getUserByEmail(email) != null) {
            registerError = "This Email address is Exists, Please login if you have registered!";
            System.out.println(registerError);
            return null;
        }
        //match email address pattern
        if (!email.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")) {
            registerError = "Please enter a valid email address!";
            System.out.println(registerError);
            return null;
        }

        if (!(password.equals(passwordConfirm))) {
            registerError = "The password you entered is not the same!";
            System.out.println(registerError);
            return null;
        }

        //}
        User user = new User(username, firstname, lastname, email, password);
        user.setUsedSpace(0l);
        
         List<Server> servers = serverService.getAllServers();
        Server minSizedServer = Collections.min(servers);
        System.out.println("在usercontroller得到的最小size 的server 是"
                + minSizedServer.getId()
                + minSizedServer.getServername()
                + minSizedServer.getServerPath()
                + minSizedServer.getSize());
        user.setServerId(minSizedServer.getId());
        minSizedServer.setSize(minSizedServer.getSize() + 1L);
        serverService.updateServer(minSizedServer);
        userService.addUser(user);
        System.out.println(user.toString());

        String userHome = serverService.getServerById(user.getServerId()).getServerPath() + user.getUsername();
        String userHomeUpload = userHome + "\\" + "upload";
        String userHomeDownload = userHome + "\\" + "download";
        
        File userFile = new File(userHome);
        File userHomeUp = new File(userHomeUpload);
        File userHomeDown = new File(userHomeDownload);

        userFile.mkdir();
        userHomeUp.mkdir();
        userHomeDown.mkdir();
        //System.out.println("新建两个用户文件夹：" + userHomeUp.getPath() + userHomeDown.getPath());

        MyFile myFileUser = new MyFile(userFile.getName(), userFile.getPath(), userFile.getParent(), "folder", user.getId());
        myFileUser.setFileSize(0l);
        MyFile myFileuserHomeUp = new MyFile(userHomeUp.getName(), userHomeUp.getPath(), userHomeUp.getParent(), "folder", user.getId());
        myFileuserHomeUp.setFileSize(0l);
        MyFile myFileuserHomeDown = new MyFile(userHomeDown.getName(), userHomeDown.getPath(), userHomeDown.getParent(), "folder", user.getId());
        myFileuserHomeDown.setFileSize(0l);
        
        myFileService.addFile(myFileUser);
        myFileService.addFile(myFileuserHomeUp);
        myFileService.addFile(myFileuserHomeDown);

        System.out.println("User added with id :" + user.getId());

        return user;
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public User login(@FormParam("username") String username, @FormParam("password") String password) {
        System.out.println("输出REST SERTVICE login 得到的参数:\n username:" + username + "\npassword" + password);

        User user = userService.getUserByName(username);
        String loginError;

        if (user == null) {
            loginError = "User not exists! Please register first.";
            System.out.println("用户不存在");
            return null;
        }
        if (!user.getPassword().equals(password)) {
            loginError = "Password is NOT correct! Please enter again.";
            System.out.println("密码不正确");
            return null;
        }
        if (user.getIsLoggedin() == true) {
            loginError = "User has logged in, Please logout first.";
            System.out.println("用户已登陆");
            return null;
        }

        user.setIsLoggedin(Boolean.TRUE);
        userService.updateUser(user);
        return user;

//        String userHomeUri = "/" + username;    
//        //  /auth/login/username
//        return Response.created(URI.create(userHomeUri)).build();
    }

    @GET
    @Path("/logout/{username}")
    public Boolean logout(@PathParam("username") String username) {
        System.out.println(username);
        User user = userService.getUserByName(username);
        user.setIsLoggedin(Boolean.FALSE);
        userService.updateUser(user);
        return true;
    }

}
