
import java.util.Arrays;



public class MagneticEncoderSerialPortService {


    private final Logger logger = Logs.getLogger(this.getClass().getSimpleName());

    private int sampleSum = 0;
    private boolean keyActivate = false;
    private int previousAngle = 0;


    /**
     * 校验数据
     * */
    public void receiveData(byte[] data, int totalCount, int angleIncrement)  {
        if (checkCRC8(data)) {
            decodeData(data,totalCount,angleIncrement);
        } else {
            logger.info("消息体差错CRC8校验失败");
        }
    }


    /**
     * 检验CRC-8多项式
     * */
    public boolean checkCRC8(byte[] data) {
        if (data == null ) {       /*data == null || data.length < 3  磁编码器可能data.length是2----在发送0时**/
            logger.info("数据是null");
            return false;
        }
        // 自定义CRC-8多项式和初始值
        int crcPoly = 0x97;
        int crcInitValue = 0x00;

        // 分离数据和CRC位
        byte[] dataBytes = Arrays.copyOf(data, data.length - 1); // 数据部分，不包括CRC位
        byte crcByte = data[data.length - 1]; // 单独提取CRC位

        int crcResult = customCRC8(dataBytes, crcPoly, crcInitValue);
        logger.info("计算得到的crcResult(int)为："+ crcResult);
        logger.info("获得的校验位(Byte)为："+String.valueOf(crcByte));
        return crcResult == (crcByte & 0xFF);
    }


    public  int customCRC8(byte[] data, int poly, int initValue) {
        int crc = initValue;  // 初始化CRC值

        // 遍历数据中的每个字节
        for (byte b : data) {
            crc ^= b;  // 将当前字节与CRC进行异或操作

            // 处理当前字节的每个位
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {  // 如果CRC最高位为1
                    crc = (crc << 1) ^ poly;  // 左移1位并与多项式异或
                } else {
                    crc <<= 1;  // 否则只左移1位
                }
            }
        }

        return crc & 0xFF;  // 返回CRC校验和的低8位（0xFF用于确保结果为1字节）
    }

    /**
     * 处理数据
     */
    public void decodeData(byte[] bytes, int totalCount, int angleIncrement) {
        // 解析数据
        byte infoByte1 = bytes[0];
        byte infoByte2 = bytes[1];
        byte statusByte = bytes[2];

        // 角度编码
        int infoDec = ((infoByte1 & 0xFF) << 8) | (infoByte2 & 0xFF);
        double infoAngle = infoDec * 360.0 / (65536 - 1);


        if (statusByte ==(byte) 0x44) {
            logger.info("!!!!!!!!!!!!!!!!");
            logger.info("黄灯警告");
            logger.info("Status (Hex): " + String.format("%02X", statusByte));
            logger.info("!!!!!!!!!!!!!!!!");
        } else if (statusByte ==(byte) 0x84 ) {
            logger.info("****************");
            logger.info("红灯报错");
            logger.info("Status (Hex): " + String.format("%02X", statusByte));
            logger.info("****************");
        } else if (statusByte ==(byte) 0x00) {

            logger.info("################");
            logger.info("正常");
            logger.info("角度编码：" + String.format("%02X%02X", infoByte1, infoByte2));
            logger.info("状态编码：" + String.format("%02X", statusByte));

            ///////////////////////////////////////////////////
            int activateMiddle = 360 / totalCount;//激发角度中心
            int multiplexerNum = (int) (Math.floor(infoAngle)/activateMiddle);//根据当前角度计算激发位置倍数
            ///////////////////////////////////////////////////
            if(multiplexerNum == 0){multiplexerNum = 1;}
            int startActivateAngle = activateMiddle*(multiplexerNum)-angleIncrement/2;// 激活区域的起始角度
            int endActivateAngle = activateMiddle*(multiplexerNum)+angleIncrement/2; // 每个区域的结束角度

            // 判断整数部分是否经过40-50度范围-10度激发范围
            if (Math.floor(infoAngle) >= startActivateAngle && Math.floor(infoAngle) < endActivateAngle && !keyActivate) {
                previousAngle = (int) Math.floor(infoAngle);
                sampleSum++;
                keyActivate = true;
            }
            // 判断整数部分是否经过55-65度范围-15度防抖
            if (Math.floor(infoAngle) >= endActivateAngle+5 && Math.floor(infoAngle) < endActivateAngle+10 && keyActivate) {
                keyActivate = false;
            }

//            // 判断整数部分是否经过70-80度范围-10度激发范围
//            if (Math.floor(infoAngle) >= 70 && Math.floor(infoAngle) < 80 && !keyActivate) {
//                previousAngle = (int) Math.floor(infoAngle);
//                sampleSum++;
//                keyActivate = true;
//            }
//            // 判断整数部分是否经过85-95度范围-5度防抖10度修改状态待激发
//            if (Math.floor(infoAngle) >= 85 && Math.floor(infoAngle) < 95 && keyActivate) {
//                keyActivate = false;
//            }
//            ///////////////////////////////////////////////////
//            // 判断整数部分是否经过100-110度范围-10度激发范围
//            if (Math.floor(infoAngle) >= 100 && Math.floor(infoAngle) < 110 && !keyActivate) {
//                previousAngle = (int) Math.floor(infoAngle);
//                sampleSum++;
//                keyActivate = true;
//            }
//            // 判断整数部分是否经过115-125度范围-15度防抖
//            if (Math.floor(infoAngle) >= 115 && Math.floor(infoAngle) < 125 && keyActivate) {
//                keyActivate = false;
//            }
//            ///////////////////////////////////////////////////
//            // 判断整数部分是否经过130-140度范围-10度激发范围
//            if (Math.floor(infoAngle) >= 130 && Math.floor(infoAngle) < 140 && !keyActivate) {
//                previousAngle = (int) Math.floor(infoAngle);
//                sampleSum++;
//                keyActivate = true;
//            }
//            // 判断整数部分是否经过140-150度范围-5度防抖10度修改状态待激发
//            if (Math.floor(infoAngle) >= 145 && Math.floor(infoAngle) < 155 && keyActivate) {
//                keyActivate = false;
//            }


            logger.info("previousAngle(上次激发角度)：" + previousAngle);
            logger.info("激发次数：" + sampleSum);
            logger.info("################");
        }
    }

}
