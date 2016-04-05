package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;



public class TestCrossModelReferenceTracking extends RecordingTestcase
{
   @Test
   public void testCrossModelLanePerformer() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      providerModel2 = modelService.findModel(PROVIDER_MODEL_ID2);

      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createRolesForProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "createRolesForProvider", false);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createRolesForProvider2.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "createRolesForProvider2", false);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/prepareMultiModelConsumer.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "prepareMultiModelConsumer", false);


      RoleType role2 = null;
      RoleType role1 = null;
      for(RoleType role : consumerModel.getRole())
      {
         if(role.getId().equals("Role2"))
         {
            role2 = role;
         }
         if(role.getId().equals("Role1"))
         {
            role1 = role;
         }
      }

      assertThat(role2, is(not(nullValue())));
      assertThat(role1, is(not(nullValue())));
      assertThat(role1.eIsProxy(), is(false));

      EList<ProcessDefinitionType> processDefinitions = consumerModel.getProcessDefinition();
      ProcessDefinitionType processDefinition = processDefinitions.get(0);
      assertThat(processDefinition, is(not(nullValue())));
      EList<ActivityType> activities = processDefinition.getActivity();
      ActivityType activity = activities.get(0);
      assertThat(activity, is(not(nullValue())));
      IModelParticipant performer = activity.getPerformer();
      assertThat(performer, is(not(nullValue())));
      assertEquals(performer, role1);


      EList<ExternalPackage> externalPackage = consumerModel.getExternalPackages().getExternalPackage();
      assertThat(externalPackage.size(), is(1));

   }

   protected boolean includeProviderModel2()
   {
      return true;
   }
}