var AjaxConstant = {
	message : {
		open : true,
		image : "layui/images/loadingGreen15px.gif",
		text : "数据正在处理中,请稍候..."
	}
};

function AppAjax(url, callback) {
	this.url = url;
	this.type="POST";
	this.callback = callback;
	this.datatype = "json";
	this.param = {};
	this.async = true;
	this.message = {
		open : AjaxConstant.message.open,
		text : AjaxConstant.message.text
	};
}

AppAjax.prototype = {
	setAsync : function(isasync) {
		this.async = isasync;
		return this;
	},
		setType : function(type) {
        this.type = type;
        return this;
    },
	setMessage : function(isopen, message) {
		this.message.open = isopen;
		if (typeof message == "string" && message != "") {
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
	setDataType : function(type) {
		this.datatype = type;
		return this;
	},
	submit : function() {
		var loader = this;

        try {
            var loadIndex = layer.load(1, {shade: [0.8, '#393D49']})
        }catch (e) {
            var loadIndex = parent.layer.load(1, {shade: [0.8, '#393D49']})
        }
		/*if (this.url.indexOf("/") == -1) {
            this.url = "action/client/invoke?transcode=" + this.url;
        }*/
		this.url = Apps.ContextPath + this.url;

		if (this.url.indexOf("?") == -1) {
			this.url = this.url + "?_=" + new Date().getTime();
		} else {
			this.url = this.url + "&_=" + new Date().getTime();
		}
		jQuery.ajax({
			type : this.type,
			url : this.url,
			data : this.param,
			async : this.async,
			dataType : this.datatype,
			beforeSend : function(data) {
				if (loader.message.open) {
					AppAjax.showMessage(loader.message.text);
				}
			},
			success : function(data, textStatus) {
                try {
                    layer.close(loadIndex);
                }catch (e) {
                    parent.layer.close(loadIndex);
                }
				if (data && data["code"] && data["code"] == "-98") {// 会话超时
					window.top.location.href = Apps.ContextPath + "/login";
					return;
				}
                if (data && data["code"] && data["code"] > 1) {// 会话超时
                	alert(data["msg"]);
                    window.top.location.href = Apps.ContextPath + "/login";
                    return;
                }
				try {
					loader.callback.call(loader, data, textStatus);
				} catch (e) {
					alert(e.message);
				}
			},
			error : function(httpreq, textStatus, errorThrown) {
                try {
                    layer.close(loadIndex);
                }catch (e) {
                    parent.layer.close(loadIndex);
                }
				//alert(textStatus + ":" + errorThrown);
			},
			complete : function(httpreq, textStatus) {
                try {
                    layer.close(loadIndex);
                }catch (e) {
                    parent.layer.close(loadIndex);
                }
				// AppAjax.hideMessage();//由ajaxStop全局事件负责处理
			}
		});
		return this;
	}
};

AppAjax.useLoadingMessage = function(isopen, message) {
	AjaxConstant.message.open = isopen;
	if (typeof message == "string" && message != "") {
		AjaxConstant.message.text = message;
	}
};

AppAjax.getTopLevelWindow = function() {
	var pw = window;
	while (pw != pw.parent) {
		if (!pw.parent.AppAjax) {
			return pw;
		}
		pw = pw.parent;
	}
	return pw;
};
AppAjax.rootWindow = AppAjax.getTopLevelWindow();
AppAjax.rootDocument = AppAjax.rootWindow.document;

jQuery(document).ajaxStop(function() {
	AppAjax.hideMessage();
});

AppAjax.showMessage = function(message) {
	var ajaxMask = jQuery("#ajaxMask", AppAjax.rootDocument);
	if (ajaxMask.length < 1) {
		ajaxMask = jQuery("<div id='ajaxMask' style='display:none;'><iframe border=0></iframe></div>", AppAjax.rootDocument);
		jQuery("body", AppAjax.rootDocument).append(ajaxMask);
	}
	//ajaxMask.show();

	var loadingMessage = message || AjaxConstant.message.text;
	var processZone = jQuery("#processZone", AppAjax.rootDocument);
	if (processZone.length < 1) {
		processZone = jQuery("<div id='processZone'><img align='absmiddle' src='" + Apps.ContextPath + AjaxConstant.message.image + "'></img><span id='messageZone'></span></div>", AppAjax.rootDocument);
		jQuery("body", AppAjax.rootDocument).append(processZone);
	}
	jQuery("#messageZone", AppAjax.rootDocument).html("&nbsp;" + loadingMessage);
	processZone.show();
};

AppAjax.hideMessage = function() {
	jQuery("#processZone", AppAjax.rootDocument).hide();
	jQuery("#ajaxMask", AppAjax.rootDocument).hide();
};

var Ajax = AppAjax;