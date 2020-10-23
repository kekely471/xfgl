/** kit_admin-v1.0.4 MIT License By http://kit/zhengjinfan.cn */
;layui.define(["jquery", "element", "nprogress"], function (i) {
    var t = layui.jquery, e = layui.element, a = t(document), l = t(window), n = function () {
        this.config = {elem: void 0, mainUrl: "home"}, this.v = "1.0.2"
    };
    (n.fn = n.prototype).set = function (i) {
        var e = this;
        return t.extend(!0, e.config, i), e
    }, n.fn.render = function () {
        var i = this, t = i.config;
        return void 0 === t.elem ? (layui.hint().error("Tab error:请配置选择卡容器."), i) : (c._config = t, c.createTabDom(), i)
    }, n.fn.tabAdd = function (i) {
        c.tabAdd(i)
    }, n.fn.tabDelete = function (i) {
        c.tabDelete(i)
    }, n.fn.tabChange = function (i) {
        c.tabChange(i)
    };
    var layTabId = "-1";
    var c = {
        _config: {},
        _filter: "kitTab",
        _title: void 0,
        _content: void 0,
        _parentElem: void 0,
        tabDomExists: function () {
            var i = this;
            return a.find("div.kit-tab").length > 0 && (i._title = t(".kit-tab ul.layui-tab-title"), i._content = t(".kit-tab div.layui-tab-content"), !0)
        },
        createTabDom: function () {
            var i = this, e = i._config;
            if (i._parentElem = e.elem, !i.tabDomExists()) {
                var a = ['<div class="layui-tab layui-tab-card kit-tab" lay-filter="' + i._filter + '">', '<ul class="layui-tab-title">', '<li class="layui-this" lay-id="-1"><i class="layui-icon">&#xe68e;</i> 首页</li>', "</ul>", '<div class="kit-tab-tool">操作&nbsp;<i class="fa fa-caret-down"></i></div>', '<div class="kit-tab-tool-body layui-anim layui-anim-upbit">', "<ul>", '<li class="kit-item" data-target="refresh">刷新当前选项卡</li>', '<li class="kit-line"></li>', '<li class="kit-item" data-target="closeCurrent">关闭当前选项卡</li>', '<li class="kit-item" data-target="closeOther">关闭其他选项卡</li>', '<li class="kit-line"></li>', '<li class="kit-item" data-target="closeAll">关闭所有选项卡</li>', "</ul>", "</div>", '<div class="layui-tab-content">', '<div class="layui-tab-item layui-show" lay-item-id="-1"><iframe src="' + e.mainUrl + '"></iframe></div>', "</div>", "</div>"];
                t(e.elem).html(a.join("")), i._title = t(".kit-tab ul.layui-tab-title"), i._content = t(".kit-tab div.layui-tab-content");
                var l = t(".kit-tab-tool"), n = t(".kit-tab-tool-body");
                l.on("click", function () {
                    n.toggle()
                }), n.find("li.kit-item").each(function () {
                    var e = t(this), a = e.data("target");
                    e.off("click").on("click", function () {
                        var e = i._title.children("li[class=layui-this]").attr("lay-id");
                        switch (a) {
                            case"refresh":
                                var n = i._content.children("div[lay-item-id=" + e + "]").children("iframe");
                                n.attr("src", n.attr("src"));
                                break;
                            case"closeCurrent":
                                var p = i._title.children("li[class=layui-this]").prev("li").attr("lay-id");
                                if(!p){
                                    p = i._title.children("span").prev("li").attr("lay-id");
                                }
                                i.tabChange(p);
                                -1 != e && i.tabDelete(e);
                                break;
                            case"closeOther":
                                i._title.children("li[lay-id]").each(function () {
                                    var a = t(this).attr("lay-id");
                                    a != e && -1 != a && i.tabDelete(a);
                                });
                                break;
                            case"closeAll":
                                i.tabChange("-1");
                                i._title.children("li[lay-id]").each(function () {
                                    var e = t(this).attr("lay-id");
                                    -1 != e && i.tabDelete(e);
                                    layui.element.render('tab');
                                })
                        }
                        l.click()
                    })
                }), i.winResize()
            }
        },
        winResize: function () {
            var i = this;
            l.on("resize", function () {
                var e = t(i._parentElem).height();
                t(".kit-tab .layui-tab-content iframe").height(e - 45)
            }).resize()
        },
        tabExists: function (i) {
            return this._title.find("li[lay-id=" + i + "]").length > 0
        },
        tabDelete: function (i) {
            e.tabDelete(this._filter, i)
        },
        tabChange: function (i) {
            e.tabChange(this._filter, i)
        },
        getTab: function (i) {
            return this._title.find("li[lay-id=" + i + "]")
        },
        getAllTabs: function (i) {
            return this._title.find("li")
        },
        tabAdd: function (i) {
            var cl = i.class;
            var t = this,
                a = t._config,
                l = (i = i || {
                    id: (new Date).getTime(),
                    title: "新标签页",
                    icon: "fa-file",
                    url: "404.html"
                }).title,
                n = i.icon,
                c = i.url,
                s = i.id;
            if (t.tabExists(s)) {
                var i = this;
                var e = i._title.children("li[class=layui-this]").attr("lay-id");
                if(e == s){
                    var n = i._content.children("div[lay-item-id=" + s + "]").children("iframe");
                    n.attr("src", n.attr("src"));
                }else {
                    t.tabChange(s);
                }
            }
            else {
                if (t.getAllTabs().length > 10 ) {
                    layer.tips('已打开太多页面，请先关闭不必要的页面！', '.kit-tab-tool', {
                        tips: [4, '#CCC96C'],
                        tipsMore:false,
                        time:2000
                    });
                    return;
                }
                NProgress.start();
                var r = ['<li class="layui-this" lay-id="' + s + '" >'];
                -1 !== n.indexOf("fa-") ? r.push('<i class="fa ' + n + '" aria-hidden="true"></i>') : r.push('<i class="' + n + '"></i>'), r.push("&nbsp;" + l), r.push('<i class="layui-icon layui-unselect layui-tab-close">&#x1006;</i>'), r.push("</li>");
                var ti = Date.parse(new Date());
                var cc = c + "?v=" + ti;
                var o = '<div class="layui-tab-item layui-show" lay-item-id="' + s + '"><iframe src="' + cc + '"></iframe></div>';
                t._title.append(r.join("")),
                    t._content.append(o),
                    t.getTab(s).find("i.layui-tab-close").off("click").on("click", function () {
                        a.closeBefore ? a.closeBefore(i) && t.tabDelete(s) : t.tabDelete(s)
                    }),
                    t.tabChange(s),
                    t.winResize(),
                    t._content.find("div[lay-item-id=" + s + "]").find("iframe").on("load", function () {
                        NProgress.done()
                    }),
                a.onSwitch && layui.element.on("tab(" + t._filter + ")",
                    function (i) {
                        a.onSwitch({
                            index: i.index, elem: i.elem, layId: t._title.children("li").eq(i.index).attr("lay-id")
                        })

                        var e = $(this).attr("lay-id");
                        if(layTabId == e){
                            var n = $(".layui-tab-item[lay-item-id=" + e + "]").children("iframe");
                            n.attr("src", n.attr("src"));
                        }
                        layTabId = e;
                    })
            }
        }
    };
    i("tab", new n)
});