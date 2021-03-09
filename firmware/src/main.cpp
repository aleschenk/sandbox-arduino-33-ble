#include <Arduino.h>

// Bluetooth BLE
#include <ArduinoBLE.h>

// Radio (Microphone)
#include <PDM.h>

// IMU
#include <Arduino_LSM9DS1.h>

// Gestures, color, light intensity and proximity Sensor
#include <Arduino_APDS9960.h>

// Air Pressure Sensor
#include <Arduino_LPS22HB.h>

// Temperature and Relative Humidity Sensor
#include <Arduino_HTS221.h>


// Device name
const char* NAME_OF_PERIPHERAL = "Octavio";
const char* uuidOfService = "00001101-0000-1000-8000-00805f9b34fb";
const char* uuidOfRxChar = "00001142-0000-1000-8000-00805f9b34fb";
const char* uuidOfTxChar = "00001143-0000-1000-8000-00805f9b34fb";

// BLE Service
BLEService microphoneService(uuidOfService);

// Setup the incoming data characteristic (RX).
const int WRITE_BUFFER_SIZE = 256;
bool WRITE_BUFFER_FIZED_LENGTH = false;

// RX / TX Characteristics
BLECharacteristic rxChar(uuidOfRxChar, BLEWriteWithoutResponse | BLEWrite, WRITE_BUFFER_SIZE, WRITE_BUFFER_FIZED_LENGTH);
BLEByteCharacteristic txChar(uuidOfTxChar, BLERead | BLENotify | BLEBroadcast);



/* 
 * Setup Microphone Module
 * PDM stands for Pulse-density modulation
 */
// void setupMicrophone() {
//   PDM.onReceive(onPDMdata)
// }

void onBLEConnected(BLEDevice central) {
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
  // connectedLight();
}

void onBLEDisconnected(BLEDevice central) {
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
  // disconnectedLight();
}

void onRxCharValueUpdate(BLEDevice central, BLECharacteristic characteristic) {
  // central wrote new value to characteristic, update LED
  Serial.print("Characteristic event, read: ");
  byte test[256];
  int dataLength = rxChar.readValue(test, 256);

  for(int i = 0; i < dataLength; i++) {
    Serial.print((char)test[i]);
  }
  Serial.println();
  Serial.print("Value length = ");
  Serial.println(rxChar.valueLength());
}

/* 
 * Setup Bluetooth BLE Module
 */
void setupBLE() {
    // begin initialization
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");

    while (1);
  }

  // Create BLE service and characteristics.
  BLE.setLocalName(NAME_OF_PERIPHERAL);
  BLE.setAdvertisedService(microphoneService);
  microphoneService.addCharacteristic(rxChar);
  microphoneService.addCharacteristic(txChar);
  BLE.addService(microphoneService);

  // Bluetooth LE connection handlers.
  BLE.setEventHandler(BLEConnected, onBLEConnected);
  BLE.setEventHandler(BLEDisconnected, onBLEDisconnected);

  // Event driven reads.
  rxChar.setEventHandler(BLEWritten, onRxCharValueUpdate);
  
  // Let's tell devices about us.
  BLE.advertise();

  // Print out full UUID and MAC address.
  // printf("Peripheral advertising info: ")
  Serial.println("Peripheral advertising info: ");
  Serial.print("Name: "); Serial.println(NAME_OF_PERIPHERAL);
  Serial.print("MAC: "); Serial.println(BLE.address());
  Serial.print("Service UUID: "); Serial.println(microphoneService.uuid());
  Serial.print("rxCharacteristic UUID: "); Serial.println(uuidOfRxChar);
  Serial.print("txCharacteristics UUID: "); Serial.println(uuidOfTxChar);

  Serial.println("Bluetooth device active, waiting for connections...");
}

/*
 * Setup Gesture Sensor Module
 */
void setupGestureSensor() {
  if (!APDS.begin()) {
    Serial.println("Error initializing APDS9960 sensor.");
    while (1);
  }
}

/*
 * Setup Pressure Sensor Module
 */
void setupPressureSensor() {
  if (!BARO.begin()) {
    Serial.println("Failed to initialize pressure sensor!");
    while (1);
  }
}

/* 
 * Setup Humidity and Temperature Sensor Module
 */
void setupHTS() {
  if (!HTS.begin()) {
    Serial.println("Failed to initialize humidity temperature sensor!");
    while (1);
  }
}

// Absolute pressure range: 260 to 1260 hPa
float readPressureSensor() {
  return BARO.readPressure();
}

