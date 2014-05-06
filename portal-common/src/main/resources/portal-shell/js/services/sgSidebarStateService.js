/*
 * 
 */
define(['portal-shell/js/shell'], function (shell) {
    'use strict';

    shell.module.provider('sgSidebarStateService', function () {
      var self = this;

      self.$get = ['sgPubSubService', function (sgPubSubService) {
        var service = {};

        service.sidebar = {visible: true, pinned: true};

        /*
         * 
         */
        service.openSidebar = function() {
          // TODO
        };

        /*
         * 
         */
        service.closeSidebar = function() {
          // TODO
        };

        /*
         * 
         */
        service.pinSidebar = function() {
          service.sidebar.pinned = true;
          service.sidebar.visible = true;
          sgPubSubService.publish('sgSidebarPinStateChanged', {oldValue: false, newValue: true});
          sgPubSubService.publish('sgSidebarVisibilityChanged', {oldValue: false, newValue: true});
        };

        /*
         * 
         */
        service.unpinSidebar = function() {
          service.sidebar.pinned = false;
          service.sidebar.visible = false;
          sgPubSubService.publish('sgSidebarPinStateChanged', {oldValue: true, newValue: false});
          sgPubSubService.publish('sgSidebarVisibilityChanged', {oldValue: true, newValue: false});
        };

        /*
         * 
         */
        service.getSidebarDetails = function() {
          var sidebarElem = service.sidebar.pinned ? jQuery(".sidebar-content") : jQuery(".sg-sidebar-toggle");
          var ret = {
            visible: service.sidebar.visible,
            pinned: service.sidebar.pinned,
            width: sidebarElem.outerWidth(),
            height: sidebarElem.outerHeight(),
            left: sidebarElem.offset().left + 1,
            zIndex: 800
          }
          return ret;
        };
        
        return service;
      }];
    });
});