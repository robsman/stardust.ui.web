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
package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractComputedColumnHandler;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IPropertyValueProvider;

public class AiComputedColumnHandler extends AbstractComputedColumnHandler<ActivityInstance, ActivityInstanceQuery>
{

   @Override
   protected Map<String, IPropertyValueProvider< ? , ActivityInstance>> getScriptingContextInfo(
         ActivityInstance t)
   {
      Map<String, IPropertyValueProvider< ? , ActivityInstance>> scriptingContextInfo
      = new HashMap<String, IPropertyValueProvider<?,ActivityInstance>>();

      AiColumnHandlerRegistry aiColumnRegistry = new AiColumnHandlerRegistry();
      for(AiDimensionField pf: AiDimensionField.values())
      {
         if(pf.isScriptingVariable())
         {
            RequestColumn fieldKey = new RequestColumn(pf.getId());
            IPropertyValueProvider< ? , ActivityInstance> propertyValueProvider
               = aiColumnRegistry.getPropertyValueProvider(fieldKey);
            scriptingContextInfo.put(fieldKey.getId(), propertyValueProvider);
         }
      }

      List<DataPath> descriptorDefinitions = t.getDescriptorDefinitions();
      for(DataPath descriptorDef: descriptorDefinitions)
      {
         final String descriptorId = descriptorDef.getId();
         final Object descriptorValue = t.getDescriptorValue(descriptorId);

         scriptingContextInfo.put(descriptorId, new IPropertyValueProvider<Object, ActivityInstance>()
         {
            @Override
            public Object provideObjectValue(HandlerContext context, ActivityInstance t)
            {
               return descriptorValue;
            }
         });
      }

      return scriptingContextInfo;
   }
}
