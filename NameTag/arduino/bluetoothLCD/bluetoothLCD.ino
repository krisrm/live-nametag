/*
 BluetoothBee Demo Code - Flowcontrol Based Implementation
 2010,2011 Copyright (c) Seeed Technology Inc. All right reserved.

 Author: Visweswara R
 Modified: John Boxall, April 2012

 This demo code is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA

 For more details about the product please check http://www.seeedstudio.com/depot/

 */


#include <LiquidCrystal.h>
#include <SoftwareSerial.h> //Software Serial Port
#define RxD 2
#define TxD 3
#define DEBUG_ENABLED 1
#define MAX_CHAR 32
#define ROW 16
#define COL 2

SoftwareSerial blueToothSerial(RxD,TxD);
LiquidCrystal lcd(12, 11, 10, 9, 8, 7);

int incoming;
int cursorPos;

void setup()
{
 pinMode(RxD, INPUT);
 pinMode(TxD, OUTPUT);
 setupBlueToothConnection();
 lcd.begin(ROW, COL);
}
void setupBlueToothConnection()
{
 Serial.begin(9600);
 blueToothSerial.begin(38400); //Set BluetoothBee BaudRate to default baud rate 38400
 delay(1000);
 sendBlueToothCommand("\r\n+STWMOD=0\r\n");
 sendBlueToothCommand("\r\n+STNA=SeeeduinoBluetooth\r\n");
 sendBlueToothCommand("\r\n+STAUTO=0\r\n");
 sendBlueToothCommand("\r\n+STOAUT=1\r\n");
 sendBlueToothCommand("\r\n +STPIN=0000\r\n");
 delay(2000); // This delay is required.
 sendBlueToothCommand("\r\n+INQ=1\r\n");
 delay(2000); // This delay is required.
 Serial.print("hello");
}
//Checks if the response "OK" is received
void CheckOK()
{
 char a,b;
 while(1)
 {
 if(blueToothSerial.available())
 {
 a = blueToothSerial.read();
if('O' == a)
 {
 // Wait for next character K. available() is required in some cases, as K is not immediately available.
 while(blueToothSerial.available())
 {
 b = blueToothSerial.read();
 break;
 }
 if('K' == b)
 {
 break;
 }
 }
 }
 }
while( (a = blueToothSerial.read()) != -1)
 {
 //Wait until all other response chars are received
 }
}
void sendBlueToothCommand(char command[])
{
 blueToothSerial.print(command);
 CheckOK();
}
void loop()
{
   Serial.print("!");
 if (Serial.available() > 0){
   incoming=Serial.read();
   Serial.print("I received: ");
   Serial.println(incoming, DEC);
//   lcd.setCursor(cursorPos % ROW, cursorPos / ROW);
//   lcd.print(incoming, DEC);
//   cursorPos++;
//   if (cursorPos >= MAX_CHAR){
//     cursorPos=0;
//   }
 }
}
