package com.tonbu.web.app.itsm.pojo;

import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@Table(Cpcs.table)
@ID("id")
public class Cpcs{
    @NotField
    public static final String table="TB_YSDJ_CPCS";
    //查铺查哨
    private String id;
//            主键
    private String ysdj_id;
//            要事登记表主键
    private String jcr;
//            检查人
    private String datetime;
//            检查时间
    private String  lby;
//            领班员
    private String  jwry;
//            警卫人员
    private String jcqk	;
//            检查情况

}