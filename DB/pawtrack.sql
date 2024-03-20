-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 20, 2024 at 09:53 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pawtrack`
--

-- --------------------------------------------------------

--
-- Table structure for table `activity`
--

CREATE TABLE `activity` (
  `id_Activity` int(11) NOT NULL,
  `name` char(13) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activity`
--

INSERT INTO `activity` (`id_Activity`, `name`) VALUES
(1, 'Very active'),
(2, 'Active'),
(3, 'Normal'),
(4, 'Inactive'),
(5, 'Very inactive');

-- --------------------------------------------------------

--
-- Table structure for table `activity_report`
--

CREATE TABLE `activity_report` (
  `id` varchar(255) NOT NULL,
  `date` datetime NOT NULL,
  `distance_walked` float NOT NULL,
  `steps_walked` int(11) NOT NULL,
  `calories_burned` int(11) NOT NULL,
  `active_time` time NOT NULL,
  `mood` int(11) DEFAULT NULL,
  `fk_Petid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `diet`
--

CREATE TABLE `diet` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `fk_Petid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `gps_data`
--

CREATE TABLE `gps_data` (
  `id` varchar(255) NOT NULL,
  `time` datetime NOT NULL,
  `lat` float NOT NULL,
  `long` float NOT NULL,
  `fk_Activity_Reportid` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `language`
--

CREATE TABLE `language` (
  `id_Language` int(11) NOT NULL,
  `name` char(8) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `language`
--

INSERT INTO `language` (`id_Language`, `name`) VALUES
(1, 'LT'),
(2, 'EN');

-- --------------------------------------------------------

--
-- Table structure for table `mood`
--

CREATE TABLE `mood` (
  `id_Mood` int(11) NOT NULL,
  `name` char(7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mood`
--

INSERT INTO `mood` (`id_Mood`, `name`) VALUES
(1, 'Excited'),
(2, 'Casual'),
(3, 'Tired'),
(4, 'Angry'),
(5, 'Unwell');

-- --------------------------------------------------------

--
-- Table structure for table `pet`
--

CREATE TABLE `pet` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `pet_picture` mediumblob DEFAULT NULL,
  `track_id` int(11) DEFAULT NULL,
  `track_status` tinyint(1) NOT NULL,
  `activity_category` int(11) NOT NULL,
  `fk_Userusername` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reminder`
--

CREATE TABLE `reminder` (
  `id` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `time` datetime NOT NULL,
  `fk_Petid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `settings`
--

CREATE TABLE `settings` (
  `id` varchar(255) NOT NULL,
  `dark_mode` tinyint(1) NOT NULL,
  `language` int(11) NOT NULL,
  `fk_Userusername` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `profile_picture` mediumblob DEFAULT NULL,
  `subscribed` tinyint(1) NOT NULL,
  `premium_expiration` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activity`
--
ALTER TABLE `activity`
  ADD PRIMARY KEY (`id_Activity`);

--
-- Indexes for table `activity_report`
--
ALTER TABLE `activity_report`
  ADD PRIMARY KEY (`id`),
  ADD KEY `mood` (`mood`),
  ADD KEY `Creates` (`fk_Petid`);

--
-- Indexes for table `diet`
--
ALTER TABLE `diet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `Assinged` (`fk_Petid`);

--
-- Indexes for table `gps_data`
--
ALTER TABLE `gps_data`
  ADD PRIMARY KEY (`id`),
  ADD KEY `Includes` (`fk_Activity_Reportid`);

--
-- Indexes for table `language`
--
ALTER TABLE `language`
  ADD PRIMARY KEY (`id_Language`);

--
-- Indexes for table `mood`
--
ALTER TABLE `mood`
  ADD PRIMARY KEY (`id_Mood`);

--
-- Indexes for table `pet`
--
ALTER TABLE `pet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `activity_category` (`activity_category`),
  ADD KEY `Has` (`fk_Userusername`);

--
-- Indexes for table `reminder`
--
ALTER TABLE `reminder`
  ADD PRIMARY KEY (`id`),
  ADD KEY `Puts` (`fk_Petid`);

--
-- Indexes for table `settings`
--
ALTER TABLE `settings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `fk_Userusername` (`fk_Userusername`),
  ADD KEY `language` (`language`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`username`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity_report`
--
ALTER TABLE `activity_report`
  ADD CONSTRAINT `Creates` FOREIGN KEY (`fk_Petid`) REFERENCES `pet` (`id`),
  ADD CONSTRAINT `activity_report_ibfk_1` FOREIGN KEY (`mood`) REFERENCES `mood` (`id_Mood`);

--
-- Constraints for table `diet`
--
ALTER TABLE `diet`
  ADD CONSTRAINT `Assinged` FOREIGN KEY (`fk_Petid`) REFERENCES `pet` (`id`);

--
-- Constraints for table `gps_data`
--
ALTER TABLE `gps_data`
  ADD CONSTRAINT `Includes` FOREIGN KEY (`fk_Activity_Reportid`) REFERENCES `activity_report` (`id`);

--
-- Constraints for table `pet`
--
ALTER TABLE `pet`
  ADD CONSTRAINT `Has` FOREIGN KEY (`fk_Userusername`) REFERENCES `user` (`username`),
  ADD CONSTRAINT `pet_ibfk_1` FOREIGN KEY (`activity_category`) REFERENCES `activity` (`id_Activity`);

--
-- Constraints for table `reminder`
--
ALTER TABLE `reminder`
  ADD CONSTRAINT `Puts` FOREIGN KEY (`fk_Petid`) REFERENCES `pet` (`id`);

--
-- Constraints for table `settings`
--
ALTER TABLE `settings`
  ADD CONSTRAINT `Sets` FOREIGN KEY (`fk_Userusername`) REFERENCES `user` (`username`),
  ADD CONSTRAINT `settings_ibfk_1` FOREIGN KEY (`language`) REFERENCES `language` (`id_Language`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
