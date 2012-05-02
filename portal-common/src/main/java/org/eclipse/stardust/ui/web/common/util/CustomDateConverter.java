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


/**
 * @author Subodh.Godbole
 *
 */
public class CustomDateConverter extends CustomDateTimeConverter
{
   private static final long serialVersionUID = -8529162372195806885L;

   /**
    * 
    */
   public CustomDateConverter()
   {
      super();
      try
      {
         setTimeZone(java.util.TimeZone.getDefault());
         setPattern(DateUtils.getDateFormat());
      }
      catch (Throwable t)
      {
         // Ignore
      }
   }
}
