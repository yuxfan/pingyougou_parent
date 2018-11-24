package com.jlb.core.sms;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

    /**
     * 短信消息监听类
     */
    public class MyMessageListener implements MessageListener {

        @Resource
        private SmsUtil smsUtil;

        @Value("${templateCode_smscode}")
        private String templateCode;

        @Value("${templateParam_smscode}")
        private String  param;

        @Override
        public void onMessage(Message message) {
            MapMessage mapMessage=(MapMessage)message;
            try {
                String mobile= mapMessage.getString("mobile");//手机号
                String smscode= mapMessage.getString("smscode");//验证码
                System.out.println("接收到短信： "+mobile+"..."+ smscode);

                //完成发送
                    smsUtil.sendSms( mobile,templateCode, param.replace("[value]",smscode));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

