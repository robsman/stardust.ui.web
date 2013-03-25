/*******************************************************************************
 * @license
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 * 
 * Contributors: Anton McConville - IBM Corporation - initial API and implementation
 ******************************************************************************/
/*global dojo dijit widgets orion  window console define localStorage*/
/*jslint browser:true*/

/* This SettingsContainer widget is a dojo border container with a left and right side. The left is for choosing a 
   category, the right shows the resulting HTML for that category. */

define(['require', 'dojo', 'dijit', 'orion/commands'], function(require, dojo, dijit, mCommands) {

	dojo.declare("orion.widgets.settings.TextField", [dijit._Widget, dijit._Templated], { //$NON-NLS-0$
	
		// templateString: '<input type="text" name="myname" data-dojo-attach-point="textfield" data-dojo-attach-event="onchange:change"/>',
		
		templateString: '<div><div>THIS IS A PLACEHOLDER</div></div>', //$NON-NLS-0$
		
		// category, item, element, ui - provided on construction
		
		category: null,
		item: null,
		element: null,
		ui: null,
		
		constructor: function(){
			this.inherited( arguments );
			console.log( 'c o n s t r u c t o r: ' + this.ui ); //$NON-NLS-0$
		},
		
		setStorageItem: function(){
			// to be overridden with a choice of function to store the picked color
			console.log( 'TextField setStorageIem' ); //$NON-NLS-0$
		},
		
		change: function(){
		
			var value = this.selection.value;
		
			this.setStorageItem( this.category, this.item, this.element, value, this.ui );
		},
		
		postCreate: function(){
			this.inherited( arguments );
			
			console.log( 'p o s t c r e a t e' ); //$NON-NLS-0$
			
//			this.textfield.name = this.ui;
		}	
	});
});

