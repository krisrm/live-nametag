#include <SoftwareSerial.h> //Software Serial Port
#include <LiquidCrystal.h>


#define NAME "LiveNameTag"
#define MAX_CHAR 32
#define ROW 16
#define COL 2

#define RxD 2
#define TxD 3

#define DEBUG_ENABLED 1

SoftwareSerial blueToothSerial(RxD,TxD);
int countdown = 5000;
int cursorPos;
char recvChar;
char* commandBuff="000";
int commandPos = 0;
boolean command;
// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(12, 11, 10, 9, 8, 7);
int brightness = 255;

void setup() 
{ 
  Serial.begin(9600);
  pinMode(RxD, INPUT);
  pinMode(TxD, OUTPUT);
  setupBlueToothConnection();
  pinMode(5,OUTPUT);
  lcd.begin(ROW, COL);
  delay(1000);
  clearLCD();
} 

void loop() 
{ 
  if (countdown > 0){
  countdown --;
  return;
  }
  analogWrite(5, brightness);

  if(blueToothSerial.available()){//check if there's any data sent from the remote bluetooth shield
    recvChar = blueToothSerial.read();

    if (command == true){
      if (recvChar == 'c'){
        clearLCD();
        command = false;
        return;
      }
      if (recvChar == '*'){
        printLCD('*');
        command= false; 
        return;
      }
      if (recvChar =='e'){
        brightness = atoi(commandBuff);
        Serial.println(commandBuff);
        Serial.println(brightness);
        commandPos = 0;
        commandBuff = "000";
        command= false; 
        return;
      }

      if (commandPos < 3){
        commandBuff[commandPos] = recvChar;     
        commandPos++;
      } 
      else {
        commandPos=0;
      }
      return;
    }

    if (recvChar == '*'){
      command = true;
      return;
    } 
    else {
      command = false;
      printLCD(recvChar);
    }

    if (cursorPos > MAX_CHAR){
      clearLCD();
      printLCD(recvChar);
    }
    Serial.print(recvChar);
  }
  if(Serial.available()){//check if there's any data sent from the local serial terminal, you can add the other applications here
    recvChar = Serial.read();
    blueToothSerial.print(recvChar);
  }

} 

void setupBlueToothConnection()
{
  blueToothSerial.begin(38400); //Set BluetoothBee BaudRate to default baud rate 38400
  blueToothSerial.print("\r\n+STWMOD=0\r\n"); //set the bluetooth work in slave mode
  blueToothSerial.print("\r\n+STNA="); //set the bluetooth name
  blueToothSerial.print(NAME);
  blueToothSerial.print("\r\n");
  blueToothSerial.print("\r\n+STOAUT=1\r\n"); // Permit Paired device to connect me
  blueToothSerial.print("\r\n+STAUTO=0\r\n"); // Auto-connection should be forbidden here
  delay(2000); // This delay is required.
  blueToothSerial.print("\r\n+INQ=1\r\n"); //make the slave bluetooth inquirable 
  Serial.println("The slave bluetooth is inquirable!");
  delay(2000); // This delay is required.
  blueToothSerial.flush();
}

void printLCD(char c){
  lcd.setCursor(cursorPos % ROW, cursorPos / ROW);
  lcd.print(c);
  cursorPos++;  
}

void clearLCD(){
  lcd.setCursor(0,0);
  lcd.print("                ");
  lcd.setCursor(0,1);
  lcd.print("                ");
  cursorPos=0;
  lcd.setCursor(0,0);
}



