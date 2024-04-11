package com.chensoul.netty.thingsboard.mqtt.auth;

public class DeviceAuthResult {

    private final boolean success;
    private final String deviceId;
    private final String errorMsg;

    public static DeviceAuthResult of(String deviceId) {
        return new DeviceAuthResult(true, deviceId, null);
    }

    public static DeviceAuthResult error(String errorMsg) {
        return new DeviceAuthResult(false, null, errorMsg);
    }

    private DeviceAuthResult(boolean success, String deviceId, String errorMsg) {
        super();
        this.success = success;
        this.deviceId = deviceId;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "DeviceAuthResult [success=" + success + ", deviceId=" + deviceId + ", errorMsg=" + errorMsg + "]";
    }

}
