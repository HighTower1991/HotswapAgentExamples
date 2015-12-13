package org.hotswap.agent.plugin.glassfish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author lysenko
 */
@RunWith(Arquillian.class)
public class InitTest {

    private static final Logger LOG = Logger.getLogger(InitTest.class.getName());

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "Test.war");
        archive.addClasses(ForHotswap.class, ActivatorServlet.class);
        archive.addAsResource("hotswap-agent.properties");
        return archive
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @ArquillianResource public URL url;

    @RunAsClient
    @Test
    public void testHello() throws Exception {
        LOG.info("" + url);
        assertEquals("Hello", readResponce());
        hotswap(ForHotswap.class, "target/hotswap-classes/");
        LOG.info("Swap");
        Thread.sleep(1000);
        assertEquals("Hello Hotswap", readResponce());
        System.gc(); //release file handler and lock
        hotswap(ForHotswap.class, "target/hotswap-classes-twice/");
        LOG.info("Swap twice");
        Thread.sleep(1000);
        assertEquals("Hello Hotswap twice", readResponce());
    }

    public static void hotswap(Class toHotswap, String from) throws Exception {
        String swapedFile = toHotswap.getName().replace(".", "/") + ".class";
        Path source = Paths.get(from + swapedFile);
//        Path target = Paths.get("target/glassfish4/glassfish/domains/domain1/applications/Test/WEB-INF/classes/" + swapedFile);
        Path target = Paths.get("target/classes/" + swapedFile);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    String readResponce() throws IOException, MalformedURLException {
        URL oracle = new URL("http://" + url.getHost() + ":" + url.getPort() + "/Test/activator");
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine = in.readLine();
        in.close();
        LOG.info(inputLine);
        return inputLine;
    }

}
