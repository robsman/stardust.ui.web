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
package org.eclipse.stardust.ui.web.bcc.legacy;

import org.eclipse.stardust.ui.web.common.util.PortalTimestampProvider;

public class DefaultTimeProvider implements ITimeProvider{

	public long getCurrentTime() {
		return PortalTimestampProvider.getTimeStampValue();
	}

}
