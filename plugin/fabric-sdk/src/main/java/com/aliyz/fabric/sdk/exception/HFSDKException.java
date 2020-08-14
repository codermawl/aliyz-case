package com.aliyz.fabric.sdk.exception;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-08-06 09:53
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class HFSDKException extends RuntimeException {

    private String errMessage;

    public HFSDKException (String message) {
        this.errMessage = message;
    }

    public HFSDKException (Throwable cause) {
        super(cause);
        this.errMessage = String.format("Fabric SDK 运行时异常！");
    }

    public HFSDKException (String type, String message) {
        this(type, message, null);
    }

    public HFSDKException (String type, Throwable cause) {
        this(type, "", cause);
    }

    public HFSDKException (String type, String message, Throwable cause) {
        super(cause);

        if ("InvalidArgumentException".equals(type)) {
            this.errMessage = String.format("请求区块链网络，参数校验异常！%s", message);
        } else if ("ProposalException".equals(type)) {
            this.errMessage = String.format("请求区块链网络，议案提交异常！%s", message);
        } else if ("TransactionException".equals(type)) {
            this.errMessage = String.format("请求区块链网络，交易提交异常！%s", message);
        } else {
            this.errMessage = String.format("Fabric SDK 运行时异常！%s", message);
        }
    }

    public String getMessage() {
        return errMessage;
    }
}
