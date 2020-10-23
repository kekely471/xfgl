var JOBPicker;
var PolicerankPicker;
var leavetypePicker;
var sprPicker;
layui.use(['form'], function() {
	var form = layui.form;
	initJob("dkfs","JOB_Picker","JOB");
	initPolicerank("dkfs","POLICERANK_Picker","POLICERANK");
	initleavetypePicker("dkfs","LEAVE_TYPE_Picker","LEAVE_TYPE");
	initspr("dkfs","SPR_Picker","SPR");
	initdate("REALITY_START_TIME");
	initdate("REALITY_END_TIME");
	initdate("RWNY");
	form.on('submit(save)', function(data) {//保存
   			 
	});
	form.on('submit(censorship)', function(data) {//送审
   			 
	});
   		
})


function initJob(code,id,hideid) {
		var data=[{"NAME":"商业贷款","VALUE":"01"},{"NAME":"公积金贷款","VALUE":"02"},{"NAME":"公转商","VALUE":"03"},{"NAME":"组合贷款","VALUE":"04"},{"NAME":"其他","VALUE":"05"}];
		$(data).each(function(index,item){
			item.text=item.NAME;
			item.value=item.VALUE;
		});
		var resultdata=data;
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

function initPolicerank(code,id,hideid) {
		var data=[{"NAME":"商业贷款1","VALUE":"01"},{"NAME":"公积金贷款","VALUE":"02"},{"NAME":"公转商","VALUE":"03"},{"NAME":"组合贷款","VALUE":"04"},{"NAME":"其他","VALUE":"05"}];
		$(data).each(function(index,item){
			item.text=item.NAME;
			item.value=item.VALUE;
		});
		var resultdata=data;
		PolicerankPicker = new mui.PopPicker();
		PolicerankPicker.setData(resultdata);
		var PolicerankPickerButton = document.getElementById(id);
		var PolicerankResult = document.getElementById(id);
		var PolicerankhideResult=document.getElementById(hideid);
		PolicerankPickerButton.addEventListener('tap', function(event) {
			PolicerankPicker.show(function(items) {
				PolicerankResult.value = items[0].text;
				PolicerankhideResult.value=items[0].value;
			});
		}, false);
}

function initleavetypePicker(code,id,hideid) {
		var data=[{"NAME":"商业贷款3","VALUE":"01"},{"NAME":"公积金贷款","VALUE":"02"},{"NAME":"公转商","VALUE":"03"},{"NAME":"组合贷款","VALUE":"04"},{"NAME":"其他","VALUE":"05"}];
		$(data).each(function(index,item){
			item.text=item.NAME;
			item.value=item.VALUE;
		});
		var resultdata=data;
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
}

function initspr(code,id,hideid) {
		var data=[{"NAME":"张强","VALUE":"01"},{"NAME":"公积金贷款","VALUE":"02"},{"NAME":"公转商","VALUE":"03"},{"NAME":"组合贷款","VALUE":"04"},{"NAME":"其他","VALUE":"05"}];
		$(data).each(function(index,item){
			item.text=item.NAME;
			item.value=item.VALUE;
		});
		var resultdata=data;
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


