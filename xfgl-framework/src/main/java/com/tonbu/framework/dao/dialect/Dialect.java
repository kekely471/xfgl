package com.tonbu.framework.dao.dialect;

public abstract class Dialect {
    public Dialect() {
    }

    public abstract boolean supportsLimitOffset();

    public abstract String getLimitString(String var1, int var2, int var3);

    public abstract String getCountString(String var1);
}
