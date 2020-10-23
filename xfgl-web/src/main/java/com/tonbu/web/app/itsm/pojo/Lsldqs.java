package com.tonbu.web.app.itsm.pojo;

import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@Table(Lsldqs.table)
@ID("id")
 public class Lsldqs{
    @NotField
    public static final String table="TB_YSDJ_LSLDQS";
    //临时来队亲属
    private String id;
//            主键
    private String ysdj_id;
//            要事登记表主键;
    private String xfyxm;
//            消防员姓名;
    private String qsxm;
//            亲属姓名;
    private String gx;
//            关系;
    private String time_laid;
//            来队时间;
    private String time_lid;
//            离队时间;


}