/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.message;

import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@SuppressWarnings("unchecked")
@Component
public class RestCommonClientMessages extends AbstractMessageBean
{
   private static final long serialVersionUID = -5309970703479835450L;

   private static final String BUNDLE_NAME = "rest-common-client-messages";

   public RestCommonClientMessages()
   {
      super(BUNDLE_NAME);
   }
}
