package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.BadRequestException;
import org.eclipse.stardust.ui.web.modeler.common.ConflictingRequestException;
import org.eclipse.stardust.ui.web.modeler.common.ItemNotFoundException;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.common.ModelingSessionLocator;
import org.eclipse.stardust.ui.web.modeler.edit.LockInfo;
import org.eclipse.stardust.ui.web.modeler.edit.MissingWritePermissionException;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.SimpleCommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.jto.ChangeDescriptionJto;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.ChangesetPostprocessingService;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ModelCommandsHandler;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ChangeJto.UiStateJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

@Service
@Scope("singleton")
public class ModelerSessionController
{
   private static final Logger trace = LogManager.getLogger(ModelerSessionController.class);

   @Resource
   private ApplicationContext springContext;

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelingSessionLocator sessionLocator;

   // TODO this is not thread safe
   private static CommandJto CommandJto;

   public static CommandJto getCommandJto()
   {
      return CommandJto;
   }

   public ChangeJto toJto(Modification change)
   {
      ChangeJto jto = new ChangeJto();

      jto.id= change.getId();
      jto.timestamp = System.currentTimeMillis();

      if (change.getMetadata().containsKey("commandId"))
      {
          jto.commandId = change.getMetadata().get("commandId");
      }
      if (change.getMetadata().containsKey("modelId"))
      {
          jto.modelId= change.getMetadata().get("modelId");
      }
      if (change.getMetadata().containsKey("account"))
      {
          jto.account = change.getMetadata().get("account");
      }

      ModelMarshaller marshaller;
      if (!isEmpty(jto.modelId))
      {
         ModelingSession currentSession = currentSession();
         ModelRepository modelRepository = currentSession.modelRepository();
         EObject model = modelRepository.findModel(jto.modelId);
         ModelBinding<EObject> modelBinding = modelRepository.getModelBinding(model);
         marshaller = modelBinding.getMarshaller();
      }
      else
      {
         marshaller = currentSession().xpdlMarshaller();
      }

      try
      {
         marshaller.init();
         for (EObject changedObject : change.getModifiedElements())
         {
            jto.changes.modified.add(marshaller.toJson(changedObject));
         }
         for (EObject addedObject : change.getAddedElements())
         {
            jto.changes.added.add(marshaller.toJson(addedObject));
         }
         for (EObject removedObject : change.getRemovedElements())
         {
            jto.changes.removed.add(marshaller.toJson(removedObject));
         }
      }
      finally
      {
         marshaller.done();
      }

      if (change.wasFailure())
      {
         ChangeJto.ProblemJto failureJto = new ChangeJto.ProblemJto();
         failureJto.severity = "error";
         failureJto.message = change.getFailure().getMessage();

         jto.problems = newArrayList();
         jto.problems.add(failureJto);
      }

      jto.uiState = toUiStateJto();

      return jto;
   }

   public ChangeJto toJto(CommandJto command, ModelCommandsHandler.ModificationDescriptor changes)
   {
      ChangeJto jto = new ChangeJto();

      jto.id= changes.getId();
      jto.timestamp = System.currentTimeMillis();

      jto.commandId = command.commandId;
      jto.modelId= command.modelId;
      jto.account = command.account;

      jto.changes.modified.addAll(changes.modified);
      jto.changes.added.addAll(changes.added);
      jto.changes.removed.addAll(changes.removed);

      if (changes.wasFailure())
      {
         ChangeJto.ProblemJto failureJto = new ChangeJto.ProblemJto();
         failureJto.severity = "error";
         failureJto.message = changes.getFailure().getMessage();

         jto.problems = newArrayList();
         jto.problems.add(failureJto);
      }

      jto.uiState = toUiStateJto();

      return jto;
   }

   private UiStateJto toUiStateJto()
   {
      UiStateJto uiStateJto = new UiStateJto();

      // TODO consider only pushing the delta since last ui-state update
      uiStateJto.modelLocks = toModelLocksJto();

      return uiStateJto;
   }

   private List<ModelLockJto> toModelLocksJto()
   {
      List<ModelLockJto> modelLocksJto = newArrayList();

      ModelingSession session = currentSession();
      for (EObject model : session.modelRepository().getAllModels())
      {
         modelLocksJto.add(toModelLockJto(session, model));
      }

      return modelLocksJto;
   }

   private ModelLockJto toModelLockJto(ModelingSession session, EObject model)
   {
      ModelLockJto lockInfoJto = new ModelLockJto();
      lockInfoJto.modelId = session.modelRepository().getModelBinding(model).getModelId(model);

      LockInfo lockInfo = session.getEditLockInfo(model);
      if (null != lockInfo)
      {
         lockInfoJto.lockStatus = lockInfo.isLockedBySession(session)
               ? ModelLockJto.STATUS_LOCKED_BY_ME
               : ModelLockJto.STATUS_LOCKED_BY_OTHER;
         // TODO full name of edit lock owner
         lockInfoJto.ownerId = lockInfo.ownerId;
         lockInfoJto.ownerName = lockInfo.ownerName;
         lockInfoJto.canBreakEditLock = lockInfo.canBreakEditLock(session);
      }
      return lockInfoJto;
   }

