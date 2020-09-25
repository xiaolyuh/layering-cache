(function ($) {

    var constant = {
        DELETE_CACHE_BUTTON: '.bind-delete-cache-button',
        RESET_STATS_BUTTON: '.bind-reset-stats-button',
        SEARCH_BUTTON: '.bind-search-button',
        REDIS_CONFIG_FORM: '#redis-config-form',
        REDIS_CONFIG_BUTTON: '#redis-config-button',
        REDIS_SELECT: '#bind-redis-select option:selected',
        SEARCH_INPUT: '.bind-search-input',
        DELETE_PROMPT: '#delete-prompt',
        DELETE_CACHE_KEYINPUT: '#delete-cache-key-input',
        RESET_CONFIRM: '#reset-confirm',
        DELETE_CONFIRM: '#delete-confirm',
        DETAIL_MODAL: '#detail-modal',
        LOGIN_OUT: '#login-out',
    };

    var viewModel = {
        cacheStats: ko.observableArray([]),
        redisList: ko.observableArray([]),
        detailCacheStats: ko.observable({}),
        fcs: ko.observable({}),
        scs: ko.observable({}),
    };

    var token = {
        getToken: function () {
            var url = location.search;
            if (url.indexOf("?") != -1) {
                var str = url.substr(1);
                return str.split("=")[1];
            }
        }
    };

    var bindEvent = {
        bindResetStats: function () {
            $(constant.RESET_STATS_BUTTON).on("click", function () {
                $(constant.RESET_CONFIRM).modal({
                    relatedTarget: this,
                    onConfirm: function (options) {
                        $.ajax({
                            type: 'POST',
                            url: 'cache-stats/reset-stats',
                            dataType: 'JSON',
                            data: {"redisClient": $(constant.REDIS_SELECT).val(), "token": token.getToken()},
                            success: function (data) {
                                if (data.status == "SUCCESS") {
                                    bindEvent.getData();
                                } else {
                                    alert(data.message);
                                }
                            },
                            error: function () {
                                window.location.href = "index.html";
                            }
                        });
                    },
                    // closeOnConfirm: false,
                    onCancel: function () {

                    }
                });
            });
        },
        bindLoginOut: function () {
            $(constant.LOGIN_OUT).on("click", function () {
                $.ajax({
                    type: 'POST',
                    url: 'user/login-out',
                    dataType: 'JSON',
                    data: {"token": token.getToken()},
                    success: function (data) {
                        if (data.status == "SUCCESS") {
                            window.location.href = "/toLogin";
                        } else {
                            alert("操作失败");
                        }
                    },
                    error: function () {
                        window.location.href = "/toLogin";
                    }
                });
            });
        },
        searchData: function () {
            $(constant.SEARCH_BUTTON).on("click", function () {
                bindEvent.getData();
            });
        },
        redisConfig: function () {
            $(constant.REDIS_CONFIG_BUTTON).on("click", function () {
                $.ajax({
                    type: 'POST',
                    url: 'redis/redis-config',
                    dataType: 'JSON',
                    data: $(constant.REDIS_CONFIG_FORM).serialize(),
                    success: function (data) {
                        if (data.status == "SUCCESS") {
                            bindEvent.redisList();
                        } else {
                            alert(data.message);
                        }
                    }
                });
            });
        },
        redisList: function () {
            $.ajax({
                type: 'POST',
                url: 'redis/redis-list',
                dataType: 'JSON',
                data: {"token": token.getToken()},
                success: function (data) {
                    if (data.status == "SUCCESS") {
                        viewModel.redisList(data.data);
                    } else {
                        alert(data.message);
                    }
                }
            });
        },
        getData: function () {

            var data = {"code":"200","data":[{"cacheName":"people1","depict":"描述","firstCacheMissCount":1,"firstCacheRequestCount":2,"hitRate":50.0,"internalKey":"4000-100000-3000","layeringCacheSetting":{"depict":"æŸ¥è¯¢ç”¨æˆ·ä¿¡æ¯1","firstCacheSetting":{"allowNullValues":true,"expireMode":"WRITE","expireTime":4,"initialCapacity":10,"maximumSize":5000,"timeUnit":"SECONDS"},"internalKey":"4000-100000-3000","secondaryCacheSetting":{"allowNullValues":true,"expiration":100,"forceRefresh":true,"preloadTime":3,"timeUnit":"SECONDS","usePrefix":true},"useFirstCache":true},"missCount":1,"requestCount":2,"secondCacheMissCount":1,"secondCacheRequestCount":1,"totalLoadTime":52}],"message":"SUCCESS","status":"SUCCESS"};
            var temp = ko.mapping.fromJS(data.data);
            format.formatInit(temp());
            viewModel.cacheStats(temp());
            $(constant.SEARCH_BUTTON).attr("disabled", "true");
            $(constant.RESET_STATS_BUTTON).attr("disabled", "true");
            $(constant.DELETE_CACHE_BUTTON).attr("disabled", "true");
            $.ajax({
                type: 'POST',
                url: 'cache-stats/list',
                dataType: 'JSON',
                data: {"redisClient": $(constant.REDIS_SELECT).val(), "cacheName": $(constant.SEARCH_INPUT).val(), "token": token.getToken()},
                success: function (data) {
                    $(constant.SEARCH_BUTTON).removeAttr("disabled");
                    $(constant.RESET_STATS_BUTTON).removeAttr("disabled");
                    $(constant.DELETE_CACHE_BUTTON).removeAttr("disabled");

                    var temp = ko.mapping.fromJS(data.data);
                    format.formatInit(temp());
                    viewModel.cacheStats(temp());
                },
                error: function () {
                    $(constant.SEARCH_BUTTON).removeAttr("disabled");
                    $(constant.RESET_STATS_BUTTON).removeAttr("disabled");
                    $(constant.DELETE_CACHE_BUTTON).removeAttr("disabled");
                    window.location.href = "/toLogin";
                }
            });
        }
    };

    var format = {
        formatInit: function (cacheStats) {
            $.each(cacheStats, function (i, cs) {
                if (cs.hitRate()) {
                    cs.hitRate = cs.hitRate().toFixed(2) + "%";
                } else {
                    cs.hitRate = (0).toFixed(2) + "%";
                }

                if (cs.firstCacheRequestCount() > 0) {
                    cs.firstHitRate = ((cs.firstCacheRequestCount() - cs.firstCacheMissCount()) / cs.firstCacheRequestCount() * 100).toFixed(2) + "%";
                } else {
                    cs.firstHitRate = (0).toFixed(2) + "%";
                }

                if (cs.secondCacheRequestCount() > 0) {
                    cs.secondHitRate = ((cs.secondCacheRequestCount() - cs.secondCacheMissCount()) / cs.secondCacheRequestCount() * 100).toFixed(2) + "%";
                } else {
                    cs.secondHitRate = (0).toFixed(2) + "%";
                }

                if (cs.missCount() > 0) {
                    cs.averageTotalLoadTime = (cs.totalLoadTime() / cs.missCount()).toFixed(2) + "毫秒";
                } else {
                    cs.averageTotalLoadTime = (0).toFixed(2) + "毫秒";
                }

                cs.deleteCache = function () {
                    $(constant.DELETE_PROMPT).modal({
                        relatedTarget: this,
                        onConfirm: function (e) {
                            $(constant.DELETE_CONFIRM).modal({
                                relatedTarget: this,
                                onConfirm: function (options) {
                                    $.ajax({
                                        type: 'POST',
                                        url: 'cache-stats/delete-cache',
                                        dataType: 'JSON',
                                        data: {
                                            "cacheName": cs.cacheName(),
                                            "internalKey": cs.internalKey(),
                                            "nameSpace": cs.nameSpace(),
                                            "key": $(constant.DELETE_CACHE_KEYINPUT).val(),
                                            "redisClient": $(constant.REDIS_SELECT).val(),
                                            "token": token.getToken()
                                        },
                                        success: function (data) {
                                            if (data.status == "SUCCESS") {
                                                $(constant.DELETE_CACHE_KEYINPUT).val("");
                                                bindEvent.getData();
                                            } else {
                                                alert(data.message);
                                            }
                                        },
                                        error: function () {
                                            window.location.href = "index.html";
                                        }
                                    });
                                },
                                // closeOnConfirm: false,
                                onCancel: function () {
                                    $(constant.DELETE_CACHE_KEYINPUT).val("");
                                }
                            });
                        },
                        onCancel: function (e) {
                            $(constant.DELETE_CACHE_KEYINPUT).val("");
                        }
                    });
                }

                cs.detail = function () {
                    viewModel.detailCacheStats(cs);
                    viewModel.fcs(cs.layeringCacheSetting.firstCacheSetting);
                    viewModel.scs(cs.layeringCacheSetting.secondaryCacheSetting);
                    $(constant.DETAIL_MODAL).modal({
                        relatedTarget: this,
                        width: 1000,
                        onConfirm: function (options) {

                        },
                        // closeOnConfirm: false,
                        onCancel: function () {

                        }
                    });
                }
            });
        }
    };

    var index = {
        init: function () {
            ko.applyBindings(viewModel);
            bindEvent.searchData();
            bindEvent.redisConfig();
            bindEvent.bindResetStats();
            bindEvent.bindLoginOut();
            bindEvent.redisList();
        }
    };

    $(function () {
        index.init();
    });
})(jQuery);