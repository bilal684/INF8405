//Standard PWM DC control
int E1 = 5;     //M1 Speed Control
int E2 = 6;     //M2 Speed Control
int M1 = 4;    //M1 Direction Control
int M2 = 7;    //M1 Direction Control

int vitesseMin = 80;
int vitesseMax = 255;
int vitesse = vitesseMin;
char currentState = 'x';
int vitesseIncrement = 5;
int vitesseRotationMin = 150;

void stop(void)                    //Stop
{
  digitalWrite(E1,LOW);   
  digitalWrite(E2,LOW);      
}   
void advance(char a,char b)          //Move forward
{
  analogWrite (E1,a);      //PWM Speed Control
  digitalWrite(M1,HIGH);    
  analogWrite (E2,b);    
  digitalWrite(M2,HIGH);
}  
void back_off (char a,char b)          //Move backward
{
  analogWrite (E1,a);
  digitalWrite(M1,LOW);   
  analogWrite (E2,b);    
  digitalWrite(M2,LOW);
}
void turn_L (char a,char b)             //Turn Left
{
  analogWrite (E1,a);
  digitalWrite(M1,LOW);    
  analogWrite (E2,b);    
  digitalWrite(M2,HIGH);
}
void turn_R (char a,char b)             //Turn Right
{
  analogWrite (E1,a);
  digitalWrite(M1,HIGH);    
  analogWrite (E2,b);    
  digitalWrite(M2,LOW);
}
void setup(void) 
{ 
  int i;
  for(i=4;i<=7;i++)
    pinMode(i, OUTPUT);  
  Serial.begin(9600);      //Set Baud Rate
  Serial.println("Run keyboard control");
} 
void loop(void) 
{
    while(!Serial.available());

    char command = Serial.read();
    
    if(command != -1)
    {
      switch(command)
      {
      case 'w'://Move Forward
        if (currentState == 'w')
        {
          vitesse = vitesse + vitesseIncrement;
          if (vitesse > vitesseMax)
          {
            vitesse = vitesseMax;
          }
        }
        else
        {
          vitesse = vitesseMin;
        }
        advance (vitesse,vitesse); //move forward in max speed
        break;
        
      case 's'://Move Backward
        if (currentState == 's')
        {
          vitesse = vitesse + vitesseIncrement;
          if (vitesse > vitesseMax)
          {
            vitesse = vitesseMax;
          }
        }
        else
        {
          vitesse = vitesseMin;
        }
        back_off (vitesse,vitesse);   //move back in max speed
        break;
      case 'a'://Turn Left
      if (currentState == 'a')
        {
          vitesse = vitesse + vitesseIncrement;
          if (vitesse > vitesseMax)
          {
            vitesse = vitesseMax;
          }
        }
        else
        {
          vitesse = vitesseRotationMin;
        }
        turn_L (vitesse,vitesse);
        break;       
      case 'd'://Turn Right
      if (currentState == 'd')
        {
          vitesse = vitesse + vitesseIncrement;
          if (vitesse > vitesseMax)
          {
            vitesse = vitesseMax;
          }
        }
        else
        {
          vitesse = vitesseRotationMin;
        }
        turn_R (vitesse,vitesse);
        break;
      case 'x':
        stop();
        break;
      default :
        stop();
        break;
      }
      currentState = command; 
    }
    else 
    {
      stop();
    }
}
