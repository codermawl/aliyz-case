package com.aliyz.fabric.sdk;

import com.aliyz.fabric.sdk.exception.HFSCADKException;
import com.aliyz.fabric.sdk.utils.EmptyUtils;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;

import java.util.Set;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 *
 * CA:
 *  用户信息的登记与注册，数字证书的颁发与管理
 *
 *  证书吊销、机构注册 ？？
 *
 * Created by mawl at 2020-08-12 14:20
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class CASDK extends ISdk {

    /**
     * @Description: 用户登记
     * @param client
     * @param affiliation 所属机构
     * @param orgAdmin 结构管理员ID
     * @param orgAdminpw 结构管理员密码
     * @param enrollId 待登记的用户ID
     * @param enrollSecret 自定义用户密码
     * @param attrs 用户自定义的一些属性信息
     * @return: enroll secret，如果参数 enrollSecret 为空，则CA返回随机密码，否则返回传入的 enrollSecret 值
     * @Author: aliyz
     * @Date: 2020-08-12 18:00
     **/
    public static String register (HFCAClient client,
                                   String affiliation,
                                   String orgAdmin,
                                   String orgAdminpw,
                                   String enrollId,
                                   String enrollSecret,
                                   Attribute ... attrs) throws HFSCADKException {

        if (EmptyUtils.isBlack(orgAdmin) || EmptyUtils.isBlack(orgAdminpw)) {
            throw new HFSCADKException("管理员信息为空，将不能访问到CA");
        }

        try {
            Enrollment adminEnrollment = client.enroll(orgAdmin, orgAdminpw);
            return register(client, affiliation, adminEnrollment, enrollId, enrollSecret, attrs);
        } catch (InvalidArgumentException e) {
            throw new HFSCADKException("InvalidArgumentException", e);
        } catch (Exception e) {
            throw new HFSCADKException(e);
        }
    }
    public static String register (HFCAClient client,
            String affiliation,
            Enrollment adminEnrollment,
            String enrollId,
            String enrollSecret,
            Attribute ... attrs) throws HFSCADKException {

        if (EmptyUtils.isBlack(enrollId)) {
            throw new HFSCADKException("'enrollId' 不能为空");
        }

        if (adminEnrollment == null) {
            throw new HFSCADKException("管理员信息为空，将不能访问到CA");
        }

        try {
            // register的时候在registrationRequest中增加自定义属性
            RegistrationRequest registrationRequest = new RegistrationRequest(enrollId); // setEnrollmentID

            if (!EmptyUtils.isBlack(affiliation)) {
                registrationRequest.setAffiliation(affiliation);
            }

    //            registrationRequest.setMaxEnrollments(); ??

            if (!EmptyUtils.isBlack(enrollSecret)) {
                registrationRequest.setSecret(enrollSecret);
            }

    //            registrationRequest.setType(); ??

            if (!EmptyUtils.isEmpty(attrs)) {
                for (Attribute attr : attrs) {
                    if (EmptyUtils.isBlack(attr.getName())) {
                        throw new HFSCADKException("属性名称不能为空");
                    }
                    registrationRequest.addAttribute(attr);
                }
            }

            User registrar = new User() {
                @Override
                public String getName() {
                    return enrollId;
                }

                @Override
                public Set<String> getRoles() {
                    return null;
                }

                @Override
                public String getAccount() {
                    return null;
                }

                @Override
                public String getAffiliation() {
                    return affiliation;
                }

                @Override
                public Enrollment getEnrollment() {
                    return adminEnrollment;
                }

                @Override
                public String getMspId() {
                    return null;
                }
            };

            return client.register(registrationRequest, registrar);

        } catch (RegistrationException e) {
            throw new HFSCADKException("RegistrationException", e);
        } catch (InvalidArgumentException e) {
            throw new HFSCADKException("InvalidArgumentException", e);
        } catch (Exception e) {
            throw new HFSCADKException(e);
        }
    }

    /**
     * @Description: 用户注册
     * @param client
     * @param enrollId 待登记的用户ID
     * @param enrollSecret 用户登记时的密码
     * @param attrs 需要写入证书的属性，这些属性必须是在用户Register时登记过的，如果 length=0，则所有属性都不写入证书
     *              如果 =null，则只写入默认属性（hf.Affiliation, hf.EnrollmentID, hf.Type）
     * @return: 用户私钥和证书
     * @Author: aliyz
     * @Date: 2020-08-12 18:03
     **/
    public static Enrollment enroll (HFCAClient client, String enrollId, String enrollSecret, String ... attrs) throws HFSCADKException {

        try {
            //定义一个enrollmentRequest，在里面设置需要加入到证书中的属性
            //或者enrollmentRequest.addAttrReq()清空属性，即不把属性加入到证书中
            //没有加入到证书中的属性也是可以在链码中读取的
            EnrollmentRequest enrollmentRequest = new EnrollmentRequest();

            if (attrs != null && attrs.length == 0) {
                enrollmentRequest.addAttrReq();
            } else if (attrs != null && attrs.length > 0) {
                for (String attr : attrs) {
                    enrollmentRequest.addAttrReq(attr);		//default attribute
                }
            }

            return client.enroll(enrollId, enrollSecret, enrollmentRequest);

        } catch (EnrollmentException e) {
            throw new HFSCADKException("EnrollmentException", e);
        } catch (InvalidArgumentException e) {
            throw new HFSCADKException("InvalidArgumentException", e);
        } catch (Exception e) {
            throw new HFSCADKException(e);
        }
    }

    public static void verif (HFCAClient client) throws Exception {
//        client.newHFCAAffiliation("");
//        client.setCryptoSuite();
    }
}
