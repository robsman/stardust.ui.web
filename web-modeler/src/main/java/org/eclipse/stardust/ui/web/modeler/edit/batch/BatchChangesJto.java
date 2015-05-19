package org.eclipse.stardust.ui.web.modeler.edit.batch;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import com.google.gson.JsonObject;

public class BatchChangesJto
{
   public JsonObject variableBindings = new JsonObject();

   public List<BatchStepJto> steps = newArrayList();
}