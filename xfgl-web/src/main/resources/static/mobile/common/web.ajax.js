var AjaxConstant = {
	message: {
		open: true,
		text: "数据正在处理中,请稍候..."
	},
	// remotePath: "http://192.168.30.248:8088/xfgl/api/v1/user"
	//remotePath: "http://218.93.115.158:8088/xfgl"
	remotePath: "http://127.0.0.1:8088/xfgl"
};

function AppAjax(url, callback ) {
	this.url = url;
	this.callback = callback;
	this.datatype = "json";
	this.param = {};
	this.async = true;
	this.message = {
		open: AjaxConstant.message.open,
		text: AjaxConstant.message.text
	};
}

AppAjax.prototype = {
	setAsync: function(isasync) {
		this.async = isasync;
		return this;
	},
	setMessage: function(isopen, message) {
		this.message.open = isopen;
		if(typeof message == "string" && message != "") {
			this.message.text = message;
		}
		return this;
	},
	add : function(name, value) {
		if (typeof name == "object") {
			for ( var o in name) {
				if (typeof o == "function")
					continue;
				this.param[o] = name[o];
			}
		} else {
			this.param[name] = (typeof value == "undefined") ? (document.getElementById(name).value || "") : value;
		}
		return this;
	},
	clear : function() {
		this.param = {};
		return this;
	},
	setDataType: function(type) {
		this.datatype = type;
		return this;
	},
	submit : function() {
		var loader = this;
		if (this.url.indexOf("/") == -1) {
			this.url = AjaxConstant.remotePath + "/action/invokeMobile/invoke?serviceId=" + this.url;
		}else{
			this.url = AjaxConstant.remotePath+"/"+this.url;
		}
		if (this.url.indexOf("?") == -1) {
			this.url = this.url + "?_=" + new Date().getTime();
		} else {
			this.url = this.url + "&_=" + new Date().getTime();
		}
		jQuery.ajax({
			type: 'POST',
			url: this.url,
			data: this.param,
			async: this.async,
			dataType: this.datatype,
			beforeSend : function(data) {
//              index = layer.load(1, {
//                  shade: [0.5,'#fff'] //0.1透明度的白色背景
//              });
			},
			success : function(data, textStatus) {
				if (data && data["code"] && data["code"] == "-98") {// 会话超时
					window.top.location.href = "/login";
					return;
				}
				try {
					loader.callback.call(loader, data, textStatus);
				} catch (e) {
					alert(e.message);
				}
			},
			error : function(httpreq, textStatus, errorThrown) {
				console.log(textStatus + ":" + errorThrown);
			},
			complete : function(httpreq, textStatus) {
//				layer.close(index);
			}
		});
		return this;
	}
};


AppAjax.getTopLevelWindow = function() {
	var pw = window;
	while(pw != pw.parent) {
		if(!pw.parent.AppAjax) {
			return pw;
		}
		pw = pw.parent;
	}
	return pw;
};
AppAjax.rootWindow = AppAjax.getTopLevelWindow();
AppAjax.rootDocument = AppAjax.rootWindow.document;


AppAjax.showMessage = function(message) {
	
};

AppAjax.hideMessage = function() {
	
};

var Ajax = AppAjax;

function Base64() {

    // private property
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    }

    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    }

    // private method for UTF-8 encoding
    _utf8_encode = function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }
        return utftext;
    }

    // private method for UTF-8 decoding
    _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}