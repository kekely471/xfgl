//回车自动登陆
$(document).keydown(function (e) {
    if (e.keyCode == 13)
        loginUser();
});

function loginUser() {
    var username = $('#username').val();
    var password = $('#password').val();
    var f=$('#login_form');
    if (username == '') {
        bootbox.alert({title: "警告", message: "用户名不能为空！"});
        return false;
    }
    if (password == '') {
        bootbox.alert({title: "警告", message: "密码不能为空！"});
        return false;
    }
    var ajax = new Ajax("check", function (ret) {
        if (ret.code && ret.code < 0) { //验证失败
            bootbox.alert({title: "警告", message: ret.msg});
            return;
        }
        if (ret.code && ret.code > 0) {
            window.location.href = Apps.ContextPath + "main?_=" + new Date().getTime();
            return;
        }
    });
    ajax.setMessage(false);
    ajax.add("username",username);
    ajax.add("password",password);
    /*if ($("#keepLogin").is(":checked")){
        ajax.add("keepLogin", 1);
    }*/
    ajax.submit();
}

