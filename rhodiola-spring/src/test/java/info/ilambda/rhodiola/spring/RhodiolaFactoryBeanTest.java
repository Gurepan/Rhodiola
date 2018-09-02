package info.ilambda.rhodiola.spring;

import info.ilambda.rhodiola.core.Rhodiola;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:application-test.xml")
public class RhodiolaFactoryBeanTest {
    @Autowired
    private Rhodiola rhodiola;
    @Test
    public void test() throws InterruptedException {
        rhodiola.post("你好！").AsyncPost(new SpringActorTest.Message0());
        Thread.sleep(5000);
    }
}