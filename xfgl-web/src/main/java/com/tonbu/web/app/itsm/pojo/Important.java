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
 * @create: 2020-06-30 10:16
 **/
@Data
@Table(Important.table)
@ID("id")
public class Important implements Serializable {

    @NotField
    public static final String table="TB_YSDJ";

private String  id;
//            主键
private String datetime;
//            x月x日;
private String week;
//            星期x;
private String weather;
//            天气
private String zby;
//            值班员;
private String  bz_gb_count;
//            编制干部人数;
private String bz_xfy_count;
//            编制消防员人数;
private String bz_count;
//            编制合计人数;
private String xy_gb_count;
//            现有干部人数;
private String xy_xfy_count;
//            现有消防员人数;
private String  xy_count;
//            现有合计人数;
private String  rwqk_zc_nr;
//            任务情况早晨内容;
private String rwqk_zc_yd_count;
//            任务情况早晨应到人数;
private String rwqk_zc_sd_count;
//            任务情况早晨实到人数;
private String  rwqk_zc_percent;
//            任务情况早晨到课率;
private String rwqk_sw_nr;
//            任务情况上午内容;
private String  rwqk_sw_yd_count;
//            任务情况上午应到人数;
private String  rwqk_sw_sd_count;
//            任务情况上午实到人数;
private String rwqk_sw_percent;
//            任务情况上午到课率;
private String  rwqk_xw_nr;
//            任务情况下午内容;
private String rwqk_xw_yd_count;
//            任务情况下午应到人数;
private String rwqk_xw_sd_count;
//            任务情况下午实到人数;
private String rwqk_xw_percent;
//            任务情况下午到课率;
private String rwqk_ws_nr;
//            任务情况晚上内容;
private String rwqk_ws_yd_count;
//            任务情况晚上应到人数
private String rwqk_ws_sd_count;
//            任务情况晚上实到人数;
private String  rwqk_ws_percent;
//            任务情况晚上到课率;
private String  gcqw;
//            公差勤务;
private String  ryzbbd;
//            人员或装备变动;
private String fjjcqk;
//            风纪检查情况;
private String  pbqk_zby_jiaobz;
//            派班情况值班员交班者;
private String pbqk_zby_jiebz;
//            派班情况值班员接班者;
private String pbqk_zry_jiaobz;
//            派班情况值日员交班者;
private String pbqk_zry_jiebz;
//            派班情况值日员接班者;
private String pbqk_cfzby_jiaobz;
//            派班情况厨房值班员交班者;
private String pbqk_cfzby_jiebz;
//            派班情况厨房值班员接班者;
private String  pbqk_jj_qk;
//            派班情况交接情况;
private String  pbqk_jj_date;
//            派班情况交接时间;
private String  pbqk_zzz;
//            派班情况组织者
private String  zysx;
//            上级通知、指示及其他重要事项;
private String status;
//            状态（1 提交 0：保存）;
private String  create_ss_id;
//            创建者;
private String create_date;
//            创建时间;
private String  bhjclqk;
//            病号及处理情况;

    @NotField
    private List<Leave> leave;
    //请假信息
    @NotField
    private List<Cpcs> cpcs;
    //查铺查哨
    @NotField
    private List<Lsldqs> lsldqs;
    //临时来队亲属

 }





