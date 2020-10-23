layui.define(['form'],function(exports){ //提示：模块也可以依赖其它模块，如：layui.define('layer', callback);
    var form = layui.form;
    var obj = {
        verify: function(){
            form.verify({
                phone: [
                    /(^$)|^1\d{10}$/
                    ,'请输入正确的手机号'
                ]
                ,email: [
                    /(^$)|^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/
                    , '邮箱格式不正确'
                ]
                ,url: [
                    /(^$)|(^#)|(^http(s*):\/\/[^\s]+\.[^\s]+)/
                    , '链接格式不正确'
                ]
                ,number: [
                    /(^$)|^\d+$/
                    , '只能填写数字'
                ]
                ,date: [
                    /(^$)|^(\d{4})[-\/](\d{1}|0\d{1}|1[0-2])([-\/](\d{1}|0\d{1}|[1-2][0-9]|3[0-1]))*$/
                    , '日期格式不正确'
                ]
                ,identity: [
                    /(^$)|(^\d{15}$)|(^\d{17}(x|X|\d)$)/
                    , '请输入正确的身份证号'
                ]
                ,telephone: [
                    /(^$)|^\d{3,4}-\d{7,8}$/
                    ,'请输入正确的固定电话，如(xxxx-xxxxxxx)!'
                ]
                ,zipCode: [
                    /(^$)|^[0-9]{6}$/
                    ,'邮编格式有误'
                ]
                ,title: function(value){
                    if(value.length < 5){
                        return '标题不得少于5个字符';
                    }
                }
                ,pass: [
                    /^[\S]{6,12}$/
                    ,'密码必须6到12位，且不能出现空格'
                ]
                ,boolean: function(value){
                    if(typeof(value) != 'boolean'){
                        return '请使用布尔类型';
                    }
                }
            });
        }
    };

    //输出test接口
    exports('verifyE', obj);
});