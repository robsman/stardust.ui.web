package org.eclipse.stardust.ui.web.modeler.xpdl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

public class XpdlBinding extends ModelBinding<ModelType>
{
   public XpdlBinding(ModelingSession session, ModelElementMarshaller marshaller, ModelElementUnmarshaller unmarshaller)
   {
      super(session, new XpdlNavigator(), marshaller, unmarshaller);
   }

   @Override
   public boolean isCompatible(EObject model)
   {
      return model instanceof ModelType;
   }

   @Override
   public String getModelId(ModelType model)
   {
      return model.getId();
   }
}
