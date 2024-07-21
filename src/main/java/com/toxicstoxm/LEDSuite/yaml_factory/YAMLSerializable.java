package com.toxicstoxm.LEDSuite.yaml_factory;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public interface YAMLSerializable {
    default YAMLConfiguration build() throws ConfigurationException, YAMLSerializer.InvalidReplyTypeException, YAMLSerializer.InvalidPacketTypeException, YAMLSerializer.TODOException {
        return YAMLSerializer.serializeYAML((YAMLMessage) this);
    }
}
