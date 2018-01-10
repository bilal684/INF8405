#include <Arduino.h>
#include <Wire.h>
#include "MeMCore.h"
#include "SoftwareSerial.h"
#include <SPI.h>
#include <PixyI2C.h>

/******************************************
       Premiers mots d'une trame UART
 ******************************************/
#define MOTORS_SPEED        0x01
#define LINUX_SWITCH_ON     0x02
#define PRESENCE_ACQUITTEE  0x03
#define LINUX_SWITCH_OFF    0x04
#define CLIGNOTANTS         0x05
#define MODE_SECURITE       0x06
#define MODE_PLATOON        0x07
#define MODE_LINEFOLLOWER   0x08

#define PRESENCE_DETECTEE   0x01
#define SECURITE_ACTIVEE    0x01
#define SECURITE_DESACTIVEE 0x00
#define PLATOON_ACTIVE      0x01
#define PLATOON_DESACTIVE   0x00
#define LINEFOLLOWER_ACTIVE 0x01
#define LINEFOLLOWER_DESACTIVE 0x00
/******************************************
 ******************************************/

/******************************************
   Mots de la trame UART pour les moteurs
 ******************************************/
#define LEFT_MOTOR_SPEED    0x02
#define RIGHT_MOTOR_SPEED   0x03
#define MARCHE_AVANT        0x00
#define MARCHE_ARRIERE      0x01
/******************************************
 ******************************************/

/******************************************
     Variables globales pour les moteurs
 ******************************************/
MeDCMotor leftMotor(M2);
MeDCMotor rightMotor(M1);
/******************************************
 ******************************************/

/******************************************
   Variables globales pour les mouvements
 ******************************************/
MePIRMotionSensor myPIRsensor(PORT_1);
boolean presenceDetectee = false;
boolean presenceEnvoyee = false;
boolean modeSecurite = false;
boolean modePlatoon = false;
boolean modeLineFollower = false;
/******************************************
 ******************************************/

/******************************************
       Variables globales pour l'UART
 ******************************************/
byte      inputSerial[7];
boolean   serialComplete = false;
byte      compteur_i = 0;
/******************************************
 ******************************************/

/******************************************
       Variables globales pour linux
 ******************************************/
boolean linuxIsOn = false;
#define LINUX_PIN 10
#define TIME_TO_SHUTDOWN 10000
/******************************************
 ******************************************/

/******************************************
         LED 1 et 2 (clignotants)
******************************************/
MeRGBLed rgbled(7, 2);
unsigned long nextTimeToBlink = 0;
boolean clignotantGauche = false, clignotantDroit = false, allume = false;
#define CLIGNOTANT_GAUCHE   0x01
#define CLIGNOTANT_DROIT    0x02
/******************************************
 ******************************************/

/******************************************
         Detecteur de ligne noire
******************************************/
MeLineFollower lineFinder(PORT_2);
double angle_rad = PI / 180.0;
double angle_deg = 180.0 / PI;
double temp;
uint8_t moveSpeed = 0;

/******************************************
 ******************************************/

/******************************************
         Object follower
******************************************/
PixyI2C pixy;

/******************************************
 ******************************************/
int signature = 0;
int x = 0;
int y = 0;
unsigned int width = 0;
unsigned int height = 0;
unsigned int area = 0;
unsigned int newarea = 0;
int Xmin = 70;
int Xmax = 200;
int maxArea = 0;
int minArea = 0;
static int i = 0;

void setup()
{
  // initialize serial:
  Serial.begin(9600);
  myPIRsensor.SetPirMotionMode(0);   //Continuous Trigger mode
  rgbled.setColor(1, 0, 0, 0);
  rgbled.setColor(2, 0, 0, 0);
  rgbled.show();
  pixy.init();
  pinMode(A7, INPUT);
  moveSpeed = 150;
}

