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
package org.eclipse.stardust.ui.web.common;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public interface MessageSource extends Serializable
{
   String getMessage(String code, Locale locale);
   String getMessage(String code, String defaultMessage, Locale locale);
   boolean hasMessage(String code, Locale locale);
}