// Temperature accuracy: ± 0.5 °C,15 to +40 °C
// Temperature range: -40 to 120° C
float readTemperature() {
  return HTS.readTemperature();
}

// Humidity accuracy: ± 3.5% rH, 20 to +80% rH
// Humidity range: 0 to 100 %
float readHumidity() {
  return HTS.readHumidity();
}


/* 
 * Setup Inertial Measurement Unit Sensor Module
 */
void setupIMU() {
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  Serial.print(F("Accelerometer sample rate = "));
  Serial.print(IMU.accelerationSampleRate());
  Serial.println(F(" Hz"));
}

void readIMU(float &x, float &y, float &z) {
  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(x, y, z);
  }

}

void readGyroscope(float &x, float &y, float &z) {
  if (IMU.gyroscopeAvailable()) {
    IMU.readGyroscope(x, y, z);
  }
}

void readMagneticField(float &x, float &y, float &z) {
  if (IMU.magneticFieldAvailable()) {
    IMU.readMagneticField(x, y, z);
  }
}


void readGestureColor(int &r, int &g, int &b) {
  // check if a color reading is available
  while (! APDS.colorAvailable()) {
    delay(5);
  }
  // read the color
  APDS.readColor(r, g, b);
}

int readProximity() {
  if (APDS.proximityAvailable()) {
    return APDS.readProximity();
  }
  return 0;
}

int readGesture() {
  if (APDS.gestureAvailable()) {
    return APDS.readGesture();
  }
  return GESTURE_NONE;
}

void setupSerial() {
  Serial.begin(9600);
  while (!Serial);
}

void setup() {
  setupSerial();

  Serial.println(F("Initializing Gesture Module"));
  setupGestureSensor();

  Serial.println(F("Initializing Temperature and Humidity Module"));
  setupHTS();

  Serial.println(F("Initializing IMU Module"));
  setupIMU();

  Serial.println(F("Initializing Presure Module"));
  setupPressureSensor();

  // Serial.println(F("Initializing BLE Module"));
  // setupBLE();
}

char gestureToChar(int gesture) {
  if (gesture == GESTURE_NONE) {
    return 'n';
  } else if (gesture == GESTURE_UP) {
    return 'u';
  } else if (gesture == GESTURE_DOWN) {
    return 'd';
  } else if (gesture == GESTURE_LEFT) {
    return 'l';
  } else if (gesture == GESTURE_RIGHT) {
    return 'r';
  }
  return 'x';
}

bool polling = false;

int state = 0;

void loop() {
  float ax, ay, az, gx, gy, gz, mx, my, mz;
  int r, g, b;

  if (Serial.available() > 0) {
    String command = Serial.readString();
    if (command.equals("START")) {
      polling = true;
    } else if (command.equals("STOP")) {
      polling = false;
    }
  }

  if (!polling) {
    return ;
  }

  float humidity = readHumidity();
  float temperature = readTemperature();
  float pressure = readPressureSensor();
  readIMU(ax, ay, az);
  readGyroscope(gx, gy, gz);
  readMagneticField(mx, my, mz);
  readGestureColor(r, g, b);
  int proximity = 0; //readProximity();
  int gesture = GESTURE_NONE; // readGesture();
  
  /*
  char str1[70], str2[20];
  memset(&str1, '\0', 70);
  memset(&str2, '\0', 20);

  // [30.43,48.48,75.71,0.14,0.19,0.96,0.43,-0.61,0.37,-1.43,26.78
  // 2.14,9,6,5,0,-1

  sprintf(str1, "[%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,", temperature, humidity, pressure, ax, ay, az, gx, gy, gz, mx, my);
  sprintf(str2, "%.2f,%d,%d,%d,%d,%d\n", mz, r, g, b, proximity, gesture);

  Serial.print(str1);
  Serial.print(str2);
  */

 Serial.print(temperature); Serial.print(",");
 Serial.print(humidity); Serial.print(",");
 Serial.print(pressure); Serial.print(",");
 Serial.print(ax); Serial.print(",");
 Serial.print(ay); Serial.print(",");
 Serial.print(az); Serial.print(",");
 Serial.print(gx); Serial.print(",");
 Serial.print(gy); Serial.print(",");
 Serial.print(gz); Serial.print(",");
 Serial.print(mx); Serial.print(",");
 Serial.print(my); Serial.print(",");
 Serial.print(mz); Serial.print(",");
 Serial.print(r); Serial.print(",");
 Serial.print(g); Serial.print(",");
 Serial.print(b); Serial.print(",");
 Serial.print(proximity); Serial.print(",");
 Serial.println(gesture);
}

