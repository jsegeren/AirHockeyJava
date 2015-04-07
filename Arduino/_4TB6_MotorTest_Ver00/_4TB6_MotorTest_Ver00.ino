#include <AccelStepper.h>

/*
 * Motor Definitions
 */
 
#define X_MOTOR_PIN 5
#define X_MOTOR_DIRECTION_PIN 6
#define Y_MOTOR_PIN 10
#define Y_MOTOR_DIRECTION_PIN 11

#define X_MOTOR_MAX_SPEED 1000
#define X_MOTOR_ACCELERATION 5000
#define Y_MOTOR_MAX_SPEED 1000
#define Y_MOTOR_ACCELERATION 5000

// Other constants
#define FIELD_DELIMITER ','
#define OUTPUT_POSITION_PREFIX '_'

AccelStepper stepperX(AccelStepper::DRIVER, X_MOTOR_PIN, X_MOTOR_DIRECTION_PIN);
AccelStepper stepperY(AccelStepper::DRIVER, Y_MOTOR_PIN, Y_MOTOR_DIRECTION_PIN);

void setup() {
  
  Serial.begin(9600);            // Set the baud rate on the serial line
  // X motor settings
  //stepperX.setMaxSpeed(X_MOTOR_MAX_SPEED);
  //stepperX.setAcceleration(X_MOTOR_ACCELERATION);
  //stepperX.moveTo(-400);

  // Y motor settings
  stepperY.setMaxSpeed(Y_MOTOR_MAX_SPEED);
  stepperY.setAcceleration(Y_MOTOR_ACCELERATION);
  stepperY.moveTo(-100);
  
  // X motor settings
  stepperX.setMaxSpeed(X_MOTOR_MAX_SPEED);
  stepperX.setAcceleration(X_MOTOR_ACCELERATION);
  stepperX.moveTo(-1000);

}

void loop() {
  // put your main code here, to run repeatedly:
  
  // Motor run!
  //stepperX.run();
  stepperY.run();
  stepperX.run();
      
    // if there is room in the outputbuffer send the following messages
    if(Serial.isOutputBufferEmpty()){
      // Send back current motor position
      Serial.println((String) OUTPUT_POSITION_PREFIX + stepperX.currentPosition() + (String) FIELD_DELIMITER + stepperY.currentPosition());
    }
}
