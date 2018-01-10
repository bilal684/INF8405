package ca.polymtl.INF8405;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

public class Main {
	
	static final String NOM_DU_ROBOT = "arduino";
	//static final String NOM_DU_ROBOT = "arduino1";
	
	static final byte MOTORS_SPEED = 0x01;
	static final byte LEFT_MOTOR_SPEED = 0x02;
	static final byte RIGHT_MOTOR_SPEED  = 0x03;
	static final byte MARCHE_AVANT  = 0x00;
	static final byte MARCHE_ARRIERE  = 0x01;
	
	private static byte[] motors_outputSerial = {MOTORS_SPEED, MARCHE_AVANT, LEFT_MOTOR_SPEED, 0, RIGHT_MOTOR_SPEED, 0, '\n'};
	
	
	static final byte CLIGNOTANTS = 0x05;
	static final byte CLIGNOTANT_GAUCHE = 0x01;
	static final byte CLIGNOTANT_DROIT = 0x02;
	
	private static byte[] clignotants_outputSerial = {CLIGNOTANTS, CLIGNOTANT_GAUCHE, 0, CLIGNOTANT_DROIT, 0, '\n'};
	
	
	static final byte LINUX_SWITCH_ON = 0x02;
	static final byte LINUX_SWITCH_OFF = 0x04;

	static final byte PRESENCE_DETECTEE = 0x01;	
	static final byte PRESENCE_ACQUITTEE = 0x03;
	
	static final byte[] CODE_SECURITE_YANN  = {0x02,0x31,0x34,0x30,0x30,0x33,0x45,0x44,0x30,0x41,0x39,0x35,0x33,0x03};
	
	static final byte MODE_SECURITE = 0x06;
	static final byte SECURITE_ACTIVEE = 0x01;
	static final byte SECURITE_DESACTIVEE = 0x00;
	
	private static byte[] securite_outputSerial = {MODE_SECURITE, SECURITE_DESACTIVEE, '\n'};
	
	static final byte MODE_PLATOON = 0x07;
	static final byte PLATOON_ACTIVE = 0x01;
	static final byte PLATOON_DESACTIVE = 0x00;
	
	private static byte[] platoon_outputSerial = {MODE_PLATOON, PLATOON_DESACTIVE, '\n'};
	
	static final byte MODE_LINEFOLLOWER = 0x08;
	static final byte LINEFOLLOWER_ACTIVE = 0x01;
	static final byte LINEFOLLOWER_DESACTIVE = 0x00;
	
	private static byte[] linefollower_outputSerial = {MODE_LINEFOLLOWER, LINEFOLLOWER_DESACTIVE, '\n'};
	
	private static Semaphore motors_mutex = new Semaphore(1);

    private static DatabaseReference database;
    
    private static Serial serial_UART = SerialFactory.createInstance();
    private static Serial serial_USB = SerialFactory.createInstance();
	

