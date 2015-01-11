#include <AccelStepper.h>

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

AccelStepper stepperX(AccelStepper::DRIVER, X_MOTOR_PIN, X_MOTOR_DIRECTION_PIN);
AccelStepper stepperY(AccelStepper::DRIVER, Y_MOTOR_PIN, Y_MOTOR_DIRECTION_PIN);

long serialCoordinates[2]; // input X,Y coordinates from Serial connection
int fieldIndex = 0;

void setup(){
  Serial.begin(9600);
  stepperX.setMaxSpeed(X_MOTOR_MAX_SPEED);
  stepperX.setAcceleration(X_MOTOR_ACCELERATION);
  
  stepperY.setMaxSpeed(Y_MOTOR_MAX_SPEED);
  stepperY.setAcceleration(Y_MOTOR_ACCELERATION);
}
/*
* Looks for input messages of this form X,Y
* ex. 12355,123466666
*/
boolean getSerialInput(){
  boolean gotInput = false;
  
  if (Serial.available()){
    char ch = Serial.read();
    if(ch >= '0' && ch <= '9') // is this an ascii digit between 0 and 9?
    {
      // yes, accumulate the value
      serialCoordinates[fieldIndex] = (serialCoordinates[fieldIndex] * 10) + (ch - '0'); 
    }
    else if (ch == ',')  // comma is our separator, so move on to the next field
    {
      if(fieldIndex < 2)
        fieldIndex++;   // increment field index
    }
    else
    {
      // any character not a digit or comma ends the acquisition of fields
      // in this example it's the newline character sent by the Serial Monitor
      if (fieldIndex != 1){
        Serial.println("Incorrect Input Format");
      }
      else{
        Serial.print( fieldIndex + 1);
        Serial.println(" fields received:");
        
        for(int i = 0; i <= fieldIndex; i++)
        {
          Serial.println(serialCoordinates[i]);
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

void moveToPosition(){
  Serial.print("Moving ");
  Serial.print(serialCoordinates[0] - stepperX.currentPosition());
  Serial.print(" in the X, and ");
  Serial.print(serialCoordinates[1] - stepperY.currentPosition());
  Serial.println(" in the Y.");
  
  stepperX.moveTo(serialCoordinates[0]);
  stepperY.moveTo(serialCoordinates[1]);
  
  //Reset coordinates for next input
  resetCoordinates();
}

void loop(){
  if(getSerialInput()){
    moveToPosition();
  }
  
  stepperX.run();
  stepperY.run();
}
