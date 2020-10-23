package com.tonbu.web.app.itsm.pojo;

import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-06-30 10:58
 **/
@Data

@Table(TestPojo.table)
@ID("id")
public class TestPojo implements Serializable {

    @NotField
    public static final String table="TB_TESTPOJO";

    private String  id;
    //            主键
    private String datetime;
    //            x月x日;
    private String week;
    //            星期x;
    private String weather;

    @NotField
    private List<TestSon> leave;
}


