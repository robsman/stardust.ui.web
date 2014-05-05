/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.ui.web.reporting.core.DataField;

public interface ISqlValueProvider<T>
{
   public T provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException;
   public DataField provideDataField(HandlerContext context);
}
