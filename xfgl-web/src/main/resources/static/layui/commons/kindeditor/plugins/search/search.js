/*******************************************************************************
* KindEditor - WYSIWYG HTML Editor for Internet
* Copyright (C) 2006-2011 kindsoft.net
*
* @author Roddy <luolonghao@gmail.com>
* @site http://www.kindsoft.net/
* @licence http://www.kindsoft.net/license.php
*******************************************************************************/
var strSelf;
KindEditor.plugin('search', function(K) {
	var self = this, name = 'search';
	self.clickToolbar(name, function() {
		var lang = self.lang(name + '.'),
			html = '<div style="padding:10px 20px;">' +
				'<div style="margin-bottom:10px;">' + lang.comment + '</div>' +
				'<iframe class="ke-textarea" frameborder="0" style="width:270px;height:20px;"></iframe>' +
				'</div>',
			dialog = self.createDialog({
				name : name,
				width : 310,
				title : self.lang(name),
				body : html,
				yesBtn : {
					name : self.lang('yes'),
					click : function(e) {
						var str = doc.body.innerHTML;
						strSelf = str;
						//str = K.clearMsWord(str, self.filterMode ? self.htmlTags : K.options.htmlTags);
						//self.insertHtml(str).hideDialog().focus();
						var html = self.html();
						var re1=new RegExp("<span style=\"color:red;\"><b>","gi");
						html = html.replace(re1,"");

						var re2=new RegExp("</b></span>","gi");
						html = html.replace(re2,"");
	
						var re3=new RegExp("<br>","gi");
						str = str.replace(re3,"");

						var re = new RegExp(str,"gi")
						html = html.replace(re,"<span style='color:red'><b>"+ str +"</b></span>");
						self.html(html).hideDialog().focus();					
					}
				}
			}),
			div = dialog.div,
			iframe = K('iframe', div),
			doc = K.iframeDoc(iframe);
		if (!K.IE) {
			doc.designMode = 'on';
		}
		doc.open();
		doc.write('<!doctype html><html><head><title>WordPaste</title></head>');
		doc.write('<body style="background-color:#FFF;font-size:12px;margin:2px;">');
		if (!K.IE) {
			doc.write('<br />');
		}
		doc.write('</body></html>');
		doc.close();
		if (K.IE) {
			doc.body.contentEditable = 'true';
		}
		iframe[0].contentWindow.focus();
	});
});
