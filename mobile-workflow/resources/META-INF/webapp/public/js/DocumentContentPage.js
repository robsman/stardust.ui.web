/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "js/Utils", "js/WorkflowService" ], function(Utils, WorkflowService) {
	return {
		create : function(deck, document) {
			var page = new DocumentContentPage();

			page.initialize(deck, document);

			return page;
		}
	};

	function DocumentContentPage() {
		this.id = "documentContentPage";

		/**
		 * 
		 */
		DocumentContentPage.prototype.initialize = function(deck, document) {
			this.deck = deck;
			this.document = document;
		};

		/**
		 * 
		 */
		DocumentContentPage.prototype.show = function() {
			var deferred = jQuery.Deferred();

			deferred.resolve();

			return deferred.promise();
		};

		/**
		 * 
		 */
		DocumentContentPage.prototype.getContentUri = function() {
			return WorkflowService.instance().getBaseUrl() + "/dms-content/"
					+ this.document.downloadToken;
		};
		/**
		 * 
		 */
		DocumentContentPage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
