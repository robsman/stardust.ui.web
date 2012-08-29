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
package org.eclipse.stardust.ui.web.common.app.messaging;

/**
 * @author Subodh.Godbole
 *
 */
public interface MessageTypeConstants
{
   String T_OPEN_VIEW = "OpenView";
   String D_VIEW_ID = "viewId";
   String D_VIEW_KEY = "viewKey";
   String D_VIEW_PARAMS = "params";
   String D_NESTED = "nested";

   String T_CHANGE_PERSPECTIVE = "ChangePerspective";
   String D_PERSPECTIVE_ID = "perspectiveId";
}
