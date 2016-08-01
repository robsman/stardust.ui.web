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
 * @author Johnson.Quadras
 */
(function() {
   'use strict';

   angular.module('workflow-ui').directive('sdActivityActionsPopoverContent', [ 'sdUtilService', DocumentPopoverContent ]);

   /*
    *
    */

   function DocumentPopoverContent(sdUtilService) {

      return {
         restrict : 'A',
         templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/activityActionsPopover.html',
      };
   }
})();