   public JsonObject toJson(ChangeJto changeJto)
   {
      return jsonIo.gson().toJsonTree(changeJto).getAsJsonObject();
   }

   public enum ModelFormat
   {
      Native,
      Xpdl,
   }

   public static interface ContentProvider
   {
      void writeContent(OutputStream os) throws IOException;
   }

   public ContentProvider getCurrentModelState(String modelId, final ModelFormat modelFormat)
   {
      ModelRepository modelRepository = currentSession().modelRepository();

      final EObject model = modelRepository.findModel(modelId);
      if (null != model)
      {
         final ModelPersistenceHandler<EObject> persistenceHandler = modelRepository.getModelBinding(
               model)
               .getPersistenceHandler(model);
         if (null != persistenceHandler)
         {
            return new ContentProvider()
            {
               @Override
               public void writeContent(OutputStream output) throws IOException
               {
                  switch (modelFormat)
                  {
                  case Native:
                     persistenceHandler.saveModel(model, output);
                     break;

                  case Xpdl:
                     persistenceHandler.saveDeployableModel(model, output);
                     break;
                  }
                  output.flush();
               }
            };
         }
         else
         {
            throw new IllegalStateException("No suitable persistence handler for requested format: ");
         }
      }
      else
      {
         // invalid model ID
         throw new ItemNotFoundException("Invalid model ID: " + modelId);
      }
   }

   public List<ModelLockJto> getEditLocksStatus()
   {
      List<ModelLockJto> jtos = toModelLocksJto();

      return jtos;
   }

   public ModelLockJto getEditLockStatus(String modelId)
   {
      ModelingSession session = currentSession();

      EObject model = session.modelRepository().findModel(modelId);
      if (null == model)
      {
         throw new ItemNotFoundException("Invalid model ID: " + modelId);
      }

      return toModelLockJto(session, model);
   }

   public ModelLockJto breakEditLockForModel(String modelId)
   {
      ModelingSession session = currentSession();

      EObject model = session.modelRepository().findModel(modelId);
      if (null == model)
      {
         throw new ItemNotFoundException("Invalid model ID: " + modelId);
      }

      if ( !session.breakEditLock(model))
      {
         throw new BadRequestException("Failed breaking edit lock.");
      }

      return getEditLockStatus(modelId);
   }

