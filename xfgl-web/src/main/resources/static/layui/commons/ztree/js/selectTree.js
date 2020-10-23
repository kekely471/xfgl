function initSelectTree(id, isMultiple,zNodes,chkboxType,idKey,pIdKey,nameKey) {
    //id div 的id，isMultiple 是否多选，chkboxType 多选框类型{"Y": "ps", "N": "s"} 详细请看ztree官网
    var setting = {
        view: {
            dblClickExpand: false,
            showLine: false
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "ID",
                pIdKey: "PID"
            },
            key: {
                name: "NAME"
            }
        },
        check: {
            enable: false,
            chkboxType: {"Y": "ps", "N": "s"}
        },
        callback: {
            onClick: onClick,
            onCheck: onCheck
        }

    };
    if (isMultiple) {
        setting.check.enable = isMultiple;
    }
    if (chkboxType !== undefined && chkboxType != null) {
        setting.check.chkboxType = chkboxType;
    }
    if (idKey !== undefined && idKey != null) {
        setting.data.simpleData.idKey = idKey;
    }
    if (pIdKey !== undefined && pIdKey != null) {
        setting.data.simpleData.pIdKey = pIdKey;
    }
    if (nameKey !== undefined && nameKey != null) {
        setting.data.key.name = nameKey;
    }
    var html = '<div class = "layui-select-title" >' +
        '<input id="' + id + 'Show" type = "text" placeholder = "请选择" value = "" class = "layui-input" readonly>' +
        '<i class= "layui-edge" ></i>' +
        '</div>';
    $("#" + id).append(html);
    $("#" + id).parent().append('<div class="tree-content scrollbar">' +
        '<input hidden id="' + id + 'Hide" ' +
        'name="' + id + '">' +
        '<ul id="' + id + 'Tree" class="ztree scrollbar" style="margin-top:0;"></ul>' +
        '</div>');
    $("#" + id).bind("click", function () {
        if($(this).children().children().attr("disabled")){
            return;
        }
        if ($(this).parent().find(".tree-content").css("display") !== "none") {
            hideMenu()
        } else {
            $(this).addClass("layui-form-selected");
            var Offset = $(this).offset();
            var width = $(this).width() - 2;
            $(this).parent().find(".tree-content").css({
                left: Offset.left + "px",
                top: Offset.top + $(this).height() + "px"
            }).slideDown("fast");
            $(this).parent().find(".tree-content").css({
                width: width
            });
            $("body").bind("mousedown", onBodyDown);
        }
    });
    $.fn.zTree.init($("#" + id + "Tree"), setting, zNodes);
}

function beforeClick(treeId, treeNode) {
    var check = (treeNode && !treeNode.isParent);
    if (!check) alert("只能选择城市...");
    return check;
}

function onClick(event, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    if (zTree.setting.check.enable == true) {
        zTree.checkNode(treeNode, !treeNode.checked, false)
        assignment(treeId, zTree.getCheckedNodes());
    } else {
        assignment(treeId, zTree.getSelectedNodes());
        hideMenu();
    }
}

function onCheck(event, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    assignment(treeId, zTree.getCheckedNodes());
}

function hideMenu() {
    $(".select-tree").removeClass("layui-form-selected");
    $(".tree-content").fadeOut("fast");
    $("body").unbind("mousedown", onBodyDown);
}

function assignment(treeId, nodes) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    var names = "";
    var ids = "";
    var resId = zTree.setting.data.simpleData.idKey;
    var resName = zTree.setting.data.key.name;
    for (var i = 0, l = nodes.length; i < l; i++) {
        names += nodes[i][resName] + ",";
        ids += nodes[i][resId] + ",";
    }
    if (names.length > 0) {
        names = names.substring(0, names.length - 1);
        ids = ids.substring(0, ids.length - 1);
    }
    treeId = treeId.substring(0, treeId.length - 4);
    $("#" + treeId + "Show").attr("value", names);
    $("#" + treeId + "Show").attr("title", names);
    $("#" + treeId + "Hide").attr("value", ids);
}

function onBodyDown(event) {
    if ($(event.target).parents(".tree-content").html() == null) {
        hideMenu();
    }
}