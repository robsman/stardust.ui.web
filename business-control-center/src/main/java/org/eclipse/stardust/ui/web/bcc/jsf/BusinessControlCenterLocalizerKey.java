/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.bcc.jsf;

import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

public class BusinessControlCenterLocalizerKey extends LocalizerKey {

	private static final String BUSINESSCONTROLCENTER_MESSAGE_BUNDLE = "business-control-center-messages";

	public static final LocalizerKey CANNOT_MODIFY_USER = new BusinessControlCenterLocalizerKey(
			"messages.common.cannotModifyUser");

	public static final LocalizerKey INVALID_SERVICE_FACTORY = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidSession");

	public static final LocalizerKey INVALID_QUERY_SERVICE = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidQueryService");

	public static final LocalizerKey INVALID_USER_SERVICE = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidUserService");

	public static final LocalizerKey INVALID_WORKFLOW_SERVICE = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidWorkflowService");

	public static final LocalizerKey INVALID_ADMINISTARTION_SERVICE = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidAdministrationService");

	public static final LocalizerKey INVALID_SESSION = new BusinessControlCenterLocalizerKey(
			"messages.common.invalidSession");

	public static LocalizerKey ALL_PROCESSES = new BusinessControlCenterLocalizerKey(
			"messages.common.allProcesses");

	private BusinessControlCenterLocalizerKey(String key) {
		super(BUSINESSCONTROLCENTER_MESSAGE_BUNDLE, key);
	}
}
