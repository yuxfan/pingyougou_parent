package com.jlb.core.service.pay;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 生成微信支付二维码
     * @param out_trade_no   订单号
     * @param total_fee  总金额
     * @return
     */
    @Override
    public HashMap createNative(String out_trade_no,String total_fee) {
        //1,创建参数
        HashMap<String,String> param=new HashMap();
        param.put("appid",appid);   //公众账号id
        param.put("mch_id",partner);   //商户号
        param.put("nonce_str",WXPayUtil.generateNonceStr());  //随机字符串
        param.put("body","品优购");   //商品描述
        param.put("out_trade_no",out_trade_no);  //商户订单号
        param.put("total_fee",total_fee);  //总金额
        param.put("spbill_create_ip","127.0.0.1");  //终端IP
        param.put("notify_url","http://www.baidu.com");   //通知地址
        param.put("trade_type","NATIVE");   //交易类型
        //2,发送请求
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数为: "+paramXml);
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(paramXml);
            client.post();
        //3,接受请求
            String result = client.getContent();
            System.out.println("接受请求 :"+result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            HashMap<String,String> map=new HashMap<>();
            map.put("code_url",resultMap.get("code_url"));  //二维码地址
            map.put("total_fee",total_fee);   //支付金额
            map.put("out_trade_no",out_trade_no);   //商家订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public HashMap queryPayStatus(String out_trade_no) {
        //1,创建参数
        HashMap<String,String> map =new HashMap<>();
        map.put("appid",appid);
        map.put("mch_id",partner);
        map.put("out_trade_no",out_trade_no);
        map.put("nonce_str",WXPayUtil.generateNonceStr());

        try {
            //2,发送请求
            String mapXml = WXPayUtil.generateSignedXml(map, partnerkey);
            System.out.println("发送请求:"+mapXml);
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(mapXml);
            httpClient.post();

            //3,接受请求
            String content = httpClient.getContent();
            HashMap<String, String> resultMap = (HashMap) WXPayUtil.xmlToMap(content);
            System.out.println("返回结果: "+resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 循环查询订单支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public HashMap queryPayStatusWhile(String out_trade_no) {
        HashMap map =null;   //初始化map
        int x=0;    //定义变量
        while (true){
            x++;
            if (x>10){  //每3秒循环一次,5分钟300秒,循环100次就停止跳出
                break;
            }
            map=queryPayStatus(out_trade_no);    //根据订单号查询支付状态
            if (map==null){    //如果为空
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))){   //如果成功
                break;
            }
            try {
                Thread.sleep(1000*3);     //间隔3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
