package org.hotswap.agent.plugin.glassfish;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author lysenko
 */
@Startup
@Singleton
public class ForHotswap {
    @PostConstruct
    public void sayHello(){
        System.out.println(getClass().getName());
    }

    public String getHello(){
        return "Hello Hotswap twice";
    }
}