    public static void main(String[] args) throws InterruptedException, UnsupportedBoardType, IOException {

    	//Initialisation de la liaison s�rie vers l'arduino
    	initSerial();
    	
    	//Initialisation de la base de donn�es Firebase
    	initDatabase();
    	
    	//Mise en place de l'�coute des donn�es de la base de donn�e
    	initChildListener();
    	
    	//Envoi de la position du robot (au d�marrage) vers FireBase
    	initPosition();

    	System.out.println("all done");
    	    	
    	//On envoie l'info sur Tx
    	try {
    		serial_USB.write(LINUX_SWITCH_ON);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
        
    	//Toute la suite du code s'execute avec des handlers. On peut se bloquer sur un s�maphore.
        new Semaphore(0).acquire();
        
    }
    
        
    private static void initDatabase() throws FileNotFoundException{
    	//Initialisation de la base de donn�es Firebase
    	FileInputStream serviceAccount = new FileInputStream("testfirebase-b8c4a-firebase-adminsdk-dj45z-e92de0861c.json");
    	FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl("https://testfirebase-b8c4a.firebaseio.com/")
                    .build();
    	FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance().getReference();
    }
    
    private static void initChildListener(){
        
    	//Mise en place de l'�coute des donn�es de la base de donn�e
        database.child(NOM_DU_ROBOT+"/leftMotorSpeed").addValueEventListener(new ValueEventListener() {
        	
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				try {
					motors_mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
        		

            	motors_outputSerial[3]=(byte)(dataSnapshot.getValue(int.class) & 0x000000FF);
            	
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(motors_outputSerial);
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
            	
            	motors_mutex.release();
            	
            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
        
        
        database.child(NOM_DU_ROBOT+"/rightMotorSpeed").addValueEventListener(new ValueEventListener() {
        	
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
        		
				try {
					motors_mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
            	motors_outputSerial[5]=(byte)(dataSnapshot.getValue(int.class) & 0x000000FF);
            	
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(motors_outputSerial);
            	} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
            	
            	motors_mutex.release();

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });

        
        database.child(NOM_DU_ROBOT+"/marcheArriere").addValueEventListener(new ValueEventListener() {
        	
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				try {
					motors_mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				if(dataSnapshot.getValue(boolean.class)){
					motors_outputSerial[1]=(byte)(MARCHE_ARRIERE);
				}else{
					motors_outputSerial[1]=(byte)(MARCHE_AVANT);
				}

				//On envoie l'info sur Tx            		
				try {            			
					serial_USB.write(motors_outputSerial);            		
				} catch (IllegalStateException | IOException e) {						
					e.printStackTrace();					
				}
				
            	motors_mutex.release();

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
        
    	
    	database.child(NOM_DU_ROBOT+"/clignotant").addValueEventListener(new ValueEventListener() {
        	
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
        		
				switch(dataSnapshot.getValue(int.class)){
				case 0://clignote pas
					clignotants_outputSerial[2]=(byte)0;
					clignotants_outputSerial[4]=(byte)0;
					break;
				case 1://gauche
					clignotants_outputSerial[2]=(byte)1;
					clignotants_outputSerial[4]=(byte)0;
					break;
				case 2://droite
					clignotants_outputSerial[2]=(byte)0;
					clignotants_outputSerial[4]=(byte)1;
					break;
				case 3://warning
					clignotants_outputSerial[2]=(byte)1;
					clignotants_outputSerial[4]=(byte)1;
					break;
				default:
					break;					
					
				}				
            	
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(clignotants_outputSerial);
            	} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
    	
    	database.child(NOM_DU_ROBOT+"/securite/modeSecurite").addValueEventListener(new ValueEventListener() {
        	
    		
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				if(dataSnapshot.getValue(boolean.class)){
					securite_outputSerial[1]=SECURITE_ACTIVEE;
					System.out.println("Security mode on --> App");
				}
				else{
					securite_outputSerial[1]=SECURITE_DESACTIVEE;
					System.out.println("Security mode off --> App");
				}
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(securite_outputSerial);
            	} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
    	
    	database.child(NOM_DU_ROBOT+"/securite/presenceDetectee").addValueEventListener(new ValueEventListener() {
        	
    		
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				if(!dataSnapshot.getValue(boolean.class))
					//On envoie l'info sur Tx
					try {
						serial_USB.write(PRESENCE_ACQUITTEE);
					} catch (IllegalStateException | IOException e) {
						e.printStackTrace();
					}

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
    	
    	database.child(NOM_DU_ROBOT+"/modePlatoon").addValueEventListener(new ValueEventListener() {
        	
    		
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				if(dataSnapshot.getValue(boolean.class)){
					platoon_outputSerial[1]=PLATOON_ACTIVE;
					System.out.println("Platoon mode on desde App");
				}
				else{
					platoon_outputSerial[1]=PLATOON_DESACTIVE;
					System.out.println("Platoon mode off desde App");
				}
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(platoon_outputSerial);
            	} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
    	
    	database.child(NOM_DU_ROBOT+"/modeLineFollower").addValueEventListener(new ValueEventListener() {
        	
    		
			@Override
        	public void onDataChange(DataSnapshot dataSnapshot) {
				
				if(dataSnapshot.getValue(boolean.class)){
					linefollower_outputSerial[1]=LINEFOLLOWER_ACTIVE;
					System.out.println("Line follower mode on from App");
				}
				else{
					linefollower_outputSerial[1]=LINEFOLLOWER_DESACTIVE;
					System.out.println("Line follower mode off from App");
				}
            	//On envoie l'info sur Tx
            	try {
            		serial_USB.write(linefollower_outputSerial);
            	} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}

            }

			@Override
			public void onCancelled(DatabaseError arg0) {}
			
        });
    	
    }
    
    
    
    
    
    
    private static void initPosition() throws JsonSyntaxException, JsonIOException, FileNotFoundException{
    	//R�cup�rer la position (enregistr�e dans le fichier localisation.json au d�marrage)
    	Gson gson = new Gson();
    	Position position = gson.fromJson(new FileReader("localisation.json"), Position.class);
    	database.child("robots/"+NOM_DU_ROBOT+"/localisation/latitude").setValue((double)position.getLatitude());
    	database.child("robots/"+NOM_DU_ROBOT+"/localisation/longitude").setValue((double)position.getLongitude());
    }
    
    private static void initSerial() throws UnsupportedBoardType, IOException, InterruptedException{
    	//Initialisation de la liaison s�rie vers l'arduino
        SerialConfig config = new SerialConfig();
        
        //Pour les �changes avec l'arduino
        config.device("/dev/ttyUSB0")
        .baud(Baud._9600)
        .dataBits(DataBits._8)
        .parity(Parity.NONE)
        .stopBits(StopBits._1)
        .flowControl(FlowControl.NONE);
        
        serial_USB.open(config);
        
        //Pour la connexion avec RFID
        config.device("/dev/ttyAMA0")
        .baud(Baud._9600)
        .dataBits(DataBits._8)
        .parity(Parity.NONE)
        .stopBits(StopBits._1)
        .flowControl(FlowControl.NONE);
        
        serial_UART.open(config);        
        
        
        //Mise en place de l'�coute de l'UART
        serial_USB.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {

                //On re�oit de la donn�e sur Rx
            	//Se reciben datos de arduino
            	try {
					
					switch(event.getBytes()[0]){
						case PRESENCE_DETECTEE:

							database.child(NOM_DU_ROBOT+"/securite/presenceDetectee").setValue((boolean)true);
							break;
						case MODE_SECURITE:
							if(event.getBytes()[1]==SECURITE_ACTIVEE)
								database.child(NOM_DU_ROBOT+"/securite/modeSecurite").setValue((boolean)true);
							else
								database.child(NOM_DU_ROBOT+"/securite/modeSecurite").setValue((boolean)false);
								database.child(NOM_DU_ROBOT+"/securite/presenceDetectee").setValue((boolean)false);
							break;
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
        
        //Mise en place de l'�coute de l'UART
        serial_UART.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {

                //On re�oit de la donn�e sur Rx
            	try {
            		boolean isYann = true;
            		
            		for(byte i = 0; i<14; i++){
            			isYann = isYann & event.getBytes()[i]==CODE_SECURITE_YANN[i];
            		}
            		
            		if(isYann){
            			System.out.println("reconnu Yann");
            			//On envoie l'info sur Tx
    					try {
    						serial_USB.write(PRESENCE_ACQUITTEE);
    					} catch (IllegalStateException | IOException e) {
    						e.printStackTrace();
    					}
            		}

				} catch (IOException e) {
					e.printStackTrace();
				}
				
            }
        });
    }

}