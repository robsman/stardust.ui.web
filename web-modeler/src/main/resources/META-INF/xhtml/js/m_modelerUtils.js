/**
 * Modeler specific utility functions.
 * 
 * @author shrikant.gangal
 */
define(
		["bpm-modeler/js/m_utils"],
		function(m_utils) {
			var SCROLL_PANE_ID = "scrollpane";
			
			return {
				getModelerScrollPosition : function() {
					return getScrollpaneScrollPosition();
				}
			};

			function getScrollpaneScrollPosition() {
				var scrollPane = document.getElementById(SCROLL_PANE_ID);
				if (scrollPane) {
					return {
						top : scrollPane.scrollTop,
						left : scrollPane.scrollLeft
					}
				}
			}
		});
