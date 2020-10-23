package com.tonbu.framework.dao.dialect;

import com.tonbu.framework.util.ClassUtils;
import org.springframework.beans.factory.FactoryBean;


public class DialectFactory implements FactoryBean<Dialect> {
    private String name = null;
    private Dialect target = null;

    public DialectFactory() {
    }

    public Dialect getObject() throws Exception {
        if (this.target == null) {
            this.target = (Dialect)ClassUtils.newInstance(this.name);
        }

        return this.target;
    }

    public Class<?> getObjectType() {
        return Dialect.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }
}
