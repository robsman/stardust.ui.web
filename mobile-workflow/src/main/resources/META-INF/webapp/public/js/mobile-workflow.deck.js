/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * 
 */
function Deck() {
	this.pages = [];

	/**
	 * 
	 */
	Deck.prototype.initialize = function(page) {
		this.pages.push(page);
		page.initialize();
	};

	/**
	 * 
	 */
	Deck.prototype.pushPage = function(page) {
		this.pages.push(page);

		$.mobile.changePage("#" + page.id, {
			transition : "none"
		});

		debug("\nPush Page - Page Stack:");
		
		for ( var n = 0; n < this.pages.length; ++n) {
			debug("#" + n + ": " + this.pages[n].id);
		}
		
		page.initialize();
	};

	/**
	 * 
	 */
	Deck.prototype.popPage = function() {
		this.pages.pop();

		$.mobile.changePage("#" + this.getTopPage().id, {
			transition : "none"
		});

		debug("\nPop Page - Page Stack:");
		
		for ( var n = 0; n < this.pages.length; ++n) {
			debug("#" + n + ": " + this.pages[n].id);
		}
		
		this.getTopPage().initialize();
	};

	/**
	 * 
	 */
	Deck.prototype.getTopPage = function() {
		return this.pages[this.pages.length - 1];
	};
}

/**
 * Singleton function
 */
function getDeck() {
	if (window.top.deck == null) {
		window.top.deck = new Deck();
	}

	return window.top.deck;
}
