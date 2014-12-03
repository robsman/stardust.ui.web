/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.spi.descriptor;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ModelElement;

/**
 * @author Yogesh.Manware
 * 
 */
public interface ISemanticalDescriptorComparator
{
   /**
    * @param dataPath1
    * @param dataPath2
    * @return
    */
   int compare2(DataPath dataPath1, DataPath dataPath2);

   /**
    * @param modelElement1
    * @param modelElement2
    * @return
    */
   int compare(ModelElement modelElement1, ModelElement modelElement2);
}
