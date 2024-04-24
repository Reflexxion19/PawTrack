/////////////////////////////////////////////
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
/////////////////////////////////////////////

#include <TinyGPS++.h>
#include <SoftwareSerial.h>
//////////////////////////////////////////////
#define SCREEN_WIDTH 128
#define OLED_RESET     -1
#define SCREEN_ADDRESS 0x3C
Adafruit_SSD1306 display(SCREEN_WIDTH, 32, &Wire, OLED_RESET);
//////////////////////////////////////////////

//EspSoftwareSerial::UART myPort;
TinyGPSPlus gps;
SoftwareSerial SerialGPS(23, 19);

void setup(){
  Serial.begin(115200); // Standard hardware serial port
  SerialGPS.begin(9600);

  /////////////////////////////////////////////
  if(!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
    Serial.println(F("SSD1306 allocation failed"));
    for(;;);
  }

  display.display();
  delay(2000);

  display.clearDisplay();
  display.drawPixel(10, 10, SSD1306_WHITE);
  display.display();
  /////////////////////////////////////////////

  Serial.println();
  Serial.println("Connecting");
}

void loop(){
  bool newData = false;

  if(SerialGPS.available() > 0){
    //Serial.println("Available");
    if(gps.encode(SerialGPS.read())){
      //Serial.println("Encoded");
      newData = true;
    }
  }

  if(newData == true){
    /////////////////////////////////////////////
    scrolltext_sats();
    /////////////////////////////////////////////
    newData = false;
    Serial.println(gps.satellites.value());
    print_data();
  }
  else{
    Serial.println("No new data");
  }

  delay(1000);
}

void print_data(){
  if(gps.location.isValid() == 1){
    /////////////////////////////////////////////
    scrolltext_alert();
    scrolltext_location();
    /////////////////////////////////////////////
    Serial.print("Lat: ");
    Serial.print(gps.location.lat(), 6);
    Serial.print("; Lng: ");
    Serial.print(gps.location.lng(), 6);
    Serial.print("; Speed: ");
    Serial.print(gps.speed.kmph());
    Serial.print("; SAT: ");
    Serial.print(gps.satellites.value());
    Serial.print("; ALT: ");
    Serial.println(gps.altitude.meters(), 0);
  }
}

void scrolltext_sats(void) {
  display.clearDisplay();

  display.setTextSize(2); // Draw 2X-scale text
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.print("Sats: ");
  display.print(gps.satellites.value());
  display.display();      // Show initial text
  delay(100);
}

void scrolltext_location(void) {
  display.clearDisplay();

  //display.setTextSize(2); // Draw 2X-scale text
  display.setTextColor(SSD1306_WHITE);

  display.setCursor(0, 0);
  display.print("Sats: ");
  display.println(gps.satellites.value());
  display.display();
  delay(4000);

  display.clearDisplay();
  display.setCursor(0, 0);
  display.println("Lat: ");
  display.println(gps.location.lat(), 4);
  display.display();
  delay(5000);

  display.clearDisplay();
  display.setCursor(0, 0);
  display.println("Lng: ");
  display.println(gps.location.lng(), 4);
  display.display();
  delay(5000);
  //display.startscrolldiagright(0x0F, 0x0F);
}

void scrolltext_alert(void) {
  display.clearDisplay();

  display.setTextSize(2); // Draw 2X-scale text
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.print("Data captured");
  display.display();      // Show initial text
  delay(4000);
}