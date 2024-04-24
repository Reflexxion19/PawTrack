<?php
require "db_config.php";

class DB_handler{
    private $sname;
    private $uname;
    private $password;
    private $db_name;
    public $conn = null;

    public function __construct()
    {
        $dbc = new DBConfig();
        $this->sname = $dbc->sname;
        $this->uname = $dbc->uname;
        $this->password = $dbc->password;
        $this->db_name = $dbc->db_name;
    }

    function dbConnect()
    {
        $this->conn = mysqli_connect($this->sname, $this->uname, $this->password, $this->db_name);
    }

    public function log_in($username, $password){
        $sql = "SELECT `password` FROM `user` WHERE `username`='$username'";
        $found_password = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['password'];
        
        if($found_password == $password){
            echo "Login successful";
        }
        else{
            echo "Login error";
        }
    }

    public function get_user_id($table, $column_name, $data){
        $sql = "SELECT `id` FROM `$table` WHERE `$column_name`='$data'";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['id'];
        return $result;
    }

    public function get_pet_id($owner_username, $pet_name){
        $sql = "SELECT `id` FROM `pet` WHERE `fk_Userusername`='$owner_username' AND `name`='$pet_name'";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['id'];
        echo $result;
    }

    public function list_pets($owner_username){
        $sql = "SELECT `id`, `name`, `pet_picture`, `track_id`, `track_status`,
        `activity_category`  FROM `pet` WHERE `fk_Userusername`='$owner_username'";
        $result = mysqli_query($this->conn, $sql);
        
        while($row = mysqli_fetch_assoc($result))
        {
            echo "i=" . $row['id'] . ";";
            echo "n=" . $row['name'] . ";";
            if($row['pet_picture'] == ""){
                echo "p_p=null;";
            }
            else{
                echo "p_p=" . $row['pet_picture'] . ";";
            }

            if($row['track_id'] == ""){
                echo "t_i=null;";
            }
            else{
                echo "t_i=" . $row['track_id'] . ";";
            }
            echo "t_s=" . $row['track_status'] . ";";
            echo "a_c=" . $row['activity_category'] . ";\n";
        }
    }

    public function get_statistics($pet_id){
        $sql = "SELECT `date`, `distance_walked`, `calories_burned`
            FROM `activity_report`
            WHERE `date` >= DATE_SUB(CURRENT_DATE, INTERVAL WEEKDAY(CURRENT_DATE) DAY)
            AND `date` < DATE_ADD(CURRENT_DATE, INTERVAL (7 - WEEKDAY(CURRENT_DATE)) DAY)
            AND `fk_Petid` = $pet_id;";
        $result = mysqli_query($this->conn, $sql);

        $d_array = array(1 => 0, 2 => 0, 3 => 0, 4 => 0, 5 => 0, 6 => 0, 7 => 0); // distance_walked array
        $c_array = array(1 => 0, 2 => 0, 3 => 0, 4 => 0, 5 => 0, 6 => 0, 7 => 0); // calories_burned array
        
        while($row = mysqli_fetch_assoc($result))
        {
            if (date('w', strtotime($row['date'])) == 1){
                $d_array[1] += $row['distance_walked'];
                $c_array[1] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 2){
                $d_array[2] += $row['distance_walked'];
                $c_array[2] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 3){
                $d_array[3] += $row['distance_walked'];
                $c_array[3] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 4){
                $d_array[4] += $row['distance_walked'];
                $c_array[4] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 5){
                $d_array[5] += $row['distance_walked'];
                $c_array[5] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 6){
                $d_array[6] += $row['distance_walked'];
                $c_array[6] += $row['calories_burned'];
            }
            else if (date('w', strtotime($row['date'])) == 7){
                $d_array[7] += $row['distance_walked'];
                $c_array[7] += $row['calories_burned'];
            }
        }

        for($i = 1; $i <= 7; $i++){
            echo "d_w=" . $d_array[$i] . ";"; // distance walked
            echo "c_b=" . $c_array[$i] . ";\n"; // calories burned
        }
    }

    public function gps_data_processing($db, $decoded_json){
        $count = $decoded_json->c;
        $fk = $decoded_json->f_a_r;

        for($i = 1; $i <= $count; $i++){
            $arr = $decoded_json->$i;
            $lat = $arr[0];
            $long = $arr[1];
            $dateTime = $arr[2];

            $data = array('`time`' => $dateTime, '`lat`' => $lat, '`long`' => $long, '`fk_Activity_Reportid`' => $fk);
            $db->insert("gps_data", $data);
        }
    }

