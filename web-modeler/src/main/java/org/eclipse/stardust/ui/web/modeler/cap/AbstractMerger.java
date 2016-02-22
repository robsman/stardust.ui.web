/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.cap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationContextTypeType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationTypeType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.BindActionType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.Code;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DataTypeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventConditionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IConnectionSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElementNodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.IdentifiableReference;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.ParameterMappingType;
import org.eclipse.stardust.model.xpdl.carnot.ParticipantType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerTypeType;
import org.eclipse.stardust.model.xpdl.carnot.UnbindActionType;
import org.eclipse.stardust.model.xpdl.carnot.merge.MergeUtils;
import org.eclipse.stardust.model.xpdl.carnot.merge.UUIDUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.DiagramUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.cap.InputContainer.Container;
import org.eclipse.stardust.ui.web.modeler.cap.MergerUtil.MergerEntry;

/**
 * @author grotjahn
 * @version $Revision: 71751 $
 */
public abstract class AbstractMerger
{
   public static final String AUTHORIZATION_SCOPE = "authorization:"; //$NON-NLS-1$

   protected Set structuredData = new HashSet();
   protected List structuredDataNameIdCache = new ArrayList();
   // if there is already a structured type in the target model with same name
   // a new name must be used, we must keep the relation between old and new id
   private Map structuredDataChangedCache = new HashMap();
   private TypeDeclarationsType targetDeclarations;

   protected boolean externalReferences = false;
   protected boolean showDialog = false;
   protected Map mergeElements = new HashMap();

   protected Map<ProcessDefinitionType, ProcessDefinitionType> processes = new HashMap<ProcessDefinitionType, ProcessDefinitionType>();
   protected Map processChildren = new HashMap();

   protected Map<DiagramType, DiagramType> diagrams = new HashMap<DiagramType, DiagramType>();

   protected Map<ActivityType, ActivityType> activities = new HashMap<ActivityType, ActivityType>();
   protected Map<TypeDeclarationType, TypeDeclarationType> typeDeclarationElements =
      new HashMap<TypeDeclarationType, TypeDeclarationType>();
   protected Map elements = new HashMap();
   protected Map globalElements = new HashMap();
   protected Map processElements = new HashMap();

   // data container for reference value dialog
   protected InputContainer referenceValueInput = new InputContainer();
   // stores content for the dialog
   protected Container container = null;

   // to copy an EObject
   protected Copier copier = new EcoreUtil.Copier()
   {
      private static final long serialVersionUID = 1L;

      public EObject copy(EObject object)
      {
         if(object instanceof XSDSchema)
         {
            XSDSchema original = (XSDSchema) object;
            XSDSchema clone = (XSDSchema) original.cloneConcreteComponent(true, false);
            Document doc = clone.updateDocument();
            if(original.getElement() != null)
            {
               Element clonedElement = (Element) doc.importNode(original.getElement(), true);
               doc.appendChild(clonedElement);
               clone.setElement(clonedElement);
            }
            return clone;
         }
         return super.copy(object);
      }
   };

   // a cache to check for duplicate name/Id (parent, eclass, hashmap)
   protected Map nameIdCache = new HashMap();
   // to set the new oids
   protected long highestOid = 0;
   // for undo/redo
   protected boolean modelChanged = false;
   // here we store original Model, Diagram, etc.
   protected StoreObject storage;
   // target
   protected ModelType targetModel;
   protected DiagramType targetDiagram;
   protected ProcessDefinitionType targetProcess;
   // only in diagram
   protected boolean isDiagram = false;
   private List<ModelVariable> targetVariables;

   protected boolean isSameModel = false;


   private Set xsdFiles = new HashSet();

   // the result of the reference value dialog, keeps the elements to be copied by value
   protected Map<EObject, EObject> changedCache = new HashMap<EObject, EObject>();

   public abstract void merge();

   public AbstractMerger(ModelType targetModel, StoreObject storage)
   {
      this.storage = storage;
      this.targetModel = targetModel;
      // build name/ID cache, set highestOid
      collectTargetData();
      // stores content for the dialog
      container = referenceValueInput.getContainer();
      // is it the same model?
      isSameModel = storage.isSameModel();
      storage.setTargetModel(targetModel);


      if(!isSameModel)
      {
         VariableContext context = VariableContextHelper.getInstance().getContext(targetModel);
         if(context != null)
         {
            context.refreshVariables(targetModel);
            targetVariables = new ArrayList<ModelVariable>(context.getVariables());
         }
      }
   }

   public ProcessDefinitionType getClonedProcess(String originalProcessID)
   {
      for (Iterator<ProcessDefinitionType> i = this.processes.keySet().iterator(); i
            .hasNext();)
      {
         ProcessDefinitionType process = i.next();
         if (process.getId().equals(originalProcessID))
         {
            return processes.get(process);
         }
      }
      return null;
   }

