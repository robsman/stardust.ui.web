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

    angular.module('bpm-common').directive('sdPopover', ['$compile', PopoverDirective]);

    /**
     *
     */
    function PopoverDirective($compile) {
        return {
            restrict: 'AE',
            compile: function(tElement, tAttrs) {
                for (var atr in tAttrs) {
                    if (tAttrs.$attr[atr] && tAttrs.$attr[atr].indexOf('sda-template') === 0) {
                        tElement.removeAttr('sda-template');
                        tElement.attr('uib-popover-template', tAttrs[atr]);
                    } else if (tAttrs.$attr[atr] && tAttrs.$attr[atr].indexOf('sda-') === 0) {
                        tElement.removeAttr(tAttrs.$attr[atr]);
                        tElement.attr(tAttrs.$attr[atr].replace('sda-', 'popover-'), tAttrs[atr]);
                    }
                }

                //Adding outside click as a defaul
                 if(angular.isUndefined(tAttrs['sdaTrigger'])) {
                    tElement.attr('popover-trigger','outsideClick');
                }
                tElement.attr('popover-animation','false');
                tElement.removeAttr('sd-popover'); // necessary to avoid infinite compile loop
                return {
                    post: function(scope, element, attr, ctrl) {
                        var fn = $compile(element);
                        fn(scope);
                    }
                };
            }
        };
    }

})();
