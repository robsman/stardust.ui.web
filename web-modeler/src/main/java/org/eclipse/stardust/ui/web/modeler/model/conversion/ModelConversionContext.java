package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Map;

public class ModelConversionContext
{
   private final String targetFormat;

   private String newModelId;

   private String newModelUuid;

   private final Map<Long, Long> elementOidMapping = newHashMap();

   private final Map<String, String> modelIdMapping = newHashMap();

   private final Map<String, String> dataIdMapping = newHashMap();

   private final Map<String, String> participantIdMapping = newHashMap();

   private final Map<String, String> participantUuidMapping = newHashMap();

   private final Map<String, ProcessConversionContext> processContexts = newHashMap();

   public ModelConversionContext(String targetFormat)
   {
      this.targetFormat = targetFormat;
   }

   public String getTargetFormat()
   {
      return targetFormat;
   }

   public void registerNewModelIdentifiers(String newModelId, String newModelUuid)
   {
      this.newModelId = newModelId;
      this.newModelUuid = newModelUuid;
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

   public void registerNewParticipantId(String originalId, String newId)
   {
      participantIdMapping.put(originalId, newId);
   }

   public void registerNewParticipantUuid(String originalUuid, String newUuid)
   {
      participantUuidMapping.put(originalUuid, newUuid);
   }

   public String newModelId()
   {
      return newModelId;
   }

   public String newModelUuid()
   {
      return newModelUuid;
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

   public String newParticipantId(String originalId)
   {
      return participantIdMapping.containsKey(originalId)
            ? participantIdMapping.get(originalId)
            : originalId;
   }

   public String newParticipantUuid(String originalUuid)
   {
      return participantUuidMapping.containsKey(originalUuid)
            ? participantUuidMapping.get(originalUuid)
            : originalUuid;
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

}
