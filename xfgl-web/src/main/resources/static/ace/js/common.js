function listToLeftTreeData(data, opt) {
    var idField, treeField, parentField;
    if (opt.parentField) {
        idField = opt.idField || 'id';
        treeField = opt.textField || 'text';
        parentField = opt.parentField || 'pid';

        var i, l, treeData = [], tmpMap = [];
        for (i = 0, l = data.length; i < l; i++) {
            tmpMap[data[i][idField]] = data[i];
        }

        for (i = 0, l = data.length; i < l; i++) {
            if (tmpMap[data[i][parentField]] && data[i][idField] != data[i][parentField]) {
                if (!tmpMap[data[i][parentField]]['item'])
                    tmpMap[data[i][parentField]]['item'] = [];
                data[i]['text'] = data[i][treeField];
                tmpMap[data[i][parentField]]['item'].push(data[i]);
            } else {
                data[i]['text'] = data[i][treeField];
                treeData.push(data[i]);
            }
        }
        return treeData;
    }
    return data;
}


//jdGrid
//replace icons with FontAwesome icons like above
function updatePagerIcons(table) {
    var replacement =
        {
            'ui-icon-seek-first': 'ace-icon fa fa-angle-double-left bigger-140',
            'ui-icon-seek-prev': 'ace-icon fa fa-angle-left bigger-140',
            'ui-icon-seek-next': 'ace-icon fa fa-angle-right bigger-140',
            'ui-icon-seek-end': 'ace-icon fa fa-angle-double-right bigger-140'
        };
    $('.ui-pg-table:not(.navtable) > tbody > tr > .ui-pg-button > .ui-icon').each(function () {
        var icon = $(this);
        var $class = $.trim(icon.attr('class').replace('ui-icon', ''));

        if ($class in replacement) icon.attr('class', 'ui-icon ' + replacement[$class]);
    })
}

function enableTooltips(table) {
    $('.navtable .ui-pg-button').tooltip({container: 'body'});
    $(table).find('.ui-pg-div').tooltip({container: 'body'});
}

//it causes some flicker when reloading or navigating grid
//it may be possible to have some custom formatter to do this as the grid is being created to prevent this
//or go back to default browser checkbox styles for the grid
function styleCheckbox(table) {
    // $(table).find('input:checkbox').addClass('ace')
    // .wrap('<label />')
    // .after('<span class="lbl align-top" />')
    // $('.ui-jqgrid-labels th[id*="_cb"]:first-child')
    // .find('input.cbox[type=checkbox]').addClass('ace')
    // .wrap('<label />').after('<span class="lbl align-top" />');
}

//获取url的值
function getQueryString() {
    var result = location.search.match(new RegExp("[\?\&][^\?\&]+=[^\?\&]+", "g"));
    for (var i = 0; i < result.length; i++) {
        result[i] = result[i].substring(1);
    }
    return result;
}

//根据name获取url的值
function getQueryStringByName(name) {
    var result = location.search.match(new RegExp("[\?\&]" + name + "=([^\&]+)", "i"));
    if (result == null || result.length < 1) {
        return "";
    }
    return result[1];
}

//根据jason数据绑定form表单
/**
 * 加载json的数据到页面的表单中，以name为唯一标示符加载
 * @param {String} jsonStr json表单数据
 */
function loadJsonDataToForm(jsonStr) {
    try {
        //var obj = eval("("+jsonStr+")");
        var obj = jsonStr;
        var key, value, tagName, type, arr;
        for (x in obj) {
            key = x;
            value = obj[x];
            $("[name='" + key + "'],[name='" + key + "[]']").each(function () {
                tagName = $(this)[0].tagName;
                type = $(this).attr('type');
                if (tagName == 'INPUT') {
                    if (type == 'radio') {
                        $(this).attr('checked', $(this).val() == value);
                    } else if (type == 'checkbox') {
                        if (value == "1") {
                            $(this).attr('checked', true);
                        } else {
                            $(this).attr('checked', false);
                        }
                    } else {
                        $(this).val(value);
                    }
                } else if (tagName == 'SELECT' || tagName == 'TEXTAREA') {
                    $(this).val(value);
                }

            });
        }
    } catch (e) {
        alert("加载表单：" + e.message + ",数据内容" + JSON.stringify(jsonStr));
    }
}

