package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.Address;
import com.kpoptrade.mapper.AddressMapper;
import com.kpoptrade.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listByUser(Long userId) {
        return lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdatedAt)
                .orderByDesc(Address::getId)
                .list();
    }

    @Override
    public Address getById(Long id) {
        return super.getById(id);
    }

    @Override
    public Address getDefaultByUser(Long userId) {
        Address def = lambdaQuery()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .one();
        if (def != null) {
            return def;
        }
        return lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getUpdatedAt)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional
    public Address createAddress(Address address) {
        Date now = new Date();
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        long count = lambdaQuery().eq(Address::getUserId, address.getUserId()).count();
        if (count == 0) {
            address.setIsDefault(1);
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        save(address);
        return address;
    }

    @Override
    @Transactional
    public boolean updateAddress(Address address) {
        address.setUpdatedAt(new Date());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        return updateById(address);
    }

    @Override
    @Transactional
    public boolean deleteAddress(Long userId, Long addressId) {
        Address existing = getById(addressId);
        if (existing == null || !userId.equals(existing.getUserId())) {
            return false;
        }
        boolean removed = removeById(addressId);
        if (removed && existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            Address next = lambdaQuery()
                    .eq(Address::getUserId, userId)
                    .orderByDesc(Address::getUpdatedAt)
                    .last("LIMIT 1")
                    .one();
            if (next != null) {
                next.setIsDefault(1);
                next.setUpdatedAt(new Date());
                updateById(next);
            }
        }
        return removed;
    }

    @Override
    @Transactional
    public boolean setDefault(Long userId, Long addressId) {
        Address existing = getById(addressId);
        if (existing == null || !userId.equals(existing.getUserId())) {
            return false;
        }
        clearDefault(userId);
        existing.setIsDefault(1);
        existing.setUpdatedAt(new Date());
        return updateById(existing);
    }

    @Override
    public boolean belongsToUser(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            return false;
        }
        Address address = getById(addressId);
        return address != null && userId.equals(address.getUserId());
    }

    private void clearDefault(Long userId) {
        lambdaUpdate()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
                .update();
    }
}
