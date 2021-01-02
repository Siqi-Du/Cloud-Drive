var divs = document.getElementById("gridPanel").children;
var btns = document.getElementsByTagName("button");
var aboutme = document.getElementsByClassName("aboutme");
window.onload = function(){
	setInterval(times,1000);
	aboutMeDis();
}
function aboutMeDis(){
	aboutme[0].className = "spanBefore aboutme dis";
	aboutme[1].className = "spanAfter aboutme dis";
}
function aboutMeHide(){
	aboutme[0].className = "spanBefore aboutme";
	aboutme[1].className = "spanAfter aboutme";	
}
//function times(){
//	var now = new Date();
//	var birth = new Date("1994/01/06");
//	var ms = parseInt((now.getTime() - birth.getTime())/1000);
//	var day = parseInt(ms/86400);
//	var hour = parseInt(ms%86400/3600);
//	var min = parseInt(ms%86400%3600/60);
//	var sec = parseInt(ms%86400%3600%60);
//	var times = document.getElementById("times");
////	times.innerHTML = `${day}天${hour}小时${min}分钟${sec}秒`;
//}
// 首页消失方法
function leave(nod1,nod2) {
    nod1.className = "content opa";
    nod1.style.left = '-50%';
    nod2.className = "content opa";
    nod2.style.left = '100%';
    aboutMeHide();
}
// 分页面显示
function back(nod1,nod2){
    nod1.style.left = '0';
    nod1.className = "content";
    nod2.style.left = '50%';
    nod2.className = "content";
}
// 首页到About页面
btns[0].onclick = function() {
    leave(divs[0],divs[1]);
    setTimeout(runAbout, 500);
}
function runAbout() {
	back(divs[2],divs[3]);
}
// 首页到Work页面
btns[1].onclick = function() {
    leave(divs[0],divs[1]);
    setTimeout(runWork, 500);
}
function runWork() {
	back(divs[4],divs[5]);
}
// 首页到Contact页面
btns[2].onclick = function() {
    leave(divs[0],divs[1]);
    setTimeout(runContact, 500);
}

function runContact() {
    back(divs[6],divs[7]);
}

// 从分页面到首页
for(var i = 3 ; i < btns.length ; i ++){
	btns[i].onclick = runHome;
}
function runHome() {
	leave(this.parentNode.previousElementSibling,this.parentNode);
    setTimeout(runBack, 500);
    setTimeout(aboutMeDis,1000);
}
function runBack() {
    back(divs[0],divs[1]);
}

// Work页面到首页
// btns[4].onclick = function() {
//     divs[4].style.left = '-50%';
//     divs[4].className = "content opa";
//     divs[5].style.left = "100%";
//     divs[5].className = "content opa";
//     setTimeout(runBack, 500);
// }

// 从contact页面到首页
// btns[5].onclick = function() {
//     divs[6].style.left = '-50%';
//     divs[6].className = "content opa";
//     divs[7].style.left = "100%";
//     divs[7].className = "content opa";
//     setTimeout(runBack, 500);
// }