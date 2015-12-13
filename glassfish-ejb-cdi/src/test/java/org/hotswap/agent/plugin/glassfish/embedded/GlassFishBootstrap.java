package org.hotswap.agent.plugin.glassfish.embedded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author lysenko
 */
public class GlassFishBootstrap {

    private static final Logger LOG = Logger.getLogger(GlassFishBootstrap.class.getName());

    public URL url;
    String appName;
    GlassFish glassfish;
    Deployer deployer;
    final String contextroot = "test";

    public GlassFishBootstrap() {
        try {
            this.url = new URL("http://localhost:8282/");
        } catch (MalformedURLException ex) {
            throw new RuntimeException();
        }
    }

    @Before
    public void startAndDeploy() throws Exception {
        GlassFishProperties glassfishProperties = new GlassFishProperties();
        glassfishProperties.setPort("http-listener", 8282);
        glassfishProperties.setPort("https-listener", 8383);
        glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
        glassfish.start();
        deployer = glassfish.getDeployer();
        ScatteredArchive archive = new ScatteredArchive("testapp", ScatteredArchive.Type.WAR);
        // target/classes directory contains complied servlets
        archive.addClassPath(new File("target", "test-classes"));
        appName = deployer.deploy(archive.toURI(), "--contextroot="+contextroot);

    }

    @Test
    public void testMethod() throws Exception {
        Assert.assertEquals("Hello", readResponce());
    }

    @After
    public void undployAndStart() throws Exception {
        deployer.undeploy(appName);
        glassfish.stop();
        glassfish.dispose();
    }

    String readResponce() throws IOException, MalformedURLException {
        URL oracle = new URL("http://" + url.getHost() + ":" + url.getPort() + "/"+contextroot+"/activator");
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine = in.readLine();
        in.close();
        LOG.info(inputLine);
        return inputLine;
    }

}
