# Bluetooth low energy

 Projeto com arquitetura MVVM em JAVA para realizar um simples SCAN e conectar em um disposítivo especifico e enviar um comando estático.

 ## Usage

 Para usar a classe BleUtils

 ```java

 BleUtils mBleUtils = BleUtils.getInstance(context);
 mBleUtils.setListener(...);
 mBleUtils.initScan();
 mBleUtils.connect(context,device,mac);