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
package org.eclipse.stardust.ui.web.bcc.messsages;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;


public class MessagesBCCBean extends AbstractMessageBean
{
	public MessagesBCCBean()
	{
	   super("business-control-center-messages");
	}
	
	public static MessagesBCCBean getInstance()
	{
		return (MessagesBCCBean) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
                  "messages_bcc");
	}
}
