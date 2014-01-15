package org.eclipse.stardust.ui.web.modeler.xpdl;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Service
@ModelFormat(ModelFormat.XPDL)
@ModelingSessionScoped
public class XpdlBinding extends ModelBinding<ModelType>
{
   static
   {
      ModelBinding.trace.info("Loaded XPDL model binding.");
   }

   @Resource
   private XpdlNavigator navigator;

   @Resource
   private ModelElementMarshaller marshaller;

   @Resource
   private ModelElementUnmarshaller unmarshaller;

   @Autowired
   public XpdlBinding(ModelingSession session)
   {
      super(session);
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

   @Override
   public XpdlNavigator getNavigator()
   {
      return navigator;
   }

   @Override
   public ModelElementMarshaller getMarshaller()
   {
      return marshaller;
   }

   @Override
   public ModelElementUnmarshaller getUnmarshaller()
   {
      return unmarshaller;
   }
}
