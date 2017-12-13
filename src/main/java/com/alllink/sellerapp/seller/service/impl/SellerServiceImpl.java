package com.alllink.sellerapp.seller.service.impl;

import com.alllink.commons.utils.RandomNumberUtil;
import com.alllink.sellerapp.seller.dao.SellerDao;
import com.alllink.sellerapp.seller.entity.SellerEntity;
import com.alllink.sellerapp.seller.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;

@Service("sellerService")
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;
    @Override
    public void activeSeller(HashMap<String, String> map) {

        sellerDao.activeSeller(map);
    }



    @Override
    public HashMap<String, Object> login(HashMap<String, String> map) {
         return sellerDao.login(map);
    }

    @Override
    public void update(SellerEntity seller) {
        sellerDao.update(seller);
    }

    @Override
    public SellerEntity checkPhone(String phoneNumber) {
        return sellerDao.checkPhone(phoneNumber);
    }

    @Override
    public void register(String phoneNumber, String verificationCode, Timestamp currentTime) {
//        String uid = UUID.randomUUID().toString().replaceAll("-", "");
//        System.out.println(uid);
//        sellerDao.register(uid, phoneNumber, checkcode, currentTime);
        String salt = RandomNumberUtil.CreateSalt();
        sellerDao.register(phoneNumber, verificationCode, currentTime, salt);
    }

    @Override
    public void updateCheckcode(String phoneNumber, String verificationCode, Timestamp currentTime) {
        sellerDao.updateCheckcode(phoneNumber, verificationCode, currentTime);
    }

    @Override
    public String getSalt(String phoneNumber) {
        return sellerDao.getSalt(phoneNumber);

    }

    @Override
    public SellerEntity findSellerById(int id) {
        return sellerDao.findSellerById(id);
    }


}
