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

define(['require', 'dojo', 'dijit', 'orion/commands', 'dijit/form/DropDownButton', 'dijit/ColorPalette'], function(require, dojo, dijit, mCommands) {

	dojo.declare("orion.widgets.settings.ColorPicker", dijit.form.DropDownButton, { //$NON-NLS-0$
	
		
		setStorageItem: function(){
			// to be overridden with a choice of function to store the picked color
			console.log( 'ColorPicker setStorageIem' ); //$NON-NLS-0$
		},
		
		postCreate: function(){
		
			this.inherited( arguments );
		
			this.dropDown = new dijit.ColorPalette({
			
				category: this.category,
				item: this.item,
				element: this.name,
				identifier: this.name,
				setStorageItem: dojo.hitch( this, 'setStorageItem' ), //$NON-NLS-0$
				colornode: this,

				onChange: function(color) {

					var category = this.category;
					var subCategory = this.item;
					var element = this.element;
					var ui = this.ui;
	
					this.setStorageItem(category, subCategory, element, color, ui);	
					this.colornode.set('label', ""); //$NON-NLS-0$
		
					dojo.style(this.colornode.domNode.firstChild, "background", color); //$NON-NLS-0$
				}
			});
			
			dojo.style(this.domNode.firstChild, "border", "1px solid #AAA"); //$NON-NLS-1$ //$NON-NLS-0$
			dojo.style(this.domNode.firstChild, "padding", "2px"); //$NON-NLS-1$ //$NON-NLS-0$
			dojo.style(this.domNode.firstChild, "padding-right", "3px"); //$NON-NLS-1$ //$NON-NLS-0$
			dojo.style(this.domNode.firstChild, "background", this.setting); //$NON-NLS-0$
			dojo.style(this.domNode.firstChild, "height", "12px"); //$NON-NLS-1$ //$NON-NLS-0$
			dojo.style(this.domNode.firstChild, "width", "12px"); //$NON-NLS-1$ //$NON-NLS-0$

			var elements = dojo.query('.dijitArrowButtonInner', this.domNode); //$NON-NLS-0$
			dojo.removeClass(elements[0], 'dijitArrowButtonInner'); //$NON-NLS-0$
		}
		
	});
});

