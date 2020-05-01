package io.apef.sdef.cluster;

import io.apef.base.config.factory.XFactoryBase;
import io.apef.base.config.factory.XFactoryConfig;


public class ApefClusterFactory extends XFactoryBase<ApefCluster, ApefClusterConfig> {
    public ApefClusterFactory(XFactoryConfig<ApefClusterConfig> factoryConfig) {
        super(factoryConfig);
    }

    @Override
    protected ApefCluster newInstance(ApefClusterConfig config) throws Exception {
        switch (config.clusterType()) {
            case K8S:
                return null;
        }
        throw new IllegalArgumentException("Invalid Cluster Type: " + config.clusterType());
    }

    @Override
    protected void close(ApefCluster instance) {

    }
}
