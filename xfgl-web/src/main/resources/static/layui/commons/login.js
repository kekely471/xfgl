layui.use('layer', function() {
    layer = layui.layer;
})
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
        layer.tips('用户名不能为空', '#username', {tips: [2, '#3595CC']});
        return false;
    }
    if (password == '') {
        layer.tips('密码不能为空', '#password', {tips: [2, '#3595CC']});
        return false;
    }
    var ajax = new Ajax("check", function (ret) {
        if (ret.code && ret.code < 0) { //验证失败
            layer.tips(ret.msg, '#password', {tips: [2, '#3595CC']});
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

