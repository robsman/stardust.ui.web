/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([], function() {
	return {
		view : [ {
			viewId : "ruleSetView",
			label : "Rule Set ${viewParams.name}[20]",
			viewHtmlUrl : "rules-manager/views/ruleSetView.html",
			iconUrl : "rules-manager/images/icons/rule-set.png"
		}, {
			viewId : "ruleView",
			label : "Rule ${viewParams.name}[20]",
			viewHtmlUrl : "rules-manager/views/ruleView.html",
			iconUrl : "rules-manager/images/icons/rule.png"
		}]
	};
});