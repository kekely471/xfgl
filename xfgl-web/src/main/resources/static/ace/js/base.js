// 字符验证
jQuery.validator.addMethod("stringCheck", function (value, element) {
    return this.optional(element) || /^[u0391-uFFE5w]+$/.test(value);
}, "只能包括中文字、英文字母、数字和下划线");
// 中文字两个字节
jQuery.validator.addMethod("byteRangeLength", function (value, element, param) {
    var length = value.length;
    for (var i = 0; i < value.length; i++) {
        if (value.charCodeAt(i) > 127) {
            length++;
        }
    }
    return this.optional(element) || (length >= param[0] && length <= param[1]);
}, "请确保输入的值在3-15个字节之间(一个中文字算2个字节)");
// 身份证号码验证
jQuery.validator.addMethod("isIdCardNo", function (value, element) {
    return this.optional(element) || isIdCardNo(value);
}, "请正确输入您的身份证号码");
// 手机号码验证
jQuery.validator.addMethod("isMobile", function (value, element) {
    var length = value.length;
    var mobile = /^(13[0-9]{9})|(18[0-9]{9})|(14[0-9]{9})|(17[0-9]{9})|(15[0-9]{9})$/;
    return this.optional(element) || (length == 11 && mobile.test(value));
}, "请正确填写您的手机号码");
// 电话号码验证
jQuery.validator.addMethod("isTel", function (value, element) {
    var tel = /^(\d{3,4}-?)?\d{7,9}$/g; //电话号码格式010-12345678
    return this.optional(element) || (tel.test(value));
}, "请正确填写您的电话号码");
// 联系电话(手机/电话皆可)验证
jQuery.validator.addMethod("isPhone", function (value, element) {
    var length = value.length;
    var mobile = /^(13[0-9]{9})|(18[0-9]{9})|(14[0-9]{9})|(17[0-9]{9})|(15[0-9]{9})$/;
    var tel = /^d{3,4}-?d{7,9}$/;
    return this.optional(element) || (tel.test(value) || mobile.test(value));
}, "请正确填写您的联系电话");
// 邮政编码验证
jQuery.validator.addMethod("isZipCode", function (value, element) {
    var tel = /^[0-9]{6}$/;
    return this.optional(element) || (tel.test(value));
}, "请正确填写您的邮政编码");

function isIdCardNo(num) {
    var factorArr = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1);
    var parityBit = new Array("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2");
    var varArray = new Array();
    var intValue;
    var lngProduct = 0;
    var intCheckDigit;
    var intStrLen = num.length;
    var idNumber = num;
// initialize
    if ((intStrLen != 15) && (intStrLen != 18)) {
        return false;
    }
// check and set value
    for (i = 0; i < intStrLen; i++) {
        varArray[i] = idNumber.charAt(i);
        if ((varArray[i] < '0' || varArray[i] > '9') &&
            (i != 17)
        ) {
            return false;
        }
        else if (i < 17) {
            varArray[i] = varArray[i] * factorArr[i];
        }
    }
    if (intStrLen == 18) {
//check date
        var date8 = idNumber.substring(6, 14);
        if (isDate8(date8) == false) {
            return false;
        }
// calculate the sum of the products
        for (i = 0; i < 17; i++) {
            lngProduct = lngProduct + varArray[i];
        }
// calculate the check digit
        intCheckDigit = parityBit[lngProduct % 11];
// check last digit
        if (varArray[17] != intCheckDigit) {
            return false;
        }
    }
    else { //length is 15
//check date
        var date6 = idNumber.substring(6, 12);
        if (isDate6(date6) == false) {
            return false;
        }
    }
    return true;
}

function isDate6(sDate) {
    if (!/^[0-9]{6}$/.test(sDate)) {
        return false;
    }
    var year, month, day;
    year = sDate.substring(0, 4);
    month = sDate.substring(4, 6);
    if (year < 1700 || year > 2500) return false
    if (month < 1 || month > 12) return false
    return true
}

function isDate8(sDate) {
    if (!/^[0-9]{8}$/.test(sDate)) {
        return false;
    }
    var year, month, day;
    year = sDate.substring(0, 4);
    month = sDate.substring(4, 6);
    day = sDate.substring(6, 8);
    var iaMonthDays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
    if (year < 1700 || year > 2500) return false
    if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) iaMonthDays[1] = 29;
    if (month < 1 || month > 12) return false
    if (day < 1 || day > iaMonthDays[month - 1]) return false
    return true
}

