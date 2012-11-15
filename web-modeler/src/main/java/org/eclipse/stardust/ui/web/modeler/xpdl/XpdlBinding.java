package org.eclipse.stardust.ui.web.modeler.xpdl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISymbolContainer;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

public class XpdlBinding extends ModelBinding<ModelType>
{
   public XpdlBinding(ModelElementMarshaller marshaller, ModelElementUnmarshaller unmarshaller)
   {
      super(new XpdlNavigator(), marshaller, unmarshaller);
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
