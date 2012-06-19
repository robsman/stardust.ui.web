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
package org.eclipse.stardust.ui.web.viewscommon.common.spi;

import java.io.Serializable;
import java.util.Date;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface IGenericInputField extends Serializable
{
   String getType();

   Object getValue();

   void setStringValue(String value);

   void setBooleanValue(Boolean value);

   void setLongValue(Long value);

   void setDoubleValue(Double value);

   void setDateValue(Date value);
}