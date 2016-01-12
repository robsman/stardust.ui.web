package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

@Component
public class DataMappingTypeChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getAddedElements())
      {
         trackDataMappingChanges(change, candidate);
      }
      for (EObject candidate : change.getModifiedElements())
      {
         trackDataMappingChanges(change, candidate);
      }
      for (EObject candidate : change.getRemovedElements())
      {
         trackDataMappingChanges(change, candidate);
      }
   }

   private void trackDataMappingChanges(Modification change, EObject candidate)
   {
      if (candidate instanceof DataMappingType)
      {
         DataMappingType dataMapping = (DataMappingType) candidate;

         DataType data = dataMapping.getData();
         if(data != null)
         {
            for (DataSymbolType dataSymbol : data.getDataSymbols())
            {
               for (DataMappingConnectionType dataMappingConnection : dataSymbol.getDataMappings())
               {
                  change.markAlsoModified(dataMappingConnection);
               }
            }
         }
         ActivityType activity = ModelUtils.findContainingActivity(dataMapping);
         if (null != activity)
         {
            change.markAlsoModified(activity);
         }
      }
   }
}
