/**
 * 通用表单
 **/
;layui.define(["jquery", "element", "form", "laydate", "layer","upload"], function (exports) {
    var $ = layui.jquery, form = layui.form, laydate = layui.laydate, layer = layui.layer,upload = layui.upload;
    var MOD_NAME = 'currentF';
    var obj = function (config) {
        // 初始化设置参数
        this.config = {
            id: "",
            // 数据
            data: []
        }
        this.config = $.extend(this.config, config);
        this.createModular = function () {
            var o = this, c = o.config, d = c.data;
            for (var index in d) {
                var i = d[index];
                $E = $("#" + c.id);                           // 表单项渲染位置
                var i_t = i.COMPONENT_TYPE;                  // 表单项类型
                var i_a = i.COMPONENT_URL_ATTR;              // 表单项接口传值
                if (i_a) i_a = i_a.split(",");
                try {
                    var i_d = o.getData(i.COMPONENT_URL, i_a);    // 表单项数据
                    if (i_d === false) {
                        console.error(MOD_NAME + ' hint：缺少数据');
                        continue;
                    }
                }catch (e) {
                    console.error(MOD_NAME + ' hint：' + e);
                }
                // 默认值
                var default_value = "";
                if(i.DEFAULT_VALUE){
                    default_value = i.DEFAULT_VALUE;
                }
                if(i.EXTEND_VALUE){
                    default_value = i.EXTEND_VALUE;
                }

                var html = '';
                var redLabel = '';
                html +=	'<div class="layui-form-item layui-td-'+i.COMPONENT_LAYOUT+'">';
                if (i.VALIDATE_RULES) {
                    if (i.VALIDATE_RULES.indexOf("required") > -1) {
                        redLabel = '<span class="redLabel">*</span>';
                    }
                }
                html += '<label class="layui-form-label">' + redLabel + i.LABLE_NAME + '</label>';
                html += '<div class="layui-input-block" id="input' + i.ID + '">';
                // 根据表单项类型添加
                switch (i_t) {
                    case 'text':
                        html += '<input type="text" class="layui-input" name="' + i.FIELD_NAME + '" placeholder="请输入' + i.LABLE_NAME + '" maxlength="' + i.MAX_LENGTH + '" minlength="' + i.MIN_LENGTH + '" value="' + default_value + '" lay-verify="' + i.VALIDATE_RULES + '">';
                        break;
                    case 'select':
                        html += '<select name="' + i.FIELD_NAME + '" lay-verify="' + i.VALIDATE_RULES + '" lay-filter="' + i.FIELD_NAME + '" selectValue="' + default_value + '"><option value="">请选择' + i.LABLE_NAME + '</option></select>';
                        break;
                    case 'radio':
                        if(i_d){
                            for(var l in i_d){
                                var radioChecked = "";
                                if(i.SELECTCOMPONENTVALUE!=undefined && i.SELECTCOMPONENTNAME !=undefined){
                                    if(i_d[l][i.SELECTCOMPONENTVALUE] == default_value){
                                        radioChecked = "checked";
                                    }
                                    html += '<input type="radio" name="' + i.FIELD_NAME + '" value="'+i_d[l][i.SELECTCOMPONENTVALUE]+'" '+radioChecked+' title="'+i_d[l][i.SELECTCOMPONENTNAME]+'">';
                                }else{
                                    if(i_d[l].VALUE == default_value){
                                        radioChecked = "checked";
                                    }
                                    html += '<input type="radio" name="' + i.FIELD_NAME + '" value="'+i_d[l].VALUE+'" '+radioChecked+' title="'+i_d[l].NAME+'">';
                                }
                            }
                            break;
                        }else {
                            continue;
                        }
                    case 'date':
                        html += '<input type="text" class="layui-input" value="'+default_value+'" name="' + i.FIELD_NAME + '" id="' + i.FIELD_NAME + '">';
                        break;
                    case 'textarea':
                        html += '<textarea name="' + i.FIELD_NAME + '" lay-verify="' + i.VALIDATE_RULES + '" placeholder="请输入" maxlength="' + i.MAX_LENGTH + '" class="layui-textarea">'+default_value+'</textarea>';
                        break;
                    case 'checkbox':
                        if(i_d){
                            var defaultValues = default_value.split(",");
                            for(var l in i_d){
                                var checkChecked = "";
                                if(i.SELECTCOMPONENTVALUE!=undefined && i.SELECTCOMPONENTNAME !=undefined){
                                    for(var k in defaultValues){
                                        if(i_d[l][i.SELECTCOMPONENTVALUE] == defaultValues[k]){
                                            checkChecked = "checked";
                                            break;
                                        }
                                    }
                                    html += '<input type="checkbox" name="'+i.FIELD_NAME+'_'+l+'" value="'+i_d[l][i.SELECTCOMPONENTVALUE]+'" '+checkChecked+' title="'+i_d[l][i.SELECTCOMPONENTNAME]+'">';
                                }else{
                                    for(var k in defaultValues){
                                        if(i_d[l].VALUE == defaultValues[k]){
                                            checkChecked = "checked";
                                            break;
                                        }
                                    }
                                    html += '<input type="checkbox" name="'+i.FIELD_NAME+'_'+l+'" value="'+i_d[l].VALUE+'" '+checkChecked+' title="'+i_d[l].NAME+'">';
                                }
                            }
                            break;
                        }else {
                            continue;
                        }
                    case 'upload':
                        var uploadLabel = "上传文件";
                        if(i.ATTACHMENTTYPE == "images" || i.ATTACHMENTTYPE == null){
                            uploadLabel = "上传图片";
                        }
                        if(i.ATTACHMENTTYPE == "video"){
                            uploadLabel = "上传视频";
                        }
                        if(i.ATTACHMENTTYPE == "audio"){
                            uploadLabel = "上传音频";
                        }
                        html += '<button type="button" class="layui-btn" id="'+i.FIELD_NAME+'_currentBtn"><i class="layui-icon">&#xe67c;</i>'+uploadLabel+'</button>';
                        html += '<input type="hidden" class="layui-input" value="'+default_value+'" name="'+i.FIELD_NAME+'" id="'+i.FIELD_NAME+'">';
                        html += '<ul class="viewer" id="'+i.FIELD_NAME+'_currentBox"></ul>';
                        break;
                    case 'image':
                        html += '<img layer-pid="" width="100%" layer-src="../layui/commons/images/home.png" src="../layui/commons/images/home.png" alt="">';
                        break;
                    case 'selectTree':
                        html += '';
                        break;
                    case 'richText':
                        html += '<textarea id="' + i.FIELD_NAME + '" name="' + i.FIELD_NAME + '" style="width:100%;height:180px;">'+default_value+'</textarea>';
                        break;
                    case 'pullTree':
                        html += '<div id="' + i.FIELD_NAME + '" class="layui-form-select select-tree"></div>';
                        break;
                    case 'table':
                        html += '<input type="text" name="'+ i.FIELD_NAME +'" onclick=\'openTable("'+ i.COMPONENT_URL +'","'+ i.LABLE_NAME +'")\' value="" readonly autocomplete="off" class="layui-input layui_input_search">';
                        break;
                    case 'multipleSelect':
                        html += '<div id="' + i.FIELD_NAME + '" class="xm-select-demo"></div><input type="hidden" name="' + i.FIELD_NAME + '" id="' + i.FIELD_NAME + 'Input" value="' + default_value + '" >';
                        break;
                    default:
                        break;
                }
                html += '</div>';
                html += '</div>';

                if ($E) {
                    $E.append(html);
                    switch (i_t) {
                        case 'select':
                            o.initSelectBox(i.FIELD_NAME, i_d,i.SELECTCOMPONENTVALUE,i.SELECTCOMPONENTNAME);
                            break;
                        case 'date':
                            o.rendderDate(i.FIELD_NAME,i.DATEFORMAT,default_value,i.DATETYPE);
                            break;
                        case 'image':
                            o.rendderImage("input" + i.ID);
                            break;
                        case 'richText':
                            o.rendderEditor(i.FIELD_NAME);
                            break;
                        case 'upload':
                            var accept = i.ATTACHMENTTYPE;
                            var whether = i.ISMULTICHOOSE;
                            if(whether == "true"){
                                whether = true;
                            }else{
                                whether = false;
                            }
                            if(i.ATTACHMENTTYPE == null){
                                accept = "images";
                            }
                            if(accept == "images"){
                                accept = "";
                            }
                            initloadpic(default_value,i.FIELD_NAME+"_currentBox",accept,i.FIELD_NAME);
                            initupload(i.FIELD_NAME+"_currentBtn",i.FIELD_NAME+"_currentBox",whether,upload,accept,i.FIELD_NAME);
                            break;
                        case 'tree':
                            var keyArr= []
                            var valArr = []
                            if (i_a) {
                                for (var index in i_a) {
                                    var _param = i_a[index].split(":");
                                    keyArr.push(_param[0]);
                                    valArr.push(_param[1]);
                                }
                            }
                            var op = o.addObj(keyArr,valArr);
                            $("#input" + i.ID).openTree({
                                idKey: i.IDKEY,
                                pIdKey: i.PIDKEY,
                                nameKey: i.NAMEKEY,
                                title: i.TREETITLE,
                                treeUrl: i.COMPONENT_URL,
                                ajax_par: op,
                                field_name: i.FIELD_NAME,
                                default_nodes: default_value
                            });
                            break;
                        case 'pullTree':
                            var pullTreeNodes = [];
                            if(default_value){
                                pullTreeNodes = default_value.split(",");
                            }
                            for(var j in i_d){
                                for(var h in pullTreeNodes){
                                    if(i_d[j][i.IDKEY] == pullTreeNodes[h]){
                                        i_d[j].checked = true;
                                    }
                                }
                            }
                            initSelectTree(i.FIELD_NAME, true,i_d,{"Y": "ps", "N": "s"},i.IDKEY,i.PIDKEY,i.NAMEKEY);
                            var zTree = $.fn.zTree.getZTreeObj(i.FIELD_NAME + "Tree");
                            assignment(i.FIELD_NAME + "Tree", zTree.getCheckedNodes());
                            break;
                        case 'multipleSelect':
                            o.initXmSelect(i.FIELD_NAME, i_d,i.SELECTCOMPONENTVALUE,i.SELECTCOMPONENTNAME,i.SEARCH_URL,i.ISMULTIPLE);
                            break;
                    }
                    form.render();
                } else {
                    console.error(MOD_NAME + ' hint：未配置渲染位置 ');
                }
            }
            $E.append('<div class="clear"></div>');
        }

        //ajax方式获取候选数据
        this.getData = function (url, params) {
            var d;
            var ajax = new Ajax(url, function (ret) {
                if (judgeRight(ret)) {
                    d = ret.dataset.rows;
                } else {
                    console.error(MOD_NAME + ' hint：候选数据ajax请求错误 ');
                    d = false;
                }
            });
            if (params) {
                for (var index in params) {
                    var param = params[index].split(":");
                    ajax.add(param[0], param[1]);
                }
            }
            ajax.setMessage(false);
            ajax.setAsync(false);
            ajax.submit();
            return d;
        }
        // 普通下拉框
        this.initSelectBox = function (name,d,resId,resName) {
            var selectData = this.formatSelectData(d,resId,resName);
            var html = '';
            for (var i = 0; i < selectData.length; i++) {
                html += '<option value="' + selectData[i].value + '">' + selectData[i].name + '</option>';
            }
            $("select[name='" + name + "']").append(html);
            $("select[name='" + name + "']").val($("select[name='" + name + "']").attr("selectValue"));
            layui.form.render('select');
        }
        // xm-select
        this.initXmSelect = function(name,d,resId,resName,searchUrl,isMultiple){
            var selectData = this.formatSelectData(d,resId,resName);
            var isRadio = true;
            if(isMultiple == "1"){
                isRadio = false;
            }

            if(searchUrl){
                var initXmSelect = xmSelect.render({
                    el: '#'+name,
                    paging: true,
                    radio: isRadio,
                    filterable: true,
                    remoteSearch: true,
                    remoteMethod: function(val, cb, show){
                        if(!val){
                            return cb(selectData);
                        }
                        $.ajax({
                            method: 'get',
                            url: searchUrl,
                            params: {
                                keyword: val,
                            },success:function(response){
                                var res = this.formatSelectData(response.data,resId,resName);
                                cb(res);
                            },error:function(){
                                cb([]);
                            }
                        })
                    },on:function(ret){
                        getSelectVal(ret.arr);
                    }
                })
            }else{
                var initXmSelect = xmSelect.render({
                    el: '#'+name,
                    paging: true,
                    radio: isRadio,
                    filterable: true,
                    data:selectData,
                    on:function(ret){
                        getSelectVal(ret.arr);
                    }
                })
            }
            // 监听下拉框赋值表单元素
            var getSelectVal = function(_arr){
                var selectVal = "";
                for (var index in _arr){
                    selectVal += _arr[index].value + ",";
                }
                $("#"+name + "Input").val(selectVal);
            }

            //获取初始化数据，赋值下拉框
            var loadSelectVal = $("#"+name + "Input").val();
            if(loadSelectVal){
                var _loadArr = loadSelectVal.split(",");
                var loadSelectArr = _loadArr.filter(function (s) {
                    return s && s.trim();
                });
                initXmSelect.setValue(loadSelectArr);
            }
        }

        this.rendderDate = function (elem,dataFormat,value,type) {
            laydate.render({
                elem: '#' + elem,   //指定元素
                format: dataFormat,
                value: value,
                type: type
            });
        }

        this.rendderImage = function (elem) {
            layer.photos({
                photos: '#' + elem
                , anim: 5
            });
        }

        this.rendderEditor = function (FIELD_NAME) {
            var editor = 'textarea[name="'+FIELD_NAME+'"]';
            var kEditor = KindEditor.create(editor,{
                afterBlur: function(){
                    this.sync();
                },
                afterChange : function() {
                    //$('.word_count1').html(this.count()); //字数统计包含HTML代码
                    //$('.word_count2').html(this.count('text'));//字数统计包含纯文本、IMG、EMBED，不包含换行符，IMG和EMBED算一个文字
                    /*if (this.count('text') > 30) {
                        var strValue = this.text();
                        kEditor.text("");
                        kEditor.appendHtml(strValue.substring(0, 30));
                    }*/
                }
            });
        }

        this.addObj = function(keyArr,valArr){
            var data = {};
            for(i = 0;i<keyArr.length;i++){
                var  key = keyArr[i];
                var  val = valArr[i];
                data[key] = val;
            }
            return data;
        }

        // 格式化下拉框数据
        this.formatSelectData = function(data,resId,resName){
            if(!resId){
                resId = "value";
            }
            if(!resName){
                resName = "name";
            }
            var selectData = [];
            for (var i = 0; i < data.length; i++) {
                selectData.push({name: data[i][resName], value: data[i][resId]});
            }
            return selectData;
        }
    }

    obj.prototype.render = function () {
        var o = this, c = o.config;

        if (Object.prototype.toString.call(c.data) != '[object Array]') {
            var data = o.getData(c.data);
            if (data === false) {
                console.error(MOD_NAME + ' hint：缺少数据');
                return false;
            }
            o.config.data = data;
        }
        if(!c.id){
            $(".layui-form:last").attr("id","currentForm");
            o.config.id = "currentForm";
        }
        //创建
        o.createModular();
    }


    // 输出模块
    exports(MOD_NAME, function (config) {
        var _this = new obj(config);
        _this.render();
        return _this;
    });
});

function openTable(url,title){
    var index = layer.open({
        type: 2,
        area: ['700px', '450px'],
        title:title,
        fixed: true, //不固定
        content: Apps.ContextPath + url,
        btn: ['确认'],
        yes: function (index, layero) {
            layer.close(index);
        }
    });
}