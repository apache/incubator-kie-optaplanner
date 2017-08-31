/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.config.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ConfigUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void mergeProperty() {
        Integer a = null;
        Integer b = null;
        assertEquals(null, ConfigUtils.mergeProperty(a, b));
        a = Integer.valueOf(1);
        assertEquals(null, ConfigUtils.mergeProperty(a, b));
        b = Integer.valueOf(10);
        assertEquals(null, ConfigUtils.mergeProperty(a, b));
        b = Integer.valueOf(1);
        assertEquals(Integer.valueOf(1), ConfigUtils.mergeProperty(a, b));
        a = null;
        assertEquals(null, ConfigUtils.mergeProperty(a, b));
    }

    @Test
    public void meldProperty() {
        Integer a = null;
        Integer b = null;
        assertEquals(null, ConfigUtils.meldProperty(a, b));
        a = Integer.valueOf(1);
        assertEquals(Integer.valueOf(1), ConfigUtils.meldProperty(a, b));
        b = Integer.valueOf(10);
        assertEquals(ConfigUtils.mergeProperty(Integer.valueOf(1), Integer.valueOf(10)), ConfigUtils.meldProperty(a, b));
        a = null;
        assertEquals(Integer.valueOf(10), ConfigUtils.meldProperty(a, b));
    }

    @Test
    public void ceilDivide() {
        assertEquals(10, ConfigUtils.ceilDivide(19, 2));
        assertEquals(10, ConfigUtils.ceilDivide(20, 2));
        assertEquals(11, ConfigUtils.ceilDivide(21, 2));

        assertEquals(-9, ConfigUtils.ceilDivide(19, -2));
        assertEquals(-10, ConfigUtils.ceilDivide(20, -2));
        assertEquals(-10, ConfigUtils.ceilDivide(21, -2));

        assertEquals(-9, ConfigUtils.ceilDivide(-19, 2));
        assertEquals(-10, ConfigUtils.ceilDivide(-20, 2));
        assertEquals(-10, ConfigUtils.ceilDivide(-21, 2));

        assertEquals(10, ConfigUtils.ceilDivide(-19, -2));
        assertEquals(10, ConfigUtils.ceilDivide(-20, -2));
        assertEquals(11, ConfigUtils.ceilDivide(-21, -2));
    }

    @Test(expected = ArithmeticException.class)
    public void ceilDivideByZero() {
        ConfigUtils.ceilDivide(20, -0);
    }

    @Test
    public void applyCustomProperties() {
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("string", "This is a sentence.");
        customProperties.put("primitiveBoolean", "true");
        customProperties.put("objectBoolean", "true");
        customProperties.put("primitiveInt", "1");
        customProperties.put("objectInteger", "2");
        customProperties.put("primitiveLong", "3");
        customProperties.put("objectLong", "4");
        customProperties.put("primitiveFloat", "5.5");
        customProperties.put("objectFloat", "6.6");
        customProperties.put("primitiveDouble", "7.7");
        customProperties.put("objectDouble", "8.8");
        ConfigUtilsTestBean bean = new ConfigUtilsTestBean();
        ConfigUtils.applyCustomProperties(bean, "customProperties", customProperties);
        assertEquals(true, bean.primitiveBoolean);
        assertEquals(Boolean.TRUE, bean.objectBoolean);
        assertEquals(1, bean.primitiveInt);
        assertEquals(Integer.valueOf(2), bean.objectInteger);
        assertEquals(3L, bean.primitiveLong);
        assertEquals(Long.valueOf(4L), bean.objectLong);
        assertEquals(5.5F, bean.primitiveFloat, 0.0F);
        assertEquals(Float.valueOf(6.6F), bean.objectFloat);
        assertEquals(7.7, bean.primitiveDouble, 0.0);
        assertEquals(Double.valueOf(8.8), bean.objectDouble);
        assertEquals("This is a sentence.", bean.string);
    }

    @Test
    public void applyCustomPropertiesSubset() {
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("string", "This is a sentence.");
        ConfigUtilsTestBean bean = new ConfigUtilsTestBean();
        ConfigUtils.applyCustomProperties(bean, "customProperties", customProperties);
        assertEquals("This is a sentence.", bean.string);
    }

    @Test(expected = IllegalStateException.class)
    public void applyCustomPropertiesNonExistingCustomProperty() {
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("doesNotExist", "This is a sentence.");
        ConfigUtilsTestBean bean = new ConfigUtilsTestBean();
        ConfigUtils.applyCustomProperties(bean, "customProperties", customProperties);
    }

    private static class ConfigUtilsTestBean {

        private boolean primitiveBoolean;
        private Boolean objectBoolean;
        private int primitiveInt;
        private Integer objectInteger;
        private long primitiveLong;
        private Long objectLong;
        private float primitiveFloat;
        private Float objectFloat;
        private double primitiveDouble;
        private Double objectDouble;
        private String string;

        public void setPrimitiveBoolean(boolean primitiveBoolean) {
            this.primitiveBoolean = primitiveBoolean;
        }

        public void setObjectBoolean(Boolean objectBoolean) {
            this.objectBoolean = objectBoolean;
        }

        public void setPrimitiveInt(int primitiveInt) {
            this.primitiveInt = primitiveInt;
        }

        public void setObjectInteger(Integer objectInteger) {
            this.objectInteger = objectInteger;
        }

        public void setPrimitiveLong(long primitiveLong) {
            this.primitiveLong = primitiveLong;
        }

        public void setObjectLong(Long objectLong) {
            this.objectLong = objectLong;
        }

        public void setPrimitiveFloat(float primitiveFloat) {
            this.primitiveFloat = primitiveFloat;
        }

        public void setObjectFloat(Float objectFloat) {
            this.objectFloat = objectFloat;
        }

        public void setPrimitiveDouble(double primitiveDouble) {
            this.primitiveDouble = primitiveDouble;
        }

        public void setObjectDouble(Double objectDouble) {
            this.objectDouble = objectDouble;
        }

        public void setString(String string) {
            this.string = string;
        }

    }

    @Test
    public void newInstance_StaticMember() {
        assertNotNull(ConfigUtils.newInstance(this, "testProperty", StaticMember.class));
    }

    @Test
    public void newInstance_StaticMemberWithArgsConstructor() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("no-arg constructor.");
        assertNotNull(ConfigUtils.newInstance(this, "testProperty", StaticMemberWithArgsConstructor.class));
    }

    @Test
    public void newInstance_NonStaticMember() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("inner class");
        assertNotNull(ConfigUtils.newInstance(this, "testProperty", NonStaticMember.class));
    }

    @Test
    public void newInstance_LocalClass() {
        class LocalClass {
            // this is an inner class
        }
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("inner class");
        assertNotNull(ConfigUtils.newInstance(this, "testProperty", LocalClass.class));
    }

    public static class StaticMember {

    }

    public static class StaticMemberWithArgsConstructor {

        public StaticMemberWithArgsConstructor(int i) {
        }

    }

    public class NonStaticMember {
        // this is an inner class
    }

}
