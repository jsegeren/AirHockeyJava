// X - Direction Motor Settings
#define X_MOTOR_PIN 10
#define X_MOTOR_DIRECTION_PIN 11
#define X_MM_PER_ROT 550
#define X_STEPS_PER_ROT 1000
#define X_STEPS_PER_MM (X_STEPS_PER_ROT / X_MM_PER_ROT)

// Y - Direction Motor Settings
#define Y_MOTOR_PIN 12
#define Y_MOTOR_DIRECTION_PIN 13
#define Y_MM_PER_ROT 550
#define Y_STEPS_PER_ROT 1000
#define Y_STEPS_PER_MM (Y_STEPS_PER_ROT / Y_MM_PER_ROT)

void setup(){
  pinMode(X_MOTOR_PIN, OUTPUT);
  pinMode(X_MOTOR_DIRECTION_PIN, OUTPUT);
  
  pinMode(Y_MOTOR_PIN, OUTPUT);
  pinMode(Y_MOTOR_DIRECTION_PIN, OUTPUT);
}

void moveMotorX(double mmDistance){
  if(mmDistance >= 0){
    digitalWrite(X_MOTOR_DIRECTION_PIN, HIGH);
  }else{
    digitalWrite(X_MOTOR_DIRECTION_PIN, LOW);
  }
}

void loop(){
}