// 身份证号码验证
jQuery.validator.addMethod("idcardno", function (value, element) {
    return this.optional(element) || isIdCardNo(value);
}, "请正确输入身份证号码");
//字母数字
jQuery.validator.addMethod("alnum", function (value, element) {
    return this.optional(element) || /^[a-zA-Z0-9]+$/.test(value);
}, "只能包括英文字母和数字");
// 邮政编码验证
jQuery.validator.addMethod("zipcode", function (value, element) {
    var tel = /^[0-9]{6}$/;
    return this.optional(element) || (tel.test(value));
}, "请正确填写邮政编码");
// 汉字
jQuery.validator.addMethod("chcharacter", function (value, element) {
    var tel = /^[u4e00-u9fa5]+$/;
    return this.optional(element) || (tel.test(value));
}, "请输入汉字");
// 字符最小长度验证（一个中文字符长度为2）
jQuery.validator.addMethod("stringMinLength", function (value, element, param) {
    var length = value.length;
    for (var i = 0; i < value.length; i++) {
        if (value.charCodeAt(i) > 127) {
            length++;
        }
    }
    return this.optional(element) || (length >= param);
}, $.validator.format("长度不能小于{0}!"));
// 字符最大长度验证（一个中文字符长度为2）
jQuery.validator.addMethod("stringMaxLength", function (value, element, param) {
    var length = value.length;
    for (var i = 0; i < value.length; i++) {
        if (value.charCodeAt(i) > 127) {
            length++;
        }
    }
    return this.optional(element) || (length <= param);
}, $.validator.format("长度不能大于{0}!"));
// 字符验证
jQuery.validator.addMethod("string", function (value, element) {
    return this.optional(element) || /^[u0391-uFFE5w]+$/.test(value);
}, "不允许包含特殊符号!");
// 手机号码验证
jQuery.validator.addMethod("mobile", function (value, element) {
    var length = value.length;
    return this.optional(element) || (length == 11 && /^(((13[0-9]{1})|(15[0-9]{1}))+d{8})$/.test(value));
}, "手机号码格式错误!");
// 电话号码验证
jQuery.validator.addMethod("phone", function (value, element) {
    var tel = /^(d{3,4}-?)?d{7,9}$/g;
    return this.optional(element) || (tel.test(value));
}, "电话号码格式错误!");
// 邮政编码验证
jQuery.validator.addMethod("zipCode", function (value, element) {
    var tel = /^[0-9]{6}$/;
    return this.optional(element) || (tel.test(value));
}, "邮政编码格式错误!");
// 必须以特定字符串开头验证
jQuery.validator.addMethod("begin", function (value, element, param) {
    var begin = new RegExp("^" + param);
    return this.optional(element) || (begin.test(value));
}, $.validator.format("必须以 {0} 开头!"));
// 验证两次输入值是否不相同
jQuery.validator.addMethod("notEqualTo", function (value, element, param) {
    return value != $(param).val();
}, $.validator.format("两次输入不能相同!"));
// 验证值不允许与特定值等于
jQuery.validator.addMethod("notEqual", function (value, element, param) {
    return value != param;
}, $.validator.format("输入值不允许为{0}!"));
// 验证值必须大于特定值(不能等于)
jQuery.validator.addMethod("gt", function (value, element, param) {
    return value > param;
}, $.validator.format("输入值必须大于{0}!"));

//多选框初始化赋值
function chose_mult_set_ini(select, values) {
    if (values == "" || values == null) {
        return;
    }
    var arr = values.split(',');
    var length = arr.length;
    var value = '';
    for (i = 0; i < length; i++) {
        value = arr[i];
        $(select + " option[value='" + value + "']").attr('selected', 'selected');
    }
    $(select).trigger("liszt:updated");
}


