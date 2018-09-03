(function ($) {

    var constant = {
        MENU_CSS: '.bind-menu-syndrome-search',
        ADD_BUTTON: '.bind-search-add-button',
        REMOVE_BUTTON: '.bind-search-remove-button',
        SYMPTOM_NAME_SELECT: '.bind-search-symptom-name-select'
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
        initData: function () {
            $.ajax({
                type: 'POST',
                url: 'cache-stats/list',
                dataType: 'JSON',
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
                cs.hitRate = ((cs.requestCount() - cs.missCount()) / cs.requestCount() * 100 ).toFixed(2) + "%";
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
            bindEvent.initData();
        }
    };

    $(function () {
        index.init();
    });
})(jQuery);