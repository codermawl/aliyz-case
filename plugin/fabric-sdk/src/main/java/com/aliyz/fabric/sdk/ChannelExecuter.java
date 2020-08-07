package com.aliyz.fabric.sdk;

import com.aliyz.fabric.sdk.exception.HFSDKException;
import com.aliyz.fabric.sdk.model.SampleOrg;
import com.aliyz.fabric.sdk.model.SampleUser;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.File;
import java.util.*;

import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-08-07 15:12
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class ChannelExecuter extends SdkExecuter {

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
     * @param client
     * @param channelName
     * @param org
     * @param orgPeers
     * @param orderers
     * @param createFabricChannel
     * @param channelTxPath
     * @return:
     * @Author: mawl
     * @Date: 2020-08-04 11:21
     **/
    public static Channel constructChannel (HFClient client,
                                            String channelName,
                                            SampleOrg org,
                                            Collection<Peer> orgPeers,
                                            Collection<Orderer> orderers,
                                            boolean createFabricChannel,
                                            String channelTxPath) throws HFSDKException {

        out("Constructing channel %s", channelName);

        try {
            SampleUser peerAdmin = org.getPeerAdmin();
            client.setUserContext(peerAdmin);

            //Just pick the first orderer in the list to create the channel.
            Orderer anOrderer = orderers.iterator().next();
            orderers.remove(anOrderer);

            Channel newChannel = null;
            if (createFabricChannel) {
                ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelTxPath));
                newChannel = client.newChannel(channelName, anOrderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));
            } else {
                newChannel = client.newChannel(channelName).addOrderer(anOrderer);
            }

            out("Created channel %s with %s admin, %s orderer. ", channelName, peerAdmin, anOrderer.getName());

            Set<String> peerNames = Collections.synchronizedSet(org.getPeerNames());
            for (Peer peer : orgPeers) {

                if (peerNames.add(peer.getName())) {
                    throw new HFSDKException(String.format("The peer named %s does not belong to org %s.", peer.getName(), org.getName()));
                }

                newChannel.joinPeer(peer, createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE))); //Default is all roles.

                out("Peer %s joined channel %s", peer.getName(), channelName);
            }

            // Make sure there is one of each type peer at the very least.
            if (newChannel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).isEmpty()) {
                throw new HFSDKException(String.format("No peer's role is %s.", Peer.PeerRole.EVENT_SOURCE));
            }

            if (newChannel.getPeers(Peer.PeerRole.NO_EVENT_SOURCE).isEmpty()) {
                throw new HFSDKException(String.format("No peer's role is %s.", Peer.PeerRole.NO_EVENT_SOURCE));
            }

            //add remaining orderers if any.
            for (Orderer orderer : orderers) {
                newChannel.addOrderer(orderer);
            }

            newChannel.initialize();

            out("Channel %s created successfully.", channelName);
            return newChannel;
        } catch (InvalidArgumentException e1) {
            throw new HFSDKException("InvalidArgumentException", e1);
        } catch (ProposalException e2) {
            throw new HFSDKException("ProposalException", e2);
        } catch (TransactionException e2) {
            throw new HFSDKException("TransactionException", e2);
        } catch (HFSDKException e4) {
            throw e4;
        } catch (Exception e) {
            throw new HFSDKException(e);
        }
    }

    // 关闭通道
    public static boolean shutdownChannel (Channel channel) throws HFSDKException {

        try {
            if (!channel.isShutdown()) {
                out("Shutting down channel: %s.", channel.getName());

                channel.shutdown(false);
                return true;
            }
        } catch (Exception e) {
            throw new HFSDKException(e);
        }

        return false;
    }

    // Peer 加入通道
    public static List<Peer> joinPeerToChannel (Channel channel, Collection<Peer> peers) {
        out("set %d peers join channel: %s.", peers.size(), channel.getName());
        List<Peer> failPeers = new ArrayList<>();

        for (Peer peer : peers) {

            try {
                if (peer == null) {
                    out("peer: %s joined...", peer.getName());
                    channel.joinPeer(peer, createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER,
                            Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE))); //Default is all roles.
                }
            } catch (ProposalException e1) {
                failPeers.add(peer);
                e1.printStackTrace();
            }
        }
        out("peer join complete, success: %d, fail: %d.", (peers.size() - failPeers.size()), failPeers.size());

        return failPeers;
    }

    // 将 Peer 从通道移除
    public static List<Peer> removePeerFromChannel (Channel channel, Collection<Peer> peers) {
        out("set %d peers remove channel: %s.", peers.size(), channel.getName());
        List<Peer> failPeers = new ArrayList<>();

        for (Peer peer : peers) {
            try {
                if (peer == null) {
                    out("peer: %s removed...", peer.getName());
                    channel.removePeer(peer);
                }
            } catch (InvalidArgumentException e1) {
                failPeers.add(peer);
                e1.printStackTrace();
            }
        }
        out("peer removed complete, success: %d, fail: %d.", (peers.size() - failPeers.size()), failPeers.size());

        return failPeers;
    }
}
