(function($){
    var defaultval = {
        ajax_par:{},//ajax传的形参实参{'a':'1'},
        css:'',
        title:'',
        treeUrl:'',
        field_name: '',
        default_nodes: '',
        setting:{
            check: {
                enable: true,
                chkboxType: {"Y": "", "N": ""}
            },
            data : {
                simpleData : {
                    enable : true,
                    idKey : "ID",
                    pIdKey : "PID"
                },
                key : {
                    name : "NAME"
                }
            },
            callback: {
                onClick: onClick
            }
        }
    };
    $.fn.openTree = function(options) {
        if (options.isMultiple) {
            defaultval.setting.check.enable = options.isMultiple;
        }
        if(options.default_nodes){
            defaultval.default_nodes = options.default_nodes;
        }
        if (options.chkboxType !== undefined && options.chkboxType != null) {
            defaultval.setting.check.chkboxType = options.schkboxType;
        }
        if (options.idKey !== undefined && options.idKey != null) {
            defaultval.setting.data.simpleData.idKey = options.idKey;
        }
        if (options.pIdKey !== undefined && options.pIdKey != null) {
            defaultval.setting.data.simpleData.pIdKey = options.pIdKey;
        }
        if (options.nameKey !== undefined && options.nameKey != null) {
            defaultval.setting.data.key.name = options.nameKey;
        }
        if (options.field_name !== undefined && options.field_name != null) {
            defaultval.field_name = options.field_name;
        }
        options = $.extend(defaultval, options);
        var id = $(this).attr("id");
        var html="<input type='text' style='"+options.css+"' name='"+id+"' onclick=\"getTree('"+id+"_open','"+options.title+"','"+options.field_name+"')\" value='' readonly autocomplete='off' class='layui-input layui_input_search'>" +
            "<input style='display:none;' name='"+options.field_name+"'>"+
            "<div id='"+id+"_open' style='display:none;'>" +
            "<div id='"+id+"Tree' class='ztree'></div>" +
            "</div>";
        $("#" + id).append(html);
        var ajax = new Ajax(options.treeUrl, function (data) {
            if (data.code == 1) {
                var nodes = data.dataset.rows;
                var openTreeNodes = [];
                if(options.default_nodes){
                    openTreeNodes = options.default_nodes.split(",");
                }
                for(var j in nodes){
                    for(var h in openTreeNodes){
                        if(nodes[j][options.idKey] == openTreeNodes[h]){
                            nodes[j].checked = true;
                        }
                    }
                }
                zTreeObj =  $.fn.zTree.init($("#"+id+"Tree"), options.setting, nodes);
                zTreeObj.expandAll(true);

                var resid = zTreeObj.setting.data.simpleData.idKey;
                var resname = zTreeObj.setting.data.key.name;
                var checkNodes = zTreeObj.getCheckedNodes(true);
                setOpenTree(id,checkNodes,resid,resname,options.field_name);
            }
        });
        if(options.ajax_par!={}){
            for(var i in options.ajax_par){
                ajax.add(i,options.ajax_par[i]);
            }
        }
        ajax.setAsync(false);
        ajax.submit();
    }

})(jQuery);

function getTree(id,title,field_name){
    var index = layer.open({
        type: 1,
        area: ['700px', '450px'],
        title:title,
        fixed: true, //不固定
        content: $('#'+id),
        btn: ['保存', '取消'],
        yes: function (index, layero) {
            var treeid=layero.find('.layui-layer-content').find("div[id$='Tree']").attr('id');
            var inputid=treeid.replace("Tree","");
            var zTreeObj =  $.fn.zTree.getZTreeObj(treeid);
            var nodes = zTreeObj.getCheckedNodes(true);
            var id="";
            var name="";
            var resid = zTreeObj.setting.data.simpleData.idKey;
            var resname = zTreeObj.setting.data.key.name;
            for(var i = 0;i < nodes.length; i++){
                i == nodes.length-1 ? id += nodes[i][resid] :  id += nodes[i][resid] + ",";
                i == nodes.length-1 ?  name+=nodes[i][resname] : name+=nodes[i][resname]+",";
            }
            $("input[name='"+inputid+"']").val(name);
            $("input[name='"+field_name+"']").val(id);
            layer.close(index);
        },
        btn2: function (index, layero) {

        }
    });

}

function setOpenTree(treeId,checkNodes,resid,resname,field_name){
    var id="";
    var name="";
    for(var i = 0;i < checkNodes.length; i++){
        i == checkNodes.length-1 ? id += checkNodes[i][resid] :  id += checkNodes[i][resid] + ",";
        i == checkNodes.length-1 ?  name+=checkNodes[i][resname] : name+=checkNodes[i][resname]+",";
    }
    $("input[name='"+treeId+"']").val(name);
    $("input[name='"+field_name+"']").val(id);
}

function onClick(event, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    zTree.checkNode(treeNode, !treeNode.checked, true);
}


