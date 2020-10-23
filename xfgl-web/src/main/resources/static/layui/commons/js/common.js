var isQuirks = document.compatMode == "BackCompat";
var isStrict = document.compatMode == "CSS1Compat";
var isGecko = navigator.userAgent.toLowerCase().indexOf("gecko") != -1;
var isChrome = navigator.userAgent.toLowerCase().indexOf("chrome") != -1;
var isOpera = navigator.userAgent.toLowerCase().indexOf("opera") != -1;
var isIE = navigator.userAgent.toLowerCase().indexOf("msie") != -1 && !isOpera;
var isIE8 = navigator.userAgent.toLowerCase().indexOf("msie 8") != -1 && !!window.XDomainRequest && !!document.documentMode;
var isIE7 = navigator.userAgent.toLowerCase().indexOf("msie 7") != -1 && !isIE8;
var isIE6 = isIE && parseInt(navigator.userAgent.split(";")[1].replace(/(^\s*)|(\s*$)/g, "").split(" ")[1]) < 7;
var isBorderBox = isIE && isQuirks;

if (!isIE && HTMLElement) {
    var p = HTMLElement.prototype;
    p.__defineSetter__("innerText", function (txt) {
        this.textContent = txt;
    });
    p.__defineGetter__("innerText", function () {
        return this.textContent;
    });
    p.insertAdjacentElement = function (where, parsedNode) {
        switch (where) {
            case "beforeBegin":
                this.parentNode.insertBefore(parsedNode, this);
                return this.previousSibling;
                break;
            case "afterBegin":
                this.insertBefore(parsedNode, this.firstChild);
                return this.firstChild;
                break;
            case "beforeEnd":
                this.appendChild(parsedNode);
                return this.lastChild;
                break;
            case "afterEnd":
                if (this.nextSibling)
                    this.parentNode.insertBefore(parsedNode, this.nextSibling);
                else
                    this.parentNode.appendChild(parsedNode);
                return this.nextSibling;
                break;
        }
    };
    p.insertAdjacentHTML = function (where, htmlStr) {
        var r = this.ownerDocument.createRange();
        r.setStartBefore(this);
        var parsedHTML = r.createContextualFragment(htmlStr);
        return this.insertAdjacentElement(where, parsedHTML);
    };
    p.attachEvent = function (evtName, func) {
        evtName = evtName.substring(2);
        this.addEventListener(evtName, func, false);
    };
    p.detachEvent = function (evtName, func) {
        evtName = evtName.substring(2);
        this.removeEventListener(evtName, func, false);
    };
    p.fireEvent = function (evtName) {
        evtName = evtName.substring(2);
        var evtObj = document.createEvent('HTMLEvents');// HTMLEvents或Events
        evtObj.initEvent(evtName, true, true);
        return this.dispatchEvent(evtObj);
    };
    window.attachEvent = document.attachEvent = p.attachEvent;
    window.detachEvent = document.detachEvent = p.detachEvent;
    window.fireEvent = document.fireEvent = p.fireEvent;
    p.__defineGetter__("currentStyle", function () {
        return this.ownerDocument.defaultView.getComputedStyle(this, null);
    });
    p.__defineGetter__("children", function () {
        var tmp = [];
        for (var i = 0; i < this.childNodes.length; i++) {
            var n = this.childNodes[i];
            if (n.nodeType == 1) {
                tmp.push(n);
            }
        }
        return tmp;
    });
    p.__defineSetter__("outerHTML", function (sHTML) {
        var r = this.ownerDocument.createRange();
        r.setStartBefore(this);
        var df = r.createContextualFragment(sHTML);
        this.parentNode.replaceChild(df, this);
        return sHTML;
    });
    p.__defineGetter__("outerHTML", function () {
        var attr;
        var attrs = this.attributes;
        var str = "<" + this.tagName.toLowerCase();
        for (var i = 0; i < attrs.length; i++) {
            attr = attrs[i];
            if (attr.specified) {
                str += " " + attr.name + '="' + attr.value + '"';
            }
        }
        if (!this.hasChildNodes) {
            return str + ">";
        }
        return str + ">" + this.innerHTML + "</" + this.tagName.toLowerCase() + ">";
    });
    p.__defineGetter__("canHaveChildren", function () {
        switch (this.tagName.toLowerCase()) {
            case "area":
            case "base":
            case "basefont":
            case "col":
            case "frame":
            case "hr":
            case "img":
            case "br":
            case "input":
            case "isindex":
            case "link":
            case "meta":
            case "param":
                return false;
        }
        return true;
    });
    p.__defineGetter__("parentElement", function () {
        if (this.parentNode == this.ownerDocument) {
            return null;
        }
        return this.parentNode;
    });
    Event.prototype.__defineGetter__("srcElement", function () {
        var node = this.target;
        while (node && node.nodeType != 1)
            node = node.parentNode;
        return node;
    });
    Event.prototype.__defineGetter__("offsetX", function () {
        return this.layerX;
    });
    Event.prototype.__defineGetter__("offsetY", function () {
        return this.layerY;
    });
    Event.prototype.__defineSetter__("returnValue", function (b) {//
        if (!b)
            this.preventDefault();
        return b;
    });
    Event.prototype.__defineSetter__("cancelBubble", function (b) {// 设置或者检索当前事件句柄的层次冒泡
        if (b)
            this.stopPropagation();
        return b;
    });
    document.parentWindow = document.defaultView;
} else {
    try {
        // document.documentElement.addBehavior("#default#userdata");
        document.execCommand('BackgroundImageCache', false, true);
    } catch (e) {
        alert(e);
    }
}

