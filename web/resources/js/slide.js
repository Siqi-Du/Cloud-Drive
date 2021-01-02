var count = 0;
var isgo = false;
var timer;
var wid = window.screen.width;

window.onload = function(){
	var ul_img = document.getElementsByClassName("ul_img")[0];
	var li_img = document.getElementsByClassName("li_img");
	var arrow = document.getElementsByClassName("arrow");
	var div_btn = document.getElementsByClassName("div_btn");

	for(var w = 0 ; w < li_img.length ; w ++){
		li_img[w].style.width = wid;
	}

	showtime();
	function showtime(){
		timer = setInterval(function(){
			if (isgo == false) {
				count ++;
				ul_img.style.transform = "translate("+ -wid * count + "px)";
				if (count >= li_img.length - 1) {
					count = li_img.length - 1;
					isgo = true;
				}
			}
			else {
				count --;
				ul_img.style.transform = "translate("+ -wid* count + "px)";
				if (count <= 0) {
					count = 0;
					isgo = false;
				}
			}
			for(var i = 0 ; i < div_btn.length ; i ++){
				div_btn[i].style.backgroundColor = "aquamarine";
			}
			div_btn[count].style.backgroundColor = 'aqua';
		},4000)
	}

	for(var i = 1 ; i < arrow.length ; i ++){
		arrow[i].onmouseover = function(){
			clearInterval(timer);
		}

		arrow[i].onmouseout = function(){
			showtime();
		}

		arrow[i].onclick = function(){
			if (this.title == 0) {
				count ++;
				if(count > 3){
					count = 0;
				}
			}
			else {
				count --;
				if (count < 0) {
					count = 3;
				}
			}

			ul_img.style.transform = "translate("+ -wid * count +"px)";
			for(var i = 0 ; i < div_btn.length ; i ++){
				div_btn[i].style.backgroundColor = 'aquamarine';
			}
			div_btn[count].style.backgroundColor = 'aqua';
		}
	}

	for(var d = 0 ; d < div_btn.length ; d ++){
		div_btn[d].index = d ;
		div_btn[d].onmouseover = function(){
			clearInterval(timer);
			for(var a = 0 ; a < div_btn.length ; a ++){
				div_btn[a].style.backgroundColor = 'aquamarine';
			}
			this.style.backgroundColor = 'aqua';
			if (this.index == 3) {
				isgo = true;
			}
			if (this.index == 0) {
				isgo = false;
			}
			count = this.index;
			ul_img.style.transform = 'translate('+ -wid * count + "px)";
		}
		div_btn[d].onmouseout = function(){
			showtime();
		}
	}
}