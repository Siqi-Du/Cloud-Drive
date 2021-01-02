$("#nav>li:lt(2)").click(function(event) {
			if ($(this).html() == "登录") {
				if (!$("#zhu").hasClass('formIn')) {
					$("#log").toggleClass('formIn');
				}
			}
			else {
				if (!$("#log").hasClass('formIn')) {
					$("#zhu").toggleClass('formIn');
				}
			}
		});
		$("span.close").click(function(event) {
			$(this).parent().removeClass('formIn');
		});
		$("form.fm>p:last-child>b").click(function(event) {
			$(this).parent().parent().parent().removeClass('formIn').next("div").addClass('formIn');
		});
		$("form.fm>p:last-child>i").click(function(event) {
			$(this).parent().parent().parent().removeClass('formIn').prev("div").addClass('formIn');
		});
		function valiName(){
			var bool = vali(this,/^\w{1,10}$/);
			if (bool) {
				div.className = "vali_success";
				return true;
			}
			else {
				div.className = "vali_fail";
				return false;
			}
		}
		function vali(txt,reg){
			return reg.test(txt.value);
		}
		function valiPwd(){
			var bool = vali(this,/^\d{6}$/);
			if (bool) {
				div.className = "vali_success";
				return true;
			}
			else {
				div.className = "vali_fail";
				return false;
			}
		}