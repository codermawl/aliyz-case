package com.aliyz.fabric.sdk;

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
     * @Description: 安装链码
     * @param hfClient
     * @param channel
     * @param chaincodeSourcePath 如："src/test/fixture/sdkintegration/gocc/sample1"
     * @param chaincodeMetainfoPath 如："src/test/fixture/meta-infs/end2endit"
     * @param chaincodePath 如："github.com"
     * @param chaincodeName 如："example_cc"
     * @param chaincodeVersion 如："1.0"
     * @param chaincodeType 链码语言类型
     * @param lccEndorsementPolicy 链码背书策略，如果为 null，在默认情况下，合约的背书策略为 majority of channel members，过半数通道成员。
     * @return: java.util.Collection<org.hyperledger.fabric.sdk.ProposalResponse>
     * @Author: mawl
     * @Date: 2020-08-04 14:45
     **/
    public static Collection<ProposalResponse> installChaincode(HFClient hfClient,
                                                                Channel channel,
                                                                String chaincodeSourcePath,
                                                                String chaincodeMetainfoPath,
                                                                String chaincodePath,
                                                                String chaincodeName,
                                                                String chaincodeVersion,
                                                                TransactionRequest.Type chaincodeType,
                                                                LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy) {
        try {
            Collection<Peer> peersFromOrg = channel.getPeers();
            Collection<Orderer> orderers = channel.getOrderers();
            Collection<LifecycleInstallChaincodeProposalResponse> responses0;
            Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> responses1;
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            if (!checkInstantiatedChaincode(channel, peersFromOrg.iterator().next(), chaincodePath, chaincodeName, chaincodeVersion)) {

                final String channelName = channel.getName();
                out("deployChaincode - channelName = " + channelName);

                //////////////////////////////////////
                // 安装链码
                out("Creating lifecycleInstallChaincodeRequest");
                LifecycleInstallChaincodeRequest lifecycleInstallChaincodeRequest = hfClient.newLifecycleInstallChaincodeRequest();
                LifecycleChaincodePackage lifecycleChaincodePackage = LifecycleChaincodePackage.fromSource(chaincodeName + ":" + chaincodeVersion,
                                                                                                            Paths.get(chaincodeSourcePath),
                                                                                                            chaincodeType,
                                                                                                            chaincodePath + "/" + chaincodeName,
                                                                                                            Paths.get(chaincodeMetainfoPath));
                lifecycleInstallChaincodeRequest.setLifecycleChaincodePackage(lifecycleChaincodePackage);
                out("Sending lifecycleInstallChaincodeRequest to all peers...");
                int numInstallProposal = peersFromOrg.size();
                responses0 = hfClient.sendLifecycleInstallChaincodeRequest(lifecycleInstallChaincodeRequest, peersFromOrg);
                for (ProposalResponse response : responses0) {
                    if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                        out("[√]Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                        successful.add(response);
                    } else {
                        failed.add(response);
                    }
                }
                out("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size());
                if (failed.size() > 0) {
                    ProposalResponse first = failed.iterator().next();
                    out("[X] Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
                }

                if (lccEndorsementPolicy != null) {
                    //////////////////////////////////////
                    // 设置链码级别的背书策略
                    LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeRequest = hfClient.newLifecycleApproveChaincodeDefinitionForMyOrgRequest();
                    lifecycleApproveChaincodeRequest.setChaincodeName(chaincodeName);
                    lifecycleApproveChaincodeRequest.setChaincodeVersion(chaincodeVersion);
                    lifecycleApproveChaincodeRequest.setChaincodeEndorsementPolicy(lccEndorsementPolicy);
                    out("Sending lifecycleApproveChaincodeRequest to all peers...");
                    successful.clear();
                    failed.clear();
                    responses1 = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeRequest, peersFromOrg);
                    for (ProposalResponse response : responses1) {
                        if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                            successful.add(response);
                            out("[√] Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                        } else {
                            failed.add(response);
                        }
                    }
                    out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses1.size(), successful.size(), failed.size());
                    if (failed.size() > 0) {
                        ProposalResponse first = failed.iterator().next();
                        out("[X] Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
                    }
                }

                ///////////////
                /// Send instantiate transaction to orderer
                out("Sending instantiateTransaction to orderer...");
                CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, orderers);

                out("calling get...");
                BlockEvent.TransactionEvent event = future.get(30, TimeUnit.SECONDS);
                out("get done...");

                out("Finished instantiate transaction with transaction id %s, %s", event.getTransactionID(), event.isValid());
                return successful;
            }
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

    /**
     * @Description: 检查当前通道是否已经安装过链码
     * @param channel
     * @param peer
     * @param ccPath
     * @param ccName
     * @param ccVersion
     * @return: boolean
     * @Author: mawl
     * @Date: 2020-08-04 15:57
     **/
    private static boolean checkInstantiatedChaincode(Channel channel, Peer peer, String ccPath, String ccName, String ccVersion) throws InvalidArgumentException, ProposalException {
        out("Checking instantiated chaincode: %s, at version: %s, on peer: %s", ccName, ccVersion, peer.getName());

        boolean found = false;

        List<Query.ChaincodeInfo> ccinfoList = channel.queryInstantiatedChaincodes(peer);
        for (Query.ChaincodeInfo ccifo : ccinfoList) {
            found = ccName.equals(ccifo.getName()) && ccPath.equals(ccifo.getPath()) && ccVersion.equals(ccifo.getVersion());
            if (found) {
                break;
            }
        }

        return found;
    }

    /** ------------------------------------- Invoke Chain Code --------------------------------------- **/

    /**
     * @Description: 图表数据支持
     * @param client
     * @param channel
     * @param chaincodeName
     * @param chaincodeVersion
     * @param fcn
     * @param args
     * @return: java.lang.String
     * @Author: mawl
     * @Date: 2020-08-04 18:39
     **/
    private static String queryChaincode (HFClient client,
                                            Channel channel,
                                            String chaincodeName,
                                            String chaincodeVersion,
                                            String fcn,
                                            String[] args) {

        out("Now query chaincode on channel %s with arguments: %s:%s.%s(%s)", channel.getName(), chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

        String expect = null;
        try {
            QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
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

    /**
     * @Description: 图表数据支持
     * @param client
     * @param channel
     * @param chaincodeName
     * @param chaincodeVersion
     * @param fcn
     * @param args
     * @param user
     * @return: java.util.concurrent.CompletableFuture<org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent>
     * @Author: mawl
     * @Date: 2020-08-04 19:00
     **/
    private static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode (HFClient client,
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
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
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
