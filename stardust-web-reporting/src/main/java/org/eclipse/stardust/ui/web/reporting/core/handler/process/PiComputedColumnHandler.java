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
package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractComputedColumnHandler;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IPropertyValueProvider;

public class PiComputedColumnHandler extends AbstractComputedColumnHandler<ProcessInstance, ProcessInstanceQuery>
{
   @Override
   protected Map<String, IPropertyValueProvider< ? , ProcessInstance>> getScriptingContextInfo(ProcessInstance t)
   {
      Map<String, IPropertyValueProvider< ? , ProcessInstance>> scriptingContextInfo
         = new HashMap<String, IPropertyValueProvider<?,ProcessInstance>>();

      PiColumnHandlerRegistry piColumnRegistry = new PiColumnHandlerRegistry();
      for(PiDimensionField pf: PiDimensionField.values())
      {
         if(pf.isScriptingVariable())
         {
            RequestColumn fieldKey = new RequestColumn(pf.getId());
            IPropertyValueProvider< ? , ProcessInstance> propertyValueProvider
               = piColumnRegistry.getPropertyValueProvider(fieldKey);
            scriptingContextInfo.put(fieldKey.getId(), propertyValueProvider);
         }
      }

      List<DataPath> descriptorDefinitions = t.getDescriptorDefinitions();
      for(DataPath descriptorDef: descriptorDefinitions)
      {
         final String descriptorId = descriptorDef.getId();
         final Object descriptorValue = t.getDescriptorValue(descriptorId);

         scriptingContextInfo.put(descriptorId, new IPropertyValueProvider<Object, ProcessInstance>()
         {
            @Override
            public Object provideObjectValue(HandlerContext context, ProcessInstance t)
            {
               return descriptorValue;
            }
         });

      }

      return scriptingContextInfo;
   }
}
