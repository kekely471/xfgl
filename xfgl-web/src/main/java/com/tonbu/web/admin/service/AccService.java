package com.tonbu.web.admin.service;

import com.tonbu.framework.data.ResultEntity;

public interface AccService {

    ResultEntity updatePassword(String oldpassword, String password, String password2);
    
}
