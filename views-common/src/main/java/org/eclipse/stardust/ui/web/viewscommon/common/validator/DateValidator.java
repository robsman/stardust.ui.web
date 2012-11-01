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
package org.eclipse.stardust.ui.web.viewscommon.common.validator;

import java.util.Date;

import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;


/**
 * @author fuhrmann
 * @version $Revision$
 */
public class DateValidator
{

   /**
    * Checks if the date 'from' is before the date 'to' if the values of both dates are
    * set.
    * 
    * @param from
    *           The start date.
    * @param to
    *           The end date.
    * 
    * @return <code>true</code> if the date 'from' is before the date 'to' or if not
    *         values of both dates are set or <code>false</code> if the date 'from' is
    *         not before the date 'to'
    */
   public static boolean validInputDate(Date from, Date to)
   {
      return validInputDate(from, to, Localizer.getString(LocalizerKey.DATE_VALID_FROM),
            Localizer.getString(LocalizerKey.DATE_VALID_TO));
   }

   /**
    * Checks if the date 'from' is before the date 'to' if the values of both dates are
    * set.
    * 
    * @param from
    *           The start date.
    * @param to
    *           The end date.
    * @param fromLabel
    *           The label of the start date for the error message if validation fails.
    * @param toLabel
    *           The label of the end date for the error message if validation fails.
    * 
    * @return <code>true</code> if the date 'from' is before the date 'to' or if not
    *         values of both dates are set or <code>false</code> if the date 'from' is
    *         not before the date 'to'
    */
   public static boolean validInputDate(Date from, Date to, String fromLabel,
         String toLabel)
   {
      boolean isValid = true;
      if (from != null && to != null)
      {
         if (from.after(to))
         {
            isValid = false;
         }
      }
      return isValid;
   }
}