void loop()
{
  //Executer les clignotants si nécessaire
  clignotant();

  //Vérifier si detecteur de mouvement activé
  presenceHumaine();

  //Line Follower si modeLineFollower activé
  lineFollow();

  //Object follower si modePlatoon activé
  pixyMain();

  //Vérifier si des messages sont arrivés sur l'UART
  checkUart();

  //Vérifier si on ne quitte pas la route
  //checkBlackLine();

  checkButton();

}

void serialEvent() {

  while (Serial.available()) {

    inputSerial[compteur_i] = (byte)Serial.read();
    if (inputSerial[compteur_i] == '\n') {
      serialComplete = true;
      compteur_i = 0;
    } else {
      compteur_i++;
    }

  }

}


void clignotant() {
  if (clignotantGauche || clignotantDroit) {
    if (millis() >= nextTimeToBlink)
    {
      if (!allume) {
        if (clignotantGauche) rgbled.setColor(2, 255, 102, 0);
        if (clignotantDroit) rgbled.setColor(1, 255, 102, 0);
      }
      else {
        rgbled.setColor(1, 0, 0, 0);
        rgbled.setColor(2, 0, 0, 0);
      }
      rgbled.show();
      allume = !allume;
      nextTimeToBlink = millis() + 500;
    }
  }
}

void presenceHumaine() {
  if (modeSecurite) {
    if ((myPIRsensor.isHumanDetected() && !presenceDetectee) || (presenceDetectee && !presenceEnvoyee)) {
      presenceDetectee = true;
      if (linuxIsOn) {
        //Ecrire sur l'UART
        presenceEnvoyee = true;
        Serial.write(PRESENCE_DETECTEE);
      } else {
        //Allumer linux
        digitalWrite(LINUX_PIN, HIGH);
      }
    }
  }
}

void checkBlackLine() {
  switch (lineFinder.readSensors())
  {
    case S1_IN_S2_IN:
      //Erreur, on a raté le moment où on sortait
      if (!(clignotantDroit && clignotantGauche)) {
        leftMotor.stop();
        rightMotor.stop();
      }
      break;

    case S1_IN_S2_OUT:
      //On va trop à gauche, attention !
      if (!clignotantGauche) {
        //Le pilote s'endort ! Tourner à droite pour lui
        leftMotor.stop();
        rightMotor.stop();
      }
      break;

    case S1_OUT_S2_IN:
      //On va trop à droite, attention !
      if (!clignotantDroit) {
        //Le pilote s'endort ! Tourner à droite pour lui
        leftMotor.stop();
        rightMotor.stop();
      }
      break;

    case S1_OUT_S2_OUT:
      //Tout est ok, pas d'endormissement à l'horizon !
      break;

    default:
      break;
  }
}

void checkUart() {
  if (serialComplete) {

    switch (inputSerial[0]) {

      case MOTORS_SPEED:
        if (inputSerial[2] == LEFT_MOTOR_SPEED && inputSerial[4] == RIGHT_MOTOR_SPEED)
        {
          if (inputSerial[1] == MARCHE_AVANT) {
            leftMotor.run(-inputSerial[3]);
            rightMotor.run(inputSerial[5]);
          } else {
            leftMotor.run(inputSerial[3]);
            rightMotor.run(-inputSerial[5]);
          }

        } else {
          //La trame est mauvaise
        }
        break;

      case LINUX_SWITCH_ON:
        linuxIsOn = true;
        break;

      case LINUX_SWITCH_OFF:
        linuxIsOn = false;
        delay(TIME_TO_SHUTDOWN);
        digitalWrite(LINUX_PIN, LOW);
        break;

      case PRESENCE_ACQUITTEE:
        presenceDetectee = false;
        presenceEnvoyee = false;
        break;

      case CLIGNOTANTS:
        if (inputSerial[1] == CLIGNOTANT_GAUCHE && inputSerial[3] == CLIGNOTANT_DROIT)
        {
          //reset
          rgbled.setColor(1, 0, 0, 0);
          rgbled.setColor(2, 0, 0, 0);
          rgbled.show();
          allume = false;
          //nouvelles conditions
          clignotantGauche = inputSerial[2];
          clignotantDroit = inputSerial[4];
        } else {
          //La trame est mauvaise
        }
        break;

      case MODE_SECURITE:

        if (inputSerial[1] == SECURITE_ACTIVEE) {
          modeSecurite = true;
          rgbled.setColor(2, 255, 0, 0);
          rgbled.setColor(1, 255, 0, 0);
        }
        else {
          modeSecurite = false;
          rgbled.setColor(2, 0, 0, 0);
          rgbled.setColor(1, 0, 0, 0);
        }

        rgbled.show();

        break;

      case MODE_PLATOON:

        if (inputSerial[1] == PLATOON_ACTIVE) {
          modePlatoon = true;
          rgbled.setColor(2, 0, 145, 80);
          rgbled.setColor(1, 0, 145, 80);


        }
        else {
          modePlatoon = false;
          move(5, moveSpeed);
          rgbled.setColor(2, 0, 0, 0);
          rgbled.setColor(1, 0, 0, 0);
        }

        rgbled.show();

        break;

      case MODE_LINEFOLLOWER:

        if (inputSerial[1] == LINEFOLLOWER_ACTIVE) {
          modeLineFollower = true;
          rgbled.setColor(2, 255, 128, 0);
          rgbled.setColor(1, 255, 128, 0);
          rgbled.show();
        }
        else {
          modeLineFollower = false;
          move(5, moveSpeed);
          rgbled.setColor(2, 0, 0, 0);
          rgbled.setColor(1, 0, 0, 0);
        }

        rgbled.show();

        break;

      default:
        break;

    }

    serialComplete = false;

  }
}

