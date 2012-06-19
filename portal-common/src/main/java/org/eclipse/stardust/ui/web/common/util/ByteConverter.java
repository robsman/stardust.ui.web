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
import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * @author Subodh.Godbole
 *
 */
public class ByteConverter implements Converter, Serializable
{
	private MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
	
	/* (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
	 */
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
	{
		if(StringUtils.isNotEmpty(arg2))
		{
			try
			{
				return Byte.parseByte(arg2);	
			}
			catch(Exception e)
			{
		         FacesContext.getCurrentInstance().addMessage(
		                 arg1.getClientId(FacesContext.getCurrentInstance()),
		                 new FacesMessage(MessageFormat.format(propsBean
		                       .getString("common.converter.byte.errorMsg"),
		                       		new Object[] {arg2, Byte.MIN_VALUE, Byte.MAX_VALUE})));				
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
	 */
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
	{
		if(arg2 != null)
		{
			if(arg2 instanceof Byte)
			{
				return String.valueOf(((Byte)arg2).intValue());
			}
			return arg2.toString();
		}
			
		return null;
	}
}
