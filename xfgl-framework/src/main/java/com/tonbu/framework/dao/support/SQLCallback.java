package com.tonbu.framework.dao.support;

import java.sql.SQLException;

public interface SQLCallback {
    Object doInCallback(String var1, Object... var2) throws SQLException;
}
