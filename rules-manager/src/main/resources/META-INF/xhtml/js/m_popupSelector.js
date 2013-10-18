/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils" ],
		function(m_utils) {

			return {
				create : function(options) {
					var popupSelector = new PopupSelector();

					popupSelector.initialize(options);

					return popupSelector;
				}
			};

			/**
			 * 
			 */
			function PopupSelector() {
				/**
				 * 
				 */
				PopupSelector.prototype.initialize = function(options) {
					this.options = options;
					this.dialog = m_utils.jQuerySelect("#" + this.options.anchor);
					this.itemList = jQuery("<ul class='popupSelectorItemList'></ul>");

					this.dialog.append(this.itemList);
					this.dialog.dialog({
						autoOpen : false,
						draggable : false,
						model : true,
						resizable : false,
						title : "<div></div>"
					});

					// Hide titlebar for popup selectors
					
					m_utils.jQuerySelect(this.dialog.parent().children(".ui-dialog-titlebar")).hide();
				};

				/**
				 * 
				 */
				PopupSelector.prototype.setEventSource = function(eventSource) {
					if (this.eventSource) {
						this.eventSource.unbind('clickoutside');
					}

					this.eventSource = eventSource;

					this.eventSource.bind('clickoutside', {
						dialog : this.dialog
					}, function(event) {
						event.data.dialog.dialog("close");
					});
				};

				/**
				 * 
				 */
				PopupSelector.prototype.open = function(event) {
					this.dialog.dialog("open");
					this.dialog.dialog("option", "position", {
						my : "left top",
						at : "left bottom",
						of : event
					});
				};

				/**
				 * 
				 */
				PopupSelector.prototype.clearItems = function(item, data,
						callback) {
					this.itemList.empty();
				};

				/**
				 * 
				 */
				PopupSelector.prototype.addItems = function(items) {
					if (items) {
						for ( var i = 0; i < items.length; ++i) {
							this.addItem(items[i].label, items[i].data,
									items[i].callback);
						}
					}
				};

				/**
				 * 
				 */
				PopupSelector.prototype.addItem = function(label, data,
						callback) {
					var link = jQuery("<a class='popupSelectorItem'>" + label
							+ "</li>");

					link.click({
						selector : this
					}, function(event) {
						callback(data);
						event.data.selector.dialog.dialog("close");
					});

					var wrapper = jQuery("<li></li>");

					this.itemList.append(wrapper);
					wrapper.append(link);
				};
			}
		});