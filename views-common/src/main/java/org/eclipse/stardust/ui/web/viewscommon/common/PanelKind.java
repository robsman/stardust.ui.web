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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.StringKey;

/**
 * @author rsauer
 * @version $Revision$
 */
public class PanelKind extends StringKey
{
   public static final PanelKind JSP_EMBEDDED = new PanelKind("jsp_embedded", "JSP Panel (embedded)"); //$NON-NLS-1$

   public static final PanelKind JSP_STANDALONE = new PanelKind("jsp_standalone", "JSP Panel (standalone)"); //$NON-NLS-1$

   public PanelKind(String id, String defaultName)
   {
      super(id, defaultName);
   }
}