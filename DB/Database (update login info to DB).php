<?php

class Database {
    private $host = "localhost";
    private $username = "your_username";
    private $password = "your_password";
    private $database = "pawtrack";
    private $connection;

    // Constructor
    public function __construct() {
        try {
            $dsn = "mysql:host={$this->host};dbname={$this->database}";
            $this->connection = new PDO($dsn, $this->username, $this->password);
            $this->connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch (PDOException $e) {
            die("Connection failed: " . $e->getMessage());
        }
    }

    // Close connection
    public function __destruct() {
        $this->connection = null;
    }

    // Execute SQL query
    public function query($sql, $params = []) {
        try {
            $statement = $this->connection->prepare($sql);
            $statement->execute($params);
            return $statement;
        } catch (PDOException $e) {
            die("Query failed: " . $e->getMessage());
        }
    }

    // Fetch single record
    public function fetch($sql, $params = []) {
        $statement = $this->query($sql, $params);
        return $statement->fetch(PDO::FETCH_ASSOC);
    }

    // Fetch all records
    public function fetchAll($sql, $params = []) {
        $statement = $this->query($sql, $params);
        return $statement->fetchAll(PDO::FETCH_ASSOC);
    }

    // Insert data
    public function insert($table, $data) {
        $columns = implode(", ", array_keys($data));
        $values = ":" . implode(", :", array_keys($data));
        $sql = "INSERT INTO $table ($columns) VALUES ($values)";
        $this->query($sql, $data);
    }

    // Update data
    public function update($table, $data, $condition) {
        $set = "";
        foreach ($data as $key => $value) {
            $set .= "$key=:$key, ";
        }
        $set = rtrim($set, ", ");
        $sql = "UPDATE $table SET $set WHERE $condition";
        $this->query($sql, $data);
    }

    // Delete data
    public function delete($table, $condition, $params = []) {
        $sql = "DELETE FROM $table WHERE $condition";
        $this->query($sql, $params);
    }
}

// Example usage:
$db = new Database();

// Fetch all users
$users = $db->fetchAll("SELECT * FROM user");

// Insert a new user
$newUser = array(
    "username" => "john_doe",
    "password" => "password123",
    "email" => "john@example.com",
    "premium" => 1,
    "role" => 1
);
$db->insert("user", $newUser);

// Update user's email
$updateData = array("email" => "new_email@example.com");
$db->update("user", $updateData, "username='john_doe'");

// Delete user
$db->delete("user", "username=:username", array("username" => "john_doe"));
?>
