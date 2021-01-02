package com.supinfo.fileup.ws.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

//all the rest service should be in /rest
@ApplicationPath("/rest")
public class RestService extends Application {

    public RestService() {
        System.out.println("hello REST web service");
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> returnValue = new HashSet<>();
        returnValue.add(UserResourceREST.class);
        returnValue.add(FileResourceREST.class);

        return returnValue;
    }
}
