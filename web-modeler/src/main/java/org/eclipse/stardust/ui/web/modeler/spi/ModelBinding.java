package org.eclipse.stardust.ui.web.modeler.spi;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;

public abstract class ModelBinding<M extends EObject>
{
   protected final ModelNavigator<M> navigator;

   protected final ModelMarshaller marshaller;

   protected final ModelUnmarshaller unmarshaller;

   protected ModelBinding(ModelNavigator<M> navigator, ModelMarshaller marshaller, ModelUnmarshaller unmarshaller)
   {
      this.navigator = navigator;
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
   }

   public abstract boolean isCompatible(EObject model);

   public abstract String getModelId(M model);

   public abstract M createModel(ModelJto jto);

   public abstract <T extends ModelElementJto> EObject createModelElement(M model,
         T jto);

   public abstract void attachModelElement(EObject container, EObject modelElement);

   public abstract EObject createProcessDiagram(EObject processDefinition,
         ProcessDiagramJto jto);

   public abstract <T extends ShapeJto> EObject createNodeSymbol(M model, T jto,
         EObject modelElement);

   public abstract void attachNodeSymbol(EObject container, EObject nodeSymbol);

   public ModelNavigator<M> getNavigator()
   {
      return navigator;
   }

   public void updateModelElement(EObject modelElement, JsonObject jto)
   {
      unmarshaller.populateFromJson(modelElement, jto);
   }

   public ModelMarshaller getMarshaller()
   {
      return marshaller;
   }

}
