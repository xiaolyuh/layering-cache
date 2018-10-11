(function ($) {

    var constant = {
        LOGIN_FORM: '#login-form',
        LOGIN_BUTTON: '#login-button',
    };

    var bindEvent = {
        bindLogin:function () {
            $(constant.LOGIN_BUTTON).on("click", function () {
                $.ajax({
                    type: 'POST',
                    url: 'user/submit-login',
                    dataType: 'JSON',
                    data: $(constant.LOGIN_FORM).serialize(),
                    success: function (data) {
                        if (data.status == "SUCCESS") {
                            window.location.href = "index.html?token=" + data.data;
                        } else {
                            alert("用户名或密码错误");
                        }
                    }
                });
            });
        }
    };

    var index = {
        init: function () {
            bindEvent.bindLogin();
        }
    };

    $(function () {
        index.init();
    });
})(jQuery);