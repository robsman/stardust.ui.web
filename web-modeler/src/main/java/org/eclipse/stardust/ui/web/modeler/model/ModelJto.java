package org.eclipse.stardust.ui.web.modeler.model;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ModelJto
{
   public final String type = ModelerConstants.MODEL_KEY;

   public String uuid;
   public String id;
   public String name;
   public String description;

   public String fileName;
   public String filePath;

   public List<ModelParticipantJto> participants = newArrayList();
   public List<StructuredDataTypeJto> typeDeclarations = newArrayList();
   public List<DataJto> dataItems = newArrayList();
   public List<ApplicationJto> applications = newArrayList();
   public List<ProcessDefinitionJto> processes = newArrayList();
}
