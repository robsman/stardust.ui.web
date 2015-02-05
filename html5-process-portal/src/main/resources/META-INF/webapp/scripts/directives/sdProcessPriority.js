/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
(function()
{
   'use strict';

   angular.module('bpm-common').directive('sdProcessPriority', ProcessPriority);

   /*
    *
    */
   function ProcessPriority()
   {

      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/ProcessPriority.html',
         controller : [ '$scope', ProcessPriorityController ]
      };
   }
   /**
    *
    */
   function ProcessPriorityController($scope, element)
   {
      this.toolTip = {
         show : false
      };
      this.i18n = $scope.i18n;
      $scope.processPriorityCtrl = this;
   }
})();
