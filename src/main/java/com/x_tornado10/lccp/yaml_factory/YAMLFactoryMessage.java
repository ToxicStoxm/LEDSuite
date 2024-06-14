package com.x_tornado10.lccp.yaml_factory;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public interface YAMLFactoryMessage {
    default YAMLConfiguration build() throws ConfigurationException, YAMLAssembly.InvalidReplyTypeException, YAMLAssembly.InvalidPacketTypeException, YAMLAssembly.TODOException {
        return YAMLAssembly.assembleYAML((YAMLMessage) this);
    }
}
