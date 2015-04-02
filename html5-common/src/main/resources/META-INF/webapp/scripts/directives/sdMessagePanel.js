/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
(function() {
	'use strict';
	angular.module('bpm-common.directives').directive('sdMessagePanel',
			[ 'sdUtilService', MessagePanel ]);

	function MessagePanel(sdUtilService) {
		var directiveDefObject = {
			restrict : 'E',
			scope : true,
			template : '<div id="messagePanel" style="border: 1px solid #CCCCCC">'
					+ '<table style="width: 100%; height: 100%;" cellpadding="0" cellspacing="0">'
					+ '<tr valign="middle">'
					+ '<td align="center">'
					+ '<div id="messageIcon"></div>'
					+ ' </td>'
					+ '<td align="left" style="vertical-align: middle !important;">'
					+ '<div id="messageDisplay">{{messagePanelCtrl.msg}}</div>'
					+ '</td>' + '</tr>' + '</table>' + '</div>',
			controller : [ '$scope', '$attrs', MessagePanel ]
		};

		function MessagePanel(scope, attr) {
			var self = this;
			this.msg = null;
			if (attr.sdaMessage) {
				this.msg = attr.sdaMessage;
			}
			if (attr.sdaIconClass) {
				angular.element('#messageIcon').parent().attr("class",
						"infoSeverityIssueItem");
				angular.element('#messagePanel').attr("class",
						"messagePanelHighlight");
			}
			scope.messagePanelCtrl = this;
			sdUtilService.safeApply(scope);
		}
		return directiveDefObject;
	}
})();