    public function respond_report_activity_id($date){
        $sql = "SELECT `id`  FROM `activity_report` WHERE `date`='$date'";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['id'];
        echo $result;
    }

    // Insert data
    public function insert($table, $data) {
        $columns = array_keys($data);
        $values = array_values($data);

        $sql = "INSERT INTO $table (".implode(',',$columns).") VALUES ('" . implode("', '", $values) . "' )";

        mysqli_query($this->conn, $sql);
    }

    // Update data
    public function update($table, $column, $value, $id) {
        $sql = "UPDATE `$table` SET `$column`=$value WHERE `id`=$id";
        mysqli_query($this->conn, $sql);
    }

    // Delete data
    public function delete($table, $column, $value) {
        $sql = "DELETE FROM `$table` WHERE `$column`='$value'";
        mysqli_query($this->conn, $sql);
    }
}

$post_json = file_get_contents('php://input');

$db = new DB_handler;
$db->dbConnect();

if($db->conn){
    if($_SERVER['REQUEST_METHOD'] == "POST"){ # POST
        $decoded_json = json_decode($post_json, false);

        if($decoded_json != ""){
            if($decoded_json->type == "u_r"){ # user registration
                $username = $decoded_json->u;
                $password = $decoded_json->p;
                $email = $decoded_json->e;
                $profile_picture = $decoded_json->p_p;
                $subscribed = $decoded_json->s;
                $premium_expiration = $decoded_json->p_e;
    
                $data = array('`username`' => $username, '`password`' => $password, '`email`' => $email,
                '`profile_picture`' => $profile_picture, '`subscribed`' => $subscribed,
                '`premium_expiration`' => $premium_expiration);
                
                $db->insert("user", $data);
            }
    
            if($decoded_json->type == 'l_i'){ # log in
                $username = $decoded_json->u;
                $password = $decoded_json->p;
    
                $db->log_in($username, $password);
            }

            if($decoded_json->type == 'p_r'){
                $user_name = $decoded_json->u_n;
                $pet_name = $decoded_json->p_n;
                $pet_picture = $decoded_json->p_p;
                $tracker_id = $decoded_json->t_i;
                $tracker_status = $decoded_json->t_s;
                $activity_category = $decoded_json->a_c;

                $data = array('`name`' => $pet_name, '`pet_picture`' => $pet_picture, '`track_id`' => $tracker_id,
                '`track_status`' => $tracker_status, '`activity_category`' => $activity_category, '`fk_Userusername`' => $user_name);

                $db->insert("pet", $data);
            }

            if($decoded_json->type == "r"){ # activity_report
                $dateTime = $decoded_json->dt;
                $distance_walked = $decoded_json->d_w;
                $calories_burned = $decoded_json->c_b;
                $active_time = $decoded_json->a_t;
                $fk = $decoded_json->p;

                $data = array('`date`' => $dateTime, '`distance_walked`' => $distance_walked, '`calories_burned`' => $calories_burned,
                '`active_time`' => $active_time, '`fk_Petid`' => $fk);

                $db->insert("activity_report", $data);
                $db->respond_report_activity_id($dateTime);
            }

            if($decoded_json->type == "g_d"){ # gps_data
                $db->gps_data_processing($db, $decoded_json);
            }
        }
    }

    if($_SERVER['REQUEST_METHOD'] == "GET"){ # GET
        if($_SERVER['QUERY_STRING'] != ''){
            $parsed_data = parse_str_get($_SERVER['QUERY_STRING']);

            if($parsed_data['type'] == 'pet_id'){ # get pet id
                $owner = $parsed_data['owner'];
                $pet = $parsed_data['pet'];

                $db->get_pet_id($owner, $pet);
            }

            if($parsed_data['type'] == 'l_p'){ # list pets
                $owner_username = $parsed_data['u'];

                $db->list_pets($owner_username);
            }

            if($parsed_data['type'] == 'g_s'){ # get statistics
                $pet_id = $parsed_data['p'];

                $db->get_statistics($pet_id);
            }
        }
    }
}

function parse_str_get($str) {
    # result array
    $arr = array();
  
    # split on outer delimiter
    $pairs = explode('&', $str);
  
    # loop through each pair
    foreach ($pairs as $i) {
      # split into name and value
      list($name,$value) = explode('=', $i, 2);
      
      # if name already exists
      if( isset($arr[$name]) ) {
        # stick multiple values into an array
        if( is_array($arr[$name]) ) {
          $arr[$name][] = $value;
        }
        else {
          $arr[$name] = array($arr[$name], $value);
        }
      }
      # otherwise, simply stick it in a scalar
      else {
        $arr[$name] = $value;
      }
    }
  
    # return result array
    return $arr;
}