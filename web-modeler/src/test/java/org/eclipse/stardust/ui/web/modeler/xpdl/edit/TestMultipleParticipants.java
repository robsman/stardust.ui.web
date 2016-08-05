package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.ParticipantType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestMultipleParticipants extends RecordingTestcase
{


   @Test
   public void testCreateMultipleParticipants() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createMultipleParticipants.txt");
      requestStream = new InputStreamReader(requestInput);
      
      replay(requestStream, "testCreateMultipleParticipants", false);
      
      OrganizationType org1 = GenericModelingAssertions.assertOrganization(providerModel, "Organization1", "Organization 1");
      OrganizationType org2 = GenericModelingAssertions.assertOrganization(providerModel, "Organization2", "Organization 2");
      OrganizationType org3 = GenericModelingAssertions.assertOrganization(providerModel, "Organization3", "Organization 3");
      RoleType role1 = GenericModelingAssertions.assertRole(providerModel, "Role1", "Role 1");
      RoleType role2 = GenericModelingAssertions.assertRole(providerModel, "Role2", "Role 2");
      RoleType role3 = GenericModelingAssertions.assertRole(providerModel, "Role3", "Role 3");
      RoleType role4 = GenericModelingAssertions.assertRole(providerModel, "Role4", "Role 4");
            
      assertThat(org1.getParticipant().isEmpty(), is(false));
      assertThat(org1.getParticipant().size(), is(3));
      assertThat(hasParticipant(org1, role1), is(true));
      assertThat(hasParticipant(org1, role2), is(true));
      assertThat(hasParticipant(org1, org2), is(true));     
      
      assertThat(org2.getParticipant().isEmpty(), is(false));
      assertThat(org2.getParticipant().size(), is(1));
      assertThat(hasParticipant(org2, org3), is(true));
      
      assertThat(org3.getParticipant().isEmpty(), is(false));
      assertThat(org3.getParticipant().size(), is(2));
      assertThat(hasParticipant(org3, role3), is(true));
      assertThat(hasParticipant(org3, role4), is(true));
      

       
   }
   
   @Test
   public void testRemoveParticipants() throws Exception
   {
      testCreateMultipleParticipants();
     
      String command = "{\"commandId\":\"participant.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"Role1\"}}]}";
      replaySimple(command, "testRemoveParticipants - Remove Role 1", null, false);
      command = "{\"commandId\":\"participant.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"Role3\"}}]}";
      replaySimple(command, "testRemoveParticipants - Remove Role 3", null, false);
      
      OrganizationType org1 = GenericModelingAssertions.assertOrganization(providerModel, "Organization1", "Organization 1");
      OrganizationType org2 = GenericModelingAssertions.assertOrganization(providerModel, "Organization2", "Organization 2");
      OrganizationType org3 = GenericModelingAssertions.assertOrganization(providerModel, "Organization3", "Organization 3");
      RoleType role2 = GenericModelingAssertions.assertRole(providerModel, "Role2", "Role 2");
      RoleType role4 = GenericModelingAssertions.assertRole(providerModel, "Role4", "Role 4");
            
      assertThat(org1.getParticipant().isEmpty(), is(false));
      assertThat(org1.getParticipant().size(), is(2));
      assertThat(hasParticipant(org1, role2), is(true));
      assertThat(hasParticipant(org1, org2), is(true));     
      
      assertThat(org2.getParticipant().isEmpty(), is(false));
      assertThat(org2.getParticipant().size(), is(1));
      assertThat(hasParticipant(org2, org3), is(true));
      
      assertThat(org3.getParticipant().isEmpty(), is(false));
      assertThat(org3.getParticipant().size(), is(1));
      assertThat(hasParticipant(org3, role4), is(true));
      
   }
   
   @Test
   public void testRemoveParticipantHierarchy() throws Exception
   {
      testCreateMultipleParticipants();
     
      String command = "{\"commandId\":\"participant.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"Organization2\"}}]}";
      replaySimple(command, "testRemoveParticipantHierarchy - Remove Org 2", null, false);
      
      OrganizationType org1 = GenericModelingAssertions.assertOrganization(providerModel, "Organization1", "Organization 1");

      RoleType role1 = GenericModelingAssertions.assertRole(providerModel, "Role1", "Role 1");
      RoleType role2 = GenericModelingAssertions.assertRole(providerModel, "Role2", "Role 2");
            
      assertThat(org1.getParticipant().isEmpty(), is(false));
      assertThat(org1.getParticipant().size(), is(2));
      assertThat(hasParticipant(org1, role1), is(true));
      assertThat(hasParticipant(org1, role2), is(true));     
            
   }

   private Boolean hasParticipant(OrganizationType org, IModelParticipant modelPart)
   {
      for (Iterator<ParticipantType> i = org.getParticipant().iterator(); i.hasNext();)
      {
         ParticipantType participant = i.next();
         if (participant.getParticipant().equals(modelPart))
         {
            return true;
         }
      }
      return false;
   }

   
}