void checkButton () {

  if (analogRead(A7) == 0) {
    modeSecurite = !modeSecurite;
    byte out[2] = {MODE_SECURITE, (byte)modeSecurite};
    Serial.write(out, 2);
    delay(500);
  }

}

void move(int direction, int speed)
{
  int leftSpeed = 0;
  int rightSpeed = 0;
  if (direction == 1) {
    // move forward
    leftSpeed = speed;
    rightSpeed = speed;
  } else if (direction == 2) {
    // move backward
    leftSpeed = -speed;
    rightSpeed = -speed;
  } else if (direction == 3) {
    // turn left
    leftSpeed = -speed;
    rightSpeed = speed;
  } else if (direction == 4) {
    // turn right
    leftSpeed = speed;
    rightSpeed = -speed;
  } else if (direction == 5) {
    // stop
    leftSpeed = 0;
    rightSpeed = 0;
  }
  leftMotor.run((9) == M1 ? -(leftSpeed) : (leftSpeed));
  rightMotor.run((10) == M1 ? -(rightSpeed) : (rightSpeed));
}

void lineFollow() {
  if (modeLineFollower) {
    temp = lineFinder.readSensors();
    if (((temp) == (0))) {
      move(1, moveSpeed); //move forward
    } else {
      if (((temp) == (1))) {
        move(3, moveSpeed); //turn left
      } else {
        if (((temp) == (2))) {
          move(4, moveSpeed); //turn right
        } else {
          move(2, moveSpeed); //move backward
        }
      }
    }
  }
}

void scan() {
  uint16_t blocks;
  blocks = pixy.getBlocks();
  signature = pixy.blocks[i].signature;
  x = pixy.blocks[i].x;
  y = pixy.blocks[i].y;
  width = pixy.blocks[i].width;
  height = pixy.blocks[i].height;
}

void pixyMain() {
  if (modePlatoon) {
    while (millis() < 5000) {
      scan();
      area = width * height;
      maxArea = area + 1000;
      minArea = area - 1000;
    }
    scan();
    if (signature == 1) {
      newarea = width * height;
      if (x < Xmin) {
        move(3, moveSpeed); //turn left
      } else if (x > Xmax) {
        move(4, moveSpeed); //turn right
      } else if (newarea < minArea) {
        move(2, moveSpeed); // move forward
      } else if (newarea > maxArea) {
        move(1, moveSpeed); //move backward
      } else {
        move(5, moveSpeed); //stop
      }
    } else {
      move(5, moveSpeed); //stop
    }
  }
}
