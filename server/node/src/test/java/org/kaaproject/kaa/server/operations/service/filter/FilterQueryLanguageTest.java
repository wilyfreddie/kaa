/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.filter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.endpoint.gen.ExtendedEndpointProfile;
import org.kaaproject.kaa.server.common.Base64Util;

public class FilterQueryLanguageTest {

    private static final String PROFILE_SCHEMA = ExtendedEndpointProfile.SCHEMA$
            .toString();
    private static final String SERVER_PROFILE_SCHEMA = ExtendedEndpointProfile.SCHEMA$
            .toString();
    
    private static final String EXTENDED_ENDPOINT_PROFILE_SOURCE_PATH = "operations/service/filter/extendedEndpointProfile.json";

    private static EndpointProfileDto profile;
    private static final String endpointKeyHash = "QMnPRTdUL+byZ/MTyyRX5MWe02Q=";
    private static final String endpointKeyHash2 = "AMBPRTEUL+byZ/MdTyRX5bWeT6b=";

    @BeforeClass
    public static void before() throws IOException {
        URL extendedEndpointProfileUrl = Thread.currentThread()
                .getContextClassLoader()
                .getResource(EXTENDED_ENDPOINT_PROFILE_SOURCE_PATH);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(extendedEndpointProfileUrl.getPath()), baos, 1024);
        profile = new EndpointProfileDto();
        profile.setClientProfileBody(new String(baos.toByteArray(), "UTF-8"));
        profile.setServerProfileBody(new String(baos.toByteArray(), "UTF-8"));
        profile.setEndpointKeyHash(Base64Util.decode(endpointKeyHash));
    }

    @Test
    public void testEpKeyHashMatch() {
        String filterBody = "{'" + endpointKeyHash + "','" + endpointKeyHash2 + "'}.contains(" + "#" + DefaultFilter.EP_KEYHASH_VARIABLE_NAME + ")";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testEpKeyHashMatchFailure() {
        String filterBody = "{'" + endpointKeyHash2 + "'}.contains(" + "#" + DefaultFilter.EP_KEYHASH_VARIABLE_NAME + ")";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.FALSE, filter.matches(profile));
    }

    @Test
    public void testQueryWithPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "simpleField == 'SIMPLE_FIELD'";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryNoServerProfileBody() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "simpleField == 'SIMPLE_FIELD'";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        String profileBody = profile.getServerProfileBody();
        profile.setServerProfileBody(null);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
        profile.setServerProfileBody(profileBody);
    }

    @Test
    public void testQueryWithArrayOfPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "arraySimpleField[1] == 'VALUE2'";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));

        filterBody = "#" + DefaultFilter.SERVER_PROFILE_VARIABLE_NAME + ".arraySimpleField[1] == 'VALUE2'";
        filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithArraySizeChecking() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "arraySimpleField.size() == 2";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithArraySizeCheckingDeprecatedProfileFilter() {
        String filterBody = "arraySimpleField.size() == 2";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithRecordPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "recordField.otherSimpleField == 123";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithRecordMapSizeChecking() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "recordField.otherMapSimpleField.size() == 2";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithArrayRecordPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "arrayRecordField[1].otherSimpleField == 789";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithArrayRecordMapFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "arrayRecordField[1].otherMapSimpleField[KEY5] == 5";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithMapPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "mapSimpleField[KEY8] == 8";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithMapRecordPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "mapRecordField[SOME_KEY2].otherSimpleField == 654";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithMapRecordMapPrimitiveFieldAccess() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "mapRecordField[SOME_KEY2].otherMapSimpleField[KEY12] == 12";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithPojoMethodCall() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "mapRecordField[SOME_KEY2].otherMapSimpleField[new java.lang.String('KEY13').toString()] == 13";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithPojoFieldAccess() {
        String filterBody = "new " + TestPojo.class.getName() + "(123).field == 123";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    @Test
    public void testQueryWithNullChecking() {
        String filterBody = "#" + DefaultFilter.CLIENT_PROFILE_VARIABLE_NAME + "." + "nullableRecordField == null";
        Filter filter = new DefaultFilter(filterBody, PROFILE_SCHEMA, SERVER_PROFILE_SCHEMA);
        Assert.assertEquals(Boolean.TRUE, filter.matches(profile));
    }

    private static class TestPojo {

        private int field;

        public TestPojo(Integer field) {
            this.field = field;
        }

        public int getField() {
            return field;
        }

        public void setField(int field) {
            this.field = field;
        }
    }
}
