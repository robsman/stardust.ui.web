/**
 * @author shrikant.gangal
 */
define(
		[ "m_utils", "m_constants", "m_dialog", "m_modelerCanvasController", "m_propertiesPanel", "m_processPropertiesPanel" ],
		function(m_utils, m_constants, m_dialog, m_modelerCanvasController, m_propertiesPanel, m_processPropertiesPanel) {
			var innerHeight;
			var innerWidth;
			var propertiesPaneVisible = false;
			var HORIZONTAL_SCROLL_OFFSET = 30;

			function initPropertiesPanelCollapseClickHandlers() {
				$("#propertiesPanelShowControl").click(function() {
					showPropertiesPane();
				});
				$("#propertiesPanelHideControl").click(function() {
					hidePropertiesPane();
				});
			}

			function initialize() {
				innerHeight = window.innerHeight;
				innerWidth = window.innerWidth;
				$("#modelerDiagramPanelWrapper").css("width",
						(innerWidth - HORIZONTAL_SCROLL_OFFSET) + "px").css(
						"overflow", "auto");
				$("#modelerPropertiesPanelWrapper").css("width", "0px").css(
						"overflow", "hidden");

				setDrawingPaneDivWidths((parseInt($("#modelerDiagramPanelWrapper").css("width")) - 10));

				m_dialog.makeVisible($("#propertiesPanelShowControl"));
				m_dialog.makeInvisible($("#propertiesPanelHideControl"));
				m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));

				initPropertiesPanelCollapseClickHandlers();
			}

			function hidePropertiesPane() {
				if (true == propertiesPaneVisible) {
					propertiesPaneVisible = false;
					$("#modelerPropertiesPanelWrapper").css("width", "0px")
							.css("overflow", "hidden");
					$("#modelerDiagramPanelWrapper").css("width",
							(innerWidth - HORIZONTAL_SCROLL_OFFSET) + "px");
				}
				
				setDrawingPaneDivWidths((parseInt($("#modelerDiagramPanelWrapper").css("width")) - 10));

				m_dialog.makeVisible($("#propertiesPanelShowControl"));
				m_dialog.makeInvisible($("#propertiesPanelHideControl"));
				m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));
			}

			function adjustPanels() {
				if (true == propertiesPaneVisible) {
					m_dialog.makeVisible($("#modelerPropertiesPanelWrapper"));
					// Collapse properties panel
					$("#modelerPropertiesPanelWrapper").css("width", "0px");
					$("#modelerDiagramPanelWrapper")
							.css(
									"width",
									(innerWidth
											- $("#modelerPropertiesPanelWrapper")[0].offsetWidth - HORIZONTAL_SCROLL_OFFSET)
											+ "px").css("overflow", "auto");

					// Expand properties panel using new values
					$("#modelerPropertiesPanelWrapper").css("width", "auto")
							.css("overflow", "auto");

					var diagWidth = innerWidth - $("#modelerPropertiesPanelWrapper")[0].offsetWidth - HORIZONTAL_SCROLL_OFFSET;
					
					if (diagWidth < getToolbarWidth()) {
						diagWidth = getToolbarWidth() + 10;
					}
					
					setDrawingPaneDivWidths((diagWidth - 10));

					$("#modelerDiagramPanelWrapper").css("width", (diagWidth) + "px").css("overflow", "auto");
				}
			}

			function showPropertiesPane() {
				if (false == propertiesPaneVisible) {
					propertiesPaneVisible = true;

					adjustPanels();
					
					m_dialog.makeVisible($("#propertiesPanelHideControl"));
					m_dialog.makeInvisible($("#propertiesPanelShowControl"));
				}
			}
			
			function getToolbarWidth() {
				return $("div.toolbar-section").last().offset().left + $("div.toolbar-section").last().width() + 20;
			}

			/*
			 * Ideally setting of toolbar / diagram pane width etc. should have been handled with width = auto / 100% etc but that
			 * didn't work in latest FF (worked in FF4) hence taking a tedious way.
			 *  
			 * */
			function setDrawingPaneDivWidths(width) {
				$("#toolbar").css("width", (width) + "px");
				$("#messagePanel").css("width", (width) + "px");
				$("#scrollpane").css("width", (width) + "px");
			}

			return {
				initialize : function(fullId) {
					initialize();
					m_modelerCanvasController.initialize(fullId, "canvas",
							5000, 1000, 'toolbar');
					m_propertiesPanel
					.initializeProcessPropertiesPanel(m_processPropertiesPanel
							.getInstance());
					this.showPropertiesPane();
				},

				reInitialize : function() {
					initialize();
				},

				showPropertiesPane : function() {
					showPropertiesPane();
				},

				hidePropertiesPane : function() {
					hidePropertiesPane();
				},

				adjustPanels : function() {
					adjustPanels();
				}
			};
		});