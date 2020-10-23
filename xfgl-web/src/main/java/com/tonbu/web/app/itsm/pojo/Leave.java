package com.tonbu.web.app.itsm.pojo;

import com.tonbu.framework.annotation.ID;
import com.tonbu.framework.annotation.NotField;
import com.tonbu.framework.annotation.Table;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@Table(Leave.table)
@ID("id")
public class Leave {
//请假信息
@NotField
public static final String table="TB_YSDJ_LEAVE";
    private String id;
    //         主键
    private String ysdj_id;
    //         要事登记表主键
    private String xm;
    //         姓名
    private String  zw;
    //         职务
    private String  sy;
    //         事由
    private String  days	;
    //         天数
    private String  spr	;
    //         审批人（审批部门）
    private String  leave_startdate ;
    //             离队时间
    private String  leave_enddate	;
    //             归队时间
    private String  leave_excess	;
//             超假天数

}