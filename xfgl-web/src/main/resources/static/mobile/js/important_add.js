
var leavetypePicker;
var sprPicker;

layui.use(['form'], function() {
	var form = layui.form;
	initType("Important_type","TYPE","IMPORTANT_TYPE");
	initspr("Important_spr","AUDIT_USER_NAME","AUDIT_USER");

	// initdate("RWNY");
	form.on('submit(save)', function(data) {//保存
		var datas = data.field;
		datas.STATUS= 1
		var ajax = new Ajax("important/saveOrUpdate",function(data){
			if(judgeRight(data)){
				parent.layer.alert("操作成功!",{icon:1},function(){
					parent.layer.closeAll();
				});
			}
		});
		ajax.add(datas);
		ajax.submit();

	});
	form.on('submit(censorship)', function(data) {//送审
		console.log(data.elem.id);
		var datas = data.field;
		datas.STATUS= 2
		var ajax = new Ajax("important/saveOrUpdate",function(data){
			if(judgeRight(data)){
				parent.layer.alert("操作成功!",{icon:1},function(){
					parent.layer.closeAll();
				});
			}
		});
		ajax.add(datas);
		ajax.submit();
	});

   		
})


function initType(code,id,hideid) {
	var ajax = new Ajax("importantMobile/dict/business",function (data) {
			var dataList=data.dataset.rows;
			$(dataList).each(function(index,item){
				item.text=item.NAME;
				item.value=item.VALUE;
			});
			var resultdata=dataList;
			leavetypePicker = new mui.PopPicker();
			leavetypePicker.setData(resultdata);
			var leavetypePickerButton = document.getElementById(id);
			var leavetypeResult = document.getElementById(id);
			var leavetypehideResult=document.getElementById(hideid);
			leavetypePickerButton.addEventListener('tap', function(event) {
				leavetypePicker.show(function(items) {
					leavetypeResult.value = items[0].text;
					leavetypehideResult.value=items[0].value;
				});
			}, false);
	});
	ajax.add("type_code","Important_type");
	ajax.submit();



	/*var rows = new Array() ;
	$.post(Apps.ContextPath+'common/dict/business',{"type_code":"Important_type"},
		function(data){
			if(data.code ==1){
			rows =data.dataset.rows;
			$(rows).each(function(index,item){
            item.text=item.NAME;
            item.value=item.VALUE;
        });
		var resultdata=rows;
        JOBPicker = new mui.PopPicker();
        JOBPicker.setData(resultdata);
        var JOBPickerButton = document.getElementById(id);
        var JOBResult = document.getElementById(id);
        var JOBhideResult=document.getElementById(hideid);
        JOBPickerButton.addEventListener('tap', function(event) {
            JOBPicker.show(function(items) {
                JOBResult.value = items[0].text;
                JOBhideResult.value=items[0].value;
            });
        }, false);
		}
	});*/
}


function initspr(code,id,hideid) {
	var ajax = new Ajax("importantMobile/findAuditUsers",function (data) {
			var dataList=data.dataset.rows;
			$(dataList).each(function(index,item){
				item.text = item.NAME;
				item.value = item.USERID;
			});
			var resultdata=dataList;
			sprPicker = new mui.PopPicker();
			sprPicker.setData(resultdata);
			var sprPickerButton = document.getElementById(id);
			var sprResult = document.getElementById(id);
			var sprhideResult=document.getElementById(hideid);
			sprPickerButton.addEventListener('tap', function(event) {
				sprPicker.show(function(items) {
					sprResult.value = items[0].text;
					sprhideResult.value=items[0].value;
				});
			}, false);
	});
	ajax.add("zw","3");
	ajax.submit();
	/*var rows = new Array() ;
	$.post(Apps.ContextPath+'important/findAuditUsers',{"zw":"3"},
		function(data) {
			if (data.code == 1) {
				rows = data.dataset.rows;
				$(rows).each(function (index, item) {
					item.text = item.NAME;
					item.value = item.USERID;
				});
			var resultdata = rows;
			sprPicker = new mui.PopPicker();
			sprPicker.setData(resultdata);
			var sprPickerButton = document.getElementById(id);
			var sprResult = document.getElementById(id);
			var sprhideResult = document.getElementById(hideid);
			sprPickerButton.addEventListener('tap', function (event) {
				sprPicker.show(function (items) {
					sprResult.value = items[0].text;
					sprhideResult.value = items[0].value;
				});
			}, false);
		}
		});*/
}


function initdate(name){
	var btn = $("input[name='"+name+"']")[0];
		btn.addEventListener('tap', function() {
			var _self = this;
			if(_self.picker) {
				_self.picker.show(function (rs) {
					$("input[name='"+name+"']").val(rs.value);
					_self.picker.dispose();
					_self.picker = null;
				});
			} else {
				var optionsJson = this.getAttribute('data-options') || '{}';
				var options = JSON.parse(optionsJson);
				var id = this.getAttribute('id');
				_self.picker = new mui.DtPicker(options);
				_self.picker.show(function(rs) {
					$("input[name='"+name+"']").val(rs.value);
					_self.picker.dispose();
					_self.picker = null;
				});
			}
		}, false);
}


