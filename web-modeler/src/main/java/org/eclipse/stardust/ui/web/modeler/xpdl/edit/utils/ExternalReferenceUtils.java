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
package org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;

public class ExternalReferenceUtils
{
   public static boolean isModelReferenced(ModelType modelToCheck, ModelingSession session)
   {
      ModelRepository modelRepository = session.modelRepository();
      for (EObject model : modelRepository.getAllModels())
      {
         if (model instanceof ModelType)
         {
            if(!modelToCheck.getId().equals(((ModelType) model).getId()))
            {
               List<String> uris = ModelUtils.getURIsForExternalPackages((ModelType) model);
               for (Iterator<String> i = uris.iterator(); i.hasNext();)
               {
                  String uri = i.next();
                  ModelType modelType = ModelUtils.getReferencedModelByURI((ModelType) model, uri);
                  if(modelType != null && modelToCheck.getId().equals(modelType.getId()))
                  {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
}