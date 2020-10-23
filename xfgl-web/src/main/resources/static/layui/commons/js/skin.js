/**
 * 皮肤切换
 */
(function(skinColour){
    // 把引入皮肤css路径<link>标签选出来
    var cssStyle = document.getElementById('skinColour');

    // 换肤函数
    skinColour.changeCss = function(name) {
        event.stopPropagation();
        cssStyle.href = Apps.ContextPath + "layui/commons/css/layui-" + name + ".css";
        //保存肤色名
        skinColour.setStorage("skinName", name);
    }

    // html5设置本地存储
    skinColour.setStorage = function(sname, vul) {
        window.localStorage.setItem(sname, vul);
    }
    skinColour.getStorage = function(attr) {
        var str = window.localStorage.getItem(attr);
        return str;
    }
    // 访问本地存储，获取皮肤名
    var cssName = skinColour.getStorage("skinName");

    // 判断是否有皮肤名，就使用获取的皮肤名，没有就用默认的
    if (cssName && cssName != null) {
        cssStyle.href = Apps.ContextPath + "layui/commons/css/layui-" + cssName + ".css";
    }else{
        // 没有皮肤就使用blue默认的路径
        cssStyle.href = Apps.ContextPath + "layui/commons/css/layui-blue.css";
    }
}(window.skin = {}))