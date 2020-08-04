package com.aliyz.fabric.sdk;

import com.alibaba.fastjson.JSON;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Test;

import java.io.File;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by mawl at 2020-08-04 10:11
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SDKTest {

    private static final String TEST_ORG = "Org1";

    private static final String TEST_FIXTURES_PATH = "src/test/fixture";

    private static final String CHAIN_CODE_PATH = "github.com/example_cc";

    private static final String CHAIN_CODE_NAME = "cc-NetworkConfigTest-001";

    private static final String CHAIN_CODE_VERSION = "1";

    private static final String FOO_CHANNEL_NAME = "mychannel";

    private static NetworkConfig networkConfig;

    static {
        try {
            networkConfig = NetworkConfig.fromYamlFile(new File("src/main/resources/network-config/fabric-aliyz.local-network_config.yaml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 部署链码测试
    @Test
    public void deployChaincodeIfRequired() throws Exception {
        System.out.println("---------------^^ 部署链码 ^^--------------");

        HFClient client = getTheClient();
        Channel channel = constructChannel(client, FOO_CHANNEL_NAME);

        Peer peer = channel.getPeers().iterator().next();
        System.out.println("---------------^^-------------: " + JSON.toJSONString(peer));

    }




    /** ------------------------------------------ private method ---------------------------------------- **/
    // Returns a new client instance
    private HFClient getTheClient() throws Exception {

        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        User peerAdmin = getAdminUser(TEST_ORG);
        client.setUserContext(peerAdmin);

        return client;
    }

    private Channel constructChannel(HFClient client, String channelName) throws Exception {

        //Channel newChannel = client.getChannel(channelName);
        Channel newChannel = client.loadChannelFromConfig(channelName, networkConfig);
        if (newChannel == null) {
            throw new RuntimeException("Channel " + channelName + " is not defined in the config file!");
        }

        return newChannel.initialize();
    }

    private User getAdminUser(String orgName) throws Exception {

        return networkConfig.getPeerAdmin(orgName);
    }
}
