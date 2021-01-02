package com.supinfo.fileup.controller;
 
import com.supinfo.fileup.entity.Theme;
import com.supinfo.fileup.service.ThemeService;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

 
@ManagedBean
@ApplicationScoped
public class ThemeController {
 
    private List<Theme> themes;
 
    @EJB
    private ThemeService service;
 
    @PostConstruct
    public void init() {
        themes = service.getThemes();
    }
 
    public List<Theme> getThemes() {
        return themes;
    }
 
    public void setService(ThemeService service) {
        this.service = service;
    }
}