$(function () {
    var flg = {
        loginFlag:true,
        inProgress:false,
        nameValid: -1,
        passValid: -1
    };

    switchLoginSignup(flg.loginFlag);

    $('#logint').click(function () {
        if (flg.inProgress)
            return;

        resetFields();

        flg.loginFlag = !flg.loginFlag;

        switchLoginSignup(flg.loginFlag);
    });

    $("#name").keyup(validateForm());
    $("#password").keyup(validateForm);
    $("#confirmpassword").keyup(validateForm);

    function validateForm() {
        var valid = true;
        var err = null;

        var name = $('#name');

        var len = name.val().length;

        if (len > 13 || len == 0) {
            if (len != 0) {
                name.css('background', 'rgb(255, 214, 190)');
                err = "ID: Too long"
            }
            else
                name.css('background', 'rgb(255, 255, 255)');

            valid = false;
        }
        else
            name.css('background', 'rgb(255, 255, 255)');

        var pass = $('#password');

        len = pass.val().length;

        if (len > 10 || len == 0) {
            if (len > 10) {
                pass.css('background', 'rgb(255, 214, 190)');
                err = "Password: Too long";
            }
            else
                pass.css('background', 'rgb(255, 255, 255)');

            valid = false;
        }
        else
            pass.css('background', 'rgb(255, 255, 255)');

        if (!flg.loginFlag) {
            var confPass = $('#confirmpassword');

            len = confPass.val().length;

            if (len > 10 || len == 0) {
                if (len > 10) {
                    confPass.css('background', 'rgb(255, 214, 190)');
                    err = 'Confirm Password: Too long';
                }
                else
                    confPass.css('background', 'rgb(255, 255, 255)');

                valid = false;
            }
            else
                confPass.css('background', 'rgb(255, 255, 255)');

            if (valid) {
                var passVal = pass.val();
                var confPassVal = confPass.val();

                if (passVal.length > 0 && confPassVal.length > 0 && !(passVal === confPassVal)) {
                    err = "Passwords do not match";
                    valid = false;
                }
            }
        }

        flg.valid = valid;

        if (err != null)
            $('#err').css('color', 'rgb(255, 57, 19)').text(err).fadeIn();
        else
            $("#err").fadeOut();

        if (valid)
            $('#signupb').css('opacity', '1').css('cursor', 'pointer');
        else
            $('#signupb').css('opacity', '0.2').css('cursor', 'default');
    }

    $('#signupb').click(function () {
        if (flg.valid && !flg.inProgress) {
            $('#name, #password, #logint, #err, #signupb').css('opacity', '0.2');

            flg.inProgress = true;

            loginOrSignup();
        }
    });

    function resetFields() {
        flg.valid = true;
        $('#err').hide();
        $('#name, #password, #logint, #err, #signupb').css('opacity', '1');
        $('#name').css('background', 'rgb(255, 255, 255)');
        $('#password').css('background', 'rgb(255, 255, 255)');
        $('#signupb').css('opacity', '0.2').css('cursor', 'default');
        $('#name, #password, #confirmpassword').val('')
    }

    function switchLoginSignup(login) {
        if (!login) {
            $('#signup').text('Sign up');
            $('#signupb').text('Sign up');
            $('#confirmpassword').fadeIn();
            $('#logint').text('Login as an existing user')
        } else {
            $('#signup').text('Login');
            $('#signupb').text('Login');
            $('#confirmpassword').fadeOut();
            $('#logint').text('Sign up as a new user')
        }
    }

    function loginOrSignup() {
        var formData = {login:$("#name").val(), password:$("#password").val(), confirmPassword:$("#confirmpassword").val()};
        var url = flg.loginFlag == 1 ? "login.do" : "users/register.do";

        $.ajax({
            url : url,
            type: "POST",
            data : formData,
            success: function(data, textStatus, jqXHR) {
                flg.inProgress = false;

                if (data.success) {
                    // Logged in, redirect to home page.
                    window.location.replace("home.html");
                }
                else {
                    $('#name, #password, #err, #logint, #signupb').css('opacity', '1');

                    $("#err").css('color', 'rgb(255, 57, 19)').text(data.err).fadeIn();
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#name, #password, #logint, #err, #signupb').css('opacity', '1');

                $("#err").text("Unexpected error occurred: " + errorThrown).fadeIn();

                flg.inProgress = false;
            }
        });
    }
});