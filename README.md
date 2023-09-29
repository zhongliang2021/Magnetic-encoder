# Magnetic-encoder
This document is the test code for a 16 bit magnetic encoder produced by kingkong.tech Company. 

This code starts by parsing serial port (RS485) data through the company's unique CRC-8 verification method (x^8+x^7+x^4+x^2+x^1+1), and then determines the indicator light status (red, yellow, and green) of this encoder. 

If the program determines that the encoder status is normal (green light), then analyze the data and calculate the angle. 

In order to excite a program of other facility, when the encoder counts to a fixed angle range, the program counts once and prints the total count times, but when the encoder shakes to a certain extent within that range, it will not repeat the count. 

The reason for using angle range instead of angle to determine is to eliminate jitter.

The Python file will zero the encoder upon receiving the encoder data, while the Java file will directly enter the data processing and analysis stage.
