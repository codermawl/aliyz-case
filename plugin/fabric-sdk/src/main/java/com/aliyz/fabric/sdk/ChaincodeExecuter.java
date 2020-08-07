package com.aliyz.fabric.sdk;

import com.aliyz.fabric.sdk.exception.HFSDKException;
import com.aliyz.fabric.sdk.utils.CompareUtils;
import com.aliyz.fabric.sdk.utils.EmptyUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-08-07 15:13
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class ChaincodeExecuter extends SdkExecuter {

    /**
     * @Description:  链码打包
     *
     *     // peer lifecycle chaincode package mycc.tar.gz \
     *     // --path github.com/hyperledger/fabric-samples/chaincode/abstore/go/ \
     *     // --lang golang \
     *     // --label mycc_1
     *
     * 链码路径组成比较多，这里先列出一个链码的绝对路径：
     *  "/Workspace/java/code.aliyz.com/fabric/gocc/mycc/src/github.com/mycc"
     * （注：chaincodeSourcePath 和 chaincodePath 中间有一层 "src" 目录，不需要传入）
     * 详细见下：
     *
     * @param chaincodeLabel 链码标签，如：mycc_1.0，一般 ${ccName}_${ccVersion}
     * @param chaincodeSourcePath 链码资源路径，如："/Workspace/java/code.aliyz.com/fabric/gocc/mycc"
     * @param chaincodeMetainfoPath 链码元数据路径，就是链码相关的 "/META-INF" 文件夹路径
     * @param chaincodePath 链码路径，如："github.com"
     * @param chaincodeName 链码名称，如："mycc"
     * @param chaincodeType 链码开发语言类型，枚举
     *
     * @return: org.hyperledger.fabric.sdk.LifecycleChaincodePackage
     * @Author: aliyz
     * @Date: 2020-08-07 10:03
     **/
    public static LifecycleChaincodePackage packageChaincode (String chaincodeLabel,
                                                              String chaincodeSourcePath,
                                                              String chaincodeMetainfoPath,
                                                              String chaincodePath,
                                                              String chaincodeName,
                                                              TransactionRequest.Type chaincodeType) throws HFSDKException {

        String ccPath = chaincodePath + "/" + chaincodeName;

        try {
            return LifecycleChaincodePackage.fromSource(chaincodeLabel,
                    Paths.get(chaincodeSourcePath),
                    chaincodeType,
                    ccPath,
                    Paths.get(chaincodeMetainfoPath));
        } catch (InvalidArgumentException e) {
            throw new HFSDKException("InvalidArgumentException", "package chaincode error: ", e);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // 链码安装
    // peer lifecycle chaincode install mycc.tar.gz
    public static String installChaincode (HFClient hfClient,
                                           Channel channel,
                                           Collection<Peer> peers,
                                           LifecycleChaincodePackage lifecycleChaincodePackage) throws ProposalException, InvalidArgumentException {

        try {

            Collection<LifecycleInstallChaincodeProposalResponse> responses;
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            out("Creating lifecycleInstallChaincodeRequest");
            LifecycleInstallChaincodeRequest lifecycleInstallChaincodeRequest = hfClient.newLifecycleInstallChaincodeRequest();
            lifecycleInstallChaincodeRequest.setLifecycleChaincodePackage(lifecycleChaincodePackage);
            lifecycleInstallChaincodeRequest.setProposalWaitTime(10 * 60 * 1000); // 等待10min
            out("Sending lifecycleInstallChaincodeRequest to selected peers...");
            int numInstallProposal = peers.size();
            responses = hfClient.sendLifecycleInstallChaincodeRequest(lifecycleInstallChaincodeRequest, peers);
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

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // 查询链码安装结果，返回已经安装了目标链码的 Peer 节点
    // peer lifecycle chaincode queryinstalled
    public static boolean queryInstalled (HFClient hfClient,
                                          Collection<Peer> peers,
                                          String packageId,
                                          String chaincodeLabel) throws HFSDKException {

        try {

            final LifecycleQueryInstalledChaincodeRequest lifecycleQueryInstalledChaincodeRequest = hfClient.newLifecycleQueryInstalledChaincodeRequest();
            lifecycleQueryInstalledChaincodeRequest.setPackageID(packageId);
            Collection<LifecycleQueryInstalledChaincodeProposalResponse> responses = hfClient.sendLifecycleQueryInstalledChaincode(lifecycleQueryInstalledChaincodeRequest, peers);

            if (peers.size() != responses.size()) {
                throw new HFSDKException(String.format("responses %d not same as peers %d.", responses.size(), peers.size()));
            }

            boolean found = false;
            for (LifecycleQueryInstalledChaincodeProposalResponse response : responses) {

                String peerName = response.getPeer().getName();

                if (response.getStatus().equals(ChaincodeResponse.Status.SUCCESS)) {
                    if (chaincodeLabel.equals(response.getLabel())) {
                        out("[√] Peer %s returned back same label: %s", peerName, response.getLabel());
                        found = true;
                    } else {
                        out("[?] Peer %s returned back different label: %s", peerName, response.getLabel());
                    }
                } else {
                    out("[X] Peer %s returned back bad status code: %s", peerName, response.getStatus());
                }
            }

            return found;

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (HFSDKException e3) {
            throw e3;
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // 审批链码
    // peer lifecycle chaincode approveformyorg \
    // -o localhost:7050 \
    // --ordererTLSHostnameOverride orderer.example.com \
    // --tls /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem \
    // --cafile  \
    // --channelID mychannel \
    // --name mycc \
    // --version 1 \
    // --package-id mycc_1:f5cc6d6e871262e8da9788f3d463442e51c482ec8288c13e4545741ad45d86fa \
    // --sequence 1 \
    // --init-required \
    // --signature-policy \
    // --collections-config
    public static CompletableFuture<BlockEvent.TransactionEvent> approveForMyOrg (HFClient hfClient,
                                                                                  Channel channel,
                                                                                  Collection<Peer> peers,
                                                                                  long sequence,
                                                                                  String chaincodeName,
                                                                                  String chaincodeVersion,
                                                                                  LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy,
                                                                                  ChaincodeCollectionConfiguration chaincodeCollectionConfiguration,
                                                                                  boolean initRequired,
                                                                                  String packageId) throws HFSDKException {


        try {

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeDefinitionForMyOrgRequest = hfClient.newLifecycleApproveChaincodeDefinitionForMyOrgRequest();
            lifecycleApproveChaincodeDefinitionForMyOrgRequest.setPackageId(packageId);
            lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeName(chaincodeName);
            lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeVersion(chaincodeVersion);
            lifecycleApproveChaincodeDefinitionForMyOrgRequest.setSequence(sequence);
            lifecycleApproveChaincodeDefinitionForMyOrgRequest.setInitRequired(initRequired);

            if (lccEndorsementPolicy != null) {
                lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeEndorsementPolicy(lccEndorsementPolicy);
            }

            if (null != chaincodeCollectionConfiguration) {
                lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeCollectionConfiguration(chaincodeCollectionConfiguration);
            }

            out("Sending lifecycleApproveChaincodeRequest to selected peers...");
            Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> responses = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeDefinitionForMyOrgRequest, peers);
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

            if (EmptyUtils.isEmpty(successful)) {

            }

            return channel.sendTransaction(successful);

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // TODO 查看链码的审批状态
    // peer lifecycle chaincode checkcommitreadiness \
    // --channelID mychannel \
    // --name mycc \
    // --version 1 \
    // --sequence 1 \
    // --output json \
    // --init-required
    public static boolean checkCommitReadiness (HFClient hfClient,
                                                Channel channel,
                                                long sequence,
                                                String chaincodeName,
                                                String chaincodeVersion,
                                                LifecycleChaincodeEndorsementPolicy chaincodeEndorsementPolicy,
                                                ChaincodeCollectionConfiguration chaincodeCollectionConfiguration,
                                                boolean initRequired,
                                                Collection<Peer> peers,
                                                Set<String> expectedApproved,
                                                Set<String> expectedUnApproved) throws HFSDKException {

        try {

            LifecycleCheckCommitReadinessRequest lifecycleCheckCommitReadinessRequest = hfClient.newLifecycleSimulateCommitChaincodeDefinitionRequest();
            lifecycleCheckCommitReadinessRequest.setSequence(sequence);
            lifecycleCheckCommitReadinessRequest.setChaincodeName(chaincodeName);
            lifecycleCheckCommitReadinessRequest.setChaincodeVersion(chaincodeVersion);

            if (null != chaincodeEndorsementPolicy) {
                lifecycleCheckCommitReadinessRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
            }

            if (null != chaincodeCollectionConfiguration) {
                lifecycleCheckCommitReadinessRequest.setChaincodeCollectionConfiguration(chaincodeCollectionConfiguration);
            }

            lifecycleCheckCommitReadinessRequest.setInitRequired(initRequired);

            Collection<LifecycleCheckCommitReadinessProposalResponse> responses = channel.sendLifecycleCheckCommitReadinessRequest(lifecycleCheckCommitReadinessRequest, peers);
            boolean check = true;
            for (LifecycleCheckCommitReadinessProposalResponse response : responses) {
                final Peer peer = response.getPeer();
                if (ChaincodeResponse.Status.SUCCESS.equals(response.getStatus())) {
                    if (!CompareUtils.isContainEachother(expectedApproved, response.getApprovedOrgs())) {
                        check = false;
                        out("Approved orgs failed on %s with expectedApproved: %s, getApprovedOrgs: %s.", peer, expectedApproved, response.getApprovedOrgs());
                    }
                    if (CompareUtils.isContainEachother(expectedUnApproved, response.getUnApprovedOrgs())) {
                        check = false;
                        out("UnApproved orgs failed on %s with expectedUnApproved: %s, getUnApprovedOrgs: %s.", peer, expectedUnApproved, response.getUnApprovedOrgs());
                    }
                } else {
                    out("[X] Check commit readiness returned back bad response on %s, status: %s, message: %s.", peer.getName(), response.getStatus(), response.getMessage());
                }
            }

            return check;

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }

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
    public static CompletableFuture<BlockEvent.TransactionEvent> commitChaincodeDefinition (HFClient hfClient,
                                                                                            Channel channel,
                                                                                            long sequence,
                                                                                            String chaincodeName,
                                                                                            String chaincodeVersion,
                                                                                            LifecycleChaincodeEndorsementPolicy lccEndorsementPolicy,
                                                                                            ChaincodeCollectionConfiguration chaincodeCollectionConfiguration,
                                                                                            boolean initRequired,
                                                                                            Collection<Peer> endorsingPeers) throws HFSDKException {

        try {

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            LifecycleCommitChaincodeDefinitionRequest lifecycleCommitChaincodeDefinitionRequest = hfClient.newLifecycleCommitChaincodeDefinitionRequest();
            lifecycleCommitChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
            lifecycleCommitChaincodeDefinitionRequest.setChaincodeVersion(chaincodeVersion);
            lifecycleCommitChaincodeDefinitionRequest.setSequence(sequence);
            lifecycleCommitChaincodeDefinitionRequest.setInitRequired(initRequired);

            if (lccEndorsementPolicy != null) {
                lifecycleCommitChaincodeDefinitionRequest.setChaincodeEndorsementPolicy(lccEndorsementPolicy);
            }

            if (null != chaincodeCollectionConfiguration) {
                lifecycleCommitChaincodeDefinitionRequest.setChaincodeCollectionConfiguration(chaincodeCollectionConfiguration);
            }

            out("Sending lifecycleApproveChaincodeRequest to selected peers...");
            Collection<LifecycleCommitChaincodeDefinitionProposalResponse>  responses = channel.sendLifecycleCommitChaincodeDefinitionProposal(lifecycleCommitChaincodeDefinitionRequest, endorsingPeers);
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

            if (EmptyUtils.isEmpty(successful)) {
                throw new HFSDKException("commit chaincode definition fail.");
            }

            ///////////////
            /// Send instantiate transaction to orderer
            out("Sending instantiateTransaction to orderer...");
            return channel.sendTransaction(successful);

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (HFSDKException e3) {
            throw e3;
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // 查看链码的提交状态
    // peer lifecycle chaincode querycommitted --channelID mychannel --name mycc
    public static boolean queryCommitted (HFClient hfClient,
                                          Channel channel,
                                          String chaincodeName,
                                          Collection<Peer> peers,
                                          long expectedSequence,
                                          boolean expectedInitRequired,
                                          byte[] expectedValidationParameter,
                                          ChaincodeCollectionConfiguration expectedChaincodeCollectionConfiguration) throws HFSDKException {

        try {
            QueryLifecycleQueryChaincodeDefinitionRequest queryLifecycleQueryChaincodeDefinitionRequest = hfClient.newQueryLifecycleQueryChaincodeDefinitionRequest();
            queryLifecycleQueryChaincodeDefinitionRequest.setChaincodeName(chaincodeName);

            Collection<LifecycleQueryChaincodeDefinitionProposalResponse> responses = channel.lifecycleQueryChaincodeDefinition(queryLifecycleQueryChaincodeDefinitionRequest, peers);

            if (peers.size() != responses.size()) {
                throw new HFSDKException(String.format("responses %d not same as peers %d.", responses.size(), peers.size()));
            }

            boolean checkResult = true;

            for (LifecycleQueryChaincodeDefinitionProposalResponse response : responses) {
                String peer = response.getPeer().getName();

                if (ChaincodeResponse.Status.SUCCESS.equals(response.getStatus())) {

                    if (expectedSequence != response.getSequence()) {
                        out("[X] With %s inconsistent -sequence，actually: %d, expect: %d", peer, response.getSequence(), expectedSequence);
                        checkResult = false;
                    }

                    if (expectedInitRequired != response.getInitRequired()) {
                        out("[X] With %s inconsistent -initRequired，actually: %s, expect: %s", peer, response.getInitRequired(), expectedInitRequired);
                        checkResult = false;
                    }

//                    if (null != expectedValidationParameter && !assertArrayEquals(expectedValidationParameter, response.getValidationParameter())) {
//                        out("[X] With %s inconsistent -validationParameter，actually: %s, expect: %s", peer, response.getValidationParameter(), expectedValidationParameter);
//                        checkResult = false;
//                    }
//
//                    if (null != expectedChaincodeCollectionConfiguration && !assertArrayEquals(expectedChaincodeCollectionConfiguration.getAsBytes(), response.getChaincodeCollectionConfiguration().getAsBytes())) {
//                        out("[X] With %s inconsistent -chaincodeCollectionConfiguration，actually: %s, expect: %s", peer, response.getChaincodeCollectionConfiguration().getAsBytes(), expectedChaincodeCollectionConfiguration);
//                        checkResult = false;
//                    }

                } else {
                    out("[X] Received %s bad response, status: %s, message: %s.", peer, response.getStatus(), response.getMessage());
                }
            }
            return checkResult;

        }  catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (HFSDKException e3) {
            throw e3;
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
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
    public static CompletableFuture<BlockEvent.TransactionEvent> initChaincode (HFClient hfClient,
                                                                                User userContext,
                                                                                Channel channel,
                                                                                boolean initRequired,
                                                                                String chaincodeName,
                                                                                String chaincodeVersion,
                                                                                TransactionRequest.Type chaincodeType,
                                                                                String[] args) throws HFSDKException {

        final String fcn = "init";
        boolean doInit = initRequired ? true : null;

        return basicInvokeChaincode(hfClient, userContext, channel, fcn, doInit, chaincodeName, chaincodeVersion, chaincodeType, args);
    }

    // 链码查询
    // peer chaincode query -C mychannel -n mycc -c '{"Args":["query","a"]}'
    public static String queryChaincode (HFClient hfClient,
                                         User userContext,
                                         Channel channel,
                                         String fcn,
                                         String chaincodeName,
                                         String chaincodeVersion,
                                         String[] args) throws HFSDKException {

        out("Now query chaincode on channel %s with arguments: %s:%s.%s(%s)", channel.getName(), chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

        try {
            QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(args);
            queryByChaincodeRequest.setFcn(fcn);
            queryByChaincodeRequest.setChaincodeName(chaincodeName);
            queryByChaincodeRequest.setChaincodeVersion(chaincodeVersion);
            queryByChaincodeRequest.setUserContext(userContext);

            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest);

            String expect = null;
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ChaincodeResponse.Status.SUCCESS) {
                    out("[X] Failed query proposal from peer %s status: %s. Messages: %s. Was verified : %s.",
                            proposalResponse.getPeer().getName(), proposalResponse.getStatus(), proposalResponse.getMessage(),
                            proposalResponse.isVerified());
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    out("[√] Query payload of b from peer %s returned %s", proposalResponse.getPeer().getName(), payload);
                    if (expect == null) {
                        expect = payload;
                    }
                }
            }

            return expect;

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
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

    public static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode (HFClient hfClient,
                                                                                  User userContext,
                                                                                  Channel channel,
                                                                                  String fcn,
                                                                                  String chaincodeName,
                                                                                  String chaincodeVersion,
                                                                                  TransactionRequest.Type chaincodeType,
                                                                                  String[] args) throws HFSDKException {
        boolean doInit = false;
        return basicInvokeChaincode(hfClient, userContext, channel, fcn, doInit, chaincodeName, chaincodeVersion, chaincodeType, args);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> basicInvokeChaincode (HFClient hfClient,
                                                                                        User userContext,
                                                                                        Channel channel,
                                                                                        String fcn,
                                                                                        Boolean doInit,
                                                                                        String chaincodeName,
                                                                                        String chaincodeVersion,
                                                                                        TransactionRequest.Type chaincodeType,
                                                                                        String[] args) throws HFSDKException {
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        try {

            TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeName(chaincodeName);
            transactionProposalRequest.setChaincodeVersion(chaincodeVersion);
            transactionProposalRequest.setChaincodeLanguage(chaincodeType);
            transactionProposalRequest.setFcn(fcn);
            transactionProposalRequest.setArgs(args);
            transactionProposalRequest.setProposalWaitTime(60);

            if (userContext != null) {
                transactionProposalRequest.setUserContext(userContext);
            }

            if (doInit != null) {
                transactionProposalRequest.setInit(doInit);
            }

            out("Sending transaction proposal on channel %s to all peers with arguments: %s:%s.%s(%s)",
                    channel.getName(), chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

            Collection<ProposalResponse> responses = channel.sendTransactionProposal(transactionProposalRequest);
            for (ProposalResponse response : responses) {
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
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(responses);
            if (proposalConsistencySets.size() != 1) {
                out("Expected only one set of consistent move proposal responses but got %d", proposalConsistencySets.size());
            }

            out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(),
                    failed.size());

            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                out("Not enough endorsers for invoke(%s:%s.%s(%s)):%d endorser error:%s. Was verified:%b",
                        chaincodeName, chaincodeVersion, fcn, Arrays.asList(args), firstTransactionProposalResponse.getStatus().getStatus(),
                        firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified());
            }

            out("Successfully received transaction proposal responses.");

            out("Sending chaincode transaction：invoke(%s:%s.%s(%s)) to orderer.", chaincodeName, chaincodeVersion, fcn, Arrays.asList(args));

            return channel.sendTransaction(successful);

        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }
}
