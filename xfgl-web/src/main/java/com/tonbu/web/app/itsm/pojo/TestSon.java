package com.tonbu.web.app.itsm.pojo;

import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
/*@ApiModel(value = "TestSon")
@Table(TestSon.table)
@ID("id")*/
public class TestSon implements Serializable{
//请假信息
//    @NotField
    public static final String table="TB_YSDJ_LSLDQS";

    private String id;
    //         主键
    private String testpojo_id;
    //         要事登记表主键
    private String xm;
    //         姓名

}