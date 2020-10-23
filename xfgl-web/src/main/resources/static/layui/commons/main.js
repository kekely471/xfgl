/**
 * 全局
 */
var Apps = {
	debug : true,
	ContextPath : "/xfgl/",
	IsView : false,

	onreadyFunctions : [],
	onloadFunctions : [],
	resizeFunctions : [],
	unloadFunctions : [],
	mouseDownFunctions : []
};

// 定位初始化
var scripts = document.getElementsByTagName("script");
for ( var i = 0; i < scripts.length; i++) {
	if (/.*js\/main\.js$/g.test(scripts[i].getAttribute("src"))) {
		var jsPath = scripts[i].getAttribute("src").replace(/js\/main\.js$/g, '');
		if (jsPath.indexOf("/") == 0 || jsPath.indexOf("://") > 0) {
			Apps.ContextPath = jsPath;
			break;
		}
		var arr1 = jsPath.split("/");
		var path = window.location.href;
		if (path.indexOf("?") != -1) {
			path = path.substring(0, path.indexOf("?"));
		}
		var arr2 = path.split("/");
		arr2.splice(arr2.length - 1, 1);
		for ( var i = 0; i < arr1.length; i++) {
			if (arr1[i] == "..") {
				arr2.splice(arr2.length - 1, 1);
			}
		}
		Apps.ContextPath = arr2.join('/') + '/';
		break;
	}
}

Apps.importScript = function(url) {
	if (!document.body) {
		document.write('<script type="text/javascript" src="' + Apps.ContextPath + url + '"><\/script>');
	} else {
		Apps.loadScript(Apps.ContextPath + url);
	}
};

Apps.importCSS = function(url) {
	if (!document.body) {
		document.write('<link rel="stylesheet" type="text/css" href="' + Apps.ContextPath + url + '" />');
	} else {
		Apps.loadCSS(Apps.ContextPath + url);
	}
};

Apps.loadScript = function(url) {// 在页面载入后添加script
	var e = document.createElement('SCRIPT');
	e.type = 'text/javascript';
	e.src = url;
	e.defer = true;
	document.getElementsByTagName("HEAD")[0].appendChild(e);
};

Apps.loadCSS = function(url) {// 在页面载入后添加script
	if (isGecko) {
		var e = document.createElement('LINK');
		e.rel = 'stylesheet';
		e.type = 'text/css';
		e.href = url;
		document.getElementsByTagName("HEAD")[0].appendChild(e);
	} else {
		document.createStyleSheet(url);
	}
};

// 页面加载事件
Apps.onLoad = function(f, i) {
	Apps.onloadFunctions.push(f);
};

// Dom加载事件
Apps.onReady = function(f, i) {
	Apps.onreadyFunctions.push(f);
};

// 页面窗口改变大小事件
Apps.onResize = function(f, i) {
	Apps.resizeFunctions.push(f);
};

// 页面销毁事件
Apps.onDestory = function(f) {
	Apps.unloadFunctions.push(f);
};

// 页面鼠标事件
Apps.onMouseDown = function(f) {
	Apps.mouseDownFunctions.push(f);
};

// 禁止右键
document.oncontextmenu = function() {
	return Apps.debug;
};

function banBackSpace(e) {
	var ev = e || window.event;// 获取event对象
	var obj = ev.target || ev.srcElement;// 获取事件源
	var t = obj.type || obj.getAttribute('type');// 获取事件源类型
	// 获取作为判断条件的事件类型
	var vReadOnly = obj.readOnly;
	var vDisabled = obj.disabled;
	// 处理undefined值情况
	vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;
	vDisabled = (vDisabled == undefined) ? true : vDisabled;
	// 当敲Backspace键时，事件源类型为密码或单行、多行文本的，
	// 并且readOnly属性为true或disabled属性为true的，则退格键失效
	var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea") && (vReadOnly == true || vDisabled == true);
	// 当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
	// 判断
	if ( flag1)
		return false;
}
document.onkeypress = banBackSpace;
document.onkeydown = banBackSpace;

if (!window.DisableScriptAutoLoad) {

	Apps.importCSS("layui/commons/layui/css/layui.css");
	Apps.importCSS("layui/commons/css/base.css");
    // Apps.importCSS("layui/commons/css/layui-blue.css");
    Apps.importCSS("layui/commons/css/layui-ext.css");
    Apps.importCSS("layui/commons/ztree/css/index.css");
    Apps.importCSS("layui/commons/ztree/css/metroStyle/metroStyle.css");
    Apps.importCSS("layui/commons/viewer/viewer.css");
    document.write('<link rel="stylesheet" type="text/css" href="' + Apps.ContextPath + 'layui/commons/css/layui-blue.css" id="skinColour" />');
    /**
     * js
     */
    Apps.importScript("layui/commons/layui/layui.js");
    Apps.importScript("layui/commons/js/jquery-1.8.3.min.js");
    Apps.importScript("layui/commons/viewer/viewer.js");
    Apps.importScript("layui/commons/viewer/openViewer.js");
    Apps.importScript("layui/commons/js/ajax.js");
    Apps.importScript("layui/commons/js/skin.js");
    Apps.importScript("layui/js/commons.js");
    Apps.importScript("layui/commons/ztree/js/jquery.ztree.all.js");
    Apps.importScript("layui/commons/ztree/js/selectTree.js");
    Apps.importScript("layui/layui_extends/xm-select.js");
}
