/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.impl.DataSymbolTypeImpl;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

/**
 * @author Barry.Grotjahn
 *
 */
@Component
public class DataSymbolChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 9;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getRemovedElements())
      {
         if (candidate instanceof DataSymbolTypeImpl)
         {
            DataType data = ((DataSymbolType) candidate).getData();
            if(data != null)
            {
               data.getDataSymbols().remove(candidate);
               change.markAlsoModified(data);
            }
         }
      }
   }
}