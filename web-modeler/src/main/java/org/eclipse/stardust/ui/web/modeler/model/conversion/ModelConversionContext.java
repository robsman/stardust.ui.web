package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Map;

public class ModelConversionContext
{
   private String newModelId;

   private final Map<Long, Long> elementOidMapping = newHashMap();

   private final Map<String, String> modelIdMapping = newHashMap();

   private final Map<String, String> dataIdMapping = newHashMap();

   private final Map<String, ProcessConversionContext> processContexts = newHashMap();

   private final Map<Long, String> flowNodeOidToProcessId = newHashMap();

   public void registerNewModelId(String newModelId)
   {
      this.newModelId = newModelId;
   }

   public void registerNewElementOid(long originalOid, long newOid)
   {
      elementOidMapping.put(originalOid, newOid);
   }

   public void registerNewModelId(String originalModelId, String newModelId)
   {
      modelIdMapping.put(originalModelId, newModelId);
   }

   public void registerNewDataId(String originalDataId, String newDataId)
   {
      dataIdMapping.put(originalDataId, newDataId);
   }

   public String newModelId()
   {
      return newModelId;
   }

   public boolean hasNewElementOid(long originalOid)
   {
      return elementOidMapping.containsKey(originalOid);
   }

   public long newElementOid(long originalOid)
   {
      return elementOidMapping.containsKey(originalOid)
            ? elementOidMapping.get(originalOid)
            : originalOid;
   }

   public String newModelId(String originalModelId)
   {
      return modelIdMapping.containsKey(originalModelId)
            ? modelIdMapping.get(originalModelId)
            : originalModelId;
   }

   public String newDataId(String originalDataId)
   {
      return dataIdMapping.containsKey(originalDataId)
            ? dataIdMapping.get(originalDataId)
            : originalDataId;
   }

   public ProcessConversionContext forProcess(String processId)
   {
      ProcessConversionContext processContext = processContexts.get(processId);
      if (null == processContext)
      {
         processContext = new ProcessConversionContext(this);
         processContexts.put(processId, processContext);
      }
      return processContext;
   }

   public void mapFlowNodeOidToProcessId(long flowNodeOid, String processId)
   {
      flowNodeOidToProcessId.put(flowNodeOid, processId);
   }

   public String retrieveProcessIdForFlowNode(long flowNodeOid)
   {
      return flowNodeOidToProcessId.get(flowNodeOid);
   }

}
