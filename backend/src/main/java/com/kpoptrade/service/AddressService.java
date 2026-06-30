package com.kpoptrade.service;

import com.kpoptrade.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> listByUser(Long userId);

    Address getById(Long id);

    Address getDefaultByUser(Long userId);

    Address createAddress(Address address);

    boolean updateAddress(Address address);

    boolean deleteAddress(Long userId, Long addressId);

    boolean setDefault(Long userId, Long addressId);

    boolean belongsToUser(Long userId, Long addressId);
}
