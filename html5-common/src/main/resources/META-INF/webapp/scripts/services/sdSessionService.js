/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
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

    angular.module('bpm-common.services').service('sdSessionService', ['sdLoggerService', '$resource', '$interval', 'sdUtilService', SessionService]);

    function SessionService(sdLoggerService, $resource, $interval, sdUtilService) {
        var trace = sdLoggerService.getLogger('bpm-common.sdSessionService');
        var baseURL = sdUtilService.getBaseUrl() + "services/rest/portal/session/";
         var self = this;
        /*
         */
        this.ping = function() {
            var restUrl = baseURL + "ping";
            return $resource(restUrl).get().$promise;
        };

        /*
         */
        this.invalidate = function() {
            //TODO Implement this
        };

        /* Pass the interval in seconds
           Call cancel to kill
         */
        this.startHeartbeat = function( interval ) {

          interval = parseInt(interval) * 1000;
          trace.debug("Heartbeat started with interval :- " + interval + " seconds.");
          var hearBeat = $interval( function(){self.ping()}, interval);

          return hearBeat;
        };

    }

})();
