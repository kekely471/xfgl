var sprPicker;
layui.use(['form'], function() {
	var form = layui.form;
	initspr("dkfs","SPR_Picker","SPR");
	form.on('submit(save)', function(data) {//保存
		
	});
	form.on('submit(save1)', function(data) {//送审
   			 
	});
	 form.on('switch(isAgree)', function(data){
	 	var isCheck=this.checked ? 'true' : 'false';
	 	if(isCheck=='true'){
	 		$("#li_spr").removeClass('hide');
	 		$("#li_spyj").removeClass('hide');
	 		$("input[name='SPR_Picker']").attr('lay-verify','required');
	 	}else{
	 		$("#li_spr").addClass('hide');
	 		$("#li_spyj").addClass('hide');
	 		$("textarea[name='ddbyj']").val('');
	 		$("input[name='SPR']").val('');
	 		$("input[name='SPR_Picker']").val('');
	 		$("input[name='SPR_Picker']").removeAttr('lay-verify','required');
	 	}
    	
  
  });
})


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



