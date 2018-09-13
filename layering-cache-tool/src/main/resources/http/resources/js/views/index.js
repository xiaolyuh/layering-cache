(function ($) {

    var constant = {
        MENU_CSS: '.bind-menu-syndrome-search',
        RESET_STATS_BUTTON: '.bind-reset-stats-button',
        SEARCH_BUTTON: '.bind-search-button',
        SEARCH_INPUT: '.bind-search-input',
        DELETE_PROMPT: '#delete-prompt',
        DELETE_CACHE_KEYINPUT: '#delete-cache-key-input',
        CONFIRM: '#my-confirm',
        DETAIL_MODAL: '#detail-modal',
    };

    var viewModel = {
        cacheStats: ko.observableArray([]),
        detailCacheStats: ko.observable({}),
        fcs: ko.observable({}),
        scs: ko.observable({}),
    };

    var bindEvent = {
        bindMenuCss: function () {
            $('.list-group-item-success')
                .removeClass('list-group-item-success');
            $(constant.MENU_CSS).addClass('list-group-item-success');
        },
        bindResetStats: function () {
            $(constant.RESET_STATS_BUTTON).on("click", function () {
                $(constant.CONFIRM).modal({
                    relatedTarget: this,
                    onConfirm: function(options) {
                        $.ajax({
                            type: 'POST',
                            url: 'cache-stats/reset-stats',
                            dataType: 'JSON',
                            success: function (data) {
                                bindEvent.getData();
                            }
                        });
                    },
                    // closeOnConfirm: false,
                    onCancel: function() {

                    }
                });
            });
        },
        searchData:function () {
            $(constant.SEARCH_BUTTON).on("click", function () {
                bindEvent.getData();
            });
        },
        getData: function () {

            // var data = {"code":"200","data":[{"cacheName":"people1","depict":"æŸ¥è¯¢ç”¨æˆ·ä¿¡æ¯1","firstCacheMissCount":1,"firstCacheRequestCount":2,"hitRate":50.0,"internalKey":"4000-100000-3000","layeringCacheSetting":{"depict":"æŸ¥è¯¢ç”¨æˆ·ä¿¡æ¯1","firstCacheSetting":{"allowNullValues":true,"expireMode":"WRITE","expireTime":4,"initialCapacity":10,"maximumSize":5000,"timeUnit":"SECONDS"},"internalKey":"4000-100000-3000","secondaryCacheSetting":{"allowNullValues":true,"expiration":100,"forceRefresh":true,"preloadTime":3,"timeUnit":"SECONDS","usePrefix":true},"useFirstCache":true},"missCount":1,"requestCount":2,"secondCacheMissCount":1,"secondCacheRequestCount":1,"totalLoadTime":52}],"message":"SUCCESS","status":"SUCCESS"};
            // var temp = ko.mapping.fromJS(data.data);
            // format.formatInit(temp());
            // viewModel.cacheStats(temp());

            $.ajax({
                type: 'POST',
                url: 'cache-stats/list',
                dataType: 'JSON',
                data: {"cacheName": $(constant.SEARCH_INPUT).val()},
                success: function (data) {
                    var temp = ko.mapping.fromJS(data.data);
                    format.formatInit(temp());
                    viewModel.cacheStats(temp());
                }
            });
        }
    };

    var format = {
        formatInit: function (cacheStats) {
            $.each(cacheStats, function (i, cs) {
                cs.hitRate = cs.hitRate().toFixed(2) + "%";
                cs.firstHitRate = ((cs.firstCacheRequestCount() - cs.firstCacheMissCount()) / cs.firstCacheRequestCount() * 100 ).toFixed(2) + "%";
                cs.secondHitRate = ((cs.secondCacheRequestCount() - cs.secondCacheMissCount()) / cs.secondCacheRequestCount() * 100 ).toFixed(2) + "%";
                cs.averageTotalLoadTime = (cs.totalLoadTime() / cs.requestCount()).toFixed(2) + "毫秒";

                cs.deleteCache = function () {
                    $(constant.DELETE_PROMPT).modal({
                        relatedTarget: this,
                        onConfirm: function(e) {
                            $(constant.CONFIRM).modal({
                                relatedTarget: this,
                                onConfirm: function(options) {
                                    $.ajax({
                                        type: 'POST',
                                        url: 'cache-stats/delete-cache',
                                        dataType: 'JSON',
                                        data: {"cacheName": cs.cacheName(),"internalKey": cs.internalKey(),"key": $(constant.DELETE_CACHE_KEYINPUT).val()},
                                        success: function (data) {
                                            $(constant.DELETE_CACHE_KEYINPUT).val("");
                                            bindEvent.getData();
                                        }
                                    });
                                },
                                // closeOnConfirm: false,
                                onCancel: function() {
                                    $(constant.DELETE_CACHE_KEYINPUT).val("");
                                }
                            });
                        },
                        onCancel: function(e) {
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
                        onConfirm: function(options) {

                        },
                        // closeOnConfirm: false,
                        onCancel: function() {

                        }
                    });
                }
            });
        }
    };

    var index = {
        init: function () {
            ko.applyBindings(viewModel);
            bindEvent.bindMenuCss();
            bindEvent.getData();
            bindEvent.searchData();
            bindEvent.bindResetStats();
        }
    };

    $(function () {
        index.init();
    });
})(jQuery);