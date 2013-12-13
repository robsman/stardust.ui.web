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

package org.eclipse.stardust.ui.web.processportal.interaction;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean.InputParameters;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public interface DocumentController extends Serializable
{
   AbstractDocumentContentInfo getDocument();

   InputParameters getDocumentViewerInputParameters();

   View getView();
   
   View openDocument(boolean nested);
   
   String getViewKey();
}
