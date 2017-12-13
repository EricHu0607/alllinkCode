package com.alllink.sellerapp.seller.controller;


import com.aliyuncs.exceptions.ClientException;
import com.alllink.commons.SendSMS;
import com.alllink.commons.utils.R;
import com.alllink.commons.utils.SHAUtil;
import com.alllink.commons.utils.TimeUtil;
import com.alllink.commons.utils.RandomNumberUtil;
import com.alllink.sellerapp.seller.entity.SellerEntity;
import com.alllink.sellerapp.seller.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(value = "/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    /**
     * 添加商家
     *
     * */
    @RequestMapping(value="/add" ,method= RequestMethod.POST)
    @ResponseBody
    public R add(@RequestBody HashMap<String, String> map) throws ParseException {
        System.out.println("add.. phoneNumber" + map.get("phoneNumber"));
        String phoneNumber = map.get("phoneNumber");
        String password = map.get("password");

        //校验添加商家的手机号和密码
        if(phoneNumber.length() != 11)
            return R.error("手机长度应该为十一位");
        if(password.length() < 3 || password.length() > 20)
            return R.error("密码长度应该在3~20位之间");

        SellerEntity sellerEntity = sellerService.checkPhone(phoneNumber);
        if(sellerEntity == null){
            System.out.println("手机号错误");
            return R.error(0, "手机号错误");
        }

        String verificationCode = map.get("verificationCode");
        Timestamp currentTime = TimeUtil.getCurrentTime();
        Timestamp codeTime = sellerEntity.getCodeCreatTime();
        Long betweenTime = currentTime.getTime() - codeTime.getTime() ;//验证码的时间差
        if(betweenTime/1000 > 60){
            return R.error(0, "验证码过期");
        }

        String code = sellerEntity.getVerificationCode();
        System.out.println(code + " = " +verificationCode);
        if(!code.equals(verificationCode)) {
            System.out.println("验证码错误");
            return R.error(0, "验证码错误");
        }

        //对密码加密从新放入map中
        map.put("password", SHAUtil.SHAEncode(password + sellerEntity.getSalt() ));
        sellerService.activeSeller(map);

        Map<String, Object> sellerMap = new HashMap<>();
        sellerMap.put("phoneNumber", phoneNumber);
        return R.ok(sellerMap);
    }

    /**
     * 商家登录
     *
     * */
    @RequestMapping(value="/login" ,method= RequestMethod.POST)
    @ResponseBody
    public R login(@RequestBody HashMap<String, String> map, HttpServletRequest request) {
        System.out.println(map.get("phoneNumber") + map.get("password"));

        String encodepassword = map.get("password") + sellerService.getSalt(map.get("phoneNumber"));
        map.put("password", SHAUtil.SHAEncode(encodepassword));
        HashMap<String, Object> sellerMap = sellerService.login(map);
        System.out.println(sellerMap.get("state") + sellerMap.toString());
        if (sellerMap.isEmpty()) {
            System.out.println("用户名密码错误");
            return R.error(0, "用户名或密码错误");
        } else if (sellerMap.get("state").equals(0)) {
            return R.error(0, "用户没有短信激活");
        } else if (sellerMap.get("state").equals(1)) {
            HttpSession session = request.getSession();
            session.setAttribute("seller", sellerMap);
            Map<String, Object> seller = new HashMap<>();
            seller.put("seller", sellerMap);
            return R.ok(seller);
        }
        return R.error();
    }

    /*
    * 登出
    * */
    @RequestMapping(value="/logout")
    public void logout(HttpServletRequest request){
        request.getSession().invalidate();
    }


    /**
     * 商家信息修改
     *
     * */
    @RequestMapping(value="/update" ,method= RequestMethod.POST)
    @ResponseBody
    public R update(@RequestBody SellerEntity seller, HttpServletRequest request) throws ParseException {
        System.out.println("seller:" + seller);
        //seller.setCreateTime(getCurrentTime());
        //SellerEntity sellerEntity = (SellerEntity) request.getSession().getAttribute("seller");
        String password = seller.getPassword();
        if(password != null) {
            String encodepassword = seller.getPassword() + sellerService.getSalt(seller.getSalt());
            seller.setPassword(SHAUtil.SHAEncode(encodepassword));
        }
//        seller.setSellerId(14);
        SellerEntity sessionSeller = (SellerEntity) request.getSession().getAttribute("seller");
        seller.setSellerId(sessionSeller.getSellerId());
        sellerService.update(seller);

        Map<String, Object> sellerMap = new HashMap<>();
        sellerMap.put("seller", sellerService.findSellerById(seller.getSellerId()));
        return R.ok(sellerMap);
    }

    /**
     *
     * 验证手机号码是否注册
     */
    @RequestMapping(value="/checkPhone" ,method= RequestMethod.POST)
    @ResponseBody
    public R checkPhone(@RequestParam("phoneNumber") String phoneNumber){
        System.out.println("SellerController checkPhone...."+phoneNumber);
        SellerEntity sellerEntity = sellerService.checkPhone(phoneNumber);
        if(sellerEntity != null && sellerEntity.getState() == 1) {
            return R.error(0, "手机号已经注册");
        }
        return R.ok();
    }

    /*
    *
    * 发送短信验证码
    * */
    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    @ResponseBody
    public R sendMessage(@RequestBody HashMap<String, String> map, HttpServletRequest request) throws ClientException, InterruptedException, ParseException {
        String phoneNumber = map.get("phoneNumber");
        String verificationCode = RandomNumberUtil.CreateVerificationCode();
        //获取当前时间
        Timestamp currentTime =TimeUtil.getCurrentTime();

        SellerEntity sellerEntity = sellerService.checkPhone(phoneNumber);

        if(sellerEntity == null) {
            sellerService.register(phoneNumber, verificationCode, currentTime);

        }else{
            sellerService.updateCheckcode(phoneNumber, verificationCode, currentTime);
        }
        //SendSMS.send(phoneNumber, verificationCode);
        System.out.println(verificationCode);
        return R.ok();
    }

}