//一组用于文件上传的
function initFileUploader(file_selector, file_container, fileids_selector) {
    var file_uploader = new plupload.Uploader(
        {
            runtimes: 'html5,flash,silverlight,html4',
            browse_button: file_selector,
            url: Apps.ContextPath + 'file/upload',
            chunk_size: '100mb',
            unique_names: true,
            filters: {
                max_file_size: '500mb'
                // mime_types: [{
                //     title: "Image files",
                //     extensions: "jpg,gif,png,jpeg,zip,rar,doc,docx,txt,xls,xlsx,ppt,pptx"
                // }]
            },
            flash_swf_url: Apps.ContextPath
            + 'assets/swf/Moxie.swf',
            silverlight_xap_url: Apps.ContextPath
            + 'assets/swf/Moxie.xap',
            init: {
                BeforeUpload: function (up, file) {

                },
                UploadProgress: function (up, file) {
                    //进度条
                    $('#bar_' + file.id).css('width', file.percent + "%");
                    $('#spn_' + file.id).text(file.percent + "%");
                },
                FilesAdded: function (up, files) {
                    //文件添加
                    $.each(files, function (i, file) {
                        $('#' + file_container).append(
                            "<li id=\"" + file.id + "\" style=\"height: 40px;\"><div class=\"clearfix\">" +
                            "<a id=\"file_" + file.id + "\" class=\"pull-left\" style=\"max-width: 300px;cursor:hand;\"><i class=\"ace-icon fa fa-file fa-2x icon-only pull-left\"></i>" + file.name + "</a>" +
                            "<span id=\"spn_" + file.id + "\" class=\"pull-right\">0%</span>" +
                            "<a id=\"del_" + file.id + "\" onclick=\"removeFile(this,'" + file.id + "','" + fileids_selector + "')\" class=\"cursor:hand;\" ><span class=\"pull-right\"><i class=\"ace-icon fa fa-trash-o fa-2x icon-only\"></i></span></a></div>" +
                            "<div id=\"bar1_" + file.id + "\" class=\"progress progress-mini progress-striped active\">" +
                            "<div id=\"bar_" + file.id + "\" style=\"width:0%\" class=\"progress-bar progress-bar-success\"></div></div></li>");
                        $("#del_" + file.id).hide();
                    });

                    file_uploader.start();
                },
                FileUploaded: function (up, file, info) {
                    //文件上传完毕
                    var ret = eval("(" + info.response + ")");
                    if (ret.code > 0) {
                        var fileids = $("#" + fileids_selector).val();
                        fileids += ret.ids + ",";
                        $("#" + fileids_selector).val(fileids);
                        $("#" + file.id).attr("file_id", ret.ids);
                        $("#file_" + file.id).attr(
                            "href",
                            Apps.ContextPath
                            + "file/download?id="
                            + ret.ids
                        );
                        $("#file_" + file.id).attr("target", "_blank");
                        $("#del_" + file.id).show();
                        $("#bar1_" + file.id).hide();
                        $("#spn_" + file.id).hide();

                    } else {
                        $.messager.alert("错误", ret.msg, "error");
                    }
                },
                UploadComplete: function (up, files) {

                },
                Error: function (up, args) {
                    bootbox.alert({title: "错误", message: "文件【" + args.file.name + "】上传失败：" + args.message});
                }
            }
        });
    file_uploader.init();
    return file_uploader;
}


function getFileIds(fileids_selector) {
    var fileids = $("#" + fileids_selector).val();
    var str = "";
    str = fileids.substring(0, fileids.length - 1);
    return str;
}

function removeFile(t, file_id, fileids_selector) {
    //var f = (f = file_uploader.getFile(fid)) && file_uploader.removeFile(f);
    $(t).parent().parent().remove();//删除节点
    var fileids = $("#" + fileids_selector).val().toString();
    fileids = fileids.replace(new RegExp(file_id + ",", "gm"), "");
    $("#" + fileids_selector).val(fileids);
}

function initFileContainer(file_container, fileids_selector, id, tableName, keyName) {
    var ajax = new Ajax("file/list", function (data) {
        if (data) {
            var rows = data.dataset.rows;
            for (i = 0; i < rows.length; i++) {
                $('#' + file_container).append(
                    "<li id=\"" + rows[i].ID + "\" file_id=\"" + rows[i].ID + "\" style=\"height: 40px;\"><div class=\"clearfix\">" +
                    "<a id=\"file_" + rows[i].ID + "\" class=\"pull-left\" style=\"max-width: 300px;cursor:hand;\" href=\"" + Apps.ContextPath + "file/download?id=" + rows[i].ID + "\" target=\"_blank\" ><i class=\"ace-icon fa fa-file fa-2x icon-only pull-left\"></i>" + rows[i].FILE_NAME + "</a>" +
                    "<span id=\"spn_" + rows[i].ID + "\" class=\"pull-right\">0%</span>" +
                    "<a id=\"del_" + rows[i].ID + "\" onclick=\"removeFile(this,'" + rows[i].ID + "','" + fileids_selector + "')\" class=\"cursor:hand;\" href=\"#\" ><span class=\"pull-right\"><i class=\"ace-icon fa fa-trash-o fa-2x icon-only\"></i></span></a></div>" +
                    "<div id=\"bar1_" + rows[i].ID + "\" class=\"progress progress-mini progress-striped active\">" +
                    "<div id=\"bar_" + rows[i].ID + "\" style=\"width:0%\" class=\"progress-bar progress-bar-success\"></div></div></li>");
                $("#bar1_" + rows[i].ID).hide();
                $("#spn_" + rows[i].ID).hide();
                var fileids = $("#" + fileids_selector).val();
                fileids += rows[i].ID + ",";
                $("#" + fileids_selector).val(fileids);
            }
        }
    });
    ajax.add("id", id);
    ajax.add("tableName", tableName);
    ajax.add("keyName", keyName);
    ajax.submit();
}

