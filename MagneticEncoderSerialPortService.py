import serial
import time


#采集总数计数
sample_sum = 0

initial_key = 0
key_activate = False
previous_angle = 0


# 创建CRC校验对象，指定多项式和初始值
# 自定义CRC-8多项式
def custom_crc8(data, poly, init_value):
    crc = init_value
    for byte in data:
        crc ^= byte
        for _ in range(8):
            if crc & 0x80:
                crc = (crc << 1) ^ poly
            else:
                crc <<= 1
    return crc & 0xFF

# 自定义CRC-8多项式和初始值
crc_poly = 0x97
crc_init_value = 0x00


# 串口配置
ser = serial.Serial('COM3', 115200)  # 替换 'COM1' 为您的串口名称
ser.timeout = 1  # 设置串口超时时间

def send_data(data):
    """
    发送十六进制数据到串口
    """
    ser.write(bytes.fromhex(data))  # 将十六进制字符串转换为字节并发送

def receive_data():
    """
    从串口接收数据并解析
    """
    data = ser.read(4)  # 从串口读取4个字节数据
    if len(data) == 4:
        return data.hex().upper()  # 将字节数据转换为十六进制字符串并转换为大写
    else:
        return None  # 如果未成功读取到4个字节数据，返回None

def analysis_data(received_data):
            global sample_sum
            global key_activate
            global previous_angle
            # 解析数据
            info_hex = received_data[:4]
            status_hex = received_data[4:6]

            if status_hex == "44":
                # 将status_hex转换为二进制
                status_bin = bin(int(status_hex, 16))[2:]  # 去掉二进制字符串前面的"0b"
                print("!!!!!!!!!!!!!!!!")
                print("黄灯警告")
                print("Status (Hex):", status_hex)
                print("Status (Bin):", status_bin)
                print("!!!!!!!!!!!!!!!!")
            
            if status_hex == "84":
                # 将status_hex转换为二进制
                status_bin = bin(int(status_hex, 16))[2:]  # 去掉二进制字符串前面的"0b"
                print("xxxxxxxxxxxxxxxx")
                print("红灯报错")
                print("Status (Hex):", status_hex)
                print("Status (Bin):", status_bin)
                print("xxxxxxxxxxxxxxxx")
            
            if status_hex == "00":
                # 在此处进行数据处理
                print("################")
                # 在此处进行数据处理
                # 将info_hex转换为十进制
                info_dec = int(info_hex, 16)
                #角度换算
                info_angle = info_dec*360/(65536-1)
                print("Info (Hex):", info_hex)
                print("Info (angle):", info_angle)
                
                # if i == 0:
                #     previous_angle = int(info_angle)
                #     print(previous_angle)
                #     i=1

                # print("Status (Hex):", status_hex)
                # 判断整数部分是否经过40-50度范围
                if int(info_angle) in range(40,50) and key_activate==False:
                    previous_angle = int(info_angle)
                    sample_sum+=1
                    key_activate = True
                if int(info_angle) in range(55,65) and key_activate==True:
                    key_activate = False

                # 判断整数部分是否经过70-80度范围
                if int(info_angle) in range(70,80) and key_activate==False:
                    previous_angle = int(info_angle)
                    sample_sum+=1
                    key_activate = True
                if int(info_angle) in range(85,95) and key_activate==True:
                    key_activate = False

                # 判断整数部分是否经过100-110度范围
                if int(info_angle) in range(100,110) and key_activate==False:
                    previous_angle = int(info_angle)
                    sample_sum+=1
                    key_activate = True
                if int(info_angle) in range(115,125) and key_activate==True:
                    key_activate = False

                # 判断整数部分是否经过130-140度范围
                if int(info_angle) in range(130,140) and key_activate==False:
                    previous_angle = int(info_angle)
                    sample_sum+=1
                    key_activate = True
                if int(info_angle) in range(145,155) and key_activate==True:
                    key_activate = False

                print("previous_angle(上次激发角度)：",previous_angle)
                print("激发次数：",sample_sum)
                print("################")
                


try:
    while True:

        if initial_key == 0:

            hex_data_to_send = "30"                    #0
            send_data(hex_data_to_send)
            # 等待一段时间（例如，0.05秒=50ms）
            time.sleep(0.05)
             # 从串口接收数据
            received_data = receive_data()
            if received_data is not None:
                info_hex = received_data[:2]
                print("发送0后收到的信息：",info_hex)
                if info_hex == "0A":
                    initial_key = 1
            hex_data_to_send = "31"                    #1
            send_data(hex_data_to_send)
            # 等待一段时间（例如，0.05秒=50ms）
            time.sleep(0.05)



        if initial_key == 0:
            time.sleep(0.1)




        # initial_key = 1
        if initial_key ==1:
            # 发送十六进制数据
            hex_data_to_send = "64"  # 替换为您要发送的十六进制数据
            send_data(hex_data_to_send)

            # 等待一段时间（例如，0.05秒=50ms）
            time.sleep(0.01)

            # 从串口接收数据
            received_data = receive_data()

            if received_data is not None:
                
                ##################################CRC验证数据######################################
                # 提取info_hex和status_hex
                info_hex = received_data[:4]
                status_hex = received_data[4:6]

                # 执行自定义CRC校验
                crc_value = custom_crc8(bytes.fromhex(info_hex + status_hex), crc_poly, crc_init_value)

                received_crc = int(received_data[6:8], 16)#received_data[6:8]是CRC位

                if crc_value == received_crc:
                    print("CRC校验通过")
                    print("原始数据([角度编码:2个字节]+[状态编码:1个字节]+[CRC校验码:1个字节]):",received_data)
                    analysis_data(received_data)
                else:
                    print("CRC校验失败")
                    print("接受到的：",received_crc)
                    print("计算到的：",crc_value)
                ##################################CRC验证数据######################################


except KeyboardInterrupt:
    print("程序已停止")
    ser.close()