var _setTimeout = function (callback, timeout, param) {
    if (typeof callback == 'function') {
        var args = Array.prototype.slice.call(arguments, 2);
        var _callback = function () {
            callback.apply(null, args);
        };
        return window.setTimeout(_callback, timeout);
    }
    return window.setTimeout(callback, timeout);
};

function extra(o, c, defaults) {// 复制对象c的成员到对象o
    if (!o) {
        o = {};
    }
    if (defaults) {
        extra(o, defaults);
    }
    if (o && c && typeof c == 'object') {
        for (var p in c) {
            o[p] = c[p];
        }
    }
    return o;
}

function extraIf(o, c) {
    if (!o) {
        o = {};
    }
    for (var p in c) {
        if (typeof (o[p]) == 'undefined') {
            o[p] = c[p];
        }
    }
    return o;
}

function toArray(a, i, j) {// 把可枚举的集合转换为数组
    if (isIE) {
        var res = [];
        for (var x = 0, len = a.length; x < len; x++) {
            res.push(a[x]);
        }
        return res.slice(i || 0, j || res.length);
    } else {
        return Array.prototype.slice.call(a, i || 0, j || a.length);
    }
}

var Core = {};
Core.addToCache = function (ele, id) {// appsclient.elCache统一存储dom元素相关数据、自定义事件
    if (!ele)
        return;
    if (!(id = id || ele.id))
        return;
    if (!Apps.elCache[id]) {
        Apps.elCache[id] = {
            el: ele,
            data: {},
            events: {}
        };
    }
};
Core.attachMethod = function (ele) {
    if (!ele || ele["$A"]) {
        return;
    }

    if (ele.nodeType == 9) {
        return;
    }
    var win;
    try {
        if (isGecko) {
            win = ele.ownerDocument.defaultView;
        } else {
            win = ele.ownerDocument.parentWindow;
        }
        extra(ele, win.ExtendElement);
    } catch (ex) {
        // alert("Core.attachMethod:"+ele)//有些对象不能附加属性，如flash
    }
};

Apps.idSeed = 0;
Apps.elCache = {};// 统一存储dom元素相关数据、自定义事件
Apps.enableGarbageCollector = true;//
function garbageCollect() {// 垃圾回收
    if (!Apps.enableGarbageCollector) {
        clearInterval(Apps.collectorThreadId);
    } else {
        var eid, el, d, o;
        for (eid in Apps.elCache) {
            o = Apps.elCache[eid];
            if (o.skipGC) {
                continue;
            }
            el = o.el;
            d = el.dom ? el.dom : el;
            if (!d || !d.parentNode || (!d.offsetParent && !document.getElementById(eid))) {
                if (Apps.enableListenerCollection) {
                    // EventManager.removeAll(d)
                }
                delete Apps.elCache[eid];
            }
        }
        if (isIE) {
            var t = {};
            for (eid in Apps.elCache) {
                t[eid] = Apps.elCache[eid];
            }
            Apps.elCache = t;
        }
    }
}

Apps.collectorThreadId = setInterval(garbageCollect, 30000);

if (window.attachEvent) {
    window.attachEvent('onunload', function () {
        Apps.elCache = {};
    });
} else {
    window.addEventListener('onunload', function () {
        Apps.elCache = {};
    });
}

function $Z(ele) {
    if (typeof (ele) == 'string') {
        ele = document.getElementById(ele);
    }
    if (!ele) {
        return null;
    }
    if (ele.tagName && ele.tagName.indexOf(":") == -1) {// 如果不为模板标签
        if (!ele.id) {
            ele.id = "uid_" + (ele.tagName ? ele.tagName.toLowerCase() : '') + (++Apps.idSeed);
        }
        Core.addToCache(ele);
        Core.attachMethod(ele);
    }
    return ele;
}

if (!window.$) {
    window.$ = $Z;
}

function $V(ele) {
    var eleId = ele;
    ele = $Z(ele);
    if (!ele) {
        alert("表单元素不存在:" + eleId);
        return null;
    }
    switch (ele.type.toLowerCase()) {
        case 'submit':
        case 'hidden':
        case 'password':
        case 'textarea':
        case 'file':
        case 'image':
        case 'select-one':
        case 'text':
            return ele.value;
        case 'checkbox':
        case 'radio':
            if (ele.checked) {
                return ele.value;
            } else {
                return null;
            }
        default:
            return "";
    }
}

