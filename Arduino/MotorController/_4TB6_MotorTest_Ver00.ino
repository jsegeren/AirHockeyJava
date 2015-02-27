#include <AccelStepper.h>

/*
 * Motor Definitions
 */
 
#define X_MOTOR_PIN 5
#define X_MOTOR_DIRECTION_PIN 6
#define Y_MOTOR_PIN 10
#define Y_MOTOR_DIRECTION_PIN 11

#define X_MOTOR_MAX_SPEED 1000
#define X_MOTOR_ACCELERATION 1000
#define Y_MOTOR_MAX_SPEED 5000
#define Y_MOTOR_ACCELERATION 5000

AccelStepper stepperX(AccelStepper::DRIVER, X_MOTOR_PIN, X_MOTOR_DIRECTION_PIN);
AccelStepper stepperY(AccelStepper::DRIVER, Y_MOTOR_PIN, Y_MOTOR_DIRECTION_PIN);

void setup() {
  
  // X motor settings
  //stepperX.setMaxSpeed(X_MOTOR_MAX_SPEED);
  //stepperX.setAcceleration(X_MOTOR_ACCELERATION);
  //stepperX.moveTo(-400);

  // Y motor settings
  stepperY.setMaxSpeed(Y_MOTOR_MAX_SPEED);
  stepperY.setAcceleration(Y_MOTOR_ACCELERATION);
  stepperY.moveTo(1600);

}

void loop() {
  // put your main code here, to run repeatedly:
  
  // Motor run!
  //stepperX.run();
  stepperY.run();
  
}
