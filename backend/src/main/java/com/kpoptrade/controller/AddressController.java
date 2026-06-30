package com.kpoptrade.controller;

import com.kpoptrade.entity.Address;
import com.kpoptrade.service.AddressService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public R<List<Address>> list() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok(addressService.listByUser(userId));
    }

    @GetMapping("/default")
    public R<Address> getDefault() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        return R.ok(addressService.getDefaultByUser(userId));
    }

    @GetMapping("/{id}")
    public R<Address> get(@PathVariable Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        Address address = addressService.getById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            return R.error("地址不存在");
        }
        return R.ok(address);
    }

    @PostMapping("/create")
    public R<Address> create(@RequestBody Address address) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        String err = validateAddress(address);
        if (err != null) {
            return R.error(err);
        }
        address.setId(null);
        address.setUserId(userId);
        return R.ok(addressService.createAddress(address));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody Address address) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        if (address.getId() == null) {
            return R.error("地址 ID 不能为空");
        }
        Address existing = addressService.getById(address.getId());
        if (existing == null || !userId.equals(existing.getUserId())) {
            return R.error("地址不存在");
        }
        String err = validateAddress(address);
        if (err != null) {
            return R.error(err);
        }
        existing.setReceiver(address.getReceiver());
        existing.setPhone(address.getPhone());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setDetail(address.getDetail());
        if (address.getIsDefault() != null) {
            existing.setIsDefault(address.getIsDefault());
        }
        return R.ok(addressService.updateAddress(existing));
    }

    @PostMapping("/set-default/{id}")
    public R<Boolean> setDefault(@PathVariable Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        if (!addressService.setDefault(userId, id)) {
            return R.error("设置默认地址失败");
        }
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        if (!addressService.deleteAddress(userId, id)) {
            return R.error("删除失败");
        }
        return R.ok(true);
    }

    private String validateAddress(Address address) {
        if (address.getReceiver() == null || address.getReceiver().trim().isEmpty()) {
            return "请填写收货人";
        }
        if (address.getPhone() == null || !address.getPhone().matches("^1\\d{10}$")) {
            return "请填写正确的手机号";
        }
        if (address.getDetail() == null || address.getDetail().trim().isEmpty()) {
            return "请填写详细地址";
        }
        if (address.getReceiver().trim().length() > 64) {
            return "收货人姓名过长";
        }
        if (address.getDetail().trim().length() > 255) {
            return "详细地址过长";
        }
        return null;
    }
}