function $S(ele, v) {
    var eleId = ele;
    ele = $Z(ele);
    if (!v && v != 0) {
        v = "";
    }
    if (!ele) {
        alert("表单元素不存在:" + eleId);
        return;
    }
    switch (ele.type.toLowerCase()) {
        case 'submit':
        case 'hidden':
        case 'password':
        case 'textarea':
        case 'button':
        case 'file':
        case 'image':
        case 'select-one':
        case 'text':
            ele.value = v;
            break;
        case 'checkbox':
        case 'radio':
            if (ele.value == "" + v) {
                ele.checked = true;
            } else {
                ele.checked = false;
            }
            break;
    }
}

function $T(tagName, ele) {
    ele = $Z(ele);
    ele = ele || document;
    var ts = ele.getElementsByTagName(tagName);// 此处返回的不是数组
    var arr = [];
    var len = ts.length;
    for (var i = 0; i < len; i++) {
        arr.push($Z(ts[i]));
    }
    return arr;
}

function $N(ele) {
    if (typeof (ele) == 'string') {
        ele = document.getElementsByName(ele);
        if (!ele || ele.length == 0) {
            return null;
        }
        var arr = [];
        for (var i = 0; i < ele.length; i++) {
            var e = ele[i];
            if (e.getAttribute("ztype") == "select") {
                e = e.parentNode;
            }
            Core.attachMethod(e);
            arr.push(e);
        }
        ele = arr;
    }
    return ele;
}

function $NV(ele) {
    ele = $N(ele);
    if (!ele) {
        return null;
    }
    var arr = [];
    for (var i = 0; i < ele.length; i++) {
        var v = $V(ele[i]);
        if (v != null) {
            arr.push(v);
        }
    }
    return arr.length == 0 ? null : arr;
}

function $NS(ele, value) {
    ele = $N(ele);
    if (!ele) {
        return;
    }
    if (!ele[0]) {
        return $S(ele, value);
    }
    if (ele[0].type == "checkbox") {
        if (value == null) {
            value = new Array(4);
        }
        var arr = value;
        if (!isArray(value)) {
            arr = value.split(",");
        }
        for (var i = 0; i < ele.length; i++) {
            for (var j = 0; j < arr.length; j++) {
                if (ele[i].value == arr[j]) {
                    $S(ele[i], arr[j]);
                    break;
                }
                $S(ele[i], arr[j]);
            }
        }
        return;
    }
    for (var i = 0; i < ele.length; i++) {
        $S(ele[i], value);
    }
}

function $F(ele) {
    if (!ele)
        return document.forms[0];
    else {
        ele = $Z(ele);
        if (ele && ele.tagName.toLowerCase() != "form")
            return null;
        return ele;
    }
}

// 多选框全选
function selectAll(ele, eles) {
    var flag = $V(ele);
    var arr = $N(eles);
    if (arr) {
        for (var i = 0; i < arr.length; i++) {
            arr[i].checked = flag;
        }
    }
}

function encodeURL(str) {
    return encodeURI(str).replace(/=/g, "%3D").replace(/\+/g, "%2B").replace(/\?/g, "%3F").replace(/\&/g, "%26");
}