   public ChangeJto undoMostCurrentChange()
   {
      EditingSession editingSession = currentSession().getSession();
      if (editingSession.canUndo())
      {
         Modification undoneChange = editingSession.undoLast();
         postprocessChange(undoneChange);
         ChangeJto jto = toJto(undoneChange);

         if (editingSession.canUndo())
         {
            Modification pendingUndo = editingSession.getPendingUndo();
            postprocessChange(pendingUndo);
            jto.pendingUndoableChange = toJto(pendingUndo);
         }
         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            postprocessChange(pendingRedo);
            jto.pendingRedoableChange = toJto(pendingRedo);
         }

         jto.isUndo = true;

         // TODO include full command?
         commandHandlingMediator().broadcastChange(undoneChange.getSession(), null, toJson(jto));

         return jto;
      }
      else
      {
         throw new ConflictingRequestException("Nothing to be undone");
      }
   }

   public ChangeJto redoMostCurrentlyUndoneChange()
   {
      EditingSession editingSession = currentSession().getSession();
      if (editingSession.canRedo())
      {
         Modification redoneChange = editingSession.redoNext();
         postprocessChange(redoneChange);
         ChangeJto jto = toJto(redoneChange);

         if (editingSession.canUndo())
         {
            Modification pendingUndo = editingSession.getPendingUndo();
            postprocessChange(pendingUndo);
            jto.pendingUndoableChange = toJto(pendingUndo);
         }
         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            postprocessChange(pendingRedo);
            jto.pendingRedoableChange = toJto(pendingRedo);
         }

         jto.isRedo = true;

         // TODO include full command?
         commandHandlingMediator().broadcastChange(redoneChange.getSession(), null, toJson(jto));

         return jto;
      }
      else
      {
         throw new ConflictingRequestException("Nothing to be redone");
      }
   }

   public ChangeJto applyChange(CommandJto commandJto)
   {
      String commandId = commandJto.commandId;
      String modelId = commandJto.modelId;

      ModelRepository modelRepository = currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);

      try
      {
         // obtain session to ensure we hold an edit lock for the model
         EditingSession editingSession = (null != model) //
               ? currentSession().getEditSession(model)
               : currentSession().getSession();

         if (null != model)
         {
            ModelBinding<EObject> modelBinding = currentSession().modelRepository()
                  .getModelBinding(model);
            if (modelBinding.isReadOnly(model)
                  && !(commandId.equalsIgnoreCase("modelLockStatus.update")))
            {
               trace.error("Failed handling command: '" + commandId
                     + "' - Request tried to modify a locked model!");
               throw new MissingWritePermissionException(
                     "Request tried to modify a locked model!");
            }
         }

         if (commandId.startsWith("model."))
         {
            return applyGlobalChange(editingSession, commandId, model, commandJto);
         }
         else
         {
            // change to be interpreted in context of a model
            if (null == model)
            {
               throw new BadRequestException("Unknown model: " + modelId);
            }

            return applyModelElementChange(editingSession, commandId, model, commandJto);
         }
      }
      catch (MissingWritePermissionException mwpe)
      {
         throw new ConflictingRequestException("Missing write permission: " + mwpe.getMessage());
      }
   }

   /**
    * @param editingSession TODO
    * @param commandId
    * @param commandJto
    * @return
    */
   private ChangeJto applyGlobalChange(EditingSession editingSession, String commandId, EObject model, CommandJto commandJto)
   {
      List<ChangeDescriptionJto> changesJson = commandJto.changeDescriptions;

      for (ChangeDescriptionJto changeDescrJto : changesJson) {
         if (null != changeDescrJto) {
            EObject targetElement = null;
            if (null != changeDescrJto.uuid)
            {
               String uuid = changeDescrJto.uuid;
               targetElement = currentSession().uuidMapper().getEObject(uuid);
            }
            else
            {
               targetElement = model;
            }

            JsonObject changeJson = changeDescrJto.changes;
            String modelFormat;
            if (null != model)
            {
               modelFormat = currentSession().modelRepository()
                     .getModelFormat(model);
            }
            else
            {
               modelFormat = extractString(changeJson, "modelFormat");
            }

            ModelCommandsHandler.ModificationDescriptor changes = null;
            for (ModelCommandsHandler handler : springContext.getBeansOfType(
                  ModelCommandsHandler.class).values())
            {
               // TODO make this a regular modification
               if (handler.handlesModel(modelFormat))
               {
                  changes = handler.handleCommand(commandId, targetElement, changeJson);
                  break;
               }
            }
            if (null != changes)
            {
               ChangeJto jto = toJto(commandJto, changes);
               // TODO broadcast change?
               return jto;
            }
            else
            {
               throw new BadRequestException("Unsupported modelFormat: " + modelFormat);
            }
         }
      }

      return null;
   }


   private ChangeJto applyModelElementChange(EditingSession editingSession, String commandId, EObject model, CommandJto commandJto)
   {
      List<CommandHandlingMediator.ChangeRequest> changeDescriptors = newArrayList();

      // pre-process change descriptions
      ModelBinding<EObject> modelBinding = currentSession().modelRepository().getModelBinding(model);
      for (ChangeDescriptionJto changeDescrJto : commandJto.changeDescriptions)
      {
         EObject targetElement = findTargetElement(model, changeDescrJto);

         changeDescriptors.add(new CommandHandlingMediator.ChangeRequest(model,
               targetElement, changeDescrJto.changes));
      }

      // TODO this is not thread safe
      ModelerSessionController.CommandJto = commandJto;

      // dispatch to actual command handler
      Modification change = commandHandlingMediator().handleCommand(editingSession,
            commandId, changeDescriptors);
      if (null != change)
      {
         postprocessChange(change);

         change.getMetadata().put("commandId", commandId);
         change.getMetadata().put("modelId", modelBinding.getModelId(model));
         if (null != commandJto.account)
         {
            change.getMetadata().put("account", commandJto.account);
         }

         ChangeJto jto = toJto(change);
         JsonObject changeJto = toJson(jto);

         commandHandlingMediator().broadcastChange(change.getSession(), commandJto,
               changeJto);

         ModelerSessionController.CommandJto = null;

         return jto;
      }
      else
      {
         throw new BadRequestException("Unsupported change request: " + commandId //
               + " [" + commandJto.changeDescriptions + "]");
      }
   }

   private EObject findTargetElement(EObject model, ChangeDescriptionJto changeDescrJto)
   {
      if (model instanceof ModelType)
      {
         return findTargetElement((ModelType) model, changeDescrJto);
      }
      else
      {
         ModelBinding<EObject> modelBinding = currentSession().modelRepository().getModelBinding(model);
         ModelNavigator<EObject> modelNavigator = modelBinding.getNavigator();
         if ( !isEmpty(changeDescrJto.uuid))
         {
            return modelNavigator.findElementByUuid(model, changeDescrJto.uuid);
         }
         else if ( !isEmpty(changeDescrJto.oid))
         {
            // HACK sometimes the modelId is passed in the oid field
            if (modelBinding.getModelId(model).equals(changeDescrJto.oid))
            {
               return model;
            }

            return modelNavigator.findElementByOid(model, Long.valueOf(changeDescrJto.oid));
         }
         else
         {
            throw new BadRequestException("Missing context element identifier: "
                  + changeDescrJto);
         }
      }
   }

   private EObject findTargetElement(ModelType model, ChangeDescriptionJto changeDescrJto)
   {
      EObject targetElement = null;
      // existing target, identified by uuid
      if (null != changeDescrJto.uuid)
      {
         String uuid = changeDescrJto.uuid;
         targetElement = currentSession().uuidMapper().getEObject(uuid);

         if (null == targetElement)
         {
            throw new BadRequestException("Unknown target element for element UUID "
                  + uuid);
         }
      }
      else if (null != changeDescrJto.oid)
      {
         // existing target, identified by oid
         String oid = changeDescrJto.oid;
         if (model.getId().equals(oid))
         {
            targetElement = model;
         }
         else
         {
            long parsedOid = Long.parseLong(oid);
            // deep search for model element by OID
            // TODO can lookup faster as oid is declared the XML index
            // field?
            for (Iterator<? > i = model.eAllContents(); i.hasNext();)
            {
               Object element = i.next();
               if ((element instanceof IModelElement)
                     && ((((IModelElement) element).getElementOid() == parsedOid)))
               {
                  targetElement = (IModelElement) element;
                  break;
               }
            }
         }

         if (null == targetElement)
         {
            throw new BadRequestException("Unknown target element for element OID " + oid
                  + " within model " + model.getId());
         }
      }
      else
      {
         throw new BadRequestException("Missing target element identifier: "
               + changeDescrJto);
      }

      return targetElement;
   }

   public void postprocessChange(Modification change)
   {
      ChangesetPostprocessingService postprocessingService = springContext.getBean(ChangesetPostprocessingService.class);

      postprocessingService.postprocessChangeset(change);
   }

   public CommandHandlingMediator commandHandlingMediator()
   {
      try
      {
         CommandHandlingMediator twophaseMediator = springContext.getBean(CommandHandlingMediator.class);
         if (null != twophaseMediator)
         {
            trace.info("Using two-phase command handling.");
            return twophaseMediator;
         }
      }
      catch (BeansException be)
      {
         // failed resolving twophase mediator, fall back to simple mediator
      }

      final SimpleCommandHandlingMediator mediator = springContext.getBean(SimpleCommandHandlingMediator.class);
      return new CommandHandlingMediator()
      {
         @Override
         public void broadcastChange(EditingSession session, org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto commandJto, JsonObject changeJson)
         {
            mediator.broadcastChange(session, commandJto, changeJson);
         }

         @Override
         public Modification handleCommand(EditingSession editingSession,
               String commandId, List<ChangeRequest> changes)
         {
            try
            {
               return mediator.handleCommand(editingSession, commandId, changes);
            }
            catch (Exception e)
            {
               trace.warn("Failed handling command '" + commandId + "'", e);

               return new Modification(editingSession, e);
            }
         }
      };
   }

   private ModelingSession currentSession()
   {
      return sessionLocator.currentModelingSession();
   }

   public static class ChangeJto
   {
      public String id;
      public long timestamp;
      public String commandId;
      public String modelId;
      public String account;

      public ChangesJto changes = new ChangesJto();

      public List<ProblemJto> problems = null;

      public String pendingUndo;
      public String pendingRedo;

      //TODO pendingUndoableChange / pendingRedoableChange is a temporary addition
      //will be replaced with something concrete once requirement is clear
      public ChangeJto pendingUndoableChange;
      public ChangeJto pendingRedoableChange;

      public Boolean isUndo;
      public Boolean isRedo;

      public UiStateJto uiState;

      public static class ChangesJto
      {
         public JsonArray modified = new JsonArray();
         public JsonArray added = new JsonArray();
         public JsonArray removed = new JsonArray();
      };

      public static class ProblemJto
      {
         public String severity;
         public String message;
      }

      public static class UiStateJto
      {
         public List<ModelLockJto> modelLocks;
      }
   };

   public static class ModelLockJto
   {
      public static final String STATUS_NOT_LOCKED = "";

      public static final String STATUS_LOCKED_BY_ME = "lockedByMe";

      public static final String STATUS_LOCKED_BY_OTHER = "lockedByOther";

      public String modelId;

      public String lockStatus = STATUS_NOT_LOCKED;

      public String ownerId;

      public String ownerName;

      public boolean canBreakEditLock = false;
   }
}