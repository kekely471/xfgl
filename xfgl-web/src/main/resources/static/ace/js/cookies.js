var cookieday;

function setCookie(name, value)// 两个参数，一个是cookie的名子，一个是值
{
	var exp = new Date(); // new Date("December 31, 9998");
	exp.setTime(exp.getTime() + cookieday * 24 * 60 * 60 * 1000);
	document.cookie = name + "=" + encodeURI(value) + ";expires=" + exp.toGMTString();
}

// 取cookies函数
function getCookie(name) {
	var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
	if (arr != null)
		return encodeURI(arr[2]);
	return null;
}

// 删除cookie
function delCookie(name) {
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval = getCookie(name);
	if (cval != null)
		document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
}

// 重置时间
function updateCookie(name) {
	var cval = getCookie(name);
	if (cval != null) {
		var exp = new Date(); // new Date("December 31, 9998");
		exp.setTime(exp.getTime() + cookieday * 24 * 60 * 60 * 1000);
		document.cookie = name + "=" + encodeURI(value) + ";expires=" + exp.toGMTString();
	}
	return null;
}