   /**
    * sets the highest oid
    * and builds a name/ID cache
    */
   protected void collectTargetData()
   {
      highestOid = ModelUtils.getMaxUsedOid(targetModel);

      String id;
      String name;
      // here we scan all children and children of those children
      for (Iterator i = targetModel.eAllContents(); i.hasNext();)
      {
         EObject child = (EObject) i.next();
         EClass eClass = child.eClass();
         EObject parent = child.eContainer();
         Map eClassNameIdCache = new HashMap();
         Object localNameIdCache = null;

         if(nameIdCache.containsKey(parent))
         {
            eClassNameIdCache = (HashMap) nameIdCache.get(parent);
            if(eClassNameIdCache.containsKey(eClass))
            {
               if (child instanceof DiagramType)
               {
                  localNameIdCache = (List) eClassNameIdCache.get(eClass);
               }
               else
               {
                  localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);
               }
            }
         }
         if(localNameIdCache == null)
         {
            if (child instanceof DiagramType)
            {
               localNameIdCache = new ArrayList();
            }
            else
            {
               localNameIdCache = new HashMap();
            }
         }
         if (child instanceof IIdentifiableElement)
         {
            id = ((IIdentifiableElement) child).getId();
            name = ((IIdentifiableElement) child).getName();
            ((HashMap) localNameIdCache).put(id, name);
            eClassNameIdCache.put(eClass, localNameIdCache);
         }
         else if (child instanceof IModelElement)
         {
            if (child instanceof DiagramType)
            {
               name = ((DiagramType) child).getName();
               ((ArrayList) localNameIdCache).add(name);
               eClassNameIdCache.put(eClass, localNameIdCache);
            }
         }
         if (child instanceof TypeDeclarationType)
         {
            id = ((TypeDeclarationType) child).getId();
            name = ((TypeDeclarationType) child).getName();
            ((HashMap) localNameIdCache).put(id, name);
            eClassNameIdCache.put(eClass, localNameIdCache);
         }
         nameIdCache.put(parent, eClassNameIdCache);
      }
   }

   public Map<EObject, EObject> getChangedCache()
   {
      return changedCache;
   }

   // check to see if we must open a dialog to change name/id
   // check the nameid cache
   protected boolean checkElementInModel(Entry entry)
   {
      // key is the original (from the copied model)
      EObject raw = (EObject) entry.getKey();
      // we must compare the copy because we may have changed the values already in the other dialog
      EObject copy = (EObject) entry.getValue();
      EClass eClass = copy.eClass();

      EObject parent = raw.eContainer();
      EObject checkParent = (EObject) changedCache.get(parent);
      if(checkParent != null)
      {
         parent = checkParent;
      }

      parent = CopyPasteUtil.getSameModelElement(parent, targetModel, null);
      if(!processChildren.isEmpty() && processChildren.containsKey(raw) &&
            (raw instanceof ActivityType
            || raw instanceof TransitionType
            || raw instanceof TriggerType))
      {
         parent = storage.getTargetProcess();
      }

      String id;
      String name;

      if(isDiagram)
      {
         if(parent instanceof ProcessDefinitionType
               || (raw instanceof ActivityType && parent == null)
               || (raw instanceof TransitionType && parent == null)
               || (raw instanceof TriggerType && parent == null))
         {
            parent = storage.getTargetProcess();
         }
      }
      Map eClassNameIdCache = new HashMap();
      Object localNameIdCache = null;

      if(nameIdCache.containsKey(parent))
      {
         eClassNameIdCache = (HashMap) nameIdCache.get(parent);
         if(eClassNameIdCache.containsKey(eClass))
         {
            if (copy instanceof DiagramType)
            {
               name = ((DiagramType) copy).getName();
               localNameIdCache = (List) eClassNameIdCache.get(eClass);
               if(((ArrayList) localNameIdCache).contains(name))
               {
                  return true;
               }
            }
            else if (copy instanceof IIdentifiableElement)
            {
               id = ((IIdentifiableElement) copy).getId();
               name = ((IIdentifiableElement) copy).getName();
               localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);
               if(((HashMap) localNameIdCache).containsKey(id)
                     || ((HashMap) localNameIdCache).containsValue(name))
               {
                  return true;
               }
            }
            else if (copy instanceof TypeDeclarationType)
            {
               id = ((TypeDeclarationType) copy).getId();
               name = ((TypeDeclarationType) copy).getName();
               localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);
               if(((HashMap) localNameIdCache).containsKey(id)
                     || ((HashMap) localNameIdCache).containsValue(name))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   // merge a single element (may open dialog)
   protected boolean mergeElement(Entry<EObject, EObject> entry)
   {
      EObject raw = (EObject) entry.getKey();

      // does the other dialog already changed name/id or is there already an element with same name/id
      if (MergerUtil.getEntryFromMap(changedCache, raw) == null
            && checkElementInModel(entry))
      {
         // the element copy will change id and name here
         if(!openDialog(entry))
         {
            modelChanged = false;
            return false;
         }
         else
         {
            changedCache.put(entry.getKey(), entry.getValue());
         }
      }
      // nameidcache must be updated
      // but for now it works without updating the cache

      // add the model element
      addModelElement(entry, true);
      return true;
   }

   // add the model element to the targetModel
   // if copyEntry is true, only the element is added
   // if copyEntry is false, all object references  will be set to the new objects (recursive)
   protected boolean addModelElement(Entry entry, boolean copyEntry)
   {
      /**
       * in the 1st stage we really add the model element where it belongs to
       * this is necessary because when setting references/objects the elements must be there
       *
       * in the 2nd stage we have the entry and must set objects and references
       * we must take the element from the model and change it in the model
       *
       * use the entry, because it is a reference, seems to work
      */

      // raw is the element in the copied model, copy may has changed id and name already
      // we add the copy to the list
      EObject raw = (EObject) entry.getKey();
      EObject copy = (EObject) entry.getValue();

      EObject parent = raw.eContainer();

      // the copy is already in the model, so we use the copy
      // because search is done by name/id

      boolean haveParent = false;
      if(!processChildren.isEmpty() && processChildren.containsKey(raw) &&
            (raw instanceof ActivityType
            || raw instanceof TransitionType
            || raw instanceof TriggerType))
      {
         parent = storage.getTargetProcess();
         haveParent = true;
      }

      if(!haveParent)
      {
         if(!copyEntry)
         {
            parent = copy.eContainer();
         }
         else
         {
            EObject checkParent = (EObject) changedCache.get(parent);
            if(checkParent != null)
            {
               parent = checkParent;
            }
         }
      }

      if(copyEntry)
      {
         EObject targetObject;
         if(haveParent)
         {
            targetObject = parent;
         }
         else
         {
            targetObject = CopyPasteUtil.getSameModelElement(parent, targetModel, changedCache);
         }
         if(isDiagram)
         {
            if(parent instanceof ProcessDefinitionType)
            {
               targetObject = storage.getTargetProcess();
            }
         }
         // do we need this?
         if(parent instanceof TypeDeclarationsType)
         {
            targetObject = targetModel.getTypeDeclarations();
         }

         // generic, feature of the container that holds the object
         EStructuralFeature feature = raw.eContainingFeature();
         if(feature != null && targetObject != null)
         {
            // get the container from the target where this element has to be copied to
            List list = (List) targetObject.eGet(feature);
            // add element
            list.add(copy);
            // references later
            modelChanged = true;
            return true;
         }
         return true;
      }

      // emf
      EList contentObjects = copy.eContents();
      List removeObjects = new ArrayList();
      for(int i = 0; i < contentObjects.size(); i++)
      {
         EObject content = (EObject) contentObjects.get(i);
         if(content instanceof IModelElement)
         {
            EStructuralFeature feature = content.eContainingFeature();
            EList copy_container = (EList) copy.eGet(feature);
            EList raw_container = (EList) raw.eGet(feature);
            int index = copy_container.indexOf(content);
            EObject rawContent = (EObject) raw_container.get(index);
            // if we have it, check if it can be deleted otherwise set new references (if)
            if(rawContent != null)
            {
               MergerEntry mergeEntry = new MergerEntry(rawContent, content);
               // set oid and references
               if(!addModelElement(mergeEntry, false))
               {
                  removeObjects.add(content);
               }
            }
            else
            {
               // removeObjects.add(content);
            }
         }
      }

      EStructuralFeature feature = copy.eContainingFeature();
      for(int i = 0; i < removeObjects.size(); i++)
      {
         EObject remove = (EObject) removeObjects.get(i);
         List list = (List) copy.eGet(feature);
         list.remove(remove);
      }

      if (copy instanceof TypeDeclarationType)
      {
         String uuid = UUIDUtils.getUUID(raw);
         if (!StringUtils.isEmpty(uuid))
         {
            UUIDUtils.setUUID((TypeDeclarationType) copy, uuid);
         }
      }

      if(copy instanceof ProcessDefinitionType
            || copy instanceof DataType
            || copy instanceof ActivityType)
      {
         List<AttributeType> attributes = ((IExtensibleElement) raw).getAttribute();
         List<AttributeType> targetAttributes = ((IExtensibleElement) copy).getAttribute();
         for (int i = 0; i < attributes.size(); i++)
         {
            AttributeType attribute = (AttributeType) attributes.get(i);
            if(attribute.getName().startsWith(AbstractMerger.AUTHORIZATION_SCOPE))
            {
               EObject attributeModel = AttributeUtil.getReferenceElement(attribute);
               if(attributeModel != null)
               {
                  IIdentifiableModelElement modelElement = getTargetModelElement(targetModel, (IIdentifiableModelElement) attributeModel);
                  if(modelElement != null)
                  {
                     // here we must use the same attribute, because more attributes with same id are possible
                     AttributeType targetAttribute = (AttributeType) targetAttributes.get(i);
                     if(targetAttribute != null)
                     {
                        AttributeUtil.setReference(targetAttribute, modelElement);
                     }
                  }
               }
            }
         }

         if(copy instanceof ProcessDefinitionType)
         {
            //Really necessary?
            //MergeUtils.mergeFormalParameter((ProcessDefinitionType) raw, (ProcessDefinitionType) copy);
         }
      }

      // EventActionTypeType must be set manual (not done by copier)
      if (copy instanceof BindActionType)
      {
         EventActionTypeType rawActionType = ((BindActionType) raw).getType();
         if(rawActionType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawActionType);
            if(modelElement != null)
            {
               ((BindActionType) copy).setType((EventActionTypeType) modelElement);
            }
         }

      }
      if (copy instanceof UnbindActionType)
      {
         EventActionTypeType rawActionType = ((UnbindActionType) raw).getType();
         if(rawActionType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawActionType);
            if(modelElement != null)
            {
               ((UnbindActionType) copy).setType((EventActionTypeType) modelElement);
            }
         }
      }
      if (copy instanceof EventActionType)
      {
         // triggered process is also copied if any, we must set the reference here
         ProcessDefinitionType targetProcess = null;
         AttributeType attribute = AttributeUtil.getAttribute((IExtensibleElement) raw,
               PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
         if (attribute != null)
         {
            if (attribute.getReference() != null)
            {
               ProcessDefinitionType rawProcess = (ProcessDefinitionType) attribute.getReference().getIdentifiable();
               if(rawProcess != null)
               {
                  targetProcess = (ProcessDefinitionType) getTargetModelElement(targetModel, rawProcess);
               }
            }
         }
         if(targetProcess != null)
         {
            attribute = AttributeUtil.getAttribute((IExtensibleElement) copy,
                  PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
            if (attribute != null)
            {
               if (attribute.getReference() != null)
               {
                  AttributeUtil.setReference(attribute, targetProcess);
               }
            }
         }
         EventActionTypeType rawActionType = ((EventActionType) raw).getType();
         if(rawActionType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawActionType);
            if(modelElement != null)
            {
               ((EventActionType) copy).setType((EventActionTypeType) modelElement);
            }
         }
      }

      // type must be set manual (not done by copier)
      if (copy instanceof EventHandlerType)
      {
         EventConditionTypeType rawConditionType = ((EventHandlerType) raw).getType();
         if(rawConditionType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawConditionType);
            if(modelElement != null)
            {
               ((EventHandlerType) copy).setType((EventConditionTypeType) modelElement);
            }
         }
      }

      // if it is diagram copy it is not necessary to do recursive calls (???)
      if(copy instanceof ActivityType)
      {
         // do more generic EMF?
         IModelParticipant rawParticipant = ((ActivityType) raw).getPerformer();
         if(rawParticipant != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawParticipant);
            if(modelElement != null)
            {
               ((ActivityType) copy).setPerformer((IModelParticipant) modelElement);
            }
         }
         IModelParticipant rawQualityControlParticipant = ((ActivityType) raw).getQualityControlPerformer();
         if(rawQualityControlParticipant != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawQualityControlParticipant);
            if(modelElement != null)
            {
               ((ActivityType) copy).setQualityControlPerformer((IModelParticipant) modelElement);
            }
         }

         ApplicationType rawApplication = ((ActivityType) raw).getApplication();
         if(rawApplication != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawApplication);
            if(modelElement != null)
            {
               ((ActivityType) copy).setApplication((ApplicationType) modelElement);
            }
         }
         ProcessDefinitionType rawProcess = ((ActivityType) raw).getImplementationProcess();
         if(rawProcess != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawProcess);
            if(modelElement != null)
            {
               ((ActivityType) copy).setImplementationProcess((ProcessDefinitionType) modelElement);
            }
         }

         EList<Code> rawValidQualityCodes = ((ActivityType) raw).getValidQualityCodes();
         if(rawValidQualityCodes != null && !rawValidQualityCodes.isEmpty())
         {
            EList<Code> targetValidQualityCodes = ((ActivityType) copy).getValidQualityCodes();
            targetValidQualityCodes.clear();
            for(Code code : rawValidQualityCodes)
            {
               Code targetCode = MergerUtil.containsQC(targetModel, code);
               if(targetCode != null)
               {
                  targetValidQualityCodes.add(targetCode);
               }
            }
         }
      }

      // when we have a lane symbol the lane may have a participant assigned
      if(copy instanceof LaneSymbol)
      {
         IModelParticipant rawLaneParticipant = ((LaneSymbol) raw).getParticipantReference();
         if(rawLaneParticipant != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawLaneParticipant);
            if(modelElement != null)
            {
               ((LaneSymbol) copy).setParticipantReference((IModelParticipant) modelElement);
            }
         }
      }

      if(copy instanceof IModelElementNodeSymbol)
      {
         IIdentifiableModelElement rawModelElement = ((IModelElementNodeSymbol) raw).getModelElement();
         if(rawModelElement != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawModelElement);
            if(modelElement != null)
            {
               ((IModelElementNodeSymbol) copy).setModelElement(modelElement);
            }
         }
      }

      if(copy instanceof IConnectionSymbol)
      {
         if(raw instanceof TransitionConnectionType)
         {
            TransitionType rawTransition = ((TransitionConnectionType) raw).getTransition();
            if(rawTransition != null)
            {
               IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawTransition);
               if(modelElement != null)
               {
                  ((TransitionConnectionType) copy).setTransition((TransitionType) modelElement);
               }
            }
         }
      }

      if(copy instanceof DataPathType)
      {
         DataType rawDataType = ((DataPathType) raw).getData();
         if(rawDataType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawDataType);
            if(modelElement != null)
            {
               ((DataPathType) copy).setData((DataType) modelElement);
            }
         }
      }

      if(copy instanceof TriggerType)
      {
         // set the type here
         TriggerTypeType rawTriggerType = ((TriggerType) raw).getType();
         if(rawTriggerType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawTriggerType);
            if(modelElement != null)
            {
               ((TriggerType) copy).setType((TriggerTypeType) modelElement);
            }
         }

         EList attributes = ((TriggerType) raw).getAttribute();
         for(int a = 0; a < attributes.size(); a++)
         {
            AttributeType attribute = (AttributeType) attributes.get(a);
            IdentifiableReference reference = attribute.getReference();
            if(reference != null)
            {
               IIdentifiableModelElement modelElement = getTargetModelElement(parent, (IIdentifiableModelElement) reference.getIdentifiable());
               if(modelElement != null)
               {
                  AttributeUtil.setReference((TriggerType) copy, attribute.getName(), modelElement);
               }
            }
         }
      }

      if (copy instanceof ParameterMappingType)
      {
         DataType rawDataType = ((ParameterMappingType) raw).getData();
         if(rawDataType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawDataType);
            if(modelElement != null)
            {
               ((ParameterMappingType) copy).setData((DataType) modelElement);
            }
         }
      }

      // attributes
      if (copy instanceof DataType)
      {
         DataTypeType rawDataTypeType = ((DataType) raw).getType();
         if(rawDataTypeType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawDataTypeType);
            if(modelElement != null)
            {
               ((DataType) copy).setType((DataTypeType) modelElement);
            }
            // is type structured data?, maybe also DMS
            if(rawDataTypeType.getId().equals(StructuredDataConstants.STRUCTURED_DATA))
            {
               String rawStructuredDataId = AttributeUtil.getAttributeValue((IExtensibleElement) raw, StructuredDataConstants.TYPE_DECLARATION_ATT);
               if(!StringUtils.isEmpty(rawStructuredDataId))
               {
                  // check changed id cache
                  String newId = (String) structuredDataChangedCache.get(rawStructuredDataId);
                  if(newId == null)
                  {
                     newId = rawStructuredDataId;
                  }
                  TypeDeclarationType typeDeclaration = targetDeclarations.getTypeDeclaration(newId);
                  // set as reference
                  if(typeDeclaration != null)
                  {
                     AttributeUtil.setReference((IExtensibleElement) copy, StructuredDataConstants.TYPE_DECLARATION_ATT, typeDeclaration);
                  }
               }
            }
         }
         if(((DataType) raw).isPredefined())
         {
            ((DataType) copy).setPredefined(false);
         }
      }

      if(copy instanceof ConditionalPerformerType)
      {
         DataType dataType = ((ConditionalPerformerType) raw).getData();
         if(dataType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, dataType);
            if(modelElement != null)
            {
               ((ConditionalPerformerType) copy).setData((DataType) modelElement);
            }
         }
         String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) raw, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
         if(!StringUtils.isEmpty(dataId))
         {
            AttributeUtil.setAttribute((IExtensibleElement) copy, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA, dataId);
         }
      }

      // check if this is OK
      if (copy instanceof OrganizationType)
      {
         // raw
         EList participants = ((OrganizationType) raw).getParticipant();
         IModelParticipant teamLead = ((OrganizationType) raw).getTeamLead();
         if(teamLead != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, teamLead);
            if(modelElement != null)
            {
               ((OrganizationType) copy).setTeamLead((RoleType) modelElement);
            }
         }

         // clear the copied participants and set the correct ones
         ((OrganizationType) copy).getParticipant().clear();
         for(int i = 0; i < participants.size(); i++)
         {
            IModelParticipant rawParticipant = ((ParticipantType) participants.get(i)).getParticipant();
            if(rawParticipant != null)
            {
               // we must find the new participant, not raw
               IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawParticipant);
               if(modelElement != null)
               {
                  ParticipantType participantType = CarnotWorkflowModelFactory.eINSTANCE.createParticipantType();
                  participantType.setParticipant((IModelParticipant) modelElement);
                  ((OrganizationType) copy).getParticipant().add(participantType);
               }
            }
         }
         ((OrganizationType) copy).getOrganizationSymbols().clear();
      }

      if(copy instanceof ContextType)
      {
         ApplicationContextTypeType rawApplicationContextType = ((ContextType) raw).getType();
         if(rawApplicationContextType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawApplicationContextType);
            if(modelElement != null)
            {
               ((ContextType) copy).setType((ApplicationContextTypeType) modelElement);
            }
         }
      }

      if (copy instanceof ApplicationType)
      {
         // the access points have the same oids like in the original
         // extended attributes only if same model?
         ApplicationTypeType rawApplicationType = ((ApplicationType) raw).getType();
         if(rawApplicationType != null)
         {
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, rawApplicationType);
            if(modelElement != null)
            {
               ((ApplicationType) copy).setType((ApplicationTypeType) modelElement);
            }
         }
         DataTypeType structuredDataType = ModelUtils.getDataType((IModelElement) copy, StructuredDataConstants.STRUCTURED_DATA);

         EList accessPoints = ((ApplicationType) copy).getAccessPoint();
         for(int i = 0; i < accessPoints.size();i++)
         {
            AccessPointType accessPoint = (AccessPointType) accessPoints.get(i);
            if(accessPoint.getType().getId().equals(PredefinedConstants.STRUCTURED_DATA))
            {
               accessPoint.setType(structuredDataType);
            }
            DataTypeType type = accessPoint.getType();
            IIdentifiableModelElement modelElement = getTargetModelElement(parent, type);
            if(modelElement != null)
            {
               accessPoint.setType((DataTypeType) modelElement);
            }
         }
      }

      if(copy instanceof DataMappingType)
      {
         DataType targetDataType = ((DataMappingType) raw).getData();
         IIdentifiableModelElement modelElement = null;
         if(targetDataType != null)
         {
            modelElement = getTargetModelElement(parent, targetDataType);
            if(modelElement != null)
            {
               ((DataMappingType) copy).setData((DataType) modelElement);
            }
            else
            {
               // remove
               return false;
            }
         }
      }
      return true;
   }

   // find the target object in the new model (to assign it where it belongs to)
   protected IIdentifiableModelElement getTargetModelElement(EObject parent, IIdentifiableModelElement rawModelElement)
   {
      IIdentifiableModelElement modelElement = null;
      Map.Entry entry = MergerUtil.getEntryFromMap(changedCache, rawModelElement);
      if(entry != null)
      {
         modelElement = (IIdentifiableModelElement) (EObject) entry.getValue();
      }
      else
      {
         // element belongs to process
         if(rawModelElement instanceof ActivityType
               || rawModelElement instanceof TransitionType
               || rawModelElement instanceof TriggerType)
         {
            EObject parentModel = parent;
            while(parentModel != null && !(parentModel instanceof ProcessDefinitionType))
            {
               parentModel = parentModel.eContainer();
            }
            if(parentModel == null)
            {
               return null;
            }
            modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameElement(rawModelElement, parentModel);
         }
         else
         {
            modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameModelElement(rawModelElement, targetModel, changedCache);
         }
      }
      return modelElement;
   }

   // open dialog to change name/id for this entry
   protected boolean openDialog(Entry entry)
   {
      EObject raw = (EObject) entry.getKey();
      EObject copy = (EObject) entry.getValue();
      EClass eClass = copy.eClass();

      EObject parent = raw.eContainer();
      // if is lane and not same model, use target diagram and get pool as parent
      if(copy instanceof LaneSymbol && !isSameModel)
      {
         parent = DiagramUtil.getDefaultPool(targetDiagram);
      }
      else
      {
         parent = CopyPasteUtil.getSameModelElement(parent, targetModel, null);
      }

      String id;
      String name;

      if(isDiagram)
      {
         if(parent instanceof ProcessDefinitionType
               || (raw instanceof ActivityType && parent == null)
               || (raw instanceof TransitionType && parent == null)
               || (raw instanceof TriggerType && parent == null))
         {
            parent = storage.getTargetProcess();
         }
      }

      Map eClassNameIdCache = new HashMap();
      Object localNameIdCache = null;

      eClassNameIdCache = (HashMap) nameIdCache.get(parent);
      if (copy instanceof DiagramType)
      {
         /*name = ((DiagramType) copy).getName();
         localNameIdCache = (ArrayList) eClassNameIdCache.get(eClass);
         NameIdDialog nameIdDialog = new NameIdDialog(null, name, (ArrayList) localNameIdCache);

         // add modified EObjects to newElements
         if (Dialog.OK == nameIdDialog.open())
         {
            ((DiagramType) copy).setName(nameIdDialog.getName());
            ((ArrayList) localNameIdCache).add(nameIdDialog.getNameCache());
            eClassNameIdCache.put(eClass, localNameIdCache);
            nameIdCache.put(parent, eClassNameIdCache);
            return true;
         }
         else
         {
            return false;
         }*/
      }
      else if (copy instanceof IIdentifiableElement)
      {
         id = ((IIdentifiableElement) copy).getId();
         name = ((IIdentifiableElement) copy).getName();
         localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);

         if (copy instanceof ProcessDefinitionType)
         {
            ProcessDefinitionType process = (ProcessDefinitionType) copy;
            String newID = process.getId() + "Cloned";
            String newName = "CLONE - " + process.getName();

            ((IIdentifiableElement) copy).setId(newID);
            ((IIdentifiableElement) copy).setName(newName);
            ((HashMap) localNameIdCache).put(newID, newName);
         }


         eClassNameIdCache.put(eClass, localNameIdCache);
         nameIdCache.put(parent, eClassNameIdCache);
         return true;
      }
      else if (copy instanceof TypeDeclarationType)
      {
         /*id = ((TypeDeclarationType) copy).getId();
         name = ((TypeDeclarationType) copy).getName();
         localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);

         NameIdDialog nameIdDialog = new NameIdDialog(null, id, name, (HashMap) localNameIdCache);

         // add modified EObjects to newElements
         if (Dialog.OK == nameIdDialog.open())
         {
            ((TypeDeclarationType) copy).setId(nameIdDialog.getId());
            ((TypeDeclarationType) copy).setName(nameIdDialog.getName());
            ((HashMap) localNameIdCache).put(nameIdDialog.getId(), nameIdDialog.getName());
            eClassNameIdCache.put(eClass, localNameIdCache);
            nameIdCache.put(parent, eClassNameIdCache);
            return true;
         }
         else
         {
            return false;
         }*/

      }
      return false;
   }

   // open dialog and get result from dialog
   protected boolean referenceOrValue()
   {
      // a copy is needed
      NameIDCache nameIdCacheObject = new NameIDCache(storage, changedCache, nameIdCache, referenceValueInput);
      /*ReferenceValueDialog copyKindDialog = new ReferenceValueDialog(null, referenceValueInput, nameIdCacheObject);
      if (Dialog.CANCEL == copyKindDialog.open())
      {
         // stop the operation
         return false;
      }*/

      List contentList = (List) referenceValueInput.getContainer().getAllContent();
      for(int i = 0; i < contentList.size(); i++)
      {
         ContentDecorator entry = (ContentDecorator) contentList.get(i);
         // copy by value
         if(!entry.isChecked())
         {
            // value has a new Name/ID already
            MergerEntry dialogEntry = (MergerEntry) entry.getContent();

            EObject raw = (EObject) dialogEntry.getKey();
            EObject copy = (EObject) dialogEntry.getValue();
            EClass eClass = copy.eClass();

            EObject parent = raw.eContainer();
            parent = CopyPasteUtil.getSameModelElement(parent, targetModel, null);

            String id;
            String name;

            if(isDiagram)
            {
               if(parent instanceof ProcessDefinitionType)
               {
                  parent = storage.getTargetProcess();
               }
            }
            Map eClassNameIdCache = new HashMap();
            Object localNameIdCache = null;

            eClassNameIdCache = (HashMap) nameIdCache.get(parent);
            if (copy instanceof DiagramType)
            {
               name = ((DiagramType) copy).getName();
               localNameIdCache = (ArrayList) eClassNameIdCache.get(eClass);

               ((ArrayList) localNameIdCache).add(name);
               eClassNameIdCache.put(eClass, localNameIdCache);
               nameIdCache.put(parent, eClassNameIdCache);
            }
            else if (copy instanceof IIdentifiableElement)
            {
               id = ((IIdentifiableElement) copy).getId();
               name = ((IIdentifiableElement) copy).getName();
               localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);

               ((HashMap) localNameIdCache).put(id, name);
               eClassNameIdCache.put(eClass, localNameIdCache);
               nameIdCache.put(parent, eClassNameIdCache);
            }
            changedCache.put(dialogEntry.getKey(), dialogEntry.getValue());
         }
      }
      return true;
   }

   // has model changed (or paste canceled)
   public boolean modelChanged()
   {
      return modelChanged;
   }

   // ask for all global elements with same id in new model if copy by value or by reference
   protected void checkForDialog(ModelType targetModel)
   {
      Map tmpGlobalElements = new HashMap();
      Iterator it = globalElements.entrySet().iterator();
      while (it.hasNext())
      {
         IIdentifiableModelElement modelElement = null;

         Map.Entry entry = (Map.Entry) it.next();
         EObject raw = (EObject) entry.getKey();
         EObject copy = (EObject) entry.getValue();
         if(raw instanceof IModelParticipant)
         {
            modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameModelElement(raw, targetModel, null);
            // we have it already, so we need to show the dialog
            if(modelElement != null)
            {
               tmpGlobalElements.put(raw, copy);
               MergerEntry dialogEntry = new MergerEntry(raw, copy);
               container.getParticipants().getContent().add(new ContentDecorator(dialogEntry));
               showDialog = true;
            }
         }
         else if(raw instanceof ApplicationType)
         {
            modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameModelElement(raw, targetModel, null);
            // we have it already, so we need to show the dialog
            if(modelElement != null)
            {
               tmpGlobalElements.put(raw, copy);
               MergerEntry dialogEntry = new MergerEntry(raw, copy);
               container.getApplications().getContent().add(new ContentDecorator(dialogEntry));
               showDialog = true;
            }
         }
         else if(raw instanceof DataType)
         {
            modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameModelElement(raw, targetModel, null);
            // we have it already, so we need to show the dialog
            if(modelElement != null)
            {
               tmpGlobalElements.put(raw, copy);
               MergerEntry dialogEntry = new MergerEntry(raw, copy);
               container.getDataTypes().getContent().add(new ContentDecorator(dialogEntry));
               showDialog = true;
            }
         }
      }
      if(!tmpGlobalElements.isEmpty())
      {
         it = tmpGlobalElements.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            globalElements.remove(raw);
         }
      }
   }

   // collect all elements that will be copied by value
   protected void collectElements()
   {
      Map typeTypes = new HashMap();
      // container for all type declarations
      Set tempStructuredData = new HashSet();
      // will be removed from elements as we will copy the type declarations separate
      Map tempStructuredDatas = new HashMap();
      Map tempEventActionTypeTypes = new HashMap();
      Map tempQualityControlCodes =  new HashMap();

      externalReferences = false;

      mergeElements.putAll(globalElements);
      mergeElements.putAll(processes);
      mergeElements.putAll(diagrams);
      Iterator it = elements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         EObject raw = (EObject) entry.getKey();
         // the selected type declarations from outline (for copy also in same model)
         if(raw instanceof TypeDeclarationType)
         {
            structuredData.add(((TypeDeclarationType) raw).getId());
            tempStructuredData.add(((TypeDeclarationType) raw).getId());
            // remove from elements in next step
            tempStructuredDatas.put(entry.getKey(), entry.getValue());
         }
         if(raw instanceof EventActionTypeType)
         {
            tempEventActionTypeTypes.put(entry.getKey(), entry.getValue());
         }

         if(!isSameModel)
         {
            if(raw instanceof Code)
            {
               if(MergerUtil.containsQC(targetModel, (Code) raw) == null)
               {
                  if(targetModel.getQualityControl() == null)
                  {
                     targetModel.setQualityControl(CarnotWorkflowModelFactory.eINSTANCE.createQualityControlType());
                  }
                  targetModel.getQualityControl().getCode().add((Code) entry.getValue());
                  tempQualityControlCodes.put(entry.getKey(), entry.getValue());
               }
            }
         }
      }

      if(!tempQualityControlCodes.isEmpty())
      {
         it = tempQualityControlCodes.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            elements.remove(raw);
         }
      }
      if(!tempEventActionTypeTypes.isEmpty())
      {
         it = tempEventActionTypeTypes.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            EObject modelElement = (IIdentifiableModelElement) CopyPasteUtil.getSameModelElement(raw, targetModel, changedCache);
            // if already in model, we will not add another
            if(modelElement != null)
            {
               elements.remove(raw);
            }
         }
      }
      // remove from elements as we will copy them separate
      if(!tempStructuredDatas.isEmpty())
      {
         it = tempStructuredDatas.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            elements.remove(raw);
         }
      }
      mergeElements.putAll(elements);

      // collect all references from selected data to type declarations (when paste into other model)
      it = mergeElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         EObject raw = (EObject) entry.getKey();

         // check if contains external reference
         if(raw instanceof DataType)
         {
            if(((DataType) raw).getExternalReference() != null)
            {
               externalReferences = true;
            }
         }
         if(raw instanceof ActivityType)
         {
            if(((ActivityType) raw).getExternalRef() != null)
            {
               externalReferences = true;
            }
         }
         if(raw instanceof ProcessDefinitionType)
         {
            if(((ProcessDefinitionType) raw).getExternalRef() != null)
            {
               externalReferences = true;
            }
            for(ActivityType checkActivity : ((ProcessDefinitionType) raw).getActivity())
            {
               if(checkActivity.getExternalRef() != null)
               {
                  externalReferences = true;
               }
            }
         }

         if(raw instanceof ApplicationType)
         {
            EList contextList = ((ApplicationType) raw).getContext();
            for(int i = 0; i < contextList.size();i++)
            {
               ContextType context = (ContextType) contextList.get(i);
               ApplicationContextTypeType contextType = context.getType();
               if(contextType != null)
               {
                  ApplicationContextTypeType targetType = (ApplicationContextTypeType) CopyPasteUtil.getSameModelElement(contextType, targetModel, null);
                  if(targetType == null && !typeTypes.containsKey(contextType))
                  {
                     typeTypes.put(contextType, copier.copy(contextType));
                  }
               }
            }
            EList accessPoints = ((ApplicationType) raw).getAccessPoint();
            for(int i = 0; i < accessPoints.size();i++)
            {
               AccessPointType accessPoint = (AccessPointType) accessPoints.get(i);
               DataTypeType accessPointType = accessPoint.getType();
               DataTypeType accessPointTargetType = (DataTypeType) CopyPasteUtil.getSameModelElement(accessPointType, targetModel, null);
               if(accessPointTargetType == null && !typeTypes.containsKey(accessPointType))
               {
                  typeTypes.put(accessPointType, copier.copy(accessPointType));
               }
            }

            ApplicationTypeType type = ((ApplicationType) raw).getType();
            if(type != null)
            {
               ApplicationTypeType targetType = (ApplicationTypeType) CopyPasteUtil.getSameModelElement(type, targetModel, null);
               if(targetType == null && !typeTypes.containsKey(type))
               {
                  typeTypes.put(type, copier.copy(type));
               }
            }
         }
         else if(raw instanceof DataType)
         {
            DataTypeType type = ((DataType) raw).getType();
            if(type != null)
            {
               DataTypeType targetType = (DataTypeType) CopyPasteUtil.getSameModelElement(type, targetModel, null);
               if(targetType == null && !typeTypes.containsKey(type))
               {
                  typeTypes.put(type, copier.copy(type));
               }
            }
            if(type.getId().equals(StructuredDataConstants.STRUCTURED_DATA))
            {
               String structuredDataId = AttributeUtil.getAttributeValue((IExtensibleElement) raw, StructuredDataConstants.TYPE_DECLARATION_ATT);
               if(!StringUtils.isEmpty(structuredDataId))
               {
                  tempStructuredData.add(structuredDataId);
               }
            }
            /*else if(GenericUtils.isDMSDataType((DataType) raw))
            {
               String structuredDataId = AttributeUtil.getAttributeValue((IExtensibleElement) raw, DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
               if(!StringUtils.isEmpty(structuredDataId))
               {
                  tempStructuredData.add(structuredDataId);
               }
            }*/
         }
      }

      if(!tempStructuredData.isEmpty())
      {
          Set internalStructuredData = new HashSet();
          ModelType sourceModel = storage.getOriginalModelCopy();
          // now check all children
          for (Iterator i = tempStructuredData.iterator(); i.hasNext();)
          {
              String id = (String) i.next();
              MergerUtil.checkAllXSDImportReferences(sourceModel, id, xsdFiles, internalStructuredData);
          }
          tempStructuredData.addAll(internalStructuredData);

          internalStructuredData = new HashSet();
          for (Iterator i = tempStructuredData.iterator(); i.hasNext();)
          {
              String id = (String) i.next();
              MergerUtil.checkAllTypeDeclarationReferences(sourceModel, id, xsdFiles, internalStructuredData);
          }
      }
      if(!isSameModel)
      {
         mergeElements.putAll(typeTypes);
         // copy all type declarations
         structuredData = tempStructuredData;
      }
      else
      {
         typeTypes.clear();
      }
   }

   public void mergeConfigurationVariables()
   {
      if(!isSameModel)
      {
         VariableContext targetContext = VariableContextHelper.getInstance().getContext(targetModel);
         List<ModelVariable> changedtargetVariables = null;
         if(targetContext != null)
         {
            targetContext.refreshVariables(targetModel);
            changedtargetVariables = targetContext.getVariables();
         }

         if(targetVariables != null && changedtargetVariables != null)
         {
            List<ModelVariable> findMergedVariables = MergerUtil.findMergedVariables(targetVariables, changedtargetVariables);
            if(findMergedVariables.size() > 0)
            {
               ModelType sourceModel = storage.getOriginalModelCopy();
               VariableContext sourceContext = VariableContextHelper.getInstance().getContext(sourceModel);
               List<ModelVariable> sourceVariables = null;
               if(sourceContext != null)
               {
                  sourceContext.refreshVariables(sourceModel);
                  sourceVariables = sourceContext.getVariables();
               }

               if(sourceVariables != null)
               {
                  for(ModelVariable findMergedVariable : findMergedVariables)
                  {
                     ModelVariable findModelVariable = MergerUtil.findModelVariable(sourceVariables, findMergedVariable);
                     if(findModelVariable != null)
                     {
                        findMergedVariable.setDescription(findModelVariable.getDescription());
                        findMergedVariable.setDefaultValue(findModelVariable.getDefaultValue());
                     }
                  }
                  targetContext.saveVariables();
               }
            }
         }
      }
   }

   public boolean mergeGlobal()
   {

      targetDeclarations = targetModel.getTypeDeclarations();
      if(!structuredData.isEmpty())
      {
         // we must ask the copy, because on cut we have no longer the source object
         ModelType sourceModel = storage.getOriginalModelCopy();
         TypeDeclarationsType sourceDeclarations = sourceModel.getTypeDeclarations();

         for (Iterator i = structuredData.iterator(); i.hasNext();)
         {
            String id = (String) i.next();
            TypeDeclarationType td = sourceDeclarations.getTypeDeclaration(id);
            if(td == null)
            {
               continue;
            }
            TypeDeclarationType newDeclaration = XpdlFactory.eINSTANCE.createTypeDeclarationType();
            newDeclaration.setId(td.getId());
            newDeclaration.setName(td.getName());
            typeDeclarationElements.put(td, newDeclaration);
         }

         for (Map.Entry<TypeDeclarationType, TypeDeclarationType> entry : typeDeclarationElements.entrySet())
         {
            TypeDeclarationType raw = entry.getKey();
            TypeDeclarationType copy = entry.getValue();
            if (MergerUtil.getEntryFromMap(changedCache, raw) == null
                  && checkElementInModel(entry))
            {
               // the element copy will change id and name here
               if(!openDialog(entry))
               {
                  modelChanged = false;
                  return false;
               }
               else
               {
                  structuredDataChangedCache.put(raw.getId(), copy.getId());
                  changedCache.put(entry.getKey(), entry.getValue());
               }
            }
         }

         for (Map.Entry<TypeDeclarationType, TypeDeclarationType> entry : typeDeclarationElements.entrySet())
         {
            TypeDeclarationType raw = entry.getKey();
            TypeDeclarationType newDeclaration = entry.getValue();

            XpdlTypeType type = raw.getDataType();
            // copy schema, paste schema
            if (type instanceof SchemaTypeType)
            {
                XSDSchema schema = ((SchemaTypeType) type).getSchema();
                // fix references to internal schemas
                XSDSchema clone = (XSDSchema) schema.cloneConcreteComponent(true, false);
                Document doc = clone.updateDocument();
                if(schema.getElement() != null)
                {
                   Element clonedElement = (Element) doc.importNode(schema.getElement(), true);
                   doc.appendChild(clonedElement);
                   clone.setElement(clonedElement);
                }
                // we need to adapt the namespace of the import
                List xsdImports = TypeDeclarationUtils.getImports(clone);
                if(xsdImports != null)
                {
                   Iterator imports = xsdImports.iterator();
	             	while (imports.hasNext())
	             	{
	             		XSDImport xsdImport = (XSDImport) imports.next();
	             		String xsdNameSpace = xsdImport.getNamespace();
	                    int idx = xsdNameSpace.lastIndexOf("/") + 1;  //$NON-NLS-1$
	                    String elementName = xsdNameSpace.substring(idx, xsdNameSpace.length());
	                    String newElementName = (String) structuredDataChangedCache.get(elementName);
	                    if(newElementName == null)
	                    {
	                       newElementName = elementName;
	                    }
	                    else
	                    {
	                        xsdImport.setSchemaLocation(StructuredDataConstants.URN_INTERNAL_PREFIX + newElementName);
	                    }
	                    String newValue = TypeDeclarationUtils.computeTargetNamespace(targetModel.getId(), newElementName);
	             		xsdImport.setNamespace(newValue);
	             	}
                }
                SchemaTypeType schemaTypeType = XpdlFactory.eINSTANCE.createSchemaTypeType();
                schemaTypeType.setSchema(clone);
                newDeclaration.setSchemaType(schemaTypeType);

                XSDNamedComponent component = TypeDeclarationUtils.findElementOrTypeDeclaration(newDeclaration, raw.getId());
                if (component != null)
                {
                   component.setName(newDeclaration.getId());
                }
                if (component instanceof XSDElementDeclaration)
                {
                   XSDElementDeclaration element = (XSDElementDeclaration) component;
                   if (!element.isElementDeclarationReference() && element.getAnonymousTypeDefinition() == null)
                   {
                      XSDTypeDefinition typeDefinition = element.getTypeDefinition();
                      // maybe we need to set the new type definition
                      if (typeDefinition != null && typeDefinition.getSchema() == clone)
                      {
                    	  typeDefinition.setName(newDeclaration.getId());
                      }
                   }
                }

                // XSDSchema clone = declaration.getSchema();
                String oldTargetNamespace = clone.getTargetNamespace();
                clone.setTargetNamespace(TypeDeclarationUtils.computeTargetNamespace(targetModel.getId(), newDeclaration.getId()));
                String prefix = TypeDeclarationUtils.computePrefix(newDeclaration.getId(), clone
                      .getQNamePrefixToNamespaceMap().keySet());
                clone.getQNamePrefixToNamespaceMap().put(prefix, clone.getTargetNamespace());
                clone.setSchemaLocation(StructuredDataConstants.URN_INTERNAL_PREFIX + newDeclaration.getId());
            }
            // copy External Reference
            else if (type instanceof ExternalReferenceType)
            {
                ExternalReferenceType copy = (ExternalReferenceType) copier.copy(type);
                newDeclaration.setExternalReference(copy);
            }
            if (raw.getExtendedAttributes() != null)
            {
               ExtendedAttributesType copy = (ExtendedAttributesType) copier.copy(raw.getExtendedAttributes());
               newDeclaration.setExtendedAttributes(copy);
               // (fh) remove vcs uuid as this is a new object
               ExtendedAttributeUtil.setAttribute(newDeclaration, "infinity:vcs:uuid", null);
            }

            targetDeclarations.getTypeDeclaration().add(newDeclaration);
            modelChanged = true;
         }

         for (TypeDeclarationType td : typeDeclarationElements.values())
         {
            XpdlTypeType type = td.getDataType();
            // update TypeDefinition
            if (type instanceof SchemaTypeType)
            {
                MergerUtil.updateTypeDefinition(td, targetModel, structuredDataChangedCache);
            }
         }
      }

      // show dialog if global elements shall be copied by value or by reference
      if(showDialog)
      {
         // when the dialog opens modelChanged must be set (cancel dialog shall stop the operation)
         // we must collect the old values somehow to know the elements for the mapping (changedCache)
         // the dialog may change name and id
         if(!referenceOrValue())
         {
            modelChanged = false;
            return false;
         }
         // changedCache contains only unchecked elements - to be copied by value
         mergeElements.putAll(changedCache);
      }

      Iterator it = mergeElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         if(!mergeElement(entry))
         {
            modelChanged = false;
            return false;
         }
      }
      it = mergeElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         addModelElement(entry, false);
      }
      return true;
   }

   // collect subprocesses
   protected void collectProcessContent()
   {
      List tmpActivities = new ArrayList();
      Iterator it = processChildren.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         EObject raw = (EObject) entry.getKey();
         EObject value = (EObject) entry.getValue();
         if(!elements.containsKey(raw))
         {
            // elements like trigger, activities
            elements.put(raw, value);
         }
         if(raw instanceof ActivityType)
         {
            ActivityType activity = (ActivityType) raw;
            tmpActivities.add(activity);
         }
         if(!isSameModel)
         {
            if(raw instanceof ActivityType)
            {
               ActivityType activity = (ActivityType) raw;
               MergerUtil.getObjectsFromActivity(activity, globalElements, processes, elements, copier);
            }
            else if(raw instanceof TriggerType)
            {
               TriggerType trigger = (TriggerType) raw;
               MergerUtil.getObjectsFromTrigger(trigger, globalElements, copier);
            }
         }
      }

      if(tmpActivities.isEmpty())
      {
         List tmpTransitions = ConnectionUtils.getTransitions(tmpActivities);
         if(!tmpTransitions.isEmpty())
         {
            for(Iterator iter = tmpTransitions.iterator(); iter.hasNext();)
            {
               TransitionType transition = (TransitionType) iter.next();
               EObject raw = transition;
               EObject value = copier.copy(transition);
               processChildren.put(raw, value);
               if(!elements.containsKey(raw))
               {
                  // elements like trigger, activities
                  elements.put(raw, value);
               }
            }
         }
      }

      // if it is the same model, we use the same objects
      if(!isSameModel)
      {
         // collect all subprocesses
         Map allSubProcesses = new HashMap();
         it = processes.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            Map subProcesses = MergerUtil.collectSubProcesses((ProcessDefinitionType) raw, copier, processes);
            if(subProcesses != null)
            {
               MergerUtil.mergeProcesses(allSubProcesses, subProcesses);
            }
         }
         MergerUtil.mergeProcesses(processes, allSubProcesses);
         it = processes.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            MergerUtil.checkAuthorizations(raw, globalElements, copier);

            MergerUtil.getObjectsFromProcess(raw, processElements, globalElements, copier);
            // collect all activities, needed for dialog check
            EList rawActivities = ((ProcessDefinitionType) raw).getActivity();
            if(!rawActivities.isEmpty())
            {
               Iterator activitiesIt = rawActivities.iterator();
               while(activitiesIt.hasNext())
               {
                  ActivityType activity = (ActivityType) activitiesIt.next();
                  if(activities != null && !activities.containsKey(activity))
                  {
                     activities.put(activity, (ActivityType) copier.copy(activity));
                  }
               }
            }
         }
         it = processElements.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry) it.next();
            EObject raw = (EObject) entry.getKey();
            if(raw instanceof EventActionTypeType
                  && !elements.containsKey(raw))
            {
               elements.put(entry.getKey(), entry.getValue());
            }
         }
         // seems that process elements are already there (activities, trigger, data path)
         processElements = new HashMap();
      }
   }
}