package com.fonfon.itagantilost;

import java.util.UUID;

public class BleConstants {

    /**
     * Button BLE service
     */
    public static final UUID FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.immediate_alert.xml">immediate_alert</a>
     */
    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.battery_service.xml">battery_service</a>
     */
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml">client_characteristic_configuration</a>
     */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
