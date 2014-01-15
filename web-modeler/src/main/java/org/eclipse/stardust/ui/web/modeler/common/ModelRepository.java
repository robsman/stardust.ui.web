package org.eclipse.stardust.ui.web.modeler.common;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Service
@ModelingSessionScoped
public class ModelRepository
{
   private static final Logger trace = LogManager.getLogger(ModelRepository.class);

   private final ModelingSession session;

   @Resource
   private List<ModelBinding<? extends EObject>> modelBindings;

   @Autowired
   public ModelRepository(ModelingSession session)
   {
      this.session = session;
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
                        return getNativeModel(xpdlModel);
                     }
                  });
         }
      };
   }

   public String getModelId(EObject model)
   {
      return getModelBinding(model).getModelId(model);
   }

   public String getModelFileName(EObject model)
   {
      for (ModelType xpdlModel : session.modelManagementStrategy().getModels().values())
      {
         if (model == getNativeModel(xpdlModel))
         {
            return session.modelManagementStrategy().getModelFileName(xpdlModel);
         }
      }
      return null;
   }

   protected EObject getNativeModel(ModelType xpdlModel)
   {
      return findModel(xpdlModel.getId());
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

   @SuppressWarnings("unchecked")
   public <M extends EObject> ModelPersistenceHandler<M> getModelPersistenceHandler(M model)
   {
      for (ModelBinding<? extends EObject> binding : modelBindings)
      {
         if (binding.isCompatible(model))
         {
            return ((ModelBinding<M>) binding).getPersistenceHandler(model);
         }
      }

      throw new IllegalArgumentException("Unsupported model: " + model);
   }

   @SuppressWarnings("unchecked")
   public <M extends EObject> String getModelFormat(M model)
   {
      for (ModelBinding<? extends EObject> binding : modelBindings)
      {
         if (binding.isCompatible(model))
         {
            return ((ModelBinding<M>) binding).getModelFormat(model);
         }
      }

      throw new IllegalArgumentException("Unsupported model: " + model);
   }
}
