(function ($) {

    var constant = {
        MENU_CSS: '.bind-menu-syndrome-search',
        RESET_STATS_BUTTON: '.bind-reset-stats-button',
        SEARCH_BUTTON: '.bind-search-button',
        SEARCH_INPUT: '.bind-search-input',
    };

    var viewModel = {
        cacheStats: ko.observableArray([]),
        itemCount: ko.observable(1)
    };

    var bindEvent = {
        bindMenuCss: function () {
            $('.list-group-item-success')
                .removeClass('list-group-item-success');
            $(constant.MENU_CSS).addClass('list-group-item-success');
        },
        bindResetStats: function () {
            $(constant.RESET_STATS_BUTTON).on("click", function () {
                if (confirm("您确认需要重置缓存统计数据吗？")) {
                    $.ajax({
                        type: 'POST',
                        url: 'cache-stats/reset-stats',
                        dataType: 'JSON',
                        success: function (data) {
                            alert("重置缓存统计数据成功");
                        }
                    });
                }

            });
        },
        searchData:function () {
            $(constant.SEARCH_BUTTON).on("click", function () {
                bindEvent.getData();
            });
        },
        getData: function () {
            $.ajax({
                type: 'POST',
                url: 'cache-stats/list',
                dataType: 'JSON',
                data: {"cacheName": $(constant.SEARCH_INPUT).val()},
                success: function (data) {
                    var temp = ko.mapping.fromJS(data);
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