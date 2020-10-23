package com.tonbu.web.admin.bean;

import java.io.Serializable;


/**
 * 类描述： [人员]
 */
public class Point implements Serializable{

	private static final long serialVersionUID = 1L;


	/**
	 * 序列
	 */
	private String id;
	/**
	 * 用户名
	 */
	private String account;

	/**
	 * 加密密码
	 */
	private String gldw;

	/**
	 * 手机
	 */
	private String phone;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 身份证号码
	 */
	private String idcard;
	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 头像
	 */
	private String avatar;
	/**
	 * 账号状态（0: 正常 1：禁用 ）
	 */
	private String job;


	/**
	 * 警衔
	 */
	private String policerank;
	/**
	 *
	 *    单位编码
	 */
	private String dwbm;

	/**
	 * 单位名称
	 */

	private String dwmc;
	/**
	 *
	 *     上级单位编码（米）
	 */
	private String sjdwbm;

	/**
	 *上级单位名称
	 */
	private String sjdwmc;

	/**
	 *入伍年月
	 */
	private String rwny;
	/**
	 *
	 * 关联用户表ID
	 */
	private String ss_id;


	/**
	 *探亲假天数
	 */
	private String tqxjts;

	/**
	 *年休假天数
	 */
	private String nxjts;
	/**
	 *TQXJT探亲年假天数是否减半（0: 否 1：是 ）
	 */
	private String tqxjtssfjb;
	/**
	 *是否可以离队住宿(40天)（0: 否 1：是 ）
	 */
	private String sfkyldzs;

	/**
	 *籍贯
	 */
	private String NATIVE_PLACE;
	/** ------------ 表单查询字段添加在下面 ------------ **/

	/** ---------- 数据库查询结果字段添加在下面 ---------- **/
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getGldw() {
		return gldw;
	}

	public void setGldw(String gldw) {
		this.gldw = gldw;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getPolicerank() {
		return policerank;
	}

	public void setPolicerank(String policerank) {
		this.policerank = policerank;
	}

	public String getDwbm() {
		return dwbm;
	}

	public void setDwbm(String dwbm) {
		this.dwbm = dwbm;
	}

	public String getDwmc() {
		return dwmc;
	}

	public void setDwmc(String dwmc) {
		this.dwmc = dwmc;
	}

	public String getSjdwbm() {
		return sjdwbm;
	}

	public void setSjdwbm(String sjdwbm) {
		this.sjdwbm = sjdwbm;
	}

	public String getSjdwmc() {
		return sjdwmc;
	}

	public void setSjdwmc(String sjdwmc) {
		this.sjdwmc = sjdwmc;
	}

	public String getRwny() {
		return rwny;
	}

	public void setRwny(String rwny) {
		this.rwny = rwny;
	}

	public String getSs_id() {
		return ss_id;
	}

	public void setSs_id(String ss_id) {
		this.ss_id = ss_id;
	}

	public String getTqxjts() {
		return tqxjts;
	}

	public void setTqxjts(String tqxjts) {
		this.tqxjts = tqxjts;
	}

	public String getNxjts() {
		return nxjts;
	}

	public void setNxjts(String nxjts) {
		this.nxjts = nxjts;
	}

	public String getTqxjtssfjb() {
		return tqxjtssfjb;
	}

	public void setTqxjtssfjb(String tqxjtssfjb) {
		this.tqxjtssfjb = tqxjtssfjb;
	}

	public String getSfkyldzs() {
		return sfkyldzs;
	}

	public void setSfkyldzs(String sfkyldzs) {
		this.sfkyldzs = sfkyldzs;
	}

	public String getNATIVE_PLACE() {
		return NATIVE_PLACE;
	}

	public void setNATIVE_PLACE(String NATIVE_PLACE) {
		this.NATIVE_PLACE = NATIVE_PLACE;
	}
}