package info.ilambda.rhodiola.spring;

import info.ilambda.rhodiola.core.Rhodiola;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Properties;

public class RhodiolaFactoryBean implements BeanPostProcessor,FactoryBean<Rhodiola>,DisposableBean {
    private Rhodiola rhodiola;
    private volatile boolean destroyed = false;

    public RhodiolaFactoryBean() {
        this.rhodiola = Rhodiola.start(new Properties(){{put("scanPackage", false);}});
    }

    @Override
    public void destroy() throws Exception {
        if (rhodiola != null && !destroyed) {
            rhodiola.stop().get();
        }
        destroyed = true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!destroyed) {
            this.rhodiola.register(bean);
        }
        return bean;
    }

    @Override
    public Rhodiola getObject() throws Exception {
        if (rhodiola != null && !destroyed) {
            return rhodiola;
        } else {
            throw new IllegalStateException("Rhodiola is destroyed");
        }
    }

    @Override
    public Class<?> getObjectType() {
        return Rhodiola.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
