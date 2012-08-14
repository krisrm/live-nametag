
#include <LiquidCrystal.h>

#define NAME "LiveNameTag"
#define MAX_CHAR 32
#define ROW 16
#define COL 2

#define RXLED 17 
#define BR 3 //brightness, needs PWM

#define DEBUG_ENABLED 1

int countdown = 100;
int rxcountdown = 0;
int cursorPos;
char recvChar;
char* commandBuff="000";
int commandPos = 0;
boolean command;
boolean message;
LiquidCrystal lcd(9, 8, 6, 5, 4, 2);
int brightness = 255;

void setup() 
{ 
 
  Serial.begin(9600*2);
  
  pinMode(RXLED, OUTPUT); 
  delay(5000);
  Serial.println("Setting up bluetooth");
  setupBlueToothConnection();
  pinMode(BR,OUTPUT);
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
  analogWrite(BR, brightness);
  if(Serial1.available()){//check if there's any data sent from the remote bluetooth shield
    recvChar = Serial1.read();
    
    
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
    } else if (recvChar == '>'){
      message = !message;
      return;
    } else if (message) {
      printLCD(recvChar);
      if (cursorPos > MAX_CHAR){
        clearLCD();
        printLCD(recvChar);
      }
    }
    command = false;
    
    //Serial.print(recvChar);
    digitalWrite(RXLED, HIGH);   // set the LED on
    rxcountdown = 100;
  }
  if (rxcountdown <= 0){
    digitalWrite(RXLED, LOW);    // set the LED off
    rxcountdown = -1;
  } else {
    digitalWrite(RXLED, HIGH);   // set the LED on
    rxcountdown--;
  }
  if(Serial.available()){//check if there's any data sent from the local serial terminal, you can add the other applications here
    recvChar = Serial.read();
    Serial1.print(recvChar);
  }

} 

void setupBlueToothConnection()
{
  TXLED1;
  Serial1.begin(38400); //Set BluetoothBee BaudRate to default baud rate 38400
  Serial1.print("\r\n+STWMOD=0\r\n"); //set the bluetooth work in slave mode
  Serial1.print("\r\n+STNA="); //set the bluetooth name
  Serial1.print(NAME);
  Serial1.print("\r\n");
  Serial1.print("\r\n+STOAUT=1\r\n"); // Permit Paired device to connect me
  Serial1.print("\r\n+STAUTO=0\r\n"); // Auto-connection should be forbidden here
  delay(2000); // This delay is required.
  Serial1.print("\r\n+INQ=1\r\n"); //make the slave bluetooth inquirable 
  Serial.println("The slave bluetooth is inquirable!");
  delay(2000); // This delay is required.
  Serial1.flush();
  TXLED0;
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



