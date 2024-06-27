package com.x_tornado10.lccp.yaml_factory;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public interface YAMLFactoryMessage {
    default YAMLConfiguration build() throws ConfigurationException, YAMLSerializer.InvalidReplyTypeException, YAMLSerializer.InvalidPacketTypeException, YAMLSerializer.TODOException {
        return YAMLSerializer.serializeYAML((YAMLMessage) this);
    }
}
