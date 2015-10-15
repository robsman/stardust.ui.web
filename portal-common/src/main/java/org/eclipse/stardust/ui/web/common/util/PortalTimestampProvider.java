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
package org.eclipse.stardust.ui.web.common.util;

import java.util.Date;

import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PortalTimestampProvider
{
   public static Date getTimeStamp()
   {
      return TimestampProviderUtils.getTimeStamp();
   }
   
   public static long getTimeStampValue()
   {
      return TimestampProviderUtils.getTimeStampValue();
   }
}
