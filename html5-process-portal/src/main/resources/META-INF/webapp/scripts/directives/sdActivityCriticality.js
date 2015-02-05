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

   angular.module('bpm-common').directive('sdActivityCriticality', ActivityCriticality);

   /*
    * 
    */

   function ActivityCriticality()
   {

      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/ActivityCriticality.html',
         controller : CriticalityController
      };

   }

   function CriticalityController($scope)
   {
      this.getTimes = function(count)
      {
         return new Array(count);
      }
      this.toolTip = {
         show : false
      };
      this.i18n = $scope.$parent.i18n;
      $scope.criticalityCtrl = this;
   }

})();
