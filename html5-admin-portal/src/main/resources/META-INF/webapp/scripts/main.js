/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

// Define modules
angular.module('admin-ui.services', []);
angular.module('admin-ui', ['admin-ui.services'])
.config(['httpInterceptorProvider',function (httpInterceptorProvider) {
    httpInterceptorProvider.whitelist('filltext.com');
    httpInterceptorProvider.whitelist('.herokuapp.');
  }
])

// Register top level module
portalApplication.registerModule('admin-ui');