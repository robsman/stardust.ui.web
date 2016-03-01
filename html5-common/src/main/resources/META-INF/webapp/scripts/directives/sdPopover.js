/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('bpm-common').directive('sdPopover', [PopoverDirective]);

    /**
     *
     */
    function resolveTemplate(tElement, tAttrs) {

        var template = [];
        for (var atr in tAttrs) {
            if (tAttrs.$attr[atr] && tAttrs.$attr[atr].indexOf('sda-text') == 0) {
                template.push("uib-popover = " + tAttrs[atr]);
            } else if (tAttrs.$attr[atr] && tAttrs.$attr[atr].indexOf('sda-template') == 0) {
                template.push('uib-popover-template = "' + tAttrs[atr] + '"');
            } else if (tAttrs.$attr[atr] && tAttrs.$attr[atr].indexOf('sda-') == 0) {
                template.push(tAttrs.$attr[atr].replace('sda-', 'popover-') + ' = "' + tAttrs[atr] + '"')
            }
        }

        var popoverTemplate = '<span ' + template.join(' ') + '> <ng-transclude></ng-transclude>' + '</span>';

        return popoverTemplate;
    }

    /**
     *
     */
	function PopoverDirective () {
		return {
			restrict : 'AE',
			template : resolveTemplate,
			transclude : true,
			replace : true
		};
	}
	
})();
