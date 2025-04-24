package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.YAJL.YAJLManager;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
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

        assertDoesNotThrow(() ->
            yaml.loadFromString(
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
            )
        );
    }

    @Test
    void testCheckIfKeyExists() {
        assertTrue(YamlTools.checkIfKeyExists("StringTest", yaml));
        ConfigurationSection section = yaml.getConfigurationSection("ParentSection");
        assertNotNull(section);
        assertTrue(YamlTools.checkIfKeyExists("ChildSection", section));
    }

    @Test
    void testEnsureKeyExists() {
        assertDoesNotThrow(() -> YamlTools.ensureKeyExists("ParentSection", yaml));
        assertThrows(DeserializationException.class, () -> YamlTools.ensureKeyExists("NotExistentSection", yaml));
    }

    @Test
    void testGetStringIfAvailable() {
        assertEquals("", YamlTools.getStringIfAvailable("StringTestNotExistent", yaml));
        assertEquals("String", YamlTools.getStringIfAvailable("StringTest", yaml));
    }

    @Test
    void testGetBooleanIfAvailable() {
        assertFalse(YamlTools.getBooleanIfAvailable("BooleanTestNotExistent", yaml));
        assertTrue(YamlTools.getBooleanIfAvailable("BooleanTest", yaml));
    }


    @Test
    void testGetIntIfAvailable() {
        assertEquals(0, YamlTools.getIntIfAvailable("IntegerTestNotExistent", yaml));
        assertEquals(5, YamlTools.getIntIfAvailable("IntegerTest", yaml));
    }

    @Test
    void testGetDoubleIfAvailable() {
        assertEquals(0, YamlTools.getDoubleIfAvailable("DoubleTestNotExistent", yaml));
        assertEquals(3.14, YamlTools.getDoubleIfAvailable("DoubleTest", yaml));
    }

    @Test
    void testGetLongIfAvailable() {
        assertEquals(0, YamlTools.getLongIfAvailable("LongTestNotExistent", yaml));
        assertEquals(435672378, YamlTools.getLongIfAvailable("LongTest", yaml));
    }

    @Test
    void testConstructIcon() {
    }
}