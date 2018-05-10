package io.nuls.network;

import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.network.module.impl.NettyNetworkModuleBootstrap;
import io.protostuff.Tag;
import org.junit.Before;
import org.junit.Test;

public class TestNetwork {

    @Before
    public void init() {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap dbModuleBootstrap = new LevelDbModuleBootstrap();
        dbModuleBootstrap.init();
        dbModuleBootstrap.start();

        NettyNetworkModuleBootstrap networkModuleBootstrap = new NettyNetworkModuleBootstrap();
        networkModuleBootstrap.init();
        networkModuleBootstrap.start();
    }

    @Test
    public void testNetworkModule() {
        while (true) {
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
