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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.common.form.Form;
import org.eclipse.stardust.ui.common.form.FormInput;
import org.eclipse.stardust.ui.common.form.InputController;
import org.eclipse.stardust.ui.common.form.ListInputController;
import org.eclipse.stardust.ui.common.form.PrimitiveInputController;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
import org.eclipse.stardust.ui.common.form.jsf.DocumentPath;
import org.eclipse.stardust.ui.common.form.jsf.JsfStructureContainer;
import org.eclipse.stardust.ui.common.form.jsf.PrimitiveInputControllerDataTypeAdapter;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.common.introspection.java.JavaPath;
import org.eclipse.stardust.ui.common.introspection.xsd.XsdPath;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.IppJsfFormGenerator;


/**
 * Represents the form generated for a Manual Activity
 *
 * @author Marc.Gille
 * @author Subodh.Godbole
 */
public class ManualActivityForm extends Form
{
   private static final Logger trace = LogManager.getLogger(ManualActivityForm.class);

   private IppJsfFormGenerator formGenerator;

   private String formId;

   private WorkflowService workflowService;
   private ApplicationContext applicationContext;
   private ActivityInstance activityInstance;

   private DocumentInputEventHandler documentInputEventHandler;
   /**
    * @param generationPreferences
    * @param formBinding
    * @param activityInstance
    * @param workflowService
    * @param applicationContext
    */
   public ManualActivityForm(FormGenerationPreferences generationPreferences, String formBinding,
         ActivityInstance activityInstance, WorkflowService workflowService, ApplicationContext applicationContext)

   {
      this(generationPreferences, formBinding, activityInstance, workflowService, applicationContext, null);
   }

   /**
    * @param generationPreferences
    * @param formBinding
    * @param activityInstance
    * @param workflowService
    * @param applicationContext
    * @param documentInputEventHandler
    */
   public ManualActivityForm(FormGenerationPreferences generationPreferences, String formBinding,
         ActivityInstance activityInstance, WorkflowService workflowService, ApplicationContext applicationContext,
         DocumentInputEventHandler documentInputEventHandler)

