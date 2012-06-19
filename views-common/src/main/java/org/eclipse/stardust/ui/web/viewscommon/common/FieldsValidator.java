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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class FieldsValidator
{
	 private final MessagesViewsCommonBean viewsCommonMessage;
   /**
    * 
    */
   public FieldsValidator()
   {
		 viewsCommonMessage= MessagesViewsCommonBean.getInstance();
   }

   public void validateId(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      String id = (String) value;
      showValidationMsg(id, "id", component);
   }

   public void validateName(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      String name = (String) value;
      showValidationMsg(name, "name", component);
   }

   private void showValidationMsg(String id, String compId, UIComponent component)
   {
      if (StringUtils.isEmpty(id.trim()) || !id.trim().equals(id))
      {
         FacesMessage message = new FacesMessage();
         String msg= viewsCommonMessage.getString("common.whitespace.error");
         message.setDetail( msg);
         message.setSummary( msg );
         message.setSeverity(FacesMessage.SEVERITY_ERROR);

         throw new ValidatorException(message);
      }

   }

}
