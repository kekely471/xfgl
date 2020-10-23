var viewer = null;
(function() {
    /***
     * viewer封装
     * @param {Object} container 内容容器(必填)
     * @param {Object} options 选项(可选)
     */
    function openViewer(container,options) {
        options.toolbar.prev = 0;
        options.toolbar.next = 0;
        var thisReady;
        if(options.ready){
            thisReady = options.ready;
        }
        options.ready = function(){
            if(thisReady) thisReady();
            $('.viewer-container').append('<div class="openView-button openView-button-left" onclick="prev()"></div>' +
                '<div class="openView-button openView-button-right" onclick="next()"></div>');
        }
        viewer = new Viewer(container, options);
    }

    window.openViewer = openViewer;

})(window);

/***
 * 自定义前后切换按钮事件
 */
var prev = function() {
    viewer.prev({
        loop: true
    });
}
var next = function() {
    viewer.next({
        loop: true
    });
}