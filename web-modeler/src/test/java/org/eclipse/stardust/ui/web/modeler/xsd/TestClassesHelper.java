package org.eclipse.stardust.ui.web.modeler.xsd;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.ClassesHelper;

public class TestClassesHelper
{

   @Test
   public void testClassesHelper()
   {
      String methodSignature = "testSignature(java.lang.String, java.util.List<java.util.Map<java.lang.String,  java.util.Map<java.lang.String, java.lang.Object>>>, java.util.List<java.lang.String>, java.util.Map<java.lang.String, java.lang.Object>)";
      
      
      ClassLoader classLoader = TestClassesHelper.class.getClassLoader();
      try
      {
         ClassesHelper.getMethodBySignature(classLoader, "org.eclipse.stardust.ui.web.modeler.xsd.ClassesHelperTest", methodSignature);
      }
      catch (Throwable e)
      {
         Assert.fail("ClassesHelper.getMethodBySignature failed!");
      }
   }
   
   public void testSignature(String a, java.util.List<java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>> b, java.util.List<java.lang.String> v, java.util.Map<java.lang.String, java.lang.Object> r)
   {
      
   }
}