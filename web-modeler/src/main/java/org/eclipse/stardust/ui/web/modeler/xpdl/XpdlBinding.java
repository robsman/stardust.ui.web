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
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;
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

   @Override
   public ModelType createModel(ModelJto jto)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <T extends ModelElementJto> EObject createModelElement(ModelType model, T jto)
   {
      IModelElement element = null;
      if ("activity".equals(jto.type))
      {
         element = CarnotWorkflowModelFactory.eINSTANCE.createActivityType();
      }
      else
      {
         throw new IllegalArgumentException("Not yet implemented: " + jto.type);
      }

      if (null != element)
      {
         if (element instanceof IIdentifiableModelElement)
         {
            ((IIdentifiableModelElement) element).setId(jto.id);
            ((IIdentifiableModelElement) element).setName(jto.name);
         }
      }

      // element OID will created upon attach

      return element;
   }

   public void attachModelElement(EObject container, EObject modelElement)
   {
      if (modelElement instanceof ActivityType)
      {
         assert container instanceof ProcessDefinitionType;

         ((ProcessDefinitionType) container).getActivity().add((ActivityType) modelElement);
      }
      else
      {
         throw new IllegalArgumentException("Not yet implemented: " + modelElement.eClass().getName());
      }

      if ((modelElement instanceof IModelElement)
            && !((IModelElement) modelElement).isSetElementOid())
      {
         ModelType model = ModelUtils.findContainingModel(container);
         long elementOi = XpdlModelUtils.getMaxUsedOid((ModelType) model) + 1;
         ((IModelElement) modelElement).setElementOid(elementOi);
      }
   }

   @Override
   public EObject createProcessDiagram(EObject processDefinition, ProcessDiagramJto jto)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <T extends ShapeJto> EObject createNodeSymbol(ModelType model, T jto,
         EObject modelElement)
   {
      INodeSymbol symbol = null;
      if (jto instanceof ActivitySymbolJto)
      {
         symbol = CarnotWorkflowModelFactory.eINSTANCE.createActivitySymbolType();
         if (modelElement instanceof ActivityType)
         {
            ((ActivitySymbolType) symbol).setActivity((ActivityType) modelElement);
         }
      }
      else
      {
         throw new IllegalArgumentException("Not yet implemented: " + jto.getClass());
      }

      // element OID will created upon attach

      return symbol;
   }

   public void attachNodeSymbol(EObject container, EObject nodeSymbol)
   {
      assert null == nodeSymbol.eContainer();
      assert container instanceof ISymbolContainer;

      if (nodeSymbol instanceof ActivitySymbolType)
      {
         ((ISymbolContainer) container).getActivitySymbol().add((ActivitySymbolType) nodeSymbol);
      }
      else
      {
         throw new IllegalArgumentException("Not yet implemented: " + nodeSymbol.eClass().getName());
      }

      if (container instanceof ISwimlaneSymbol)
      {
         // adjust coordinates from global to local
         int laneOffsetX = 0;
         int laneOffsetY = 0;

         ISwimlaneSymbol swimlane = (ISwimlaneSymbol) container;
         while (null != swimlane)
         {
            laneOffsetX += swimlane.getXPos();
            laneOffsetY += swimlane.getYPos();

            // recurse
            swimlane = (swimlane.eContainer() instanceof ISwimlaneSymbol)
                  ? (ISwimlaneSymbol) swimlane.eContainer()
                  : null;
         }

         ((INodeSymbol) nodeSymbol).setXPos(((INodeSymbol) nodeSymbol).getXPos() - laneOffsetX);
         ((INodeSymbol) nodeSymbol).setYPos(((INodeSymbol) nodeSymbol).getYPos() - laneOffsetY);
      }

      if ((nodeSymbol instanceof IModelElement)
            && !((IModelElement) nodeSymbol).isSetElementOid())
      {
         ModelType model = ModelUtils.findContainingModel(container);
         long elementOi = XpdlModelUtils.getMaxUsedOid((ModelType) model) + 1;
         ((IModelElement) nodeSymbol).setElementOid(elementOi);
      }

   }
}
