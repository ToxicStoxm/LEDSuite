package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJL.YAJLManager;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YamlToolsTest {

    private static final YamlConfiguration yaml = new YamlConfiguration();

    @BeforeAll
    static void setup() {
        YAJLManager.configure(
                YAJLManagerConfig.builder()
                        .enableYAMLConfig(false)
                        .bridgeYAJSI(false)
                        .muteLogger(true)
                        .build()
        );

        assertDoesNotThrow(() -> yaml.loadFromString(
                """
                StringTest: "String"
                IntegerTest: 5
                DoubleTest: 3.14
                LongTest: 435672378
                BooleanTest: true
                StringList:
                  - "Hello"
                  - " "
                  - "World"
                  - "!"
                ParentSection:
                  ChildSection:
                    ChildSectionValue: 8
                """
        ));
    }

    @Test
    void testCheckIfKeyExists() {
        assertTrue(YamlTools.checkIfKeyExists("StringTest", yaml));
        assertFalse(YamlTools.checkIfKeyExists("NonExistentKey", yaml));
    }

    @Test
    void testEnsureKeyExists() {
        assertDoesNotThrow(() -> YamlTools.ensureKeyExists("ParentSection", yaml));
        DeserializationException ex = assertThrows(DeserializationException.class, () ->
                YamlTools.ensureKeyExists("MissingKey", yaml));
        assertEquals(ErrorCode.RequiredKeyIsMissing, ex.getErrorCode());
    }

    @Test
    void testEnsureKeyExistsWithCustomErrorCode() {
        ErrorCode customErrorCode = ErrorCode.Undefined;
        DeserializationException ex = assertThrows(DeserializationException.class, () ->
                YamlTools.ensureKeyExists("AnotherMissingKey", yaml, customErrorCode));
        assertEquals(customErrorCode, ex.getErrorCode());
    }

    @Test
    void testGetStringIfAvailable() {
        assertEquals("String", YamlTools.getStringIfAvailable("StringTest", yaml));
        assertEquals("", YamlTools.getStringIfAvailable("UnknownString", yaml));
        assertEquals("Default", YamlTools.getStringIfAvailable("UnknownString", "Default", yaml));
    }

    @Test
    void testGetBooleanIfAvailable() {
        assertTrue(YamlTools.getBooleanIfAvailable("BooleanTest", yaml));
        assertFalse(YamlTools.getBooleanIfAvailable("UnknownBoolean", yaml));
        assertTrue(YamlTools.getBooleanIfAvailable("UnknownBoolean", true, yaml));
    }

    @Test
    void testGetIntIfAvailable() {
        assertEquals(5, YamlTools.getIntIfAvailable("IntegerTest", yaml));
        assertEquals(0, YamlTools.getIntIfAvailable("MissingInt", yaml));
        assertEquals(99, YamlTools.getIntIfAvailable("MissingInt", 99, yaml));
    }

    @Test
    void testGetDoubleIfAvailable() {
        assertEquals(3.14, YamlTools.getDoubleIfAvailable("DoubleTest", yaml));
        assertEquals(0.0, YamlTools.getDoubleIfAvailable("MissingDouble", yaml));
        assertEquals(1.23, YamlTools.getDoubleIfAvailable("MissingDouble", 1.23, yaml));
    }

    @Test
    void testGetLongIfAvailable() {
        assertEquals(435672378L, YamlTools.getLongIfAvailable("LongTest", yaml));
        assertEquals(0L, YamlTools.getLongIfAvailable("MissingLong", yaml));
        assertEquals(9876543210L, YamlTools.getLongIfAvailable("MissingLong", 9876543210L, yaml));
    }
}