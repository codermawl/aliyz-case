package com.aliyz.fabric.sdk;

import com.aliyz.fabric.sdk.utils.MySDKUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Create by mawl at 2020-08-04 10:57
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class SDK {

    private static final String CHANNEL_TX_FILE_SUFFIX = ".tx";


    /** ------------------------------------- Channel --------------------------------------- **/

    /**
     * @Description: 创建并初始化通道：
     * 1、通过configtxgen命令生成交易通道初始文件
     *      configtxgen -profile MyChannel -outputCreateChannelTx mychannel.tx -channelID mychannel
     *
     * 2、下载 testsoft.tx 到本机
     *
     * 3、sdk 安装通道代码及解释
     *
     * @param hfClient
     * @param channelTxPath channel配置文件（yourchannel.tx）路径
     * @param channelName channel名称
     * @param orderer
     * @param signer 签名channel的用户
     * @return:
     * @Author: mawl
     * @Date: 2020-08-04 11:21
     **/
    public static Channel installChannel (HFClient hfClient, String channelTxPath, String channelName, Orderer orderer, User signer) {

        if (StringUtils.isBlank(channelName)) {
            throw new IllegalArgumentException("参数 'channelName' 不能为空");
        }

        Channel channel = null;

        try {
            //初始化 ChannelConfiguration 对象，参数是通道初始化文件路径
            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(Paths.get(channelTxPath, channelName,CHANNEL_TX_FILE_SUFFIX).toString()));
            channel = hfClient.newChannel(channelName, orderer, channelConfiguration,
                    hfClient.getChannelConfigurationSignature(channelConfiguration, signer));

            channel.initialize();

        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (TransactionException e3) {
            e3.printStackTrace();
        }
        return channel;
    }

    public static boolean uninstallChannel (Channel channel) {

        if (!channel.isShutdown()) {
            channel.shutdown(false);
            return true;
        }

        return false;
    }

    /**
     * @Description: 将 Peer 加入通道
     * @param channel
     * @param peers
     * @return: java.util.List<org.hyperledger.fabric.sdk.Peer>
     * @Author: mawl
     * @Date: 2020-08-04 11:31
     **/
    public static List<Peer> addPeersToChannel (Channel channel, Peer ... peers) {
        List<Peer> failurePeers = new ArrayList<>();

        for (Peer peer : peers) {
            if (peer == null) {
                failurePeers.add(peer);
                continue;
            }

            try {
                channel.joinPeer(peer);
            } catch (ProposalException e1) {
                failurePeers.add(peer);
                e1.printStackTrace();
            }
        }

        return failurePeers;
    }

    /**
     * @Description: 将 Peer 从通道移除
     * @param channel
     * @param peers
     * @return: java.util.List<org.hyperledger.fabric.sdk.Peer>
     * @Author: mawl
     * @Date: 2020-08-04 14:08
     **/
    public static List<Peer> removePeersFromChannel (Channel channel, Peer ... peers) {

        List<Peer> failurePeers = new ArrayList<>();

        for (Peer peer : peers) {
            if (peer == null) {
                failurePeers.add(peer);
                continue;
            }

            try {
                channel.removePeer(peer);
            } catch (InvalidArgumentException e1) {
                failurePeers.add(peer);
                e1.printStackTrace();
            }
        }

        return failurePeers;
    }


    /** ------------------------------------- Chain Code --------------------------------------- **/

    /**
     * @Description: 安装链码，Fabric 2.0在链码操作上有很大改动，整体的流程可以分为四个步骤：打包、安装、机构审批、链码提交。
     * @param hfClient
     * @param channel
     * @param chaincodeSourcePath 如："src/test/fixture/sdkintegration/gocc/sample1"
     * @param chaincodeMetainfoPath 如："src/test/fixture/meta-infs/end2endit"
     * @param chaincodePath 如："github.com"
     * @param chaincodeName 如："example_cc"
     * @param chaincodeVersion 如："1.0"
     * @param chaincodeType 链码语言类型
     * @param selectPeerNames 需要部署链码的节点名称
     * @param lccEndorsementPolicy 链码背书策略，如果为 null，在默认情况下，合约的背书策略为 majority of channel members，过半数通道成员。
     * @return: java.util.Collection<org.hyperledger.fabric.sdk.ProposalResponse>
     * @Author: mawl
     * @Date: 2020-08-04 14:45
     **/
    public static String installChaincode0(HFClient hfClient,
                                                                Channel channel,
                                                                String label,
                                                                String chaincodeSourcePath,
                                                                String chaincodeMetainfoPath,
                                                                String chaincodePath,
                                                                String chaincodeName,
                                                                String chaincodeVersion,
                                                                TransactionRequest.Type chaincodeType,
                                                                String[] selectPeerNames,
                                                                LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy) {
        try {
            Collection<Orderer> orderers = channel.getOrderers();

            final String channelName = channel.getName();
            out("deployChaincode - channelName = " + channelName);

            //////////////////////////////////////
            // 一、打包链码
            LifecycleChaincodePackage lifecycleChaincodePackage = packageChaincode(label, chaincodeSourcePath, chaincodeMetainfoPath, chaincodePath, chaincodeName, chaincodeType);

            //////////////////////////////////////
            // 二、安装链码
            String packageId = installChaincode(hfClient, channel, selectPeerNames, lifecycleChaincodePackage);

            //////////////////////////////////////
            // 三、审核链码
            approveForMyOrg(hfClient, channel, selectPeerNames, packageId, chaincodeName, chaincodeVersion, lccEndorsementPolicy);
            checkCommitReadiness(hfClient, channel, label, selectPeerNames[0]);

            //////////////////////////////////////
            // 四、提交链码
            String ok = commitChaincodeDefinition(hfClient, channel, selectPeerNames, chaincodeName, chaincodeVersion, lccEndorsementPolicy);

            return ok;

        } catch (InvalidArgumentException e1) {
            out("Caught an InvalidArgumentException running channel %s", channel.getName());
            e1.printStackTrace();
        } catch (ProposalException e2) {
            out("Caught an ProposalException running channel %s", channel.getName());
            e2.printStackTrace();
        } catch (IOException e4) {
            out("Caught an InvalidArgumentException running channel %s", channel.getName());
            e4.printStackTrace();
        } catch (Exception e) {
            out("Caught an InvalidArgumentException running channel %s", channel.getName());
            e.printStackTrace();
        }

        return null;
    }

    // 链码打包
    // peer lifecycle chaincode package mycc.tar.gz \
    // --path github.com/hyperledger/fabric-samples/chaincode/abstore/go/ \
    // --lang golang \
    // --label mycc_1
    public static LifecycleChaincodePackage packageChaincode ( String label,
                                                               String chaincodeSourcePath,
                                                               String chaincodeMetainfoPath,
                                                               String chaincodePath,
                                                               String chaincodeName,
                                                               TransactionRequest.Type chaincodeType) throws IOException, InvalidArgumentException {

        String ccPath = chaincodePath + "/" + chaincodeName;

        return LifecycleChaincodePackage.fromSource(label,
                Paths.get(chaincodeSourcePath),
                chaincodeType,
                ccPath,
                Paths.get(chaincodeMetainfoPath));
    }

    // 链码安装
    // peer lifecycle chaincode install mycc.tar.gz
    public static String installChaincode (HFClient hfClient,
                                         Channel channel,
                                         String[] selectPeerNames,
                                         LifecycleChaincodePackage lifecycleChaincodePackage) throws ProposalException, InvalidArgumentException {

        Collection<Peer> peersFromOrg = MySDKUtils.extractPeersFromChannel(channel, selectPeerNames);

        Collection<LifecycleInstallChaincodeProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        out("Creating lifecycleInstallChaincodeRequest");
        LifecycleInstallChaincodeRequest lifecycleInstallChaincodeRequest = hfClient.newLifecycleInstallChaincodeRequest();
        lifecycleInstallChaincodeRequest.setLifecycleChaincodePackage(lifecycleChaincodePackage);
        lifecycleInstallChaincodeRequest.setProposalWaitTime(10 * 60 * 1000); // 等待10min
        out("Sending lifecycleInstallChaincodeRequest to selected peers...");
        int numInstallProposal = peersFromOrg.size();
        responses = hfClient.sendLifecycleInstallChaincodeRequest(lifecycleInstallChaincodeRequest, peersFromOrg);
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                out("[√]Successful InstallChaincode proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }
        out("Received %d InstallChaincode proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("[X] Not enough endorsers for install : %d. %s", successful.size(), first.getMessage());
        }

        return ((LifecycleInstallChaincodeProposalResponse) successful.iterator().next()).getPackageId();
    }

    // TODO 查询链码安装结果
    // peer lifecycle chaincode queryinstalled
    private static Query.ChaincodeInfo queryInstalled (HFClient hfClient,
                                       Channel channel,
                                       String chaincodePath,
                                       String chaincodeName,
                                       String chaincodeVersion,
                                       String selectPeerName) throws InvalidArgumentException, ProposalException {

//        LifecycleQueryInstalledChaincodeRequest lifecycleQueryInstalledChaincodeRequest = hfClient.newLifecycleQueryInstalledChaincodeRequest();
//        lifecycleQueryInstalledChaincodeRequest.setPackageID();

        Collection<Peer> peers = MySDKUtils.extractPeersFromChannel(channel, new String[]{selectPeerName});

        List<Query.ChaincodeInfo> ccinfoList = channel.queryInstantiatedChaincodes(peers.iterator().next());
        boolean found;
        for (Query.ChaincodeInfo ccifo : ccinfoList) {
            found = chaincodePath.equals(ccifo.getPath()) && chaincodeName.equals(ccifo.getName()) && chaincodeVersion.equals(ccifo.getVersion());
            if (found) {
                return ccifo;
            }
        }
        return null;
    }

    // 审批链码
    // peer lifecycle chaincode approveformyorg \
    // --tls true \
    // --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem \
    // --channelID mychannel \
    // --name mycc \
    // --version 1 \
    // --init-required \
    // --package-id mycc_1:f5cc6d6e871262e8da9788f3d463442e51c482ec8288c13e4545741ad45d86fa \
    // --sequence 1 \
    // --waitForEvent
    public static void approveForMyOrg (HFClient hfClient,
                                        Channel channel,
                                        String[] selectPeerNames,
                                        String packageId,
                                        String chaincodeName,
                                        String chaincodeVersion,
                                        LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy) throws ProposalException, InvalidArgumentException {

        Collection<Peer> peersFromOrg = MySDKUtils.extractPeersFromChannel(channel, selectPeerNames);

        Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeRequest = hfClient.newLifecycleApproveChaincodeDefinitionForMyOrgRequest();
        lifecycleApproveChaincodeRequest.setPackageId(packageId);
        lifecycleApproveChaincodeRequest.setChaincodeName(chaincodeName);
        lifecycleApproveChaincodeRequest.setChaincodeVersion(chaincodeVersion);
        lifecycleApproveChaincodeRequest.setSequence(1L); // TODO ... ???
        if (lccEndorsementPolicy != null) {
            lifecycleApproveChaincodeRequest.setChaincodeEndorsementPolicy(lccEndorsementPolicy);
        }
        out("Sending lifecycleApproveChaincodeRequest to selected peers...");
        responses = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeRequest, peersFromOrg);
        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                out("[√] Succesful ApproveChaincode proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
            } else {
                failed.add(response);
            }
        }
        out("Received %d ApproveChaincode proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("[X] Not enough endorsers for ApproveChaincode : %d endorser failed with %s. Was verified: %s", successful.size(), first.getMessage(), first.isVerified());
        }

        LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse r1 = responses.iterator().next();

//        return successful;
    }

    // TODO 查看链码的审批状态
    // peer lifecycle chaincode checkcommitreadiness \
    // --channelID mychannel \
    // --name mycc \
    // --version 1 \
    // --sequence 1 \
    // --output json \
    // --init-required
    private static void checkCommitReadiness (HFClient hfClient,
                                             Channel channel,
                                             String chaincodeName,
                                             String selectPeerName) throws InvalidArgumentException, ProposalException {

        Collection<Peer> peersFromOrg = MySDKUtils.extractPeersFromChannel(channel, new String[]{selectPeerName});

//        LifecycleCheckCommitReadinessRequest lifecycleCheckCommitReadinessRequest = new LifecycleCheckCommitReadinessRequest(hfClient.getUserContext());
//        Collection<LifecycleCheckCommitReadinessProposalResponse> responses1 = channel.sendLifecycleCheckCommitReadinessRequest();

        QueryLifecycleQueryChaincodeDefinitionRequest lifecycleQueryChaincodeDefinitionRequest = hfClient.newQueryLifecycleQueryChaincodeDefinitionRequest();
        lifecycleQueryChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
        Collection<LifecycleQueryChaincodeDefinitionProposalResponse> responses = channel.lifecycleQueryChaincodeDefinition(lifecycleQueryChaincodeDefinitionRequest, peersFromOrg);

        LifecycleQueryChaincodeDefinitionProposalResponse response = responses.iterator().next();
        System.out.println("+++++++++" + response);

    }

    // 提交链码
    // peer lifecycle chaincode commit \
    // -o orderer.example.com:7050 \
    // --tls true \
    // --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem \
    // --channelID mychannel \
    // --name mycc \
    // --peerAddresses peer0.org1.example.com:7051 \
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
    // --peerAddresses peer0.org2.example.com:9051 \
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
    // --version 1 \
    // --sequence 1 \
    // --init-required
    public static String commitChaincodeDefinition (HFClient hfClient,
                                                  Channel channel,
                                                  String[] selectPeerNames,
                                                  String chaincodeName,
                                                  String chaincodeVersion,
                                                  LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy) throws ProposalException, InvalidArgumentException {

        Collection<Peer> peersFromOrg = MySDKUtils.extractPeersFromChannel(channel, selectPeerNames);

        Collection<LifecycleCommitChaincodeDefinitionProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        LifecycleCommitChaincodeDefinitionRequest lifecycleCommitChaincodeDefinitionRequest = hfClient.newLifecycleCommitChaincodeDefinitionRequest();
        lifecycleCommitChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
        lifecycleCommitChaincodeDefinitionRequest.setChaincodeVersion(chaincodeVersion);
        lifecycleCommitChaincodeDefinitionRequest.setSequence(1L);
        if (lccEndorsementPolicy != null) {
            lifecycleCommitChaincodeDefinitionRequest.setChaincodeEndorsementPolicy(lccEndorsementPolicy);
        }
        out("Sending lifecycleApproveChaincodeRequest to selected peers...");
        responses = channel.sendLifecycleCommitChaincodeDefinitionProposal(lifecycleCommitChaincodeDefinitionRequest, peersFromOrg);
        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                out("[√] Succesful CommitChaincode proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
            } else {
                failed.add(response);
            }
        }
        out("Received %d CommitChaincode proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("[X] Not enough endorsers for CommitChaincode : %d endorser failed with %s. Was verified: %s", successful.size(), first.getMessage(), first.isVerified());
        }

        ///////////////
        /// Send instantiate transaction to orderer
        out("Sending instantiateTransaction to orderer...");
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, channel.getOrderers());

        BlockEvent.TransactionEvent event = null;
        try {
            out("calling get...");
            event = future.get(30, TimeUnit.SECONDS);
            out("get done...");
            out("Finished CommitChaincode transaction with transaction id %s, %s", event.getTransactionID(), event.isValid());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "OK";
    }

    // 查看链码的提交状态
    // peer lifecycle chaincode querycommitted --channelID mychannel --name mycc
    private static void queryCommitted (HFClient hfClient,
                                       Channel channel,
                                       String chaincodeName) {

        // TODO ...
    }

    // 调用链码的Init方法
    // peer chaincode invoke -o orderer.example.com:7050 \
    // --tls true \
    // --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem \
    // -C mychannel \
    // -n mycc \
    // --peerAddresses peer0.org1.example.com:7051
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
    // --peerAddresses peer0.org2.example.com:9051 \
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
    // --isInit \
    // -c '{"Args":["Init","a","100","b","100"]}'
    public static void chaincodeInvokeInit () {

    }

    // 链码查询
    // peer chaincode query -C mychannel -n mycc -c '{"Args":["query","a"]}'
    public static String chaincodeQuery (HFClient hfClient,
                                            Channel channel,
                                            String chaincodeName,
                                            String chaincodeVersion,
                                            String fcn,
                                            String[] args) {

        out("Now query chaincode on channel %s with arguments: %s:%s.%s(%s)", channel.getName(), chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

        String expect = null;
        try {
            QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(args);
            queryByChaincodeRequest.setFcn(fcn);
            queryByChaincodeRequest.setChaincodeName(chaincodeName);
            queryByChaincodeRequest.setChaincodeVersion(chaincodeVersion);

            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest);

            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ChaincodeResponse.Status.SUCCESS) {
                    out("[X] Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                            ". Messages: " + proposalResponse.getMessage()
                            + ". Was verified : " + proposalResponse.isVerified());
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    out("[√] Query payload of b from peer %s returned %s", proposalResponse.getPeer().getName(), payload);
                    if (expect == null) {
                        expect = payload;
                    }
                }
            }

        } catch (InvalidArgumentException e1) {
            out("Caught an InvalidArgumentException running channel %s", channel.getName());
            e1.printStackTrace();
        } catch (ProposalException e2) {
            out("Caught an ProposalException running channel %s", channel.getName());
            e2.printStackTrace();
        } catch (Exception e) {
            out("Caught an Exception running channel %s", channel.getName());
            e.printStackTrace();
        }
        return expect;
    }

    // 链码调用
    // peer chaincode invoke -o orderer.example.com:7050 \
    // --tls true \
    // --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem \
    // -C mychannel \
    // -n mycc \
    // --peerAddresses peer0.org1.example.com:7051 \
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
    // --peerAddresses peer0.org2.example.com:9051 \
    // --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
    // -c '{"Args":["invoke","a","b","10"]}'
    private static CompletableFuture<BlockEvent.TransactionEvent> chaincodeInvoke (HFClient hfClient,
                                                                                   Channel channel,
                                                                                   String chaincodeName,
                                                                                   String chaincodeVersion,
                                                                                   String fcn,
                                                                                   String[] args,
                                                                                   User user) {
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        try {
            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeName(chaincodeName);
            transactionProposalRequest.setChaincodeVersion(chaincodeVersion);
            transactionProposalRequest.setFcn(fcn);
            transactionProposalRequest.setArgs(args);
//            transactionProposalRequest.setProposalWaitTime(30);
            if (user != null) { // specific user use that
                transactionProposalRequest.setUserContext(user);
            }
            out("Sending transaction proposal on channel %s to all peers with arguments: %s:%s.%s(%s)",
                    channel.getName(), chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

            Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
            for (ProposalResponse response : invokePropResp) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    out("[√] Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    out("[X] Failed transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                    failed.add(response);
                }
            }

            // Check that all the proposals are consistent with each other. We should have only one set
            // where all the proposals above are consistent.
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(invokePropResp);
            if (proposalConsistencySets.size() != 1) {
                out("Expected only one set of consistent move proposal responses but got %d", proposalConsistencySets.size());
            }

            out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                    invokePropResp.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                out("Not enough endorsers for invoke(%s:%s.%s(%s)):%d endorser error:%s. Was verified:%b",
                        chaincodeName, chaincodeVersion, fcn, Arrays.asList(args), firstTransactionProposalResponse.getStatus().getStatus(),
                        firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified());
            }
            out("Successfully received transaction proposal responses.");

            ////////////////////////////
            // Send transaction to orderer
            out("Sending chaincode transaction：invoke(%s:%s.%s(%s)) to orderer.", chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));
            if (user != null) {
                return channel.sendTransaction(successful, user);
            }

            return channel.sendTransaction(successful);
        } catch (InvalidArgumentException e1) {
            out("Caught an InvalidArgumentException running channel %s", channel.getName());
            e1.printStackTrace();
        } catch (ProposalException e2) {
            out("Caught an ProposalException running channel %s", channel.getName());
            e2.printStackTrace();
        } catch (Exception e) {
            out("Caught an Exception running channel %s", channel.getName());
            e.printStackTrace();
        }

        return null;
    }



    /** ------------------------------------- Private Method --------------------------------------- **/

    private static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

}
