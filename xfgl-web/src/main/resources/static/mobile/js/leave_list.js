$(function(){
			//创建MeScroll对象
			var mescroll = new MeScroll("mescroll", {
				down: {
					auto: false, //是否在初始化完毕之后自动执行下拉回调callback; 默认true
					callback: downCallback //下拉刷新的回调
				},
				up: {
					auto: false, //是否在初始化时以上拉加载的方式自动加载第一页数据; 默认false
					callback: upCallback //上拉回调,此处可简写; 相当于 callback: function (page) { upCallback(page); }
				}
			});
			
			/*下拉刷新的回调 */
			function downCallback(){
					mescroll.endSuccess();
			}
			
			/*上拉加载的回调 page = {num:1, size:10}; num:当前页 从1开始, size:每页数据条数 */
			function upCallback(){
					mescroll.endSuccess();
			}
		});