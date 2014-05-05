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

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;

public interface IDescriptorHandler<U extends IDescriptorProvider, V extends Query> extends IColumnHandler<Object, U, V>
{

}