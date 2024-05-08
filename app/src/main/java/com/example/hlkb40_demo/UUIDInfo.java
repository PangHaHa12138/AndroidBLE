package com.example.hlkb40_demo;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class UUIDInfo {

    private UUID uuid;
    private String strCharactInfo;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    public UUIDInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getStrCharactInfo() {
        return strCharactInfo;
    }

    public void setStrCharactInfo(String strCharactInfo) {
        this.strCharactInfo = strCharactInfo;
    }

    public String getUUIDString() {
        return uuid.toString();
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return bluetoothGattCharacteristic;
    }

    public void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }
}
