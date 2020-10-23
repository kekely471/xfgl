package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommonServiceImpl implements CommonService {

    @Override
    public List<Object> getTreeList(Map<String, String> param) {

        List<Map<String, Object>> list = DBHelper.queryForList("SELECT ID,PARENTID,MENUNAME FROM SS_MENU WHERE STATUS='1' ");
        List<Object> treeList = getMenus("00", list);
        return treeList;
    }

//    public List<Map<String,Object>> getChildrenTree(List<Map<String,Object>> list){
//
//        for(Map<String,Object> loop : list){
//            loop.put("name",loop.get("name"));
//            String id = loop.get("id")==null? "":loop.get("id")+"";
//            List<Map<String,Object>> cList = DBHelper.queryForList("select id,parentid pid,menuname name from ss_menu where status='1' and parentid=? ",id);
//            loop.put("children",cList);
//            getChildrenTree(cList);
//        }
//        return list;
//    }

    private List<Object> getMenus(String id, List<Map<String, Object>> menus) {
        List<Object> r = new ArrayList<>();
        for (Map<String, Object> map : menus) {
            if (StringUtils.equals(id, map.get("PARENTID").toString())) {
                Map<String, Object> map_c = new HashMap<>();
                map_c.put("id", map.get("ID"));
                map_c.put("pid", map.get("PARENTID"));
                map_c.put("url", map.get("URL"));
                map_c.put("icon", map.get("ICON"));
                map_c.put("name", map.get("MENUNAME"));
                //找兒子，看兒子是不是有數據，如果有，遞歸；
                List<Object> c = getMenus(map.get("ID").toString(), menus);
                if (c.size() > 0) {
                    map_c.put("children", c);
                }
                r.add(map_c);
            }
        }
        return r;
    }

    /**
     * 通用左边树生成,全部数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadTree(Map<String, String> param) {
        String pid = param.get("parentid");
        String tableid = param.get("tableid");
        if (StringUtils.isBlank(tableid)) {
            throw new RuntimeException("缺少参数！");
        }
        List<Map<String, Object>> l = new ArrayList<>();
        if (StringUtils.isBlank(pid)) {
            pid = "0";
        }
        getChildNodes(l, pid, tableid);
        return l;
    }

    private void getChildNodes(List<Map<String, Object>> l, String pId, String tableid) {
        List<Map<String, Object>> l_r = getChildData(pId, tableid);
        if (l_r.size() > 0) {
            for (Map<String, Object> r :
                    l_r) {
                //递归子节点
                Map<String, Object> map_1 = new LinkedHashMap<>();
                map_1.put("text", r.get("NAME"));
                map_1.put("type", AppConstant.NODE_FOLDER);
                getChildItems(map_1, r.get("ID").toString(), r.get("NAME").toString(), tableid);
                l.add(map_1);
            }
        }
    }

    private void getChildItems(Map<String, Object> map_1, String pId, String parentName, String tableid) {
        List<Map<String, Object>> l_r = getChildData(pId, tableid);
        if (l_r.size() > 0) {
            map_1.put("text", parentName);
            map_1.put("type", AppConstant.NODE_FOLDER);
            //AdditionalParameters
            Map<String, Object> additionalParameters = new LinkedHashMap<>();
            additionalParameters.put("id", pId);
            Map<String, Object> map_c = new LinkedHashMap<>();
            for (Map<String, Object> r :
                    l_r) {
                Map<String, Object> map_2 = new LinkedHashMap<>();
                getChildItems(map_2, r.get("ID").toString(), r.get("NAME").toString(), tableid);
                map_c.put(r.get("NAME").toString(), map_2);
            }
            additionalParameters.put("children", map_c);
            map_1.put("additionalParameters", additionalParameters);
        } else {
            map_1.put("text", parentName);
            map_1.put("type", AppConstant.NODE_ITEM);
            Map<String, Object> additionalParameters = new LinkedHashMap<>();
            additionalParameters.put("ID", pId);
            map_1.put("additionalParameters", additionalParameters);
        }
    }

    private List<Map<String, Object>> getChildData(String pId, String tableid) {
        switch (tableid) {
            case "ss_dept":
                return DBHelper.queryForList("SELECT ID,DEPTNAME NAME FROM SS_DEPT WHERE PARENTID=? AND STATUS <> ? ORDER BY SORT ", pId, AppConstant.STATUS_DEL);
            case "ss_menu":
                return DBHelper.queryForList("SELECT ID,MENUNAME NAME FROM SS_MENU WHERE PARENTID=? AND STATUS <> ? ORDER BY SORT ", pId, AppConstant.STATUS_DEL);
            default:
                return DBHelper.queryForList("SELECT ID,NAME FROM " + tableid + " WHERE PARENTID=? AND STATUS <> ? ORDER BY SORT ", pId, AppConstant.STATUS_DEL);

        }
    }


    /**
     * ZTREE的数据源
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getZtreeData(Map<String, String> param) {
        String tableid = param.get("tableid");
        if (StringUtils.isBlank(tableid)) {
            throw new RuntimeException("缺少参数！");
        }
        return DBHelper.queryForList("select id,parentid pId,name,'true' open  from " + tableid + " where status <> ? order by sort ", AppConstant.STATUS_DEL);
    }

    //部门下拉框数据源
    public List<Map<String, Object>> loadDeptList() {
        return DBHelper.queryForList("SELECT ID,DEPTNAME NAME,DEPTNAME||'('||DEPTNO||')' NAME2  FROM SS_DEPT WHERE STATUS <> ? ORDER BY SORT ", AppConstant.STATUS_DEL);
    }

    //用户下拉框数据源
    public List<Map<String, Object>> loadUserList() {
        return DBHelper.queryForList("SELECT ID,USERNAME NAME  FROM SS_USER WHERE STATUS <> ? AND ID<>1 ORDER BY SORT ", AppConstant.STATUS_DEL);
    }

    //字典类型下拉框数据源
    public List<Map<String, Object>> loadDictTypeList(String type) {
        return DBHelper.queryForList("SELECT CODE ID,NAME  FROM SS_DICT_TYPE WHERE TYPE=? ORDER BY ID ", type);
    }

    //菜单下拉框数据源
    public List<Map<String, Object>> loadMenuList() {
        return DBHelper.queryForList("SELECT ID,MENUNAME NAME FROM SS_MENU WHERE STATUS <> ? ORDER BY SORT ", AppConstant.STATUS_DEL);
    }

    //服务目录
    public List<Map<String, Object>> loadServiceDirectoryList() {
        return DBHelper.queryForList("SELECT ID,NAME,EXT1 FROM BUSINESS_DIRECTORY WHERE STATUS <> ? ORDER BY SORT ", AppConstant.STATUS_DEL);
    }

    //sla
    public List<Map<String, Object>> loadSlaList() {
        return DBHelper.queryForList("SELECT ID,NAME,EXT1 FROM BUSINESS_SLA WHERE STATUS <> ? ORDER BY SORT ", AppConstant.STATUS_DEL);
    }

    //合同
    public List<Map<String, Object>> loadContractList(String dept_id) {
        return DBHelper.queryForList("SELECT ID,CONTRACTNAME NAME,CONCAT(CONTRACTNAME,'(',CONTRACTNO,')') NAME2 FROM BUSINESS_CONTRACT WHERE STATUS = ? AND DEPT_ID=? ORDER BY SIGN_TIME DESC ", AppConstant.STATUS_ENABLED, dept_id);
    }


    //流程
    public List<Map<String, Object>> loadFlowList() {
        return DBHelper.queryForList("SELECT ID,FLOW_NAME NAME,CONCAT(FLOW_NAME,'(',FLOW_NO,')') NAME2 FROM FLOW_INFO WHERE STATUS <> ?  ", AppConstant.STATUS_DEL);
    }

    //步骤
    public List<Map<String, Object>> loadStepList(String flow_id) {

        List<Map<String, Object>> l = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("ID", "0");
        m.put("NAME", "流程发起");
        l.add(m);
        if (StringUtils.isBlank(flow_id)) {
            return l;
        } else {
            String flow_no = DBHelper.queryForScalar("SELECT FLOW_NO FROM FLOW_INFO WHERE ID=? ", String.class, flow_id);
            l.addAll(DBHelper.queryForList("SELECT ID,STEP_NAME NAME FROM FLOW_STEP WHERE STATUS <> ? AND FLOW_NO=? ", AppConstant.STATUS_DEL, flow_no));
        }
        return l;
    }

    /**
     * 获取系统字典数据源
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadSSDictList(Map<String, String> param) {
        String type_code = param.get("type_code");

        return DBHelper.queryForList(" SELECT ID,NAME,VALUE,EXT1 FROM SS_DICT WHERE TYPE_CODE=? AND STATUS <> '" + AppConstant.STATUS_DEL + "' ORDER BY SORT ", type_code);
    }

    /**
     * 获取业务字典数据源
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadBusDictList(Map<String, String> param) {
        String type_code = param.get("type_code");
        return DBHelper.queryForList("  SELECT ID ,VALUE,NAME,EXT1 FROM BUSINESS_DICT WHERE TYPE_CODE=? AND STATUS = '" + AppConstant.STATUS_ENABLED + "' ORDER BY SORT ", type_code);
    }

    /**
     * 获取单位字典
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadDeptDictList(Map<String, String> param) {
//        return DBHelper.queryForList("   SELECT DEPTNO as ID ,DEPTNO  AS VALUE,DEPTNAME AS NAME FROM SS_DEPT  ORDER BY SORT ");
//        return DBHelper.queryForList("SELECT ID as ID,DEPTNAME NAME,DEPTNAME||'('||DEPTNO||')' NAME2  FROM SS_DEPT WHERE STATUS <> ? and isunit =1  ORDER BY SORT ", AppConstant.STATUS_DEL);
        return DBHelper.queryForList("SELECT ID as ID,DEPTNAME NAME,DEPTNAME||'('||DEPTNO||')' NAME2  FROM SS_DEPT WHERE STATUS <> ?   ORDER BY SORT ", AppConstant.STATUS_DEL);

    }
    /**
     * 获取单位字典
     *
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> loadJobDictList(Map<String, String> param) {
        return DBHelper.queryForList("select  value as ID,NAME from BUSINESS_DICT d where d.TYPE_CODE='zw' ");

    }

    /**
     * 获取考勤新增单位字典
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadkqDeptDictList(Map<String, String> param) {
//        return DBHelper.queryForList("   SELECT DEPTNO as ID ,DEPTNO  AS VALUE,DEPTNAME AS NAME FROM SS_DEPT  ORDER BY SORT ");
        return DBHelper.queryForList("SELECT ID as ID, DEPTNAME NAME, DEPTNAME || '(' || DEPTNO || ')' NAME2 FROM SS_DEPT WHERE STATUS <> ? and isunit=1 and id not in (select bm from TB_KQPZ) ORDER BY SORT ", AppConstant.STATUS_DEL);

    }


    /**
     * 获取单位字典
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> informationTypeList(Map<String, String> param) {
        return DBHelper.queryForList("SELECT value as ID,NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'information_type'  AND bd.STATUS<> ? ORDER BY SORT ", AppConstant.STATUS_DEL);

    }

    /**
     * 获取单位字典
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> zbztTypeList(Map<String, String> param) {
        return DBHelper.queryForList("SELECT value as ID,NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zbtype'  AND bd.STATUS<> ? ORDER BY SORT ", AppConstant.STATUS_DEL);

    }


    /**
     * 获取角色数据源
     *
     * @return
     */
    public List<Map<String, Object>> loadRoleList() {
        return DBHelper.queryForList("SELECT ID,ROLENAME NAME FROM SS_ROLE WHERE STATUS <> ?  ", AppConstant.STATUS_DEL);
    }


    /**
     * 权限数据源
     *
     * @param param
     * @return 这个返回值没有采用大写，原因是ZTREE这个狗日的控件，自定义参数名有BUG，等待新版本更新没有该问题之后修正
     */
    public List<Map<String, Object>> loadMenuZTree(Map<String, String> param) {
        String rid = param.get("rid");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select DISTINCT  v.id,v.parentid pId ,v.menuname name,'true' open , case when rm.role_id is null then 'false' else 'true' end checked,v.sort  from ss_menu v   ");
        sql.append(" left join ss_role_menu rm on rm.menu_id=v.id and rm.role_id=? ");
        sql.append(" where v.status <>  " + AppConstant.STATUS_DEL);
        sql.append(" order by v.sort ");
        args.add(rid);
        return DBHelper.queryForList(sql.toString(), args.toArray());
    }


    /**
     * 流程管理的左边树生成,全部数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> loadFlowInfoTree(Map<String, String> param) {
        List<Map<String, Object>> l = new ArrayList<>();
        List<Map<String, Object>> l_r = DBHelper.queryForList("SELECT FLOW_NAME,ID,FLOW_NO FROM FLOW_INFO WHERE  STATUS <> ? ORDER BY SORT ", AppConstant.STATUS_DEL);

        if (l_r.size() > 0) {
            for (Map<String, Object> r :
                    l_r) {
                Map<String, Object> map_1 = new LinkedHashMap<>();
                map_1.put("text", r.get("FLOW_NAME") + "(" + r.get("FLOW_NO") + ")");
                map_1.put("type", AppConstant.NODE_ITEM);
                //AdditionalParameters
                Map<String, Object> additionalParameters = new LinkedHashMap<>();
                additionalParameters.put("id", r.get("ID"));
                map_1.put("additionalParameters", additionalParameters);
                l.add(map_1);
            }

        }
        return l;
    }

    @Override
    public List<Map<String, Object>> loadTreeList(Map<String, String> param) {
        String pid = param.get("parentid");
        String tableid = param.get("tableid");
        if (StringUtils.isBlank(tableid)) {
            throw new RuntimeException("缺少参数！");
        }
        if (StringUtils.isBlank(pid)) {
            pid = "0";
        }
        List<Map<String, Object>> list = getList(tableid);
        List<Map<String, Object>> treeList = getChildTree(pid, list);
        return treeList;
    }

    @Override
    public List<Map<String, Object>> loadzTreeList(Map<String, String> param) {
        String pid = param.get("parentid");
        String tableid = param.get("tableid");
        if (StringUtils.isBlank(tableid)) {
            throw new JSONException("缺少参数！");
        }
        if (StringUtils.isBlank(pid)) {
            pid = "0";
        }
        List<Map<String, Object>> list = getList(tableid);
        return list;
    }

    private List<Map<String, Object>> getChildTree(String id, List<Map<String, Object>> list) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (StringUtils.equals(id, map.get("PARENTID").toString())) {
                Map<String, Object> map_c = new HashMap<>();
                map_c.put("id", map.get("ID"));
                map_c.put("pid", map.get("PARENTID"));
                map_c.put("url", map.get("URL"));
                map_c.put("icon", map.get("ICON"));
                map_c.put("name", map.get("NAME"));
                //查找子节点
                List<Map<String, Object>> c = getChildTree(map.get("ID").toString(), list);
                if (c.size() > 0) {
                    map_c.put("children", c);
                }
                r.add(map_c);
            }
        }
        return r;
    }

    private List<Map<String, Object>> getList(String tableid) {
        switch (tableid) {
            case "ss_dept":
                return DBHelper.queryForList("SELECT ID,DEPTNAME NAME,PARENTID FROM SS_DEPT WHERE STATUS <> ? ", AppConstant.STATUS_DEL);
            case "ss_menu":
                return DBHelper.queryForList("SELECT ID,MENUNAME NAME,PARENTID FROM SS_MENU WHERE STATUS <> 0 ");
            default:
                return DBHelper.queryForList("SELECT ID,NAME,PARENTID FROM " + tableid + " WHERE STATUS <> ? ", AppConstant.STATUS_DEL);
        }
    }

    /**
     * 获取所有字典类型
     *
     * @return
     */
    public List<Map<String, Object>> getDictTypeList() {
        return DBHelper.queryForList("SELECT d.type_code as \"type_code\",dt.name as \"name\" FROM SS_DICT d left join SS_DICT_TYPE dt on dt.code = d.type_code where  STATUS <>? group by d.type_code,dt.name ", AppConstant.STATUS_DEL);
    }

    /**
     * 根据表名查询数据源
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getDataByTable(Map<String, String> param) {
        String tableName = param.get("table_name");
        if (tableName == null) {
            tableName = param.get("'table_name'");
            tableName = tableName == null ? null : tableName.replaceAll("'", "");
        }
        return DBHelper.queryForList("select * from  " + tableName + "");
    }

    /*
     * 动态组件渲染数据
     * */
    @Transactional(value = "primaryTransactionManager")
    public List<Map<String, Object>> getRenderingKeyBySSFORMFRAME(Map<String, String> param) {
        //扩展项
        StringBuffer delSql = new StringBuffer(" DELETE FROM SS_FORMFRAME_VALUE WHERE FORM_ID=? and FORM_TYPE=?");
        DBHelper.execute(delSql.toString(), param.get("id"), param.get("formType"));
        StringBuffer extendSql = new StringBuffer(" INSERT INTO SS_FORMFRAME_VALUE(ID,FORM_EXTEND_ID,FORM_ID,VALUE,FORM_TYPE) VALUES(?,?,?,?,?) ");

        //动态组件渲染数据
        StringBuffer renderingSQL = new StringBuffer(" SELECT A.* FROM SS_DICT B left JOIN  SS_FORMFRAME A " +
                " ON A.FORM_TYPE=B.VALUE " +
                " WHERE A.FORM_TYPE=? AND B.TYPE_CODE = 'bdlx'" +
                " AND A.ISDISABLED = 1 AND A.ISDELETE<>" + AppConstant.STATUS_DEL);
        List<Map<String, Object>> renderingList = DBHelper.queryForList(renderingSQL.toString(), param.get("formType"));
        for (int i = 0; i < renderingList.size(); i++) {
            if (renderingList.get(i).get("COMPONENT_TYPE").equals("checkbox")) {
                String values = parseMapForFilter(param, renderingList.get(i).get("FIELD_NAME").toString());/*renderingList.get(i).get("FIELD_NAME").toString()*/
                String value[] = values.split(",");
                String valuesInfo = "";
                for (String str : value) {
                    if (StringUtils.isNotBlank(str)) {
                        valuesInfo += str.trim() + ",";
                    }
                }
                if (StringUtils.isNotBlank(valuesInfo)) {
                    valuesInfo = valuesInfo.substring(0, valuesInfo.length() - 1);
                }
                DBHelper.execute(extendSql.toString(), getFormFrameId().toString(), renderingList.get(i).get("ID"), param.get("id"), valuesInfo, param.get("formType"));
            } else {
                DBHelper.execute(extendSql.toString(), getFormFrameId().toString(), renderingList.get(i).get("ID"), param.get("id"), param.get(renderingList.get(i).get("FIELD_NAME")) == null ? "" : param.get(renderingList.get(i).get("FIELD_NAME")), param.get("formType"));
            }

        }
        return renderingList;
    }

    /*
     * SS_FORMFRAME表序列
     * */
    public Integer getFormFrameId() {
        Integer currentId = DBHelper.queryForScalar(" SELECT FORM_FORMFRAME_VALUE_SEQ.NEXTVAL FROM DUAL ", Integer.class);
        return currentId;
    }

    /**
     * 从map中查询想要的map项，根据key
     */
    public static String parseMapForFilter(Map<String, String> map, String filters) {
        String checkboxs;
        if (map == null) {
            return null;
        } else {
            map = map.entrySet().stream()
                    .filter((e) -> checkKey(e.getKey(), filters))
                    .collect(Collectors.toMap(
                            (e) -> (String) e.getKey(),
                            (e) -> e.getValue()
                    ));
            checkboxs = map.values().toString();
        }
        return checkboxs.substring(1, checkboxs.length() - 1);
    }

    /**
     * 通过indexof匹配想要查询的字符
     */
    private static boolean checkKey(String key, String filters) {
        if (key.indexOf(filters) > -1) {
            return true;
        } else {
            return false;
        }
    }

}
