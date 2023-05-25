module com.xingray.java.server.spring {
    requires spring.context;
    requires spring.web;
    requires spring.webmvc;
    requires spring.core;
    requires spring.beans;

    requires com.xingray.java.util;
    requires com.xingray.java.json;

    exports com.xingray.java.server.spring.valid;
    exports com.xingray.java.server.spring.mvc.param;
}
