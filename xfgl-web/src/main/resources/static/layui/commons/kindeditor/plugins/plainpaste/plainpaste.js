/*******************************************************************************
* KindEditor - WYSIWYG HTML Editor for Internet
* Copyright (C) 2006-2011 kindsoft.net
*
* @author Roddy <luolonghao@gmail.com>
* @site http://www.kindsoft.net/
* @licence http://www.kindsoft.net/license.php
*******************************************************************************/

KindEditor.plugin('plainpaste', function(K) {
	var self = this, name = 'plainpaste';
	self.clickToolbar(name, function() {
		var lang = self.lang(name + '.'),
			html = '<div style="padding:10px 20px;">' +
				'<div style="margin-bottom:10px;">' + lang.comment + '</div>'+ 
				'<select><option>'+lang.option+
				'</option><option>'+lang.option1+
				'</option><option>'+lang.option2+
				'</option><option>'+lang.option3+
				'</option><option>'+lang.option4+
				'</option><option>'+lang.option5+
				'</option><option>'+lang.option6+
				'</option><option>'+lang.option7+
				'</option><option>'+lang.option8+'</option></select><div style="margin-bottom:10px;">'+lang.comment1+'</div>'+
				'<select><option>'+lang.option+'</option><option>'+lang.option9+
				'</option><option>'+lang.option10+
				'</option><option>'+lang.option11+
				'</option><option>'+lang.option12+'</option></select>' +
				'</div>',
			dialog = self.createDialog({
				name : name,
				width : 310,
				title : self.lang(name),
				body : html,
				yesBtn : {
					name : self.lang('yes'),
					click : function(e) {
						var html = self.html();
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
