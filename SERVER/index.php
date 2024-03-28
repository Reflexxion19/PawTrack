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
            if($decoded_json->type == "g_d"){ # gps_data
                $lo = $decoded_json->lo;
                $la = $decoded_json->la;
                $dt = $decoded_json->dt;
                $far = $decoded_json->f_a_r;
    
                $data = array('`long`' => $lo, '`lat`' => $la,
                'time' => $dt, '`fk_Activity_Reportid`' => $far);
    
                $db->insert("gps_data", $data);
            }
    
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