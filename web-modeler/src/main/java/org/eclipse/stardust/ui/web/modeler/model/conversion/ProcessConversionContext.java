package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import com.google.gson.JsonObject;

public class ProcessConversionContext
{
   private final ModelConversionContext modelConversionContext;

   private final List<JsonObject> diagramRepository = newArrayList();

   public ProcessConversionContext(ModelConversionContext modelConversionContext)
   {
      this.modelConversionContext = modelConversionContext;
   }

   public ModelConversionContext forModel()
   {
      return modelConversionContext;
   }

   public boolean hasNewElementOid(long originalOid)
   {
      return modelConversionContext.hasNewElementOid(originalOid);
   }

   public long newElementOid(long originalOid)
   {
      return modelConversionContext.newElementOid(originalOid);
   }

   public String newModelId()
   {
      return modelConversionContext.newModelId();
   }

   public String newModelId(String originalModelId)
   {
      return modelConversionContext.newModelId(originalModelId);
   }

   public String newDataId(String originalDataId)
   {
      return modelConversionContext.newDataId(originalDataId);
   }

   public void registerDiagramForProcess(JsonObject diagramJson)
   {
      if ( !diagramRepository.contains(diagramJson))
      {
         diagramRepository.add(diagramJson);
      }
   }

   public List<JsonObject> retrieveProcessDiagrams()
   {
      return diagramRepository;
   }
}