//序列化Form表单元素
function formSerialize(frmName) {
    var emtparam = {};
    var frm = null;
    if (typeof frmName == "string") {
        frm = jQuery(frmName);
    } else {
        frm = jQuery(frmName);
    }

    function serializeParam(i) {
        var key = this.name || this.id;
        if (key) {
            if (typeof this.value != "undefined") {
                if (this.dataType == "numberic" || this.datatype == "numberic") {
                    emtparam[key] = this.value.trim().replaceAll(",", "") || "0.00";
                } else {
                    emtparam[key] = this.value.trim() || "";
                }
            } else {
                emtparam[key] = "";
            }
        }
    }

    frm.find(":text").each(serializeParam);

    frm.find("input[type=hidden]").each(serializeParam);

    frm.find(":password").each(serializeParam);

    frm.find("select").each(serializeParam);

    frm.find("textarea").each(serializeParam);
    // :radio[checked]
    frm.find("input[type='radio']:checked").each(serializeParam);

    frm.find("input[type='checkbox']:checked").each(function (i) {
        var key = this.name || this.id;
        if (emtparam[key] != undefined) {
            emtparam[key] += "," + this.value;
        } else {
            emtparam[key] = this.value;
        }
    });

    return emtparam;
}

//初始化select控件
function initChosenSelect(_selector, _type_code, _source_type, _width, _value) {
    var html = '';
    $(_selector).empty();
    var _source = getSSourceDate(_type_code, _source_type);
    for (var i = 0; i < _source.length; i++) {
        html += '<option value="' + _source[i].VALUE + '">' + _source[i].NAME + '</option>';
    }
    $(_selector).append(html);
    $(_selector).chosen({allow_single_deselect: true, width: _width == undefined ? "460px" : _width});
    if (_value == undefined||_value=="") {
        $(_selector).val("1");
    } else {
        $(_selector).val(_value);
    }
    $(_selector).trigger("chosen:updated");
}
//初始化排序控件
function initSort(_selector){
    $(_selector).ace_spinner({
        width: "140px",
        value: 1,
        min: 0,
        max: 999,
        step: 1,
        touch_spinner: true,
        icon_up: 'ace-icon fa fa-caret-up bigger-110',
        icon_down: 'ace-icon fa fa-caret-down bigger-110'
    });
}

//格式化状态列数据
function sex_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("sex",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

//是否判断
function bool_format(cellvalue, options, rowObject) {
    if(cellvalue=="0"){
        return "<span class='label label-danger'>否</span>";
    }else{
        return "<span class='label label-success'>是</span>";
    }
}

//菜单图标显示
function icon_format(cellvalue, options, rowObject) {
    if(cellvalue!=""){
        return "<i class=\"menu-icon "+cellvalue+" \"></i>";
    }else{
        return "";
    }
}

function status_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("data_status",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

//常见问题的发布情况
function faq_status_format(cellvalue, options, rowObject) {
    if (cellvalue == "1") {
        return "<span class='label label-success'>已发布</span>";
    } else {
        return "<span class='label label-grey'>未发布</span>";
    }
}

//格式化状态列数据
function sla_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("business_sla_type",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}
function role_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("role_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function user_type_format(cellvalue, options, rowObject){
    var _source=getSSourceDate("user_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function service_type_format(cellvalue, options, rowObject){
    var _source=getSSourceDate("business_service_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}
function dict_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function menu_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("menu_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function contract_status_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("business_contract_status",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}


function user_qual_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("user_qual_type",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function dept_qual_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("dept_qual_type",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

//step_type_format
function step_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("step_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}


//faq_type_format
function faq_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("faq_type",1);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

//knowledge_type_format
function knowledge_type_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("knowledge_type",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

//faq_type_format
function process_status_format(cellvalue, options, rowObject) {
    var _source=getSSourceDate("process_status",0);
    for(var i in _source){
        if(_source[i].VALUE==cellvalue){
            if(_source[i].EXT1==""||_source[i].EXT1==null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            }else{
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}
//获取格式列的数据源
function getSSourceDate(_type_code, _source_type) {
    //localStorage.setItem(i,i);
    // localStorage.getItem(i);
    //localStorage.removeItem(i);
    var local_source = localStorage.getItem(_type_code);
    var _source=null;
    if (local_source == null || local_source == undefined) {
        var source = "common/dict/system";
        if (_source_type == 1) {
            source = "common/dict/business";
        }
        var ajax = new Ajax(source, function (ret) {
            if (ret.code && ret.code < 0) {
                console.log(ret.msg);
                return;
            }
            if (ret.code && ret.code > 0) {
                _source=ret.dataset.rows;
                localStorage.setItem(_type_code,JSON.stringify(_source));
            }
        });
        ajax.setMessage(true);
        ajax.setAsync(false);
        ajax.add("type_code", _type_code);
        ajax.submit();
    } else {
        _source=$.parseJSON(local_source);
    }
    return _source;
}

//清除本地字典缓存
function  _clearConfig() {
    localStorage.clear();
}

