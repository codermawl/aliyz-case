package com.aliyz.fabric.sdk.exception;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by mawl at 2020-08-06 09:53
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class HFSCADKException extends RuntimeException {

    private String errMessage;

    public HFSCADKException(String message) {
        this.errMessage = message;
    }

    public HFSCADKException(Throwable cause) {
        super(cause);
        this.errMessage = String.format("Fabric-CA SDK 运行时异常！");
    }

    public HFSCADKException(String type, String message) {
        this(type, message, null);
    }

    public HFSCADKException(String type, Throwable cause) {
        this(type, "", cause);
    }

    public HFSCADKException(String type, String message, Throwable cause) {
        super(cause);

        if ("InvalidArgumentException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，参数校验异常！%s", message);
        } else if ("ProposalException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，议案提交异常！%s", message);
        } else if ("TransactionException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，交易提交异常！%s", message);
        } else if ("RegistrationException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，用户注册异常！%s", message);
        } else if ("EnrollmentException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，用户登记异常！%s", message);
        } else if ("IdentityException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，身份操作异常！%s", message);
        } else if ("AffiliationException".equals(type)) {
            this.errMessage = String.format("请求Fabric-CA网络，联盟操纵异常！%s", message);
        } else {
            this.errMessage = String.format("Fabric-CA SDK 运行时异常！%s", message);
        }
    }

    public String getMessage() {
        return errMessage;
    }
}
