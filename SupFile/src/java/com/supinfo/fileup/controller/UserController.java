/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.supinfo.fileup.controller;

import com.supinfo.fileup.entity.Server;
import com.supinfo.fileup.entity.User;
import com.supinfo.fileup.service.ServerService;
import com.supinfo.fileup.service.UserService;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Siqi DU
 */
@ManagedBean(name = "userController")
@SessionScoped
public class UserController implements Serializable {

    @ManagedProperty(value = "#{serverController}")
    ServerController serverController;

//    @ManagedProperty(value = "#{fileController}")
//    FileController fileController;
    @EJB
    private UserService userService;
    @EJB
    private ServerService serverService;

    private Long id;
    private String username;
    private String password;
    private String passwordConfirm;
    private String firstname;
    private String lastname;
    private String email;

    private String loginError;
    private String registerError;
    private String updateError;

    private String userLogon;

    private User user;

    private Boolean show;
    private String showPage;

    //根据emial address 注册
    public String register() {
        System.out.println("username:" + username
                + "\n password:" + password
                + "\n passwordConfirm" + passwordConfirm
                + "\n firstname:" + firstname
                + "\n lastname:" + lastname
                + "\n email:" + email
        );

        //while (userService.getUserByName(username) != null || userService.getUserByEmail(email) != null || !(password.equals(passwordConfirm)) || password == null) {
        if (password.isEmpty()) {
            registerError = "Password can not be NULL!";
            addMessage(FacesMessage.SEVERITY_ERROR, "RegisterError", registerError);
            System.out.println(registerError);
            return "register";
        }
        if (userService.getUserByName(username) != null) {
            registerError = "User Exists, Please enter another username!";
            addMessage(FacesMessage.SEVERITY_ERROR, "RegisterError", registerError);
            System.out.println(registerError);
            return "register";
        }

        if (userService.getUserByEmail(email) != null) {
            registerError = "This Email address is Exists, Please login if you have registered!";
            addMessage(FacesMessage.SEVERITY_ERROR, "RegisterError", registerError);
            System.out.println(registerError);
            return "register";
        }
        //match email address pattern
        if (!email.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")) {
            registerError = "Please enter a valid email address!";
            addMessage(FacesMessage.SEVERITY_ERROR, "RegisterError", registerError);
            System.out.println(registerError);
            return "register";
        }

        if (!(password.equals(passwordConfirm))) {
            registerError = "The password you entered is not the same!";
            addMessage(FacesMessage.SEVERITY_ERROR, "RegisterError", registerError);
            System.out.println(registerError);
            return "register";
        }

        user = new User(username, firstname, lastname, email, password);

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
        System.out.println("User added with id :" + user.getId());
        System.out.println("现在的server size是"
                + minSizedServer.getServerPath()
                + minSizedServer.getSize());

        registerError = null;
        loginError = null;

        return "login?faces-redirect=true";

    }

    //根据用户名和密码登陆
    public String login() {

        user = userService.getUserByName(username);
        if (user == null) {
            loginError = "User not exists, Please Register first!";
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            loginError = "Password is incorrect!";
            return "login";

        }

        if (user.getIsLoggedin() == true) {
            loginError = "User" + user.getUsername() + " is logged in";
            return "login";
        }

        user.setIsLoggedin(true);
        userService.updateUser(user);

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute("user", user);
        loginError = null;
        userLogon = "yes";
//        fileController.setShowPage("all_file");
        return "all_files?faces-reidrect=true";

    }

    public boolean isLoggedIn() {
        System.out.println("........................................isLoggedIn");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session == null) {
            return false;
        }
        User sessionUser = (User) session.getAttribute("user");
        return sessionUser != null;
    }

    public String logout() {

        user.setIsLoggedin(false);
        userService.updateUser(user);
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.invalidate();
        return "home?faces-redirect=true";
    }

//    public String update() {
//        
//        show = true;
//        getShow();
////        showPage = "user_profile";
//        return "all_files";
//    }
    //还需添加判断条件是否能更新    message不能正常显示
    public String confirmUpdate() {
        System.out.println("阿阿阿阿阿阿阿阿");
        user.setUsername(username);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        if (password.isEmpty() || passwordConfirm.isEmpty()) {
            updateError = "Please enter a password !";
            System.out.println(updateError);
            return "all_files";
        }
        if (password.equals(passwordConfirm)) {
            user.setPassword(password);
        } else {
            updateError = "The Password Is NOT the Same!";
            System.out.println(updateError);
            addMessage(FacesMessage.SEVERITY_ERROR, "UpdateUserError", "The Password Is NOT the Same!");
            return "all_files";
        }

        userService.updateUser(user);
//        show = false;
        updateError = null;
        System.out.println("鹅鹅鹅鹅鹅鹅鹅鹅鹅");
        return "all_files?faces-redirect=true";
    }

    public String cancelUpdate() {
//        show = false;
        updateError = null;
        return "all_files?faces-redirect=true";
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        try {
            return user.getFirstname();
        } catch (Exception e) {
            return firstname;
        }
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        try {
            return user.getLastname();
        } catch (Exception e) {
            return lastname;
        }

    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        try {
            return user.getEmail();
        } catch (Exception e) {
            return email;
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginError() {
        return loginError;
    }

    public void setLoginError(String loginError) {
        this.loginError = loginError;
    }

    public String getRegisterError() {
        return registerError;
    }

    public void setRegisterError(String registerError) {
        this.registerError = registerError;
    }

    public User getUser() {
        user = userService.getUserByName(username);
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserLogon() {
        return userLogon;
    }

    public void setUserLogon(String userLogon) {
        this.userLogon = userLogon;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public Boolean addMessage(Severity severity, String name, String details) {

        FacesMessage message = new FacesMessage(severity, name, details);
        FacesContext.getCurrentInstance().addMessage("messageUser", message);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        System.out.println("在addMessage（）输出message信息：" + message.getDetail());

        return true;
    }

    public ServerController getServerController() {
        return serverController;
    }

    public void setServerController(ServerController serverController) {
        this.serverController = serverController;
    }

    public String getShowPage() {
        showPage = "user_profile";
        return showPage;
    }

    public void setShowPage(String showPage) {
        this.showPage = showPage;
    }

    public String getUpdateError() {
        return updateError;
    }

    public void setUpdateError(String updateError) {
        this.updateError = updateError;
    }

}
