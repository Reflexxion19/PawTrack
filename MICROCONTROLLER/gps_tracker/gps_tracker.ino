#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

#define TRACKER_ID 547865;

const char* ssid = "Reflexxion";
const char* password = "duokivesiu";

char jsonOutput[12800];

int pet_id = 0;

String gps_points[500][8] = {
{"2.5555666", "-2.655345", "2024", "10", "15", "13", "25", "23"},
{"3.5555666123123122343", "-3.655345123123123", "2024", "12", "10", "15", "25", "24"}};
int gps_points_len = 2;

TinyGPSPlus gps;
SoftwareSerial SerialGPS(23, 19);

double homepoint_lat = 54.906716;
double homepoint_lng = 23.968269;

bool walk_started = false;

void setup() {
  Serial.begin(115200);
  SerialGPS.begin(9600);

  connect_to_wifi();
}

void loop() {
  if ((WiFi.status() == WL_CONNECTED)){
    if(gps_points_len > 0){
      int pet_id = get_pet_id();

      if(pet_id != -1){
        int ari = create_activity_report(pet_id);

        if(ari != -1){
          send_gps_data(ari);
        }
      }
    }

    if(walk_started){
      walk_stop();
    }
  }
  else{
    Serial.println("Not connected to WiFi");
    connect_to_wifi();

    get_gps_point();
  }
 
  delay(1000);
}



//// Functions ////

void connect_to_wifi(){
  Serial.print("Connecting to WiFi");
  WiFi.disconnect();
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  int c = 0;
  while (c < 10) {
    if (WiFi.status() == WL_CONNECTED) {
      WiFi.setAutoReconnect(true);
      WiFi.persistent(true);
      Serial.println();
      Serial.print("WiFi Status: ");
      Serial.println(WiFi.status());
      Serial.println(WiFi.localIP());
      delay(100);
      return;
    }
    Serial.print(".");
    delay(900);
    c++;
  }
}

void get_gps_point(){
  bool newData = false;

  if(SerialGPS.available() > 0){
    if(gps.encode(SerialGPS.read())){
      newData = true;
    }
  }

  if(newData == true){
    if(gps.location.isValid() == 1){
      if(calculate_distance_from_homepoint(gps.location.lng(), gps.location.lat()) >= 20){
        if(!walk_started){
          walk_start();
        }
        else{
          save_data();
        }
      }
      else{
        walk_stop();
      }
    }
  }
  else{
    Serial.println("No new data");
  }
}

void walk_start(){
    walk_started = true;
    save_data();
}

void walk_stop(){
  walk_started = false;
}

double calculate_distance_from_homepoint(double lat, double lng){
  double lat1 = homepoint_lat / 57.29577951;
  double lng1 = homepoint_lng / 57.29577951;

  double lat2 = lat / 57.29577951;
  double lng2 = lng / 57.29577951;

  double distance = 3963.0 * acos((sin(lat1) * sin(lat2)) + cos(lat1) * cos(lat2) * cos(lng2 - lng1)) * 1.609344 * 1000;

  return (int)distance;
}

void save_data(){
  gps_points_len++;
  gps_points[gps_points_len][0] = String(gps.location.lat());
  gps_points[gps_points_len][1] = String(gps.location.lng());

  gps_points[gps_points_len][2] = String(gps.date.year());
  gps_points[gps_points_len][3] = String(gps.date.month());
  gps_points[gps_points_len][4] = String(gps.date.day());

  gps_points[gps_points_len][5] = String(gps.time.hour());
  gps_points[gps_points_len][6] = String(gps.time.minute());
  gps_points[gps_points_len][7] = String(gps.time.second());
}



//// Get data from the server ////

// int get_activity_report_id(){
//   HTTPClient client;

//   client.begin("https://pvp.seriouss.am/?type=g_l_a_r_i");
//   int httpCode = client.GET();
//   Serial.println("Statuscode:" + String(httpCode));

//   int payload = -1;

//   if (httpCode > 0){
//     payload = client.getString().toInt();
//     Serial.println(payload);

//     client.end();
//   }
//   else{
//     Serial.println("Error on HTTP request");
//   }

//   return payload;
// }

int get_pet_id(){
  HTTPClient client;

  String request = "https://pvp.seriouss.am/?type=g_p_i_b_t_i&t_i=";
  request += TRACKER_ID;

  client.begin(request);
  int httpCode = client.GET();
  Serial.println("Statuscode:" + String(httpCode));

  int payload = -1;

  if (httpCode > 0){
    payload = client.getString().toInt();
    Serial.println(payload);

    client.end();
  }
  else{
    Serial.println("Error on HTTP request");
  }

  return payload;
}



//// Send data to the server ////

int create_activity_report(int pet_id){
  HTTPClient client;
  
  client.begin("https://pvp.seriouss.am");
  client.addHeader("Content-Type", "application/json");

  const size_t CAPACITY = JSON_OBJECT_SIZE(1);
  JsonDocument doc;

  JsonObject object = doc.to<JsonObject>();
  
  object["type"] = "r";
  object["dt"] = "2024-04-24 17:30:38";
  object["d_w"] = "0";
  object["c_b"] = "0";
  object["a_t"] = "00:00:12";
  object["p"] = pet_id;

  serializeJson(doc, jsonOutput);

  int httpCode = client.POST(String(jsonOutput));
  Serial.println("Create_activity_report Statuscode:" + String(httpCode));

  int payload = -1;

  if (httpCode > 0){
    payload = client.getString().toInt();
    Serial.println(payload);

    client.end();
  }
  else{
    Serial.println("Error on HTTP request");
  }

  return payload;
}

void send_gps_data(int ari){
  HTTPClient client;
  
  client.begin("https://pvp.seriouss.am");
  client.addHeader("Content-Type", "application/json");

  const size_t CAPACITY = JSON_OBJECT_SIZE(1);
  JsonDocument doc;

  JsonObject object = doc.to<JsonObject>();

  object["type"] = "g_d";
  object["c"] = gps_points_len;
  object["f_a_r"] = ari;

  for (int i = 0; i < gps_points_len; i++){
    object[String(i + 1)][0] = gps_points[i][0];
    object[String(i + 1)][1] = gps_points[i][1];

    String dt;
    dt = gps_points[i][2];
    dt += "-";
    dt += gps_points[i][3];
    dt += "-";
    dt += gps_points[i][4];
    dt += " ";
    dt += gps_points[i][5];
    dt += ":";
    dt += gps_points[i][6];
    dt += ":";
    dt += gps_points[i][7];

    object[String(i + 1)][2] = dt;
  }

  serializeJson(doc, jsonOutput);

  int httpCode = client.POST(String(jsonOutput));
  Serial.println("Send_gps_data Statuscode:" + String(httpCode));
  // Serial.println(String(jsonOutput));

  if (httpCode > 0){
    String payload = client.getString();
    Serial.println(payload);

    client.end();
  }
  else{
    Serial.println("Error on HTTP request");
  }
}