function htmlEncode(str) {
    return str.replace(/&/g, "&amp;").replace(/\"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/ /g, "&nbsp;");
}

function htmlDecode(str) {
    return str.replace(/\&quot;/g, "\"").replace(/\&lt;/g, "<").replace(/\&gt;/g, ">").replace(/\&nbsp;/g, " ").replace(/\&amp;/g, "&");
}

function javaEncode(txt) {
    if (!txt) {
        return txt;
    }
    return txt.replace("\\", "\\\\").replace("\r\n", "\n").replace("\n", "\\n").replace("\"", "\\\"").replace("\'", "\\\'");
}

function javaDecode(txt) {
    if (!txt) {
        return txt;
    }
    return txt.replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\'", "\'");
}

function isInt(str) {
    return /^\-?\d+$/.test("" + str);
}

function isNumber(str) {
    // var t = ""+str;
    var dotCount = 0;
    for (var i = 0; i < str.length; i++) {
        var c = str.charAt(i);
        if (c == ".") {
            if (i == 0 || i == str.length - 1 || dotCount > 0) {
                return false;
            } else {
                dotCount++;
            }
        } else if (c == '-') {
            if (i != 0) {
                return false;
            }
        } else if (isNaN(parseInt(c))) {
            return false;
        }
    }
    return true;
}

function isTime(str) {
    if (!str) {
        return false;
    }
    var arr = str.split(":");
    if (arr.length != 3) {
        return false;
    }
    if (!isNumber(arr[0]) || !isNumber(arr[1]) || !isNumber(arr[2])) {
        return false;
    }
    var date = new Date();
    date.setHours(arr[0]);
    date.setMinutes(arr[1]);
    date.setSeconds(arr[2]);
    return date.toString().indexOf("Invalid") < 0;
}

function isDate(str) {
    if (!str) {
        return false;
    }
    var arr = str.split("-");
    if (arr.length != 3) {
        return false;
    }
    if (!isNumber(arr[0]) || !isNumber(arr[1]) || !isNumber(arr[2])) {
        return false;
    }
    var date = new Date();
    date.setFullYear(arr[0]);
    date.setMonth(arr[1]);
    date.setDate(arr[2]);
    return date.toString().indexOf("Invalid") < 0;
}

function isDateTime(str) {
    if (!str) {
        return false;
    }
    if (str.indexOf(" ") < 0) {
        return isDate(str);
    }
    var arr = str.split(" ");
    if (arr.length < 2) {
        return false;
    }
    return isDate(arr[0]) && isTime(arr[1]);
}

function isArray(v) {
    return !!v && Object.prototype.toString.apply(v) === '[object Array]';
}

function isFunction(v) {
    return !!v && Object.prototype.toString.apply(v) === '[object Function]';
}

function isObject(v) {
    return !!v && Object.prototype.toString.call(v) === '[object Object]';
}

function isElement(v) {
    return !!v && v.tagName;
}

function isEmpty(v, allowBlank) {
    return v === null || v === undefined || ((isArray(v) && !v.length)) || (!allowBlank ? v === '' : false);
};

function isNull(v) {
    return v === null || typeof (v) == "undefined";
}

function isNotNull(v) {
    return !isNull(v);
}

function getEvent(evt) {
    evt = evt || window.event;
    if (!evt) {
        var func = getEvent.caller;
        while (func) {
            var evt = func.arguments[0];
            if (evt && (evt.constructor == Event || evt.constructor == MouseEvent)) {
                break;
            }
            func = func.caller;
        }
    }
    return evt;
}

function fixEvent(event) {// 在IE下为event对象添加pageX、pageY属性
    if (!event) {
        return null;
    }
    var doc = document;
    var sl = Math.max(doc.documentElement.scrollLeft, doc.body.scrollLeft);
    var st = Math.max(doc.documentElement.scrollTop, doc.body.scrollTop);
    if (typeof event.pageX == "undefined")
        event.pageX = event.clientX + sl - doc.body.clientLeft;
    if (typeof event.pageY == "undefined")
        event.pageY = event.clientY + st - doc.body.clientTop;
    return event;
}

function stopEvent(evt) {// 阻止一切事件执行,包括浏览器默认的事件
    evt = getEvent(evt);
    if (!evt) {
        return;
    }
    if (isGecko) {
        evt.preventDefault();
        evt.stopPropagation();
    }
    evt.cancelBubble = true;
    evt.returnValue = false;
}

function cancelEvent(evt) {// 仅阻止用户定义的事件
    evt = getEvent(evt);
    evt.cancelBubble = true;
}

function getEventPosition(evt) {// 返回相对于最上级窗口的左上角原点的坐标
    evt = getEvent(evt);
    var pos = {
        x: evt.clientX,
        y: evt.clientY
    };
    var win;
    if (isGecko) {
        win = evt.srcElement.ownerDocument.defaultView;
    } else {
        win = evt.srcElement.ownerDocument.parentWindow;
    }
    var sw, sh;
    while (win != win.parent) {
        if (win.frameElement && win.parent.Apps) {
            pos2 = $E.getPosition(win.frameElement);
            pos.x += pos2.x;
            pos.y += pos2.y;
        }
        sw = Math.max(win.document.body.scrollLeft, win.document.documentElement.scrollLeft);
        sh = Math.max(win.document.body.scrollTop, win.document.documentElement.scrollTop);
        pos.x -= sw;
        pos.y -= sh;
        if (!win.parent.Apps) {
            break;
        }
        win = win.parent;
    }

    return pos;
}

function getEventPositionLocal(evt) {// 返回事件在当前页面上的坐标
    evt = getEvent(evt);
    var pos = {
        x: evt.clientX,
        y: evt.clientY
    };
    var win;
    if (isGecko) {
        win = evt.srcElement.ownerDocument.defaultView;
    } else {
        win = evt.srcElement.ownerDocument.parentWindow;
    }
    pos.x += Math.max(win.document.body.scrollLeft, win.document.documentElement.scrollLeft);
    pos.y += Math.max(win.document.body.scrollTop, win.document.documentElement.scrollTop);
    return pos;
}

function toXMLDOM(xml) {
    var doc;
    if (isIE) {
        try {
            doc = new ActiveXObject("Microsoft.XMLDOM");
        } catch (ex) {
            doc = new ActiveXObject("Msxml2.DOMDocument");
        }
        doc.loadXML(xml);
    } else {
        var p = new DOMParser();
        doc = p.parseFromString(xml, "text/xml");
    }
    return doc;
}

//
// /**
//  * JSON
//  */
// var JSON = {};
//
// JSON.type = function(o) {
// 	var _toS = Object.prototype.toString;
// 	var _types = {
// 		'undefined' : 'undefined',
// 		'number' : 'number',
// 		'boolean' : 'boolean',
// 		'string' : 'string',
// 		'[object Function]' : 'function',
// 		'[object RegExp]' : 'regexp',
// 		'[object Array]' : 'array',
// 		'[object Date]' : 'date',
// 		'[object Error]' : 'error'
// 	};
// 	return _types[typeof o] || _types[_toS.call(o)] || (o ? 'object' : 'null');
// };
//
// var $specialChars = {
// 	'\b' : '\\b',
// 	'\t' : '\\t',
// 	'\n' : '\\n',
// 	'\f' : '\\f',
// 	'\r' : '\\r',
// 	'"' : '\\"',
// 	'\\' : '\\\\'
// };
//
// var $replaceChars = function(chr) {
// 	return $specialChars[chr] || '\\u00' + Math.floor(chr.charCodeAt() / 16).toString(16) + (chr.charCodeAt() % 16).toString(16);
// };
//
// JSON.toString = function(o) {
// 	var s = [];
// 	switch (JSON.type(o)) {
// 	case 'undefined':
// 		return 'undefined';
// 		break;
// 	case 'null':
// 		return 'null';
// 		break;
// 	case 'number':
// 	case 'boolean':
// 	case 'date':
// 	case 'function':
// 		return o.toString();
// 		break;
// 	case 'string':
// 		return '"' + o.replace(/[\x00-\x1f\\"]/g, $replaceChars) + '"';
// 		break;
// 	case 'array':
// 		for ( var i = 0, l = o.length; i < l; i++) {
// 			s.push(JSON.toString(o[i]));
// 		}
// 		return '[' + s.join(',') + ']';
// 		break;
// 	case 'error':
// 	case 'object':
// 		for ( var p in o) {
// 			s.push(p + ':' + JSON.toString(o[p]));
// 		}
// 		return '{' + s.join(',') + '}';
// 		break;
// 	default:
// 		return '';
// 		break;
// 	}
// };
//
// JSON.evaluate = function(str) {
// 	if (typeof str == "string") {
// 		if (!!JSON.parse) {
// 			return JSON.parse(str);
// 		}
// 		if (isIE) {
// 			return eval('(' + str + ')');
// 		} else {
// 			return new Function("return " + str);
// 		}
// 	} else {
// 		return str;
// 	}
// };

/**
 * 全部替换
 */
String.prototype.replaceAll = function (search, replace) {
    var regex = new RegExp(search, "g");
    return this.replace(regex, replace);
};

/**
 * String
 */
String.format = function (str) {
    var args = Array.prototype.slice.call(arguments, 1);
    return str.replace(/\{(\d+)\}/g, function (m, i) {
        return args[i];
    });
};

String.templ = function (str, obj, urlencode) {
    return str.replace(/\{([\w_$]+)\}/g, function (c, $1) {
        var a = obj[$1];
        if (a === undefined || a === null) {
            return '';
        }
        return urlencode ? encodeURIComponent(a) : a;
    });
};

String.prototype.startsWith = String.prototype.startWith = function (str) {
    return this.indexOf(str) == 0;
};

String.prototype.endsWith = String.prototype.endWith = function (str) {
    var i = this.lastIndexOf(str);
    return i >= 0 && this.lastIndexOf(str) == this.length - str.length;
};

/**
 * 去除空格
 */
String.prototype.trim = function () {
    return this.replace(/(^\s*)|(\s*$)/g, "");
};

String.prototype.leftPad = function (c, count) {
    if (!isNaN(count)) {
        var a = "";
        for (var i = this.length; i < count; i++) {
            a = a.concat(c);
        }
        a = a.concat(this);
        return a;
    }
    return null;
};

String.prototype.rightPad = function (c, count) {
    if (!isNaN(count)) {
        var a = this;
        for (var i = this.length; i < count; i++) {
            a = a.concat(c);
        }
        return a;
    }
    return null;
};

Array.prototype.clone = function () {
    var len = this.length;
    var r = [];
    for (var i = 0; i < len; i++) {
        if (typeof (this[i]) == "undefined" || this[i] == null) {
            r[i] = this[i];
            continue;
        }
        if (this[i].constructor == Array) {
            r[i] = this[i].clone();
        } else {
            r[i] = this[i];
        }
    }
    return r;
};

Array.prototype.insert = function (index, data) {
    if (isNaN(index) || index < 0 || index > this.length) {
        this.push(data);
    } else {
        var temp = this.slice(index);
        this[index] = data;
        for (var i = 0; i < temp.length; i++) {
            this[index + 1 + i] = temp[i];
        }
    }
    return this;
};

Array.prototype.remove = function (s, dust) {// 如果dust为ture，则返回被删除的元素
    var dustArr = null;
    for (var i = 0; i < this.length; i++) {
        if (s == this[i]) {
            dustArr = this.splice(i, 1)[0];
        }
    }
    if (dust) {
        return dustArr;
    } else {
        dustArr = null;
        return this;
    }
};

Array.prototype.indexOf = function (func) {
    var len = this.length;
    for (var i = 0; i < len; i++) {
        if (this[i] == arguments[0])
            return i;
    }
    return -1;
};

Array.prototype.each = function (func, scope) {
    var len = this.length;
    for (var i = len - 1; i >= 0; i--) {
        try {
            func.call(scope || this[i], this[i], i, this);
        } catch (ex) {
            // alert("Array.prototype.each:"+ex.message);
        }
    }
};

function copyTo(target, source) {
    if (source && target) {
        for (var o in source) {
            target[o] = source[o];
        }
    }
    return target;
}

function formatNum(num, d) {
    var digit = 0;
    if (d || d == 0) {
        digit = d;
    } else {
        digit = 2;
    }
    num = num + ",";
    num = this.replaceComma(num);
    num = parseFloat(num).toFixed(digit);
    if (!/^(\+|-)?(\d+)(\.\d+)?$/.test(num)) {
        return parseFloat("0").toFixed(digit);
    }
    var a = RegExp.$1, b = RegExp.$2, c = RegExp.$3;
    var re = new RegExp().compile("(\\d)(\\d{3})(,|$)");
    while (re.test(b)) {
        b = b.replace(re, "$1,$2$3");
    }
    if (c && digit && new RegExp("^.(\\d{" + digit + "})(\\d)").test(c)) {
        if (RegExp.$2 > 4) {
            c = (parseFloat(RegExp.$1) + 1) / Math.pow(10, digit);
        } else {
            c = "." + RegExp.$1;
        }
    }
    var l_num = a + "" + b + "" + (c + "").substr((c + "").indexOf("."));
    /*
     * if (l_num.indexOf(".") < 0) { l_num += ".00"; }
     */
    return l_num;
}

/**
 * *去除金额中的逗号
 */
function replaceComma(n) {
    var l_n = "";
    for (var i = 0; i < n.length; i++) {
        var str = n.substring(i, i + 1);
        l_n += str.replace(",", "");
    }
    return l_n;
}

/**
 * StringBuffer,优化字符串拼装性能
 */
function StringBuffer() {
    this._strings = [];
    for (var i = 0; i < arguments.length; i++) {
        this._strings.push(arguments[i]);
    }
};

StringBuffer.prototype = {
    append: function (str) {
        this._strings.push(str);
        return this;
    },
    clear: function () {
        this._strings = [];
    },
    length: function () {
        var str = this._strings.join("");
        return str.length;
    },
    del: function (num) {
        var len = this.length();
        var str = this.toString();
        str = str.slice(0, len - num);
        this._strings = [];
        this._strings.push(str);
    },
    remove: function (num) {
        this.del(num);
    },
    join: function (split) {
        return this._strings.join(split || "");
    },
    toString: function () {
        return this._strings.join("");
    }
};

function getSize() {// 取宽度
    var windowWidth = 0, windowHeight = 0;
    if (self.innerHeight) {
        windowWidth = self.innerWidth;
        windowHeight = self.innerHeight;
    } else if (document.documentElement && document.documentElement.clientHeight) {
        windowWidth = document.documentElement.clientWidth;
        windowHeight = document.documentElement.clientHeight;
    } else if (document.body) {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
    }
    return {
        h: windowHeight,
        w: windowWidth
    };
}

// 为Form赋值
function setFormValue(frmName, data) {
    var frm = null;

    if (data == null)
        return;

    if (typeof frmName == "string") {
        frm = jQuery("#" + frmName);
    } else {
        frm = jQuery(frmName);
    }

    if (JSON.type(data) == "array") {
        data = data[0];
    }
    if (typeof data != "object") {
        return;
    }

    function setValue_CallBack() {
        var iscb = true;
        var name = (this.name || this.id).toUpperCase();
        var name_lower = name.toLowerCase();
        if (this.type == "button")
            return;

        var val = this.value;

        if (typeof data[name] === "undefined" && typeof data[name_lower] === "undefined") {
            iscb = false;
        } else {
            val = data[name] || data[name_lower];

            if (val == null) {
                if (data[name] == '0' || data[name_lower] == '0') {
                    val = 0;
                } else {
                    val = "";
                }
            }
        }

        if (this.type == "radio" || this.type == "checkbox") {
            if (iscb && this.value == val) {
                this.checked = true;
            }
            return;
        }

        if (typeof this.datatype != undefined) {
            if (this.datatype == "numberic") {
                var digt = this.digt;
                jQuery(this).val(formatNum(val, digt));
            } else {
                jQuery(this).val(val);
            }
        } else {
            jQuery(this).val(val);
        }
    }

    frm.find("input").each(setValue_CallBack);
    frm.find("textarea").each(setValue_CallBack);
    frm.find("select").each(setValue_CallBack);
}

// 清空表单信息
function formClear(frmName) {
    var frm = null;
    if (typeof frmName == "string") {
        frm = jQuery("#" + frmName);
    } else {
        frm = jQuery(frmName);
    }

    function clearvcallback(i) {
        jQuery(this).val("");
    }

    function clearccallback(i) {
        jQuery(this).attr("checked", false);
    }

    frm.find(":text").each(clearvcallback);

    frm.find("input[type=hidden]").each(clearvcallback);

    frm.find(":password").each(clearvcallback);

    frm.find("textarea").each(clearvcallback);

    frm.find(":radio[checked]").each(clearccallback);

    frm.find(":checkbox[checked]").each(clearccallback);
}

// 序列化Form表单元素
function formSerialize(frmName) {
    var emtparam = {};
    var frm = null;
    if (typeof frmName == "string") {
        frm = jQuery("#" + frmName);
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

/**
 * 合并 两个 序列化的对象
 *
 * @param jsonbject1
 * @param jsonbject2
 * @returns {___anonymous26591_26592}
 */
function mergeJsonObject(jsonbject1, jsonbject2) {
    var resultJsonObject = {};

    for (var attr in jsonbject1) {
        resultJsonObject[attr] = jsonbject1[attr];
    }

    for (var attr in jsonbject2) {
        resultJsonObject[attr] = jsonbject2[attr];
    }

    return resultJsonObject;
};

/**
 * 平滑数据转成树形结构
 *
 * @param data
 *            平滑数据
 * @param opt
 *            eg: { idField : 'id',textField : 'name',parentField : 'parent_id' }
 * @returns
 */
function toTreeData(data, opt) {
    var idField, treeField, parentField;
    if (opt.parentField) {
        idField = opt.idField || 'ID';
        treeField = opt.textField || 'TEXT';
        parentField = opt.parentField || 'PID';

        var i, l, treeData = [], tmpMap = [];
        for (i = 0, l = data.length; i < l; i++) {
            tmpMap[data[i][idField]] = data[i];
        }

        for (i = 0, l = data.length; i < l; i++) {
            if (tmpMap[data[i][parentField]] && data[i][idField] != data[i][parentField]) {
                if (!tmpMap[data[i][parentField]]['CHILDREN'])
                    tmpMap[data[i][parentField]]['CHILDREN'] = [];
                data[i]['TEXT'] = data[i][treeField];
                tmpMap[data[i][parentField]]['CHILDREN'].push(data[i]);
            } else {
                data[i]['TEXT'] = data[i][treeField];
                treeData.push(data[i]);
            }
        }
        return treeData;
    }
    return data;
}

function toTreeDataEX(data, opt) {
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
                if (!tmpMap[data[i][parentField]]['children'])
                    tmpMap[data[i][parentField]]['children'] = [];
                data[i]['text'] = data[i][treeField];
                data[i]['id'] = data[i][idField];
                tmpMap[data[i][parentField]]['children'].push(data[i]);
            } else {
                data[i]['text'] = data[i][treeField];
                data[i]['id'] = data[i][idField];
                treeData.push(data[i]);
            }
        }
        return treeData;
    }
    return data;
}

