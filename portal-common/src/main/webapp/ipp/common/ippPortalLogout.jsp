<%@ page session="false" contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>

<html>
	<head>
		<script lang="javascript">
			function logout() {
				var href = window.location.href;
				href = href.substr(0, href.indexOf("/ipp/common/ippPortalLogout.jsp"));
				
				var win = window;
	
				try {
					// window has parent
					if (window.parent != window) {
						var pHref = window.parent.location.href;
						if (pHref.indexOf("?") > -1) {
							pHref = pHref.substr(0, pHref.indexOf("?"));
						}
	
						if (pHref.indexOf("#") > -1) {
							pHref = pHref.substr(0, pHref.indexOf("#"));
						}
						
						if ((href + "/main.html") == pHref) {
							win = window.parent;
						}
					}					
				} catch (e) {
					// Error in getting parent. May not be accessible
				}
	
				if (href != "" && href != win.location.href) {
					win.location.replace(href);
				}
			}
		</script>
	</head>
	<body onload="logout();">
	</body>
</html>
