/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author subodh.godbole
 */

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.UIMashupController) {
	bpm.portal.UIMashupController = function UIMashupController() {

		var ctrl = new bpm.portal.GenericController();
		jQuery.extend(this, ctrl);
		inheritMethods(this, ctrl);

		var self = this;

		/*
		 * Not easy to override method in JS and still keep super method
		 * So defining new method
		 */
		UIMashupController.prototype.init = function(i18nProvider) {
			// Delete mobile markup if desktop and vice versa
			//  as having both at the same time causes issues.
			if (isMobile) {
				jQuery(".hideIfMobile").remove();
			} else {
				jQuery(".hideIfDesktop").remove();
			}

			var clientDateFormat = "yy-mm-dd";

			var metaData = jQuery(".metaData");

			var dataMappings = metaData.attr("data-dataMappings");
			dataMappings = JSON.parse(dataMappings);
			this.log("Found Data Mappings " + dataMappings);

			var bindings = metaData.attr("data-binding");
			bindings = JSON.parse(bindings);
			this.log("Found Binding " + bindings);

			var nestedMarkupsDetails = {};
			jQuery.each(jQuery(".nestedMarkups").children(), function(i, elem) {
				var element = jQuery(elem);
				var xPath = element.attr("data-xpath");
				var binding = element.attr("data-binding");
				var markup = element.html();
				nestedMarkupsDetails[xPath] = {binding: binding, markup: markup};
				self.log("Found Nested Markup for " + xPath);
			});
			this.nestedMarkupsDetails = nestedMarkupsDetails;
			jQuery(".nestedMarkups").html("");

			var i18nLabelProvider = i18nProvider != undefined ? i18nProvider : this.i18nProvider();
			this.interactionProvider = new bpm.portal.Interaction();
			this.initialize(dataMappings, bindings, clientDateFormat, i18nLabelProvider, this.interactionProvider, this.nestedMarkupProvider());

			this.runInAngularContext(function($scope){
				jQuery.extend($scope, self);
				inheritMethods($scope, self);
			});
		};

		/*
		 *
		 */
		UIMashupController.prototype.getContextRootUrl = function() {
			return this.interactionProvider.isModelerMode() ? "../../../../../../../.." : "../../../../..";
		};

		/*
		 *
		 */
		UIMashupController.prototype.i18nProvider = function() {
			return {
				getLabel : getLabel
			};

			/*
			 *
			 */
			function getLabel(val, defaultValue) {
				var ret = val;
				if (defaultValue != undefined && defaultValue != null) {
					ret = defaultValue;
				}
				return ret;
			}
		};

		/*
		 *
		 */
		UIMashupController.prototype.nestedMarkupProvider = function() {
			return {
				getMarkup : getMarkup
			};

			/*
			 *
			 */
			function getMarkup(path, prefix, i18nProvider, ignoreParentXPath, formName) {
				var markupDetails = self.nestedMarkupsDetails[path.fullXPath];

				var markup = markupDetails.markup;
				markup = markup.replace(/BINDING\['REPLACEME'\]/g, prefix);
				markup = markup.replace(/BINDING\[\\'REPLACEME\\']/g, prefix.replace(/'/g, '\\\''));
				markup = markup.replace(/FORM_REPLACEME/g, formName);

				var binding = JSON.parse(markupDetails.binding);

				return {binding: binding, html: markup};
			}
		};

		/*
		 *
		 */
		function inheritMethods (childObject, parentObject) {
			for (var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		}
	};
}