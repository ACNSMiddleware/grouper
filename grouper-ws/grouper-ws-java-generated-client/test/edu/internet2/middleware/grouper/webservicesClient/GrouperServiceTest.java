

/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperServiceTest Junit test case
    */

    public class GrouperServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite222=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite222 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite222));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite groupDeleteLite222=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite222 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite222,
                    new tempCallbackN1000C()
                );
              


        }

        private class tempCallbackN1000C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1000C(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave224=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave224 here
                
                        assertNotNull(stub.groupSave(
                        groupSave224));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave groupSave224=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave224 here
                

                stub.startgroupSave(
                         groupSave224,
                    new tempCallbackN10032()
                );
              


        }

        private class tempCallbackN10032  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10032(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments getAttributeAssignments226=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments226 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments226));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments getAttributeAssignments226=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments226 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments226,
                    new tempCallbackN10058()
                );
              


        }

        private class tempCallbackN10058  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10058(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite228=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite228 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite228));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite addMemberLite228=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite228 here
                

                stub.startaddMemberLite(
                         addMemberLite228,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite getMembershipsLite230=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite230 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite230));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite getMembershipsLite230=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite230 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite230,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite232=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite232 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite232));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite232=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite232 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite232,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember234=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember234 here
                
                        assertNotNull(stub.hasMember(
                        hasMember234));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMember hasMember234=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMember.class);
                    // TODO : Fill in the hasMember234 here
                

                stub.starthasMember(
                         hasMember234,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete236=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete236 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete236));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete stemDelete236=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete236 here
                

                stub.startstemDelete(
                         stemDelete236,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite238=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite238 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite238));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite stemSaveLite238=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite238 here
                

                stub.startstemSaveLite(
                         stemSaveLite238,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite240=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite240 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite240));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite groupSaveLite240=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite240 here
                

                stub.startgroupSaveLite(
                         groupSaveLite240,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember242=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember242 here
                
                        assertNotNull(stub.addMember(
                        addMember242));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AddMember addMember242=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AddMember.class);
                    // TODO : Fill in the addMember242 here
                

                stub.startaddMember(
                         addMember242,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups244=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups244 here
                
                        assertNotNull(stub.findGroups(
                        findGroups244));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups findGroups244=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups244 here
                

                stub.startfindGroups(
                         findGroups244,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes assignAttributes246=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes246 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes246));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes assignAttributes246=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes246 here
                

                stub.startassignAttributes(
                         assignAttributes246,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember248=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember248 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember248));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember deleteMember248=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember248 here
                

                stub.startdeleteMember(
                         deleteMember248,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite250=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite250 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite250));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite hasMemberLite250=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite250 here
                

                stub.starthasMemberLite(
                         hasMemberLite250,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite252=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite252 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite252));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite stemDeleteLite252=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite252 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite252,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite254=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite254 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite254));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite254=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite254 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite254,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite256=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite256 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite256));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite getMembersLite256=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite256 here
                

                stub.startgetMembersLite(
                         getMembersLite256,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships getMemberships258=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships258 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships258));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships getMemberships258=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships258 here
                

                stub.startgetMemberships(
                         getMemberships258,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments getPermissionAssignments260=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments260 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments260));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments getPermissionAssignments260=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments260 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments260,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite262=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite262 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite262));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite getGroupsLite262=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite262 here
                

                stub.startgetGroupsLite(
                         getGroupsLite262,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite264 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite264));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite264 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite264,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite assignAttributesLite266=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite266 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite266));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite assignAttributesLite266=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite266 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite266,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges assignGrouperPrivileges268=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges268 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges268));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges assignGrouperPrivileges268=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges268 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges268,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite getSubjectsLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite270 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite270));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite getSubjectsLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite270 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite270,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects getSubjects272=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects272 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects272));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects getSubjects272=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects272 here
                

                stub.startgetSubjects(
                         getSubjects272,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite274=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite274 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite274));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite findGroupsLite274=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite274 here
                

                stub.startfindGroupsLite(
                         findGroupsLite274,
                    new tempCallbackN103E8()
                );
              


        }

        private class tempCallbackN103E8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103E8(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite276 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite276));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite276 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite276,
                    new tempCallbackN1040E()
                );
              


        }

        private class tempCallbackN1040E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1040E(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete278=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete278 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete278));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete groupDelete278=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete278 here
                

                stub.startgroupDelete(
                         groupDelete278,
                    new tempCallbackN10434()
                );
              


        }

        private class tempCallbackN10434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10434(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups280=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups280 here
                
                        assertNotNull(stub.getGroups(
                        getGroups280));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups getGroups280=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups280 here
                

                stub.startgetGroups(
                         getGroups280,
                    new tempCallbackN1045A()
                );
              


        }

        private class tempCallbackN1045A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1045A(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems282=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems282 here
                
                        assertNotNull(stub.findStems(
                        findStems282));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStems findStems282=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStems.class);
                    // TODO : Fill in the findStems282 here
                

                stub.startfindStems(
                         findStems282,
                    new tempCallbackN10480()
                );
              


        }

        private class tempCallbackN10480  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10480(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite284=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite284 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite284));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite memberChangeSubjectLite284=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite284 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite284,
                    new tempCallbackN104A6()
                );
              


        }

        private class tempCallbackN104A6  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104A6(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers286=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers286 here
                
                        assertNotNull(stub.getMembers(
                        getMembers286));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers getMembers286=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers286 here
                

                stub.startgetMembers(
                         getMembers286,
                    new tempCallbackN104CC()
                );
              


        }

        private class tempCallbackN104CC  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104CC(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite288 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite deleteMemberLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite288 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite288,
                    new tempCallbackN104F2()
                );
              


        }

        private class tempCallbackN104F2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104F2(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave290=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave290 here
                
                        assertNotNull(stub.stemSave(
                        stemSave290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.StemSave stemSave290=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.StemSave.class);
                    // TODO : Fill in the stemSave290 here
                

                stub.startstemSave(
                         stemSave290,
                    new tempCallbackN10518()
                );
              


        }

        private class tempCallbackN10518  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10518(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite292 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite findStemsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite292 here
                

                stub.startfindStemsLite(
                         findStemsLite292,
                    new tempCallbackN1053E()
                );
              


        }

        private class tempCallbackN1053E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1053E(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject294=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject294 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject memberChangeSubject294=
                                                        (edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject294 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject294,
                    new tempCallbackN10564()
                );
              


        }

        private class tempCallbackN10564  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10564(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    