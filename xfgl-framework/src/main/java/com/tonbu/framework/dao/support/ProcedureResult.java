package com.tonbu.framework.dao.support;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProcedureResult {
    private Map<String, Object> retMap;

    public ProcedureResult(Map<String, Object> retmap) {
        this.retMap = retmap;
    }

    public Map<String, Object> getResultMap() {
        return this.retMap;
    }

    public List<Map<String, Object>> getDataSet(int index) {
        return (List)this.retMap.get("dataset_" + index);
    }

    public List<Map<String, Object>> getFirstDataSet() {
        return this.getDataSet(0);
    }

    public Object get(String key) {
        key = key.toLowerCase();
        return this.retMap.get(key);
    }

    public String getString(String key) {
        return (String)this.get(key);
    }

    public BigDecimal getBigDeciaml(String key) {
        return (BigDecimal)this.get(key);
    }

    public Integer getInteger(String key) {
        return (Integer)this.get(key);
    }

    public Float getFloat(String key) {
        return (Float)this.get(key);
    }

    public Double getDouble(String key) {
        return (Double)this.get(key);
    }

    public List<Map<String, Object>> getList(String key) {
        return (List)this.get(key);
    }
}

