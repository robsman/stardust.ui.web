package org.eclipse.stardust.ui.web.modeler.model;

import java.util.LinkedHashMap;
import java.util.Map;

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

   public Map<String, ModelParticipantJto> participants = new LinkedHashMap<String, ModelParticipantJto>();
   public Map<String, TypeDeclarationJto> typeDeclarations = new LinkedHashMap<String, TypeDeclarationJto>();
   public Map<String, DataJto> dataItems = new LinkedHashMap<String, DataJto>();
   public Map<String, ApplicationJto> applications = new LinkedHashMap<String, ApplicationJto>();
   public Map<String, ProcessDefinitionJto> processes = new LinkedHashMap<String, ProcessDefinitionJto>();
}
