package com.aliyz.fabric.sdk.utils;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-08-05 10:46
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class MySDKUtils {

    public static Collection<Peer> extractPeersFromChannel (Channel channel, String[] peerNames) throws InvalidArgumentException {

        Collection<Peer> allPeers = channel.getPeers();

        if (peerNames == null || peerNames.length ==0) {
            return allPeers;
        } else {
            Collection<Peer> selectPeers = new ArrayList<>();
            for (String peerName : peerNames) {

                boolean isFind = false;
                for (Peer peer : allPeers) {
                    if (peer.getName().equals(peerName)) {
                        selectPeers.add(peer);
                        isFind = true;
                        break;
                    }
                }

                if (!isFind) {
                    throw new InvalidArgumentException(String.format("The specified peer %s does not exist in the current channel: %s.", peerName, channel.getName()));
                }
            }

            return selectPeers;
        }
    }
}
