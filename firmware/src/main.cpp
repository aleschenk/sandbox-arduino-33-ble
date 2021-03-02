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
  Serial.println();
  Serial.println(F("Acceleration in G's"));
  Serial.println(F("X\tY\tZ"));
}

void readIMU(float &x, float &y, float &z) {
  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(x, y, z);
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

void loop() {
  float x, y, z;
  int r, g, b;

  // delay(1000);
  float humidity = readHumidity();
  float temperature = readTemperature();
  float pressure = readPressureSensor();
  readIMU(x, y, z);
  readGestureColor(r, g, b);
  int proximity = 0; //readProximity();
  int gesture = GESTURE_NONE; // readGesture();
  char str[120];
  memset(&str, '\0', 120);

  sprintf(str, "%.2f,%.2f,%2.f,%2.f,%2.f,%2.f,%d,%d,%d,%d,%d\n", temperature, humidity, pressure, x, y, z, r, g, b, proximity, gesture);
  Serial.print(str);

/*
  Serial.print(F("h: ")); Serial.print(humidity); Serial.print(F("%\t"));
  Serial.print(F("t: ")); Serial.print(temperature); Serial.print(F("°C\t"));
  Serial.print(F("p: ")); Serial.print(pressure); Serial.print(F(" hPa\t"));
  Serial.print("x: "); Serial.print(x); Serial.print("\ty: "); Serial.print(y); Serial.print("\tz: "); Serial.print(z); Serial.print("\t");
  Serial.print("r: "); Serial.print(r); Serial.print("\tg: "); Serial.print(g); Serial.print("\tb: "); Serial.print(b); Serial.print("\t");
  Serial.print("pr: "); Serial.print(proximity); Serial.print("\tge: "); Serial.println(gestureToChar(gesture));
  */
}