function resetFileContainer(file_container,fileids_selector) {
    $("#" + fileids_selector).val("");
    $("#" + file_container).empty();
}

function initFileContainer2(file_container, id, tableName, keyName) {
    var ajax = new Ajax("file/list", function (data) {
        if (data) {
            var rows = data.dataset.rows;
            for (i = 0; i < rows.length; i++) {
                $('#' + file_container).append(
                    "<li><a href=\"" + Apps.ContextPath + "file/download?id=" + rows[i].ID + "&servicetype=business\" target=\"_blank\" class=\"attached-file\"><i class=\"ace-icon fa fa-file-o bigger-110\"></i><span class=\"attached-name\">" + rows[i].FILE_NAME + "</span></a></li>");
            }
        }
    });
    ajax.add("id", id);
    ajax.add("tableName", tableName);
    ajax.add("keyName", keyName);
    ajax.submit();
}

function initHTMLEdit(content_selector) {
    var _editor;
    //富文本编辑器
    KindEditor.options.filterMode = false;//是否开启过滤机制
    _editor = KindEditor.create(content_selector, {
        uploadJson: Apps.ContextPath + 'file/upload',
        items: ['fontname', 'fontsize', '|', 'forecolor', 'bold', 'italic', 'underline', '|', 'justifyleft', 'justifycenter', 'justifyright', 'quickformat', '|', 'image', 'link'],
        afterCreate: function () {
            this.sync();
        },
        afterBlur: function () {
            this.sync();
        },
        afterChange: function () {
            this.sync();
        },
        minHeight: 180,
        minWidth: 320,
        resizeType: 0
    });
    _editor.sync();
    return _editor;
}

//大页面切换时候的存储方法
//作者：金磊
//时间：2018-08-21
//备注：请邹玲看看呗！～,别看了，失败了!~NND
//htmlload方式加载
var loadContent = function (url, data, navname,menuname,submenuname) {
    console.log(url);
    console.log(data);
    console.log(navname);
    console.log(menuname);
    console.log(submenuname);
    //$.ajaxSetup({cache: false });
    //localStorage.setItem("_oldPage",$('#mainContent')[0].innerHTML);
    $("#mainContent").load(url + " #content ", data, function (result) {
        //alert(result);
        //将被加载页的JavaScript加载到本页执行
        if (result.indexOf("<title>用户登录</title>") > 0) {
            window.location.href = "login";
        } else {
            $result = $(result);
            $result.find("script").appendTo('#mainContent');
            $("#content .modal-header").remove();
            $("#content #closeBtn").remove();
        }
    });
    //初始化导航条,这里只能通过AJAX的方式了！哎！~

    if(menuname!=undefined) {
        $("#menunav").show();
        $("#submenunav").show();
        $("#topnav").show();
        $("#topnav")[0].innerText = navname;
        $("#menunav")[0].innerText = menuname;
        $("#submenunav")[0].innerText = submenuname;
    }else{
        $("#menunav").hide();
        $("#submenunav").hide();
        $("#topnav").hide();
    }

};


// var backContent=function () {
//     var result=localStorage.getItem("_oldPage");
//     $("#mainContent").empty();
//     $("#mainContent").html(result);
// };


//模态窗口方式加载
var loadModal = function (_url,_formId,_data) {
    if(_formId==undefined){
        _formId="#base-form";
    }
    $(_formId).modal({
        backdrop: 'static',
        remote: _url
    }).on("hidden.bs.modal", function () {
        $(this).removeData("bs.modal");
    });
};


var closeModel = function (_formId) {
    if(_formId==undefined){
        _formId="#base-form";
    }
    $(_formId).modal('hide');
}


//一组工作流页面调用的方法

