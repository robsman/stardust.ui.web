/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('bpm-common.directives').directive('sdCollapsiblePanel', [CollapsiblePanel ]);
	/*
	 * 
	 */
	function CollapsiblePanel() {

		return {
			restrict : 'EA',
			scope : { // Creates a new sub scope
				template : '@sdaTemplate',  
				aid : '@sdaAid',
				title : '@sdaTitle',
				bExpanded : '@sdaExpanded',
				bDisabled : '@sdaDisabled', //this will be used to disabled the panel
				onExpandCollapse : '&sdaOnExpandCollapse'
			},
			transclude : true,
			replace : true,
			template : "<div>" + 
			             "<div class='panel-padding'>" +
			                "<div ng-show='!disabled'>" +
						        "<a href='#' class='heading' style='cursor: pointer; text-decoration: none;'" +
						            "ng-click='expanded = !expanded' aid='{{aid}}'>" +
						           "<span>{{title}}</span>" +
		                           "<i ng-show='expanded' class='pi pi-trigger-expanded pi-lg' style='float: right;'></i>" +
						           "<i ng-show='!expanded' class='pi pi-trigger-collapsed pi-lg' style='float: right;'></i>" +
						        "</a>" +
						     "</div>" +
						     "<div ng-show='disabled'>" +
							     "<span class='heading'>{{title}}</span>" +
							 "</div>" +
					     "</div>" + 
					     "<div ng-show='expanded'>" + 
					       "<div ng-show='showUserTemplate' ng-include='template'></div>" +
					       "<div ng-show='!showUserTemplate' ng-transclude></div>" +
					     "</div>" + 
					     "<div class='clearing'></div>" + 
					   "</div>",
			link : function(scope, element, attrs, ctrl) {

				if(scope.bExpanded == undefined){
			    	scope.expanded = true;
                }else{
                	scope.expanded = scope.bExpanded;
                }
			    
				if(scope.bDisabled == undefined){
				    scope.disabled = false;
				}else{
					scope.disabled = scope.bDisabled;
				}
				
				if (!angular.isDefined(scope.i18n)) {
					scope.i18n = scope.$parent.i18n;
				}
				
				scope.showUserTemplate = false;
				if (scope.template != undefined) {
					scope.showUserTemplate = true;
				}

			}
		};
	}
})();
