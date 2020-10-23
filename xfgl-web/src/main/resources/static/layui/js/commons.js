//获取格式列的数据源
function getSSourceDate(_type_code, _source_type) {
    var local_source = localStorage.getItem(_type_code);
    var _source = null;
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
                _source = ret.dataset.rows;
                localStorage.setItem(_type_code, JSON.stringify(_source));
            }
        });
        ajax.setMessage(true);
        ajax.setAsync(false);
        ajax.add("type_code", _type_code);
        ajax.submit();
    } else {
        _source = $.parseJSON(local_source);
    }
    return _source;
}

//row塞值
function init_rows(_source, res) {
    for (var i in _source) {
        if (_source[i].VALUE == res) {
            if (_source[i].EXT1 == "" || _source[i].EXT1 == null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";
            } else {
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

// 清除html标签
function escapeHtml(t) {
    if (t == null) {
        return "";
    } else {
        var reTag = /<(?:.|\s)*?>/g;
        return t.replace(reTag, "");
    }
}

//菜单图标显示
function init_icon(cellvalue) {
    if (cellvalue != "") {
        return "<i class=\"menu-icon " + cellvalue + " \"></i>";
    } else {
        return "";
    }
}

function init_menu_type(_source, cellvalue) {
    for (var i in _source) {
        if (_source[i].VALUE == cellvalue) {
            if (_source[i].EXT1 == "" || _source[i].EXT1 == null) {
                return "<span class='label label-success'>" + _source[i].NAME + "</span>";

            } else {
                return "<span class='" + _source[i].EXT1 + "'>" + _source[i].NAME + "</span>";
            }
            break;
        }
    }
}

function openPage(openType, methodType, param, isReload) {
    var btns = ['保存', '取消'];
    var yes = function (index, layero) {
        layero.find('iframe')[0].contentWindow.mySubmit();
        if (isReload) {
            location.reload();
        }
    }
    if (param.btn) {
        btns = param.btn;
        yes = function (index, layero) {
            layer.close(index);
        }
    }
    if (openType == 1) {
        var index = layer.open({
            type: 2,
            area: param.area,
            title: param.title,
            fixed: false, //不固定
            content: param.content,
            btn: btns,
            yes: yes,
            btn2: function (index, layero) {
            }
        });
    } else {
        top.tab.tabAdd({
            class: "",
            icon: "",
            id: tabIndex + methodType,
            title: param.title,
            url: param.content
        });
    }
}

//初始化下拉框
function initSelectBox(name, url, typename_value, resid, resname) {
    if (typename_value != undefined && typename_value != '' && typename_value != null) {
        typename_value = JSON.parse(typename_value);
    }
    var ajax = new Ajax(url, function (ret) {
        if (ret.code && ret.code < 0) {
            layer.alert(ret.msg, {icon: 1});
            return;
        }
        if (ret.code && ret.code > 0) {
            var html = '';
            for (var i = 0; i < ret.dataset.rows.length; i++) {
                if (resid != undefined && resname != undefined) {
                    html += '<option value="' + ret.dataset.rows[i][resid] + '">' + ret.dataset.rows[i][resname] + '</option>';
                } else {
                    html += '<option value="' + ret.dataset.rows[i].ID + '">' + ret.dataset.rows[i].NAME + '</option>';
                }
            }
            $("select[name='" + name + "']").append(html);
            $("select[name='" + name + "']").val($("select[name='" + name + "']").attr("selectValue"));
            layui.form.render('select');
        }
    });
    ajax.setMessage(false);
    ajax.setAsync(false);
    for (var i in typename_value) {
        ajax.add(i, typename_value[i]);
    }

    ajax.submit();
}

//刪除
function rowsDel(url, check_data, msg) {
    if (check_data.length == 0) {
        layer.msg("请选择最少一行数据！", function () {
        });
        return;
    }
    layer.confirm(msg, function (index) {
        var dataes = check_data;
        var ids = "";
        for (j = 0; j < dataes.length; j++) {
            var id = dataes[j].ID;
            ids = id + "+" + ids
        }
        ids = ids.substring(0, ids.length - 1);
        var ajax = new Ajax(url, function (data) {
            if (judgeRight(data)) {
                layer.close(index);
                layer.alert('删除成功！', {icon: 1});
                tableIns.reload({where: {"v": Date.parse(new Date())}});
                if (typeof initTree === "function") { //是函数    其中 initTree 为函数名称
                    initTree();
                }
            }
        })
        ajax.add("ids", ids);
        ajax.submit();
    })
}

//处理ajax返回是数据异常情况
function judgeRight(data) {
    if (data.code == -98) {
        if (window.location.href == top.window.location.href) {
            window.location.href = Apps.ContextPath + "logout";
        } else {
            window.location.href = Apps.ContextPath + "logout";
        }
    } else if (data.code > 0) {
        return true;
    } else {
        layer.alert(data.msg, {icon: 0});
        return false;
    }
}

//初始化上传uploadid：上传按钮id,wz:存放ul id,whether:true，false,是否多选，upload：upload控件,accept:不传默认图片，存储数据input的ID
function initupload(uploadid, wz, whether, upload, accept, inputId) {
    var accept = accept == undefined ? "" : accept;
    var uploadListIns = upload.render({
        elem: '#' + uploadid //绑定元素
        , url: Apps.ContextPath + 'file/upload/' //上传接口
        , multiple: whether
        , accept: accept
        , acceptMime: accept + '/*'
        , done: function (res) {
            if (res.code > 0) {
                var uploadHtml;
                if (accept == '') {
                    uploadHtml = "<li class='li_images' id='" + res.ids + "'>" +
                        "<img src='" + Apps.ContextPath + "file/download?id=" + res.ids + "' class='layui-upload-img'>" +
                        "<div class='del pic_del'></div>" +
                        "</li>";
                } else {
                    uploadHtml = "<li class='li_file' id='" + res.ids + "'>" +
                        "<span><a class='file_name' href='" + Apps.ContextPath + "file/download?id=" + res.ids + "'>" + res.dataset.rows["0"].name + "." + res.dataset.rows["0"].ext + "</a></span>" +
                        "<span class='del wj_del'>删除</span>" +
                        "</li>";
                }
                if (whether) {
                    $('#' + wz).append(uploadHtml);
                    if ($('#' + inputId).val()) {
                        $('#' + inputId).val($('#' + inputId).val() + "," + res.ids);
                    } else {
                        $('#' + inputId).val(res.ids);
                    }
                } else {
                    $('#' + wz).html(uploadHtml);
                    $('#' + inputId).val(res.ids);
                }
                if (accept == '') {
                    initPicView(wz);
                }

                $('.del').unbind().on('click', function () {
                    var delete_img = $(this);
                    layer.confirm('确认删除吗？', {
                        btn: ['确定', '取消'], //按钮
                        icon: 0
                    }, function (index) {
                        delete_img.parent().remove();
                        uploadListIns.config.elem.siblings("input").val(""); //清空 input file 值，以免删除后出现同名文件不可选
                        var liNum = 1;
                        $('#' + wz + " li").each(function () {
                            if (liNum == 1) {
                                $('#' + inputId).val($(this).attr("id"));
                            } else {
                                $('#' + inputId).val($('#' + inputId).val() + "," + $(this).attr("id"));
                            }
                            liNum++;
                        })
                        if (accept == '') {
                            initPicView(wz);
                        }
                        layer.close(index);
                    }, function () {

                    });
                });
                /*$("#files").href="${ctx.contextPath}/file/download?id="+res.ids;*/

            } else {

                alert(res.msg);
            }

            //上传完毕回调

        }
        , error: function () {
            //请求异常回调
        }
    });
}

//初始化上传uploadid：上传按钮id,wz:存放ul id,whether:true，false,是否多选，upload：upload控件,accept:不传默认图片，存储数据input的ID
function inituploadsyinfo(uploadid, wz, whether, upload, accept, inputId) {
    var accept = accept == undefined ? "" : accept;
    var uploadListIns = upload.render({
        elem: '#' + uploadid //绑定元素
        , url: Apps.ContextPath + 'file/uploadSYInfo/' //上传接口
        , multiple: whether
        , accept: accept
        , acceptMime: accept + '/*'
        , done: function (res) {
            if (res.code > 0) {
                var uploadHtml;
                if (accept == '') {
                    uploadHtml = "<li class='li_images' id='" + res.ids + "'>" +
                        "<img src='" + Apps.ContextPath + "file/download?id=" + res.ids + "' class='layui-upload-img'>" +
                        "<div class='del pic_del'></div>" +
                        "</li>";
                } else {
                    uploadHtml = "<li class='li_file' id='" + res.ids + "'>" +
                        "<span><a class='file_name' href='" + Apps.ContextPath + "file/downloadsyinfo?id=" + res.ids + "'>" + res.dataset.rows["0"].name + "." + res.dataset.rows["0"].ext + "</a></span>" +
                        "<span class='del wj_del'>删除</span>" +
                        "</li>";
                }
                if (whether) {
                    $('#' + wz).append(uploadHtml);
                    if ($('#' + inputId).val()) {
                        $('#' + inputId).val($('#' + inputId).val() + "," + res.ids);
                    } else {
                        $('#' + inputId).val(res.ids);
                    }
                } else {
                    $('#' + wz).html(uploadHtml);
                    $('#' + inputId).val(res.ids);
                }
                if (accept == '') {
                    initPicView(wz);
                }
                $('.del').unbind().on('click', function () {
                    var delete_img = $(this);
                    layer.confirm('确认删除吗？', {
                        btn: ['确定', '取消'], //按钮
                        icon: 0
                    }, function (index) {
                        delete_img.parent().remove();
                        uploadListIns.config.elem.siblings("input").val(""); //清空 input file 值，以免删除后出现同名文件不可选
                        var liNum = 1;
                        $('#' + wz + " li").each(function () {
                            if (liNum == 1) {
                                $('#' + inputId).val($(this).attr("id"));
                            } else {
                                $('#' + inputId).val($('#' + inputId).val() + "," + $(this).attr("id"));
                            }
                            liNum++;
                        })
                        if (accept == '') {
                            initPicView(wz);
                        }
                        layer.close(index);
                    }, function () {

                    });
                });
                /*$("#files").href="${ctx.contextPath}/file/download?id="+res.ids;*/

            } else {

                alert(res.msg);
            }

            //上传完毕回调

        }
        , error: function () {
            //请求异常回调
        }
    });
}

function initloadpic(Arr, view, accept, inputId) {
    if (Arr != '') {
        var wjArr = Arr.split(',');
        $(wjArr).each(function (index, item) {
            var picId = Apps.ContextPath + "action/file/download?id=" + item;
            if (accept == '') {
                $('#' + view).append("<li class='li_images' id='" + item + "'>" +
                    "<img src='" + Apps.ContextPath + "file/download?id=" + item + "' layer-src='" + Apps.ContextPath + "file/download?id=" + item + "' class='layui-upload-img'>" +
                    "<div class='del pic_del'></div>" +
                    "</li>");
            } else {
                var ajax = new Ajax('file/getFileDetail', function (ret) {
                    $('#' + view).append("<li class='li_file' id='" + item + "'>" +
                        "<span><a class='file_name' href='" + Apps.ContextPath + "file/download?id=" + item + "'>" + ret.FILE_NAME + "</a></span>" +
                        "<span class='del wj_del'>删除</span>" +
                        "</li>");
                });
                ajax.add("id", item);
                ajax.setAsync(false);
                ajax.submit();
            }
        });
        if (accept == '') {
            initPicView(view);
        }
        $('.del').unbind().on('click', function () {
            var delete_img = $(this);
            layer.confirm('确认删除吗？', {
                btn: ['确定', '取消'], //按钮
                icon: 0
            }, function (index) {
                delete_img.parent().remove();
                $('#' + view).siblings("input").val("");
                var liNum = 1;
                $('#' + view + " li").each(function () {
                    if (liNum == 1) {
                        $('#' + inputId).val($(this).attr("id"));
                    } else {
                        $('#' + inputId).val($('#' + inputId).val() + "," + $(this).attr("id"));
                    }
                    liNum++;
                })
                if (accept == '') {
                    initPicView(view);
                }
                layer.close(index);
            }, function () {

            });
        });
    }
}

var openView;
var viewerFlag = false;

function initPicView(el) {
    if (viewerFlag) {
        openView.destroy();
    }
    openView = new Viewer(document.getElementById(el), {
        show: function () {
            openView.update();
        }
    });
    viewerFlag = true;
}

/**switch开关启用  selector是标签id status状态值**/
function isUse(selector, status) {
    if (status == 1) {
        $("#" + selector).attr('checked', 'checked');
    } else {
        $("#" + selector).removeAttr("checked");
    }
}


function closeTabAndRefreshPreTab(currentTabIndex, preTabIndex) {
    var n = top.$(".layui-tab-content").children("div[lay-item-id=" + preTabIndex + "]").children("iframe");
    // n.attr("src", n.attr("src"));

    var preTab = n[0].contentWindow.tableIns;//刷新页面后搜索参数带回
    preTab.reload({});

    top.tab.tabChange(preTabIndex);
    top.tab.tabDelete(currentTabIndex);
}

function closeTabAndRefreshPreTable(currentTabIndex){
    var  preTabIndex = currentTabIndex.replace(/[^\d]/g, '')
    var n = top.$(".layui-tab-content").children("div[lay-item-id="+preTabIndex+"]").children("iframe");
    n.attr("src", n.attr("src"));
    top.tab.tabChange(preTabIndex);
    top.tab.tabDelete(currentTabIndex);
}

function initBtns(toolclass, tableId, openType, ajaxPath) {
    var tabIndex = top.$(".layui-tab-title li.layui-this").attr("lay-id");
    var obj = $(".layui-btn-group");
    if (toolclass != null && toolclass != undefined) {
        obj = $("." + toolclass);
    }
    var ajax = new Ajax("menu/loadOperations", function (ret) {
        if (ret.code && ret.code < 0) {
            console.log(ret.msg);
            return;
        }
        if (ret.code && ret.code > 0) {
            var list = ret.dataset.rows;
            $.each(list, function (i, val) {
                if (i < 3 || (i == 3 && list.length == 4)) {
                    if (tableId) {
                        obj.append("<button class='layui-btn layui-btn-sm'  id=\"" + list[i].URL + "\" onclick=\"buttonEvents('" + list[i].URL + "','" + tableId + "','" + openType + "','" + ajaxPath + "')\"><i class=\"layui-icon " + list[i].ICON + "\"></i>" + list[i].MENUNAME + "</button>");
                    } else {
                        var url = list[i].URL;
                        var icon = list[i].ICON;
                        if (url == 'del'|| icon=='layui-icon layui-icon-delete') {
                            obj.append("<button class='layui-btn layui-btn-sm' style='background-color: red'  id=\"" + url + "\" onclick=\"buttonEvents('" + url + "')\"><i class=\"layui-icon  " + list[i].ICON + "\"></i>" + list[i].MENUNAME + "</button>");
                        } else {
                            obj.append("<button class='layui-btn layui-btn-sm'  id=\"" + url + "\" onclick=\"buttonEvents('" + url + "')\"><i class=\"layui-icon  " + list[i].ICON + "\"></i>" + list[i].MENUNAME + "</button>");
                        }
                    }
                } else if (i == 3 && list.length > 4) {
                    obj.append('<div class="extList-more-btn-group"><button class="layui-btn layui-btn-sm" onclick="moreBtnList(this)"><i class="layui-icon layui-icon-down"></i>更多</button><dl class="extList-more-btn-list" id="moreBtnList"></dl></div>');
                    if (tableId) {
                        $("#moreBtnList").append("<dd><a href=\"javascript:void(0)\" onclick=\"buttonEvents('" + list[i].URL + "','" + tableId + "','" + openType + "','" + ajaxPath + "')\">" + list[i].MENUNAME + "</a></dd>");
                    } else {
                        $("#moreBtnList").append("<dd><a href=\"javascript:void(0)\" onclick=\"buttonEvents('" + list[i].URL + "')\">" + list[i].MENUNAME + "</a></dd>");
                    }
                } else {
                    if (tableId) {
                        $("#moreBtnList").append("<dd><a href=\"javascript:void(0)\" onclick=\"buttonEvents('" + list[i].URL + "','" + tableId + "','" + openType + "','" + ajaxPath + "')\">" + list[i].MENUNAME + "</a></dd>");
                    } else {
                        $("#moreBtnList").append("<dd><a href=\"javascript:void(0)\" onclick=\"buttonEvents('" + list[i].URL + "')\">" + list[i].MENUNAME + "</a></dd>");
                    }
                }
            });
        }
    });
    ajax.setMessage(true);
    ajax.setAsync(false);
    ajax.add("pid", tabIndex);
    ajax.submit();
}

/**
 * 按钮公共方法
 * @param name
 * @returns {*}
 */
function buttonEvents(type, tableId, openType, ajaxPath) {
    var check = table.checkStatus(tableId);
    var param = {
        type: 2,
        area: ['500px', '80%'],
        title: '角色管理表单',
        fixed: false, //不固定
        content: Apps.ContextPath + ajaxPath + "/add/"
    };
    if (type == "add") {
        openPage(openType, type, param);
    } else if (check.data.length != 1 && (type == 'edit' || type == 'set' || type == 'set_account')) {
        layer.msg("请选择一行数据！", function () {
        });
    } else if (type == "edit") {
        param.content = Apps.ContextPath + ajaxPath + '/add/' + check.data[0].ID;
        openPage(openType, type, param);
    } else if (type == 'set') {
        param.title = "权限设置";
        param.content = Apps.ContextPath + ajaxPath + '/set/' + check.data[0].ID;
        openPage(openType, type, param);
    } else if (type == 'set_account') {
        param.area = ['800px', '80%'];
        param.title = "账号设置";
        param.content = Apps.ContextPath + ajaxPath + '/setUser/' + check.data[0].ID;
        openPage(openType, type, param, true);
    } else if (type == "del") {
        rowsDel(ajaxPath + "/del", check.data, "你确定删除所选数据吗?");//刪除事件
    } else if (type == 'realDel') {
        rowsDel(ajaxPath + "/realDel", check.data, "彻底删除数据后无法恢复，确定要彻底删除所选数据?");
    }
}

function getParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}

/**
 * 重制列表 事件集合
 * 1.更多按钮
 * 2.高级搜索
 */
function moreBtnList(obj) {
    var _btnBox = $(obj).next("#moreBtnList");
    if (_btnBox.hasClass("layui-show")) {
        _btnBox.removeClass("layui-show");
    } else {
        _btnBox.addClass("layui-show");
    }
}

function moreSearchList(obj) {
    var _searchBox = $("#extListMoreSearch");
    var pHeight = $(window).height();
    if (_searchBox.hasClass("layui-show")) {
        _searchBox.removeClass("layui-show");
        $(obj).children('i').removeClass("icon-double-up");
        $(obj).children('i').addClass("icon-double-down");
        $(obj).removeClass("extList-more-search-btn");
        pHeight = $(window).height() - 30 - 34;
    } else {
        _searchBox.addClass("layui-show");
        $(obj).children('i').removeClass("icon-double-down");
        $(obj).children('i').addClass("icon-double-up");
        $(obj).addClass("extList-more-search-btn");
        pHeight = $(window).height() - 214;
    }
    table.reload('tab_list', {
        height: pHeight
    });
}


/**
 * 回车键绑定事件，事件调用之后禁用按钮（开放延时启用）
 * 更改回车绑定状态
 */
(function (enterKey) {
    var enterState = true;
    var enterClickState = true;

    // callBack：回调事件、closeMsg：再次回车是否关闭layer提示框（若没有提示则为否）、ime：开启延时
    enterKey.bindEnterKey = function (callBack, closeMsg, time) {
        if (typeof (eval(callBack)) != "function") {
            console.error(callBack + ' hint：方法不存在');
            return false;
        }
        document.onkeyup = function (e) {
            e = e ? e : window.event;
            var code = e.charCode || e.keyCode;
            if (code == 13) {
                if (enterState) {
                    enterState = false;
                    if (time && !isNaN(time)) {
                        setTimeout(function () {
                            enterState = true;
                        }, time);
                    }
                    callBack();
                } else {
                    if (closeMsg && layer) {
                        enterState = true;
                        layer.close(layer.index);
                    }
                }
            }
        }
    }
    // state：绑定状态
    enterKey.enterState = function (state) {
        enterState = state;
    }

    // 点击锁定按钮
    enterKey.lockBtn = function (callBack, closeMsg, time) {
        if (typeof (eval(callBack)) != "function") {
            console.error(callBack + ' hint：方法不存在');
            return false;
        }

        if (enterClickState) {
            enterClickState = false;
            if (time && !isNaN(time)) {
                setTimeout(function () {
                    enterClickState = true;
                }, time);
            }
            callBack();
        } else {
            if (closeMsg && layer) {
                enterClickState = true;
                layer.close(layer.index);
            }
        }
    }

}(window.commons = {}))

function refreshPage(){
    window.location.reload();
}