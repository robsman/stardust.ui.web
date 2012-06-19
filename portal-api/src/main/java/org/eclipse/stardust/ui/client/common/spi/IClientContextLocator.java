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
package org.eclipse.stardust.ui.client.common.spi;

import org.eclipse.stardust.ui.client.common.ClientContext;

/**
 * @author sauer
 * @version $Revision: 31039 $
 */
public interface IClientContextLocator
{
   ClientContext getClientContext();

   interface Factory
   {
      IClientContextLocator getLocator(String contextId);
   }
}
