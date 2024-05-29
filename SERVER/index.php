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

    public function get_image($id, $type){
        $result = '';
        if($type == 'user'){
            $sql = "SELECT `profile_picture` FROM `user` WHERE `username`='$id'";
            $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['profile_picture'];
        }
        elseif($type == 'pet'){
            $sql = "SELECT `pet_picture` FROM `pet` WHERE `id`='$id'";
            $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['pet_picture'];
        }

        echo $result;
    }

    public function user_image_update($user_name, $url){
        $sql = "UPDATE `user` SET `profile_picture`='$url' WHERE `username`='$user_name'";
        mysqli_query($this->conn, $sql);
    }

    public function pet_image_update($id, $url){
        $sql = "UPDATE `pet` SET `pet_picture`='$url' WHERE `id`='$id'";
        mysqli_query($this->conn, $sql);
    }

    public function get_last_activity_report_id(){
        $sql = "SELECT `id` FROM `activity_report` WHERE `id`=(SELECT max(id) FROM `activity_report`)";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['id'];

        // $data = [ 'name' => $result ];
        // header('Content-Type: application/json; charset=utf-8');
        // echo json_encode($data);
        echo $result;
    }

    public function get_activity_reports($pet_id) {
        $sql = "SELECT `id`, `date`, `distance_walked`, `calories_burned`, `active_time` FROM `activity_report` WHERE `fk_Petid`='$pet_id'";
        $result = mysqli_query($this->conn, $sql);
    
        while($row = mysqli_fetch_assoc($result)) {
            echo "id=" . $row['id'] . ";" . "d=" . $row['date'] . ";" . "d_w=" . $row['distance_walked'] . ";" . 
            "c_b=" . $row['calories_burned'] . ";" . "a_t=" . $row['active_time'] . "\n";
        }
    }

    public function get_activity_report_ids($id, $month, $year){
        $sql = "SELECT `id` FROM `activity_report` WHERE `fk_Petid`=$id AND MONTH(date) = $month AND YEAR(date) = $year";
        $result = mysqli_query($this->conn, $sql);

        while($row = mysqli_fetch_assoc($result))
        {
            echo $row['id'] . "\n";
        }
    }

    public function get_location_points($id){
        $sql = "SELECT `lat`, `long` FROM `gps_data` WHERE `fk_Activity_Reportid`='$id'";
        $result = mysqli_query($this->conn, $sql);

        while($row = mysqli_fetch_assoc($result))
        {
            echo "lat=" . $row['lat'] . ";" . "long=" . $row['long'] . "\n";
        }
    }

    public function get_pet_id_by_tracker_id($tracker_id){
        $sql = "SELECT `id` FROM `pet` WHERE `track_id`='$tracker_id'";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['id'];

        echo $result;
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

    public function password_update($user_name, $current_pass, $new_pass){
        $sql = "SELECT `password` FROM `user` WHERE `username`='$user_name'";
        $result = mysqli_fetch_assoc(mysqli_query($this->conn, $sql))['password'];

        if($result == $current_pass){
            $sql = "UPDATE `user` SET `password`='$new_pass' WHERE `username`='$user_name'";

            mysqli_query($this->conn, $sql);
            echo "Success";
        }
        else{
            echo "Incorrect";
        }
    }

    public function email_update($user_name, $email){
        $sql = "UPDATE `user` SET `email`='$email' WHERE `username`='$user_name'";
        mysqli_query($this->conn, $sql);
    }

    public function respond_report_activity_id($date, $pet_id){
        $sql = "SELECT `id`  FROM `activity_report` WHERE `date`='$date' AND `fk_Petid` = $pet_id";
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

    public function pet_remove($name, $fk) {
        $sql = "DELETE FROM `pet` WHERE `name`='$name' AND `fk_Userusername`='$fk'";
        mysqli_query($this->conn, $sql);
    }

    // Update data
    public function pet_update($name, $new_name, $pet_picture, $track_id, $track_status, $activity_category, $fk) {
        $sql = "UPDATE `pet` SET `name`='$new_name', `pet_picture`='$pet_picture', `track_id`='$track_id',
        `track_status`='$track_status', `activity_category`='$activity_category'
        WHERE `name`='$name' AND `fk_Userusername`='$fk'";
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
            if($decoded_json->type == "u_r"){ # user register
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

            if($decoded_json->type == 'p_r'){ # pet register
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

            if($decoded_json->type == "r"){ # create activity report
                $dateTime = $decoded_json->dt;
                $distance_walked = $decoded_json->d_w;
                $calories_burned = $decoded_json->c_b;
                $active_time = $decoded_json->a_t;
                $fk = $decoded_json->p;

                $data = array('`date`' => $dateTime, '`distance_walked`' => $distance_walked, '`calories_burned`' => $calories_burned,
                '`active_time`' => $active_time, '`fk_Petid`' => $fk);

                $db->insert("activity_report", $data);
                $db->respond_report_activity_id($dateTime, $fk);
            }

            if($decoded_json->type == "g_d"){ # gps data
                $db->gps_data_processing($db, $decoded_json);
            }

            if($decoded_json->type == "p_rm"){ # pet remove
                $pet_name = $decoded_json->p_n;
                $fk = $decoded_json->u_n;

                $db->pet_remove($pet_name, $fk);
            }

            if($decoded_json->type == "p_u"){ # pet update
                $pet_name = $decoded_json->p_n;
                $new_pet_name = $decoded_json->n_p_n;
                $pet_picture = $decoded_json->p_p;
                $track_id = $decoded_json->t_i;
                $track_status = $decoded_json->t_s;
                $activity_category = $decoded_json->a_c;
                $fk = $decoded_json->u_n;

                echo "Yes";

                $db->pet_update($pet_name, $new_pet_name, $pet_picture, $track_id, $track_status, $activity_category, $fk);
            }

            if($decoded_json->type == "c_p"){ # change password
                $user_name = $decoded_json->u_n;
                $current_pass = $decoded_json->cr_p;
                $new_pass = $decoded_json->n_p;

                $db->password_update($user_name, $current_pass, $new_pass);
            }

            if($decoded_json->type == "c_e"){ # change email
                $user_name = $decoded_json->u_n;
                $email = $decoded_json->e;

                $db->email_update($user_name, $email);
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

            if($parsed_data['type'] == 'g_p_i'){ # get pet image
                $pet_id = $parsed_data['p'];

                $db->get_image($pet_id, 'pet');
            }

            if($parsed_data['type'] == 'g_u_i'){ # get user image
                $user_id = $parsed_data['u'];

                $db->get_image($user_id, 'user');
            }

            if($parsed_data['type'] == 'g_l_a_r_i'){ # get last activity id
                $db->get_last_activity_report_id();
            }

            if($parsed_data['type'] == 'g_a_r_i'){ # get activity ids
                $id = $parsed_data['p_i'];
                $month = $parsed_data['m'];
                $year = $parsed_data['y'];

                $db->get_activity_report_ids($id, $month, $year);
            }

            if($parsed_data['type'] == 'g_a_r'){ # get activity reports
                $pet_id = $parsed_data['p'];

                $db->get_activity_reports($pet_id);
            }

            if($parsed_data['type'] == 'g_l_p'){ # get location points
                $id = $parsed_data['a_r_i'];

                $db->get_location_points($id);
            }

            if($parsed_data['type'] == 'g_p_i_b_t_i'){ # get pet id by tracker id
                $tracker_id = $parsed_data['t_i'];

                $db->get_pet_id_by_tracker_id($tracker_id);
            }
        }
    }

    if(isset($_POST["type"])){
        if($_POST["type"] == "u_i_u"){ # user image upload
            $user_name = $_POST["u_n"];

            $target_dir = "img/";
            $target_file = $target_dir . basename($_FILES["img"]["name"]);
            $imageFileType = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));
            $upload_path = $target_dir . $user_name . "." . $imageFileType;
            
            $check = getimagesize($_FILES["img"]["tmp_name"]);
            if($check != false) {
                // Check file size
                if ($_FILES["img"]["size"] > 8000000) {
                    echo "Sorry, your file is too large.";
                }
                else{
                    // Allow certain file formats
                    if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
                    && $imageFileType != "gif" ) {
                      echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.";
                    }
                    else{
                        // Try to upload file
                        if (move_uploaded_file($_FILES["img"]["tmp_name"], $upload_path)){
                            echo "The file ". htmlspecialchars( basename( $_FILES["img"]["name"])). " has been uploaded.";

                            // Delete existing images with different file extensions
                            if(file_exists(__DIR__ . "/img/" . $user_name . ".jpg") && $imageFileType != "jpg"){
                                unlink(__DIR__ . "/img/" . $user_name . ".jpg");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . ".png") && $imageFileType != "png"){
                                unlink(__DIR__ . "/img/" . $user_name . ".png");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . ".jpeg") && $imageFileType != "jpeg"){
                                unlink(__DIR__ . "/img/" . $user_name . ".jpeg");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . ".gif") && $imageFileType != "gif"){
                                unlink(__DIR__ . "/img/" . $user_name . ".gif");
                            }

                            // Change image link in the database
                            $url = "https://pvp.seriouss.am/" . $upload_path;
                            $db->user_image_update($user_name, $url);
                        }
                        else{
                            echo "Sorry, there was an error uploading your file.";
                        }
                    }
                }
            } else {
                echo "File is not an image.";
            }
        }

        if($_POST["type"] == "p_i_u"){ # pet image upload
            $user_name = $_POST["u_n"];
            $id = $_POST["p_i"];

            $target_dir = "img/";
            $target_file = $target_dir . basename($_FILES["img"]["name"]);
            $imageFileType = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));
            $upload_path = $target_dir . $user_name . "-" . $id . "." . $imageFileType;
            
            $check = getimagesize($_FILES["img"]["tmp_name"]);
            if($check != false) {
                // Check file size
                if ($_FILES["img"]["size"] > 8000000) {
                    echo "Sorry, your file is too large.";
                }
                else{
                    // Allow certain file formats
                    if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
                    && $imageFileType != "gif" ) {
                      echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.";
                    }
                    else{
                        // Try to upload file
                        if (move_uploaded_file($_FILES["img"]["tmp_name"], $upload_path)){
                            echo "The file ". htmlspecialchars( basename( $_FILES["img"]["name"])). " has been uploaded.";

                            // Delete existing images with different file extensions
                            if(file_exists(__DIR__ . "/img/" . $user_name . "-" . $id . ".jpg") && $imageFileType != "jpg"){
                                unlink(__DIR__ . "/img/" . $id . ".jpg");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . "-" . $id . ".png") && $imageFileType != "png"){
                                unlink(__DIR__ . "/img/" . $id . ".png");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . "-" . $id . ".jpeg") && $imageFileType != "jpeg"){
                                unlink(__DIR__ . "/img/" . $id . ".jpeg");
                            }
                            else if(file_exists(__DIR__ . "/img/" . $user_name . "-" . $id . ".gif") && $imageFileType != "gif"){
                                unlink(__DIR__ . "/img/" . $id . ".gif");
                            }

                            // Change image link in the database
                            $url = "https://pvp.seriouss.am/" . $upload_path;
                            $db->pet_image_update($id, $url);
                        }
                        else{
                            echo "Sorry, there was an error uploading your file.";
                        }
                    }
                }
            } else {
                echo "File is not an image.";
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