/**
 * 数字转中文
 *
 * @number {Integer} 形如123的数字
 * @return {String} 返回转换成的形如 一百二十三 的字符串
 */
function chn(number) {
    /* 单位 */
    var units = '个十百千万@#%亿^&~';
    /* 字符 */
    var chars = '零一二三四五六七八九';
    var a = (number + '').split(''), s = [];
    if (a.length > 12) {
        throw new Error('too big');
    } else {
        for (var i = 0, j = a.length - 1; i <= j; i++) {
            if (j == 1 || j == 5 || j == 9) {// 两位数 处理特殊的 1*
                if (i == 0) {
                    if (a[i] != '1')
                        s.push(chars.charAt(a[i]));
                } else {
                    s.push(chars.charAt(a[i]));
                }
            } else {
                s.push(chars.charAt(a[i]));
            }
            if (i != j) {
                s.push(units.charAt(j - i));
            }
        }
    }
    // return s;
    return s.join('').replace(/零([十百千万亿@#%^&~])/g, function (m, d, b) {// 优先处理
        // 零百 零千
        // 等
        b = units.indexOf(d);
        if (b != -1) {
            if (d == '亿')
                return d;
            if (d == '万')
                return d;
            if (a[j - b] == '0')
                return '零';
        }
        return '';
    }).replace(/零+/g, '零').replace(/零([万亿])/g, function (m, b) {// 零百 零千处理后 可能出现
        // 零零相连的
        // 再处理结尾为零的
        return b;
    }).replace(/亿[万千百]/g, '亿').replace(/[零]$/, '').replace(/[@#%^&~]/g, function (m) {
        return {
            '@': '十',
            '#': '百',
            '%': '千',
            '^': '十',
            '&': '百',
            '~': '千'
        }[m];
    }).replace(/([亿万])([一-九])/g, function (m, d, b, c) {
        c = units.indexOf(d);
        if (c != -1) {
            if (a[j - c] == '0')
                return d + '零' + b;
        }
        return m;
    });
}

function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

// 计算两个日期相差的天数
function DateDiff(sDate1, sDate2) { // sDate1和sDate2是2004-10-18格式
    var aDate, oDate1, oDate2, iDays;

    aDate = sDate1.split("-");
    oDate1 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]); // 转换为10-18-2004格式
    aDate = sDate2.split("-");
    oDate2 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]);
    iDays = parseInt((oDate1 - oDate2) / 1000 / 60 / 60 / 24); // 把相差的毫秒数转换为天数
    return iDays;
}

// 日期处理
var now = new Date(); // 当前日期
var nowDayOfWeek = now.getDay(); // 今天本周的第几天
var nowDay = now.getDate(); // 当前日
var nowMonth = now.getMonth(); // 当前月
var nowYear = now.getFullYear(); // 当前年

// 格式化日期：yyyy-MM-dd
function formatDate(date) {
    var myyear = date.getFullYear();
    var mymonth = date.getMonth() + 1;
    var myweekday = date.getDate();
    if (mymonth < 10) {
        mymonth = "0" + mymonth;
    }
    if (myweekday < 10) {
        myweekday = "0" + myweekday;
    }
    return (myyear + "-" + mymonth + "-" + myweekday);
}

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S": this.getMilliseconds()
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

// 获得当前格式化日期：yyyy-MM-dd
function getNowFormatDate() {
    return formatDate(now);
}

// 获得某月的天数
function getMonthDays(myMonth) {
    var monthStartDate = new Date(nowYear, myMonth, 1);
    var monthEndDate = new Date(nowYear, myMonth + 1, 1);
    var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24);
    return days;
}

// 获得本季度的开始月份
function getQuarterStartMonth() {
    var quarterStartMonth = 0;
    if (nowMonth < 3) {
        quarterStartMonth = 0;
    }
    if (2 < nowMonth && nowMonth < 6) {
        quarterStartMonth = 3;
    }
    if (5 < nowMonth && nowMonth < 9) {
        quarterStartMonth = 6;
    }
    if (nowMonth > 8) {
        quarterStartMonth = 9;
    }
    return quarterStartMonth;
}

// 获得本周的开始日期
function getWeekStartDate() {
    var weekStartDate = new Date(nowYear, nowMonth, nowDay - nowDayOfWeek);
    return formatDate(weekStartDate);
}

// 获得本周的结束日期
function getWeekEndDate() {
    var weekEndDate = new Date(nowYear, nowMonth, nowDay + (6 - nowDayOfWeek));
    return formatDate(weekEndDate);
}

// 获得本月的开始日期
function getMonthStartDate() {
    var monthStartDate = new Date(nowYear, nowMonth, 1);
    return formatDate(monthStartDate);
}

// 获得上月的开始日期
function getLastMonthStartDate() {
    var monthStartDate = new Date(nowYear, nowMonth - 1, 1);
    return formatDate(monthStartDate);
}

// 获得本月的结束日期
function getMonthEndDate() {
    var monthEndDate = new Date(nowYear, nowMonth, getMonthDays(nowMonth));
    return formatDate(monthEndDate);
}

// 获得本季度的开始日期
function getQuarterStartDate() {
    var quarterStartDate = new Date(nowYear, getQuarterStartMonth(), 1);
    return formatDate(quarterStartDate);
}

// 获的本季度的结束日期
function getQuarterEndDate() {
    var quarterEndMonth = getQuarterStartMonth() + 2;
    var quarterStartDate = new Date(nowYear, quarterEndMonth, getMonthDays(quarterEndMonth));
    return formatDate(quarterStartDate);
}

