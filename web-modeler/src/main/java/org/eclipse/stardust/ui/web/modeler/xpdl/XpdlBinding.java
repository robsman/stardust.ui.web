package org.eclipse.stardust.ui.web.modeler.xpdl;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Service
@ModelingSessionScoped
public class XpdlBinding extends ModelBinding<ModelType>
{
   static
   {
      ModelBinding.trace.info("Loaded XPDL model binding.");
   }

   @Autowired
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
   public String getModelFormat(ModelType model)
   {
      return "xpdl";
   }

   @Override
   public String getModelId(ModelType model)
   {
      return model.getId();
   }
}
