#include <AccelStepper.h>
#include <Wire.h> 
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"

// X - Direction Motor Settings
#define X_MOTOR_PIN 10
#define X_MOTOR_DIRECTION_PIN 11
#define X_MOTOR_MAX_SPEED 6000
#define X_MOTOR_ACCELERATION 500

// Y - Direction Motor Settings
#define Y_MOTOR_PIN 12
#define Y_MOTOR_DIRECTION_PIN 13
#define Y_MOTOR_MAX_SPEED 1500
#define Y_MOTOR_ACCELERATION 100000

// Other constants
#define FIELD_DELIMITER ','
#define OUTPUT_POSITION_PREFIX '_'

AccelStepper stepperX(AccelStepper::DRIVER, X_MOTOR_PIN, X_MOTOR_DIRECTION_PIN);
AccelStepper stepperY(AccelStepper::DRIVER, Y_MOTOR_PIN, Y_MOTOR_DIRECTION_PIN);

long serialCoordinates[2]; // input X,Y coordinates from Serial connection
int serialScore[2]; // input User and Robot score from Serial connection
int currentScore[2]; // current User and Robot scores
int fieldIndex = 0;
bool moveFlag = true;
char type;  // type of data being received
Adafruit_7segment matrix = Adafruit_7segment();

void setup(){
 #ifndef __AVR_ATtiny85__
  Serial.begin(9600);
 #endif
   Serial.println("hi guys");

  stepperX.setMaxSpeed(X_MOTOR_MAX_SPEED);
  stepperX.setAcceleration(X_MOTOR_ACCELERATION);
  
  stepperY.setMaxSpeed(Y_MOTOR_MAX_SPEED);
  stepperY.setAcceleration(Y_MOTOR_ACCELERATION);
  
  matrix.begin(0x70);
}
/*
* Looks for input messages of this form X,Y,ScoreU,ScoreR
* ex. 12355,123466666,0,1
*/
boolean getSerialInput(){
  boolean gotInput = false;
  
  if (Serial.available()){
    char ch = (char) Serial.read();
    
    if(ch >= '0' && ch <= '9') // is this an ascii digit between 0 and 9?
    {
      if(type == 'P'){  // positional data is being received
        if(fieldIndex < 3){
          // yes, accumulate the Coordinate value
          serialCoordinates[fieldIndex-1] = (serialCoordinates[fieldIndex-1] * 10) + (ch - '0'); 
        }
      }
      else if(type == 'S'){  // score data is being received
        if(fieldIndex < 3){
          // yes, accumulate the Score value        
          serialScore[fieldIndex-1] = ch - '0';
        }
      }
    }
    else if (ch >= 'A' && ch <= 'Z')  // determine the type of data being passed to the arduino
    {
      type = ch;  // make type equal to the correct type of data being received
      Serial.println((String) type);
    }
    else if (ch == FIELD_DELIMITER)  // comma is our separator, so move on to the next field
    {
      if(fieldIndex < 3)
        fieldIndex++;   // increment field index
    }
    else
    {
      // any character not a digit or comma ends the acquisition of fields
      // in this example it's the newline character sent by the Serial Monitor
      if (fieldIndex != 1){
        // Serial.println("Incorrect Input Format");
      }
      else{
        // Serial.print( fieldIndex + 1);
        // Serial.println(" fields received:");
        
        for(int i = 0; i <= fieldIndex; i++)
        {
          // Serial.println(serialCoordinates[i]);
        }
        
        gotInput = true;
      }
      fieldIndex = 0;  // ready to start over
    }
  }
  
  return gotInput;
}

void resetCoordinates(){
  serialCoordinates[0] = 0;
  serialCoordinates[1] = 0;
}

void resetScore(){
  serialScore[0] = 0;
  serialScore[1] = 0;
}

void moveToPosition(){
    Serial.println("moving guys!");

  //long distanceX = serialCoordinates[0] - stepperX.currentPosition();
  //long distanceY = serialCoordinates[1] - stepperY.currentPosition();
  //Serial.println("Moving " + distanceX + " in the X, and " + distanceY + " in the Y.");
  
  stepperX.moveTo(100);
  stepperY.moveTo(100);
  
  //Reset coordinates for next input
  resetCoordinates();
}

void displayScore(){
  //matrix.writeDigitNum(0, 0, false);
  matrix.writeDigitNum(1, serialScore[0], true);
  matrix.drawColon(true);
  matrix.writeDigitNum(3, serialScore[1], true);
  //matrix.writeDigitNum(4, serialScore[1], true);
  matrix.writeDisplay();
  
  currentScore[0] = serialScore[0];
  currentScore[1] = serialScore[1];  
}

void loop(){
//  if(getSerialInput()){
    
  if(moveFlag){
      moveToPosition();  
      moveFlag = false;
  }
//  }
  
  if(serialScore[0] != currentScore[0] || serialScore[1] != currentScore[1]){
    displayScore();
  }
  
  stepperX.run();
  stepperY.run();

  // Send back current motor position
  Serial.println((String) OUTPUT_POSITION_PREFIX + stepperX.currentPosition() + (String) FIELD_DELIMITER + stepperY.currentPosition() + (String) FIELD_DELIMITER + serialScore[0] + (String) FIELD_DELIMITER + serialScore[1]);
}