// 获得本年度的开始日期
function getYearStartDate() {
    var yearStartDate = new Date(nowYear, 0, 1);
    return formatDate(yearStartDate);
}

// 获得本年度的开始日期
function getLastYearStartDate() {
    var yearStartDate = new Date(nowYear - 1, 0, 1);
    return formatDate(yearStartDate);
}

//查询
var qry_list = function (dgid, searchid) {
    var dgid = dgid || 'dg';
    var searchid = searchid || 'tb';
    var inputs = $("#" + searchid).find("input");
    var queryParams = {};
    $.each(inputs, function (i, n) {
        var inputname = $(n).attr("name");
        if (inputname && inputname.indexOf("FORM_") != -1) {
            var vval = $("input[name='" + inputname + "']").val();
            if (vval) {
                queryParams[inputname] = vval;
            }

        }

    });
    $('#' + dgid).datagrid({
        queryParams: queryParams
    });
}

//重置
function resetSearch(dgid, searchid) {
    var dgid = dgid || 'dg';
    var searchid = searchid || 'tb';
    var inputs = $("#" + searchid).find("input");
    $.each(inputs, function (i, n) {
        var id = $(n).attr("id");
        $("#" + id).textbox("setText", "");
        $("#" + id).val("");
        $("input[name='" + id + "']").val("");
        $('#' + dgid).datagrid({
            queryParams: {}
        });

    });
}
