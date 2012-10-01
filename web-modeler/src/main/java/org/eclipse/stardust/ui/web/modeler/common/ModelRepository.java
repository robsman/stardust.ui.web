package org.eclipse.stardust.ui.web.modeler.common;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.xpdl.XpdlBinding;

public class ModelRepository
{
   private static final Logger trace = LogManager.getLogger(ModelRepository.class);

   private final ModelingSession session;

   private final List<ModelBinding<? extends EObject>> modelBindings;

   public ModelRepository(ModelingSession session)
   {
      this.session = session;

      this.modelBindings = newArrayList();
      modelBindings.add(new XpdlBinding(session.modelElementMarshaller(),
            session.modelElementUnmarshaller()));

      // TODO migrate to Spring based discovery?
      // see also DefaultModelManagementStrategy
      try
      {
         String fqcnBpmn2Binding = "org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Binding";
         @SuppressWarnings("unchecked")
         Class<? extends ModelBinding<?>> clsBpmn2Binding = Reflect.getClassFromClassName(fqcnBpmn2Binding, false);
         if (null != clsBpmn2Binding)
         {
            modelBindings.add(clsBpmn2Binding.cast(Reflect.createInstance(clsBpmn2Binding, null, null)));
            trace.info("Registered BPMN2 model binding.");
         }
         else
         {
            trace.info("Could not load BPMN2 model binding, BPMN2 support will not be available.");
         }
      }
      catch (Exception e)
      {
         trace.warn("Failed loading BPMN2 model binding.", e);
      }
   }

   public EObject findModel(String modelId)
   {
      return (session.modelManagementStrategy() instanceof AbstractModelManagementStrategy)
            ? ((AbstractModelManagementStrategy) session.modelManagementStrategy()).getNativeModel(modelId)
            : session.modelManagementStrategy().getModels().get(modelId);
   }

   public Iterable<? extends EObject> getAllModels()
   {
      return new Iterable<EObject>()
      {
         @Override
         public Iterator<EObject> iterator()
         {
            // TODO quick hack during soft-transitioning to BPMN2
            return new TransformingIterator<ModelType, EObject>( //
                  session.modelManagementStrategy().getModels(false).values().iterator(),
                  new Functor<ModelType, EObject>()
                  {
                     @Override
                     public EObject execute(ModelType xpdlModel)
                     {
                        return (session.modelManagementStrategy() instanceof AbstractModelManagementStrategy)
                              ? ((AbstractModelManagementStrategy) session.modelManagementStrategy()).getNativeModel(xpdlModel.getId())
                              : xpdlModel;
                     }
                  });
         }
      };
   }

   @SuppressWarnings("unchecked")
   public <M extends EObject> ModelBinding<M> getModelBinding(M model)
   {
      for (ModelBinding<? extends EObject> binding : modelBindings)
      {
         if (binding.isCompatible(model))
         {
            return (ModelBinding<M>) binding;
         }
      }

      throw new IllegalArgumentException("Unsupported model: " + model);
   }
}
