package com.aliyz.fabric.sdk;

import com.alibaba.fastjson.JSON;
import com.aliyz.fabric.sdk.exception.HFSDKException;
import com.aliyz.fabric.sdk.utils.HFSDKUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by mawl at 2020-08-04 10:11
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SdkExecuterTest {

    private static final String ORG_1 = "Org1";
    private static final String ORG_2 = "Org2";
    private static final String ORG_1_MSP = "Org1MSP";
    private static final String ORG_2_MSP = "Org2MSP";
    private static final String PEER_0_ORG_1 = "peer0.org1.example.com";
    private static final String PEER_1_ORG_1 = "peer1.org1.example.com";
    private static final String PEER_0_ORG_2 = "peer0.org2.example.com";



    private static final String CHAIN_CODE_SOURCE_PATH = "/Users/mawl/Workspace/java/code.tusdao.com/fabric/fabric-sdk-java/src/test/fixture/sdkintegration/gocc/sample1";

    private static final String CHAIN_CODE_METAINFO_PATH = "/Users/mawl/Workspace/java/code.tusdao.com/fabric/fabric-sdk-java/src/test/fixture/meta-infs/end2endit";

    private static final String CHAIN_CODE_PATH = "github.com";

    private static final String CHAIN_CODE_NAME = "example_cc";

    private static final String CHAIN_CODE_VERSION = "1.0";

    private static final String MY_CHANNEL_NAME = "mychannel";

    private static NetworkConfig networkConfig;
    private static HFClient org1Client;

    static {
        try {
            networkConfig = NetworkConfig.fromYamlFile(new File("src/main/resources/network-config/fabric-aliyz.local-network_config.yaml"));

            org1Client = getTheClient(ORG_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 部署链码测试
    @Test
    public void deployChaincodeTest() {
        System.out.println("---------------^^ 部署链码 ^^--------------");

        String[] org1PeerNames = new String[]{PEER_0_ORG_1};
        String[] org2PeerNames = new String[]{PEER_0_ORG_2};
        String chaincodeSourcePath = CHAIN_CODE_SOURCE_PATH;
        String chaincodeMetainfoPath = CHAIN_CODE_METAINFO_PATH;
        String chaincodePath = CHAIN_CODE_PATH;
        String chaincodeName = CHAIN_CODE_NAME;
        String chaincodeVersion = CHAIN_CODE_VERSION;
        TransactionRequest.Type chaincodeType = TransactionRequest.Type.GO_LANG;
        long sequence = 1;
        boolean initRequired = true;

        try {


            Channel channel = constructChannel(org1Client, MY_CHANNEL_NAME);
            System.out.println("deployChaincode - channelName = " + channel.getName());

            String chaincodeLabel = HFSDKUtils.genSampleChaincodeLabel(chaincodeName, chaincodeVersion);

            LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy = LifecycleChaincodeEndorsementPolicy.fromSignaturePolicyYamlFile(
                    Paths.get("/Users/mawl/Workspace/java/code.aliyz.com/aliyz-case/plugin/fabric-sdk/src/main/resources/policy-config/org1.chaincode-endorsement_policy.yaml"));

            // TODO
            ChaincodeCollectionConfiguration ccCollectionConfiguration = ChaincodeCollectionConfiguration.fromYamlFile(
                    new File("/Users/mawl/Workspace/java/code.aliyz.com/aliyz-case/plugin/fabric-sdk/src/main/resources/coll-config/org1.collection-config.yaml"));

            Collection<Peer> org1Peers = HFSDKUtils.extractPeersFromChannel(channel, org1PeerNames);
//            Collection<Peer> org2Peers = HFSDKUtils.extractPeersFromChannel(channel, org2PeerNames);

            //////////////////////////////////////
            // 一、打包链码
            LifecycleChaincodePackage lifecycleChaincodePackage = ChaincodeExecuter.packageChaincode(chaincodeLabel, chaincodeSourcePath,
                    chaincodeMetainfoPath, chaincodePath, chaincodeName, chaincodeType);
            System.out.println("////////////////// cc package ///////////////" + lifecycleChaincodePackage.getLabel());

            //////////////////////////////////////
            // 二、安装链码
            String packageId = ChaincodeExecuter.installChaincode(org1Client, channel, org1Peers, lifecycleChaincodePackage);
            System.out.println("////////////////// cc packageId ///////////////" + packageId);

            boolean checkInstall = ChaincodeExecuter.queryInstalled(org1Client, org1Peers, packageId, chaincodeLabel);
            System.out.println("////////////////// check installed cc ///////////////" + checkInstall);

            //////////////////////////////////////
            // 三、审核链码
            BlockEvent.TransactionEvent approveEvents = ChaincodeExecuter.approveForMyOrg(org1Client, channel, org1Peers, sequence, chaincodeName,
                    chaincodeVersion, lccEndorsementPolicy, ccCollectionConfiguration, initRequired, packageId)
                    .get(60, TimeUnit.SECONDS);
            System.out.println("////////////////// approve cc result ///////////////" + approveEvents.isValid());

            boolean checkResult = ChaincodeExecuter.checkCommitReadiness(org1Client, channel, sequence, chaincodeName, chaincodeLabel, lccEndorsementPolicy,
                    ccCollectionConfiguration, initRequired, org1Peers, new HashSet<>(Arrays.asList(ORG_1_MSP)),
                    Collections.emptySet());
            System.out.println("////////////////// check approve result ///////////////" + checkResult);

            //////////////////////////////////////
            // 四、提交链码
            BlockEvent.TransactionEvent commitEvent = ChaincodeExecuter.commitChaincodeDefinition(org1Client, channel, sequence, chaincodeName,
                    chaincodeVersion, lccEndorsementPolicy, ccCollectionConfiguration, initRequired, org1Peers)
                    .get(60, TimeUnit.SECONDS);
            System.out.println("////////////////// commit cc result ///////////////" + commitEvent.isValid());
            checkResult = ChaincodeExecuter.queryCommitted(org1Client, channel, chaincodeName, org1Peers, sequence, initRequired, null,
                    ccCollectionConfiguration);
            System.out.println("////////////////// check commit result ///////////////" + checkResult);

            //////////////////////////////////////
            // 五、初始化链码
            BlockEvent.TransactionEvent initEvent = ChaincodeExecuter.initChaincode(org1Client, org1Client.getUserContext(), channel, initRequired, chaincodeName,
                    chaincodeVersion, chaincodeType, new String[]{"a,", "100", "b", "300"})
                    .get(60, TimeUnit.SECONDS);
            System.out.println("////////////////// init cc result ///////////////" + initEvent.isValid());

            System.out.println("##---------------^^ 结束 ^^--------------##");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 链码查询测试
    @Test
    public void queryChaincodeTest () {
        try {

            System.out.println("---------------^^ 查询链码 ^^--------------");

            Channel channel = constructChannel(org1Client, MY_CHANNEL_NAME);

            String payload = ChaincodeExecuter.queryChaincode(org1Client, org1Client.getUserContext(), channel, "move", CHAIN_CODE_NAME, CHAIN_CODE_VERSION,
                    new String[]{"a", "b", "10"});

            System.out.println("-------------- payload -------------" + JSON.toJSONString(payload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ------------------------------------------ private method ---------------------------------------- **/
    // Returns a new client instance
    private static HFClient getTheClient(String orgName) throws Exception {

        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        client.setUserContext(networkConfig.getPeerAdmin(orgName));

        return client;
    }

    private Channel constructChannel(HFClient client, String channelName) throws Exception {

        //Channel newChannel = client.getChannel(channelName);
        Channel newChannel = client.loadChannelFromConfig(channelName, networkConfig);
        if (newChannel == null) {
            throw new HFSDKException("Channel " + channelName + " is not defined in the config file!");
        }

        return newChannel.initialize();
    }
}