   {
      super();
      this.activityInstance = activityInstance;
      this.workflowService = workflowService;
      this.applicationContext = applicationContext;
      this.documentInputEventHandler = documentInputEventHandler;

      // If not specified use this as default as just a flow layout, controls just be one below the other
      if (null == generationPreferences)
      {
         generationPreferences = new FormGenerationPreferences(1, 0);
      }

      this.formGenerator = new IppJsfFormGenerator(generationPreferences, formBinding);
      generateForm();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.Form#isFormValidationsPresent()
    */
   public boolean isFormValidationsPresent()
   {
      Iterator<?> it = FacesContext.getCurrentInstance().getMessages();
      if (null != it && it.hasNext())
      {
         String clientId;
         Iterator<?> clientIds = FacesContext.getCurrentInstance().getClientIdsWithMessages();
         while(clientIds.hasNext())
         {
            clientId = (String)clientIds.next();
            if (null != clientId && clientId.contains(getFormId()))
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Validation Error Present for clientId = " + clientId);
               }
               return true;
            }
         }
      }
      return false;
   }

   /**
    *
    */
   public void setData()
   {
      Map<String, Serializable> inDataValues = getWorkflowService().getInDataValues(activityInstance.getOID(),
            getApplicationContext().getId(), null);

      for (Object object : getApplicationContext().getAllDataMappings())
      {
         DataMapping dataMapping = (DataMapping) object;

         if (dataMapping.getDirection().equals(Direction.IN) || dataMapping.getDirection().equals(Direction.IN_OUT))
         {
            Object value = inDataValues.get(dataMapping.getId());
            setValue(dataMapping.getId(), value);
         }
      }
   }

   /**
    * @return
    */
   public Map<String, Object> retrieveData()
   {
      Map<String, Object> map = new HashMap<String, Object>();

      for (Object object : getApplicationContext().getAllDataMappings())
      {
         DataMapping dataMapping = (DataMapping) object;

         if (dataMapping.getDirection().equals(Direction.OUT) || dataMapping.getDirection().equals(Direction.IN_OUT))
         {
            InputController inputCtrl = getTopLevelInputController(dataMapping.getId());
            Object value = getUnwrapValue(dataMapping.getId());

            // Handle Documents Specially
            if (null != value && inputCtrl instanceof DocumentInputController)
            {
               // Handle Unsaved Documents
               // Save Document, And if it's saved then again fetch the same and use it
               if (((DocumentInputController)inputCtrl).saveDocument())
               {
                  value = getUnwrapValue(dataMapping.getId());
               }
            }
            map.put(dataMapping.getId(), value);
         }
      }

      return map;
   }

   /*
    * Generates the top level panel for all Data Mappings of the Activity.
    */
   @SuppressWarnings("unchecked")
   public void generateForm()
   {
      List<Object> allOutMappings = getApplicationContext().getAllOutDataMappings();
      List<Object> allInMappings = getApplicationContext().getAllInDataMappings();

      // Process OUT Mappings first
      List<Object> allMappings = new ArrayList<Object>();
      allMappings.addAll(allOutMappings);
      allMappings.addAll(allInMappings);

      // Process All IN/OUT Mappings and collect all of them in ManualActivityPath
      Path path = null;
      ManualActivityPath manualActivityPath = new ManualActivityPath("MA" + activityInstance.getOID(), false);
      Map<Path, DataMapping> pathDataMappingMap = new HashMap<Path, DataMapping>();
      Map<String, DataMapping> dataMappingMap = new HashMap<String, DataMapping>();
      for (Object object : allMappings)
      {
         DataMapping dataMapping = (DataMapping) object;

         if (dataMappingMap.containsKey(dataMapping.getId()))
         {
            continue;
         }

         dataMappingMap.put(dataMapping.getId(), dataMapping);
         if (trace.isDebugEnabled())
         {
            trace.debug("Processing Data Mapping - " + dataMapping.getId() + ":" + dataMapping.getName());
         }

         // Handle Data Mapping as per Type
         if (ModelUtils.isSystemDefinedData(dataMapping))
         {
            path = createSystemDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isDMSType(getModel(), dataMapping))
         {
            path = createDMSDataMapping(dataMapping, allInMappings, manualActivityPath);
         }
         else if (ModelUtils.isEnumerationType(getModel(), dataMapping))
         {
            path = createStructureDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isPrimitiveType(getModel(), dataMapping))
         {
            path = createPrimitiveDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isStructuredType(getModel(), dataMapping))
         {
            path = createStructureDataMapping(dataMapping, manualActivityPath);
         }

         if (null != path)
         {
            manualActivityPath.getChildPaths().add(path);
            pathDataMappingMap.put(path, dataMapping);
         }
         else
         {
            trace.warn("Skipping Data Mapping - Not supported - " + dataMapping.getId() + ":" + dataMapping.getName());
         }
      }

      // Set Label Provider along with required data for I18N
      formGenerator.setLabelProvider(new ManualActivityLabelProvider(getModel(), pathDataMappingMap));

      setRootContainer(formGenerator.createRootComponent());

      // Process ManualActivityPath and generate form
      processManualActivityPath(manualActivityPath, pathDataMappingMap);

      // Debug Information
      if (trace.isDebugEnabled())
      {
         trace.debug("Full Path Map = " + getFullPathInputControllerMap());
         trace.debug("Top Level Map = " + getTopLevelInputControllerMap());
         trace.debug("Markup:\n" + generateMarkup());
      }
   }

   /**
    * @return
    */
   public String getFormId()
   {
      if(StringUtils.isEmpty(formId))
      {
         Random o = new Random();
         formId = "MAF" + o.nextInt(10000);
      }

      return formId;
   }

   /**
    * @param onlyHavingDocViewerOpened
    * @param checkDocementAvailable
    * @return
    */
   public List<DocumentInputController> getDisplayedMappedDocuments(boolean onlyHavingDocViewerOpened, boolean checkDocementAvailable)
   {
      IppDocumentInputController docInputCtrl;
      List<DocumentInputController> mappedDocs = new ArrayList<DocumentInputController>();
      for (InputController inputCtrl : getTopLevelInputControllerMap().values())
      {
         if (inputCtrl instanceof IppDocumentInputController
               && (!checkDocementAvailable || ((IppDocumentInputController) inputCtrl).isDocumentAvailable()))
         {
            docInputCtrl = (IppDocumentInputController) inputCtrl;
            if (!onlyHavingDocViewerOpened || docInputCtrl.isDocumentViewerOpened())
            {
               mappedDocs.add(docInputCtrl);
            }
         }
      }

      return mappedDocs;
   }

   /**
    * @return
    */
   public IppDocumentInputController getIfSingleDocument()
   {
      if (getTopLevelInputControllerMap().size() == 1)
      {
         Collection<InputController> values = getTopLevelInputControllerMap().values();
         InputController ctrl = values.iterator().next();
         if (ctrl instanceof IppDocumentInputController)
         {
            return (IppDocumentInputController) ctrl;
         }
      }

      return null;
   }

   /**
    * @return
    */
   public String generateMarkup()
   {
      return formGenerator.generateMarkup(((JsfStructureContainer) getRootContainer()).getRootGrid(), null);
   }

   /**
    * @param systemPath
    * @return
    */
   private boolean handleSystemPath(IppSystemPath systemPath)
   {
      PrimitiveInputController inputController = new PrimitiveInputController(systemPath, new PrimitiveInputControllerDataTypeAdapter(systemPath));

      FormInput input = null;
      if (systemPath.isEnumeration())
      {
         input = formGenerator.addEnumerationInput(getRootContainer(), inputController);
      }
      else if (systemPath.isPrimitive())
      {
         input = formGenerator.addPrimitiveInput(getRootContainer(), inputController);
      }

      if(null != input)
      {
         getRootContainer().getInputs().add(input);
         getFullPathInputControllerMap().put("/" + systemPath.getId(), inputController);
         getTopLevelInputControllerMap().put(systemPath.getId(), inputController);

         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @param path
    * @return
    */
   private boolean handlePrimitivePath(Path path)
   {
      PrimitiveInputController inputController = new PrimitiveInputController(path,
            new PrimitiveInputControllerDataTypeAdapter(path));
      FormInput input = formGenerator.addPrimitiveInput(getRootContainer(), inputController);
      getRootContainer().getInputs().add(input);

      getFullPathInputControllerMap().put("/" + path.getId(), inputController);
      getTopLevelInputControllerMap().put(path.getId(), inputController);

      return true;
   }

   /**
    * @param path
    * @return
    */
   private boolean handleEnumerationPath(Path path)
   {
      PrimitiveInputController inputController = new PrimitiveInputController(path,
            new PrimitiveInputControllerDataTypeAdapter(path));
      FormInput input = formGenerator.addEnumerationInput(getRootContainer(), inputController);
      getRootContainer().getInputs().add(input);

      getFullPathInputControllerMap().put("/" + path.getId(), inputController);
      getTopLevelInputControllerMap().put(path.getId(), inputController);

      return true;
   }

   /**
    * @param path
    * @return
    */
   private boolean handleStructureDataMapping(Path path)
   {
      getTopLevelInputControllerMap().put(path.getId(),
            formGenerator.generateStructurePanel(getFullPathInputControllerMap(), getRootContainer(), path));
      return true;
   }

   /**
    * @param dataMapping
    * @param allInMappings
    * @param maPath
    * @return
    */
   private Path createDMSDataMapping(DataMapping dataMapping, List<Object> allInMappings, ManualActivityPath maPath)
   {
      if (ModelUtils.isDocumentType(getModel(), dataMapping)) // Document
      {
         if (!isWriteOnly(dataMapping, allInMappings))
         {
            DocumentType documentType = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getDocumentTypeFromData(
                  getModel(), getModel().getData(dataMapping.getDataId()));

            if (documentType == null)
            {
               trace.debug("Could not resolve type for Document:, " + dataMapping.getQualifiedId() + ". It may be set defualt by design");
            }

            return new DocumentPath(maPath, dataMapping.getId(), documentType, null,
                  Direction.IN == dataMapping.getDirection());
         }
         else
         {
            trace.warn("Skipping Data Mapping - Found it as Write Only - " + dataMapping.getId() + ":" + dataMapping.getName());
         }
      }
      else if (ModelUtils.isFolderType(getModel(), dataMapping)) // Folder
      {
         // Skip, Not supported
      }
      else // Only Meta Data
      {
         Path docTypePath = null;
         Data documentData = getModel().getData(dataMapping.getDataId());
         String metaDataTypeId = (String) documentData.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);

         if (StringUtils.isNotEmpty(metaDataTypeId))
         {
            TypeDeclaration typeDeclaration = getModel().getTypeDeclaration(metaDataTypeId);
            Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(getModel(), typeDeclaration);

            for (TypedXPath path : allXPaths)
            {
               if ("properties".equals(dataMapping.getDataPath())) // Mapping to entire properties
               {
                  if (null == path.getParentXPath())
                  {
                     docTypePath = new XsdPath(maPath, path, dataMapping.getId(),
                           Direction.IN == dataMapping.getDirection());
                     break;
                  }
               }
               else if (dataMapping.getDataPath().equals("properties/" + path.getXPath())) // Mapping to nested item in properties
               {
                  docTypePath = new XsdPath(maPath, path, dataMapping.getId(),
                        Direction.IN == dataMapping.getDirection());
                  break;
               }
            }
         }

         // if null means - Mapping to documenmt's attributes e.g. id, owner, etc
         if (null == docTypePath)
         {
            // This is the only possibility, but still check
            if (ModelUtils.isPrimitiveType(getModel(), dataMapping))
            {
               docTypePath = createPrimitiveDataMapping(dataMapping, maPath);
            }
         }

         return docTypePath;
      }

      return null;
   }

   /**
    * @param dataMapping
    * @param maPath
    * @return
    */
   private Path createSystemDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      return new IppSystemPath(maPath, dataMapping.getId(), isReadOnly(dataMapping));
   }

   /**
    * @param dataMapping
    * @param path
    * @param maPath
    * @return
    */
   private Path createPrimitiveDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      return JavaPath.createFromClass(maPath, dataMapping.getId(), dataMapping.getMappedType(), isReadOnly(dataMapping));
   }

   /**
    * @param dataMapping
    * @param maPath
    * @return
    */
   private Path createStructureDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      Set<TypedXPath> xpaths = ModelUtils.getXPaths(getModel(), dataMapping);

      for (TypedXPath path : xpaths)
      {
         if (path.getParentXPath() == null)
         {
            return new XsdPath(maPath, path, dataMapping.getId(), Direction.IN == dataMapping.getDirection());
         }
      }
      return null;
   }

   /**
    * @param manualActivityPath
    * @param pathDataMapping
    */
   private void processManualActivityPath(ManualActivityPath manualActivityPath, Map<Path, DataMapping> pathDataMapping)
   {
      // Process Children
      for (Path path : manualActivityPath.getChildPaths())
      {
         if (path instanceof IppSystemPath)
         {
            handleSystemPath((IppSystemPath)path);
         }
         else if (path instanceof DocumentPath)
         {
            handleDocumentPath((DocumentPath)path, pathDataMapping.get(path));
         }
         else if (path.isEnumeration())
         {
            handleEnumerationPath(path);
         }
         else if (path.isPrimitive())
         {
            handlePrimitivePath(path);
         }
         else if(path.isList())
         {
            handleListDataMapping(path);
         }
         else if (path.getChildPaths().size() > 0)
         {
            handleStructureDataMapping(path);
         }
      }

      // For Single Document Case
      IppDocumentInputController docController = getIfSingleDocument();
      if (null != docController && null == docController.getValue())
      {
         docController.setOpenDocument(true);
         docController.setEnableOpenDocument(false);
      }
   }

   /**
    * @param documentPath
    * @param dataMapping
    * @return
    */
   private boolean handleDocumentPath(DocumentPath documentPath, DataMapping dataMapping)
   {
      DocumentInputController docInputController = new IppDocumentInputController(documentPath,
            activityInstance, dataMapping, documentInputEventHandler);

      formGenerator.addDocumentInput(getFullPathInputControllerMap(), getRootContainer(), docInputController);

      getFullPathInputControllerMap().put("/" + documentPath.getId(), docInputController);
      getTopLevelInputControllerMap().put(documentPath.getId(), docInputController);

      return true;
   }

   /**
    * @param path
    * @return
    */
   private boolean handleListDataMapping(Path path)
   {
      ListInputController listInputController = new ListInputController(path);
      formGenerator.addListComponent(getRootContainer(), path);

      getFullPathInputControllerMap().put("/" + path.getId(), listInputController);
      getTopLevelInputControllerMap().put(path.getId(), listInputController);

      return true;
   }

   /**
    * @param dataMapping
    * @return
    */
   private boolean isReadOnly(DataMapping dataMapping)
   {
      if (ModelUtils.isSystemDefinedReadOnlyData(dataMapping))
      {
         return true;
      }
      else if (ModelUtils.isDMSReadOnlyData(getModel(), dataMapping))
      {
         return true;
      }
      return Direction.IN == dataMapping.getDirection();
   }

   /**
    * @param dataMapping
    * @param allInMappings
    * @return
    */
   private boolean isWriteOnly(DataMapping dataMapping, List<Object> allInMappings)
   {
      if (Direction.IN == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         return false;
      }
      else if (Direction.OUT == dataMapping.getDirection())
      {
         for (Object object : allInMappings)
         {
            DataMapping dm = (DataMapping) object;
            if (dm.getId().equals(dataMapping.getId()))
            {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * @return
    */
   public Model getModel()
   {
      return org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(activityInstance.getModelOID());
   }

   public ApplicationContext getApplicationContext()
   {
      return applicationContext;
   }

   public WorkflowService getWorkflowService()
   {
      return workflowService;
   }
}