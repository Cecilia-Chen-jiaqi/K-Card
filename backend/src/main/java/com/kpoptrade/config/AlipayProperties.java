package com.kpoptrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    private String gateway;
    private String appId;
    private String privateKey;
    private String publicKey;
    private String notifyUrl;
    private String returnUrl;
    /** 手机沙箱支付完成后跳转页（需公网 HTTPS，如 ngrok 指向 8080 的 /api/pay/return） */
    private String sandboxReturnFallback;
    /** 本地 notify-url 不可达时，沙箱下单用的占位公网地址（可选） */
    private String sandboxNotifyFallback;
    /** 沙箱 page.pay 不传 notify_url，避免网关校验回调超时 504 */
    private boolean omitNotifyInSandbox = true;
    /** 沙箱不传 return_url，避免手机支付完成后跳转 localhost/ngrok 失败 */
    private boolean omitReturnUrlInSandbox = true;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getSandboxReturnFallback() {
        return sandboxReturnFallback;
    }

    public void setSandboxReturnFallback(String sandboxReturnFallback) {
        this.sandboxReturnFallback = sandboxReturnFallback;
    }

    public String getSandboxNotifyFallback() {
        return sandboxNotifyFallback;
    }

    public void setSandboxNotifyFallback(String sandboxNotifyFallback) {
        this.sandboxNotifyFallback = sandboxNotifyFallback;
    }

    public boolean isOmitNotifyInSandbox() {
        return omitNotifyInSandbox;
    }

    public void setOmitNotifyInSandbox(boolean omitNotifyInSandbox) {
        this.omitNotifyInSandbox = omitNotifyInSandbox;
    }

    public boolean isOmitReturnUrlInSandbox() {
        return omitReturnUrlInSandbox;
    }

    public void setOmitReturnUrlInSandbox(boolean omitReturnUrlInSandbox) {
        this.omitReturnUrlInSandbox = omitReturnUrlInSandbox;
    }
}
