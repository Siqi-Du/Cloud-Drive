window.onload = function(){
				document.getElementsByTagName("body")[0].style.backgroundColor = '#46485f';
				setTimeout(gridIn,500);
			}
			function gridIn(){
				var grid = document.getElementById("grid");
				grid.style.left = '50%';
				grid.style.opacity = '1';
			}