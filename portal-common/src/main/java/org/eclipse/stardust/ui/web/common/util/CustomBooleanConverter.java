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

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.BooleanConverter;

/**
 * @author Subodh.Godbole
 *
 */
public class CustomBooleanConverter extends BooleanConverter implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Override
   public String getAsString(FacesContext context, UIComponent component, Object value)
   {
      if (value instanceof Boolean)
      {
         return MessagePropertiesBean.getInstance().getString((Boolean) value ? "common.true" : "common.false");
      }

      return super.getAsString(context, component, value);
   }
}
