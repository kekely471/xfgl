package com.tonbu.web.admin.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-03-05 10:15
 **/
@Data
public class Submit implements Serializable {
    private String ecName;
    private String apId;
    private String secretKey;
    private String mobiles;
    private String content;
    private String sign;
    private String addSerial;
    private String MAC;

}
