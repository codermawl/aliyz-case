package com.aliyz.fabric.sdk;

import com.alibaba.fastjson.JSON;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by mawl at 2020-08-04 10:11
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SDKTest {

    private static final String TEST_ORG = "Org1";

    private static final String CHAIN_CODE_SOURCE_PATH = "/Users/mawl/Workspace/java/code.tusdao.com/fabric/fabric-sdk-java/src/test/fixture/sdkintegration/gocc/sample1";

    private static final String CHAIN_CODE_METAINFO_PATH = "/Users/mawl/Workspace/java/code.tusdao.com/fabric/fabric-sdk-java/src/test/fixture/meta-infs/end2endit";

    private static final String CHAIN_CODE_PATH = "github.com";

    private static final String CHAIN_CODE_NAME = "example_cc";

    private static final String CHAIN_CODE_VERSION = "1.0";

    private static final String MY_CHANNEL_NAME = "mychannel";

    private static NetworkConfig networkConfig;

    static {
        try {
            networkConfig = NetworkConfig.fromYamlFile(new File("src/main/resources/network-config/fabric-aliyz.local-network_config.yaml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void networkTest() throws Exception {
        System.out.println("---------------^^ 网络测试 ^^--------------");

        HFClient hfClient = getTheClient();
        Channel channel = constructChannel(hfClient, MY_CHANNEL_NAME);

        System.out.println("-------------- channel -------------" + JSON.toJSONString(channel));
    }

    // 部署链码测试
    @Test
    public void installChaincodeTest() throws Exception {
        System.out.println("---------------^^ 部署链码 ^^--------------");

        HFClient hfClient = getTheClient();
        Channel channel = constructChannel(hfClient, MY_CHANNEL_NAME);

        LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy = LifecycleChaincodeEndorsementPolicy.fromSignaturePolicyYamlFile(
                Paths.get("/Users/mawl/Workspace/java/code.aliyz.com/aliyz-case/plugin/fabric-sdk/src/main/resources/policy-config/chaincodeendorsementpolicy.yaml"));

//        lccEndorsementPolicy = null;

        String responses = SDK.installChaincode0(hfClient,
                channel,
                "mylabel",
                CHAIN_CODE_SOURCE_PATH,
                CHAIN_CODE_METAINFO_PATH,
                CHAIN_CODE_PATH,
                CHAIN_CODE_NAME,
                CHAIN_CODE_VERSION,
                TransactionRequest.Type.GO_LANG,
                new String[]{"peer0.org1.example.com"},
                lccEndorsementPolicy);

        System.out.println("-------------- responses -------------" + JSON.toJSONString(responses));
    }

    @Test
    public void queryChaincodeTest () throws Exception {
        System.out.println("---------------^^ 查询链码 ^^--------------");

        HFClient hfClient = getTheClient();
        Channel channel = constructChannel(hfClient, MY_CHANNEL_NAME);

        String payload = SDK.chaincodeQuery(hfClient,
                channel, CHAIN_CODE_NAME, CHAIN_CODE_VERSION, "query", new String[]{"a"});

        System.out.println("-------------- payload -------------" + JSON.toJSONString(payload));

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
