/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XPDLFinderUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.cap.AbstractMerger;
import org.eclipse.stardust.ui.web.modeler.cap.CopyPasteUtil;
import org.eclipse.stardust.ui.web.modeler.cap.OutlineMerger;
import org.eclipse.stardust.ui.web.modeler.cap.StoreObject;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class ProcessChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;
   private StoreObject storage;

   @OnCommand(commandId = "process.create")
   public void createProcess(ModelType model, JsonObject request)
   {
      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(model, null, extractString(request, ModelerConstants.NAME_PROPERTY),
            extractString(request, "defaultLaneName"), extractString(request, "defaultPoolName"));

      // Add process definition to UUID map.

      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(processDefinition);
      DiagramType diagram = processDefinition.getDiagram().get(0);
      for(LaneSymbol lane : diagram.getPoolSymbols().get(0).getLanes())
      {
         mapper.map(lane);
      }
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "process.delete")
   public void deleteProcess(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      ProcessDefinitionType processDefinition = XPDLFinderUtils.findProcessDefinition(model, id);
      synchronized (model)
      {
    	  model.getProcessDefinition().remove(processDefinition);
      }
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "process.clone")
   public void cloneProcess(ModelType model, JsonObject request)
   {
      String processID = request.get("id").getAsString();
      ProcessDefinitionType processDefinition = XPDLFinderUtils.findProcessDefinition(model, processID);

      ProcessDefinitionType clonedProcess = cloneProcess(model, processDefinition);

      //Post clone operations

      fixReferencesForIntermediateEvents(processDefinition, clonedProcess);
      fixExternalReferences(processDefinition, clonedProcess);
      addUUIDs(clonedProcess);

   }

   private void fixExternalReferences(ProcessDefinitionType processDefinition,
         ProcessDefinitionType clonedProcess)
   {
      for (Iterator<ActivityType> i = processDefinition.getActivity().iterator(); i
            .hasNext();)
      {
         ActivityType activity = i.next();
         if (activity.getExternalRef() != null)
         {
            IdRef idRef = EcoreUtil.copy(activity.getExternalRef());
            ActivityType clonedActivity = XPDLFinderUtils.findActivity(clonedProcess,
                  activity.getId());
            clonedActivity.setExternalRef(idRef);
         }

         AttributeType attribute = AttributeUtil.getAttribute(activity,
               "carnot:connection:uri");

         if (attribute != null)
         {
            ActivityType clonedActivity = XPDLFinderUtils.findActivity(clonedProcess,
                  activity.getId());
            AttributeUtil.setAttribute(clonedActivity, "carnot:connection:uri",
                  attribute.getAttributeValue());
         }

      }

   }

   public ProcessDefinitionType cloneProcess(ModelType model,
         ProcessDefinitionType processDefinition)
   {
      List copySet = null;
      ArrayList list = new ArrayList();
      list.add(processDefinition);
      copySet = CopyPasteUtil.createCopySet(new Integer(1), list, processDefinition, false);
      AbstractMerger util;
      copySet = extractStorage(copySet);
      util = new OutlineMerger(model, copySet, storage);
      util.merge();
      ProcessDefinitionType clonedProcess = util.getClonedProcess(processDefinition.getId());
      return clonedProcess;
   }

   private void fixReferencesForIntermediateEvents(
         ProcessDefinitionType processDefinition, ProcessDefinitionType clonedProcess)
   {
      List<IntermediateEventSymbol> originalSymbols  = getAllIntermediateEventSymbols(processDefinition);
      List<IntermediateEventSymbol> clonedSymbols  = getAllIntermediateEventSymbols(clonedProcess);

      for (int i = 0; i < originalSymbols.size(); i++)
      {
         IntermediateEventSymbol originalSymbol = originalSymbols.get(i);
         IntermediateEventSymbol clonedSymbol = clonedSymbols.get(i);
         ActivityType originalActivity = EventMarshallingUtils.resolveHostActivity(originalSymbol);
         ActivityType clonedActivity = (ActivityType) ModelUtils.findElementById(clonedProcess.getActivity(), originalActivity);

         AttributeUtil.setAttribute(clonedActivity, "stardust:bpmnEvent:" + originalSymbol.getElementOid(), null);
         String xmlNodeValue = AttributeUtil.getCDataAttribute(originalActivity, "stardust:bpmnEvent:" + originalSymbol.getElementOid());
         AttributeUtil.setCDataAttribute(clonedActivity, "stardust:bpmnEvent:" + clonedSymbol.getElementOid(), xmlNodeValue);

         EventHandlerType originalHandler = null;

         for (Iterator<EventHandlerType> j = originalActivity.getEventHandler().iterator(); j.hasNext();)
         {
            EventHandlerType eventHandler = j.next();
            if (AttributeUtil.getAttribute(eventHandler, "stardust:bpmnEvent:" + originalSymbol.getElementOid()) != null)
            {
               originalHandler = eventHandler;
            }
         }

         for (Iterator<EventHandlerType> j = clonedActivity.getEventHandler().iterator(); j.hasNext();)
         {
            EventHandlerType eventHandler = j.next();
            if (AttributeUtil.getAttribute(eventHandler, "stardust:bpmnEvent:" + originalSymbol.getElementOid()) != null)
            {
               AttributeUtil.setAttribute(eventHandler, "stardust:bpmnEvent:" + originalSymbol.getElementOid(), null);
               xmlNodeValue = AttributeUtil.getCDataAttribute(originalHandler, "stardust:bpmnEvent:" + originalSymbol.getElementOid());
               AttributeUtil.setCDataAttribute(eventHandler, "stardust:bpmnEvent:" + clonedSymbol.getElementOid(), xmlNodeValue);
            }
         }
      }
   }

   private void addUUIDs(ProcessDefinitionType clonedProcess)
   {
      //Setting modelUUID for new cloned process
      AttributeUtil.setAttribute(clonedProcess, "carnot:model:uuid", UUID
            .randomUUID().toString());

      //Setting modelUUID for all cloned activities
      for (Iterator<ActivityType> i = clonedProcess.getActivity().iterator(); i.hasNext();)
      {
         ActivityType activity = i.next();
         AttributeUtil.setAttribute(activity, "carnot:model:uuid", UUID
               .randomUUID().toString());
      }

      //Performing session based UUID mapping for cloned process and its content elements
      modelService().uuidMapper().map(clonedProcess);
      for (TreeIterator<EObject> i = clonedProcess.eAllContents(); i.hasNext();)
      {
         EObject o = i.next();
         modelService().uuidMapper().map(o);
      }
   }

   private List<IntermediateEventSymbol> getAllIntermediateEventSymbols(
         ProcessDefinitionType process)
   {
      List<IntermediateEventSymbol> symbols = new ArrayList<IntermediateEventSymbol>();
      for (Iterator<DiagramType> i = process.getDiagram().iterator(); i.hasNext();)
      {
         DiagramType diagram = i.next();
         for (Iterator<PoolSymbol> j = diagram.getPoolSymbols().iterator(); j.hasNext();)
         {
            PoolSymbol poolSymbol = j.next();
            for (Iterator<LaneSymbol> k = poolSymbol.getLanes().iterator(); k.hasNext();)
            {
               LaneSymbol lane = k.next();
               symbols.addAll(lane.getIntermediateEventSymbols());
               // Todo ChildLanes ?
            }
         }
      }
      return symbols;

   }

   protected List extractStorage(List currentContent)
   {
      List copySet = new ArrayList();
      for (int i = 0; i < currentContent.size(); i++)
      {
         Object entry = currentContent.get(i);
         if (entry instanceof StoreObject)
         {
            storage = (StoreObject) entry;
            storage.setSameModel(true);
         }
         else
         {
            copySet.add(entry);
         }
      }
      return copySet;
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}
