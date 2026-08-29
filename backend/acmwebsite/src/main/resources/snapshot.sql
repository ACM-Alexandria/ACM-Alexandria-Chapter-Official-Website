-- MariaDB dump 10.19  Distrib 10.5.24-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: alexhosting_acm_db
-- ------------------------------------------------------
-- Server version	10.5.24-MariaDB-cll-lve

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `club`
--

DROP TABLE IF EXISTS `club`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `club` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `google_sheet_url` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `sheet_last_updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKobh7q4yqh38kicj65tm0wd4t4` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `club`
--

LOCK TABLES `club` WRITE;
/*!40000 ALTER TABLE `club` DISABLE KEYS */;
INSERT INTO `club` VALUES (1,'Empowering minds to grow through logic, challenge, and innovation in competitive programming.','https://docs.google.com/spreadsheets/d/1NKYhrXOC3EAz1rGIa4MYPl1pQcqPAeOKsRU1dxP2oHM/edit','https://res.cloudinary.com/ckraglw8/image/upload/v1787584784/czcupsqohin9bki2qzk4.jpg','CP Club','2026-08-28 16:23:45.000000'),(2,'Software Engineering Club\n\nThe Software Engineering Club is where ideas become real products. It is dedicated to building, maintaining, and improving the software projects developed by ACM Alexandria Student Chapter.\n\nThe club is divided into two main tracks:\n\n* Project Development: Dedicated teams work together to design, develop, and launch new ACM projects from the ground up, following real-world software engineering practices.\n* Open Source Contribution: Members contribute to our existing projects by fixing bugs, implementing new features, improving performance, and helping maintain the software used by our community.\n\nThis club provides a practical environment to collaborate with other developers, work with modern development workflows, and gain hands-on experience through real projects that serve the chapter.\n\nNote: The Software Engineering Club is an internal club, meaning only ACM Alexandria Student Chapter members are eligible to join.','https://docs.google.com/spreadsheets/d/108_bxu6ZCpPC3H2VWDW9pPQdYJ8BT1kOeo5MRWG5BAQ/edit','https://res.cloudinary.com/ckraglw8/image/upload/v1787584846/kdiotpxypuyfzuwove18.jpg','Software Engineering Club','2026-08-28 16:24:02.000000');
/*!40000 ALTER TABLE `club` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `club_form_question`
--

DROP TABLE IF EXISTS `club_form_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `club_form_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_required` bit(1) NOT NULL,
  `question_text` text NOT NULL,
  `question_type` enum('CHECKBOX','MULTIPLE_CHOICE','TEXT') NOT NULL,
  `club_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcxcnhxnpcad1kcpw293h4c4j3` (`club_id`),
  CONSTRAINT `FKcxcnhxnpcad1kcpw293h4c4j3` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `club_form_question`
--

LOCK TABLES `club_form_question` WRITE;
/*!40000 ALTER TABLE `club_form_question` DISABLE KEYS */;
INSERT INTO `club_form_question` VALUES (1,'','Codeforces Handle','TEXT',1),(2,'','Are you ACM Member? (Note this is internal club, you have to be ACM member to apply)','CHECKBOX',2),(3,'','What is you Committee?','MULTIPLE_CHOICE',2),(4,'','Your Github Username','TEXT',2);
/*!40000 ALTER TABLE `club_form_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `club_question_options`
--

DROP TABLE IF EXISTS `club_question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `club_question_options` (
  `question_id` bigint(20) NOT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  KEY `FKmkotyk7ff2w2p6u8jrlym8f8i` (`question_id`),
  CONSTRAINT `FKmkotyk7ff2w2p6u8jrlym8f8i` FOREIGN KEY (`question_id`) REFERENCES `club_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `club_question_options`
--

LOCK TABLES `club_question_options` WRITE;
/*!40000 ALTER TABLE `club_question_options` DISABLE KEYS */;
INSERT INTO `club_question_options` VALUES (3,'T&D'),(3,'Media'),(3,'OC'),(3,'PR'),(3,'HR'),(2,'Yes');
/*!40000 ALTER TABLE `club_question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `committee`
--

DROP TABLE IF EXISTS `committee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `committee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `is_open` bit(1) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `message_for_calls_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK18hvjag302ubq4cqdgswi348l` (`name`),
  UNIQUE KEY `UK6yem8yp4fh348n5v0ly1fr7kg` (`message_for_calls_id`),
  CONSTRAINT `FKntceptd8u7ktxls67o0rouepo` FOREIGN KEY (`message_for_calls_id`) REFERENCES `message` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `committee`
--

LOCK TABLES `committee` WRITE;
/*!40000 ALTER TABLE `committee` DISABLE KEYS */;
INSERT INTO `committee` VALUES (1,'Plans and executes engaging events, workshops, competitions, and social activities that create valuable experiences for members.','\0','https://res.cloudinary.com/ckraglw8/image/upload/v1787584253/gmqycfhnhmdfmic6qsph.jpg','Training & Development Committee',10),(2,'Ensures every event runs smoothly by managing logistics, coordinating teams, organizing event operations, and making sure every detail is planned and executed efficiently.','\0','https://res.cloudinary.com/ckraglw8/image/upload/v1787584675/oc0cyh1zhoccejrvdrbk.jpg','Organization Committee',11),(3,'Creates visual and digital content, including photography, videography, graphic design, and social media coverage to showcase the chapter’s activities.','','https://res.cloudinary.com/ckraglw8/image/upload/v1787584414/zqxp99mycaj9rxveeydn.jpg','Media Committee',12),(4,'Builds the chapter’s public image by managing partnerships, sponsorships, outreach, and communication with external organizations.','\0','https://res.cloudinary.com/ckraglw8/image/upload/v1787584449/gvziffaab25fnkz2b5d7.jpg','PR Committee ',22),(5,'Focuses on member recruitment, onboarding, team development, and fostering a supportive and collaborative chapter culture.','','https://res.cloudinary.com/ckraglw8/image/upload/v1787584633/gdwjoswzmcqglg9nrolw.jpg','HR Committee',24);
/*!40000 ALTER TABLE `committee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `committee_board`
--

DROP TABLE IF EXISTS `committee_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `committee_board` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) DEFAULT NULL,
  `linkedin_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `order` int(11) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `committee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7v3dwqyjiktr8t1d9bu6ctvj9` (`committee_id`),
  CONSTRAINT `FK7v3dwqyjiktr8t1d9bu6ctvj9` FOREIGN KEY (`committee_id`) REFERENCES `committee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `committee_board`
--

LOCK TABLES `committee_board` WRITE;
/*!40000 ALTER TABLE `committee_board` DISABLE KEYS */;
INSERT INTO `committee_board` VALUES (1,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576587/dutiqcrk6letfdedz8xh.jpg','https://www.linkedin.com/in/antoine-sobhy-716775308/','Antoine Sobhy',1,'Head',1),(2,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576652/wjzodqne3aurzwbfeypi.jpg','https://www.linkedin.com/in/roaa-mostafa-eliwa/','Roaa Mostafa',2,'Vice Head',1),(3,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576847/wlfqxtiashvm4astm43a.jpg','https://www.linkedin.com/in/ziad-abdelhaleem/','Ziad Abdelhaleem',1,'Head',2),(4,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576976/oxo4p5ggzllfhcd09r1n.jpg','https://www.linkedin.com/in/sama-yosri-70810a2b3/','Sama Yosri',2,'Vice Head',2),(5,'https://res.cloudinary.com/ckraglw8/image/upload/v1787577891/usdac77bn023pjrl1cav.jpg','https://www.linkedin.com/in/yousefwalid/','Yousef Walid',1,'Head',3),(6,'https://res.cloudinary.com/ckraglw8/image/upload/v1787581414/inongirsomqpblwpzv04.jpg','https://www.linkedin.com/in/lojine-sameh-39718a331/','Lojine Sameh',2,'Vice Head',3),(7,'https://res.cloudinary.com/ckraglw8/image/upload/v1787581547/bkftufouacpe8xravcgd.jpg','https://www.linkedin.com/in/abdelrahman-sakr-99853b396/','Abdelrahman Sakr',1,'Head',4),(8,'https://res.cloudinary.com/ckraglw8/image/upload/v1787581685/ixy9ss49t4daxapyjbp2.jpg','https://www.linkedin.com/in/alya-ahmed-413bba303/','Alya Ahmed',2,'Vice Head',4),(9,'https://res.cloudinary.com/ckraglw8/image/upload/v1787581815/vah6iup2iiaefwpzqsvl.jpg','https://www.linkedin.com/in/nada-mohamed-88076a352/','Nada Mohamed',1,'Head',5),(10,'https://res.cloudinary.com/ckraglw8/image/upload/v1787581994/wfoyprawhprxugb6pekk.jpg','https://www.linkedin.com/in/anas-ali-016323315/','Anas Ali',2,'Vice Head',5);
/*!40000 ALTER TABLE `committee_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `committee_call`
--

DROP TABLE IF EXISTS `committee_call`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `committee_call` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `closed_at` datetime(6) DEFAULT NULL,
  `google_sheet_url` varchar(255) DEFAULT NULL,
  `opened_at` datetime(6) NOT NULL,
  `sheet_last_updated_at` datetime(6) DEFAULT NULL,
  `committee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaqkjhm9hlpcp6dh5ws5riugax` (`committee_id`),
  CONSTRAINT `FKaqkjhm9hlpcp6dh5ws5riugax` FOREIGN KEY (`committee_id`) REFERENCES `committee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `committee_call`
--

LOCK TABLES `committee_call` WRITE;
/*!40000 ALTER TABLE `committee_call` DISABLE KEYS */;
INSERT INTO `committee_call` VALUES (2,'2026-07-25 17:19:50.000000','https://docs.google.com/spreadsheets/d/1sVTHLLG7UL7iZJ92kU8d7aiL1pieEsMgcul14z9SqRI/edit','2026-07-25 17:13:27.000000','2026-07-25 17:16:46.000000',1),(3,'2026-07-25 17:21:05.000000',NULL,'2026-07-25 17:19:52.000000',NULL,1),(4,'2026-07-31 23:02:59.000000','https://docs.google.com/spreadsheets/d/1ioniXlbtPTwXOq-9F5AfNT827_chmoky95PWWN72H0o/edit','2026-07-26 13:52:55.000000','2026-08-26 21:41:04.000000',1),(5,'2026-07-31 23:03:01.000000','https://docs.google.com/spreadsheets/d/1LCCKc4aFx7qt649RHxYUJu4ESaXtGe_sZkkKJMYrPAw/edit','2026-07-26 13:52:56.000000','2026-08-15 04:47:34.000000',2),(6,'2026-07-31 23:03:03.000000','https://docs.google.com/spreadsheets/d/1nClF3yG7nWb98-y8rXSpYnL3JsC5f3eXKYTWdWyVAZY/edit','2026-07-26 13:52:58.000000','2026-08-01 16:31:14.000000',3),(7,'2026-07-31 23:03:07.000000','https://docs.google.com/spreadsheets/d/1AHOrNu_BkZTfmvjkFcItI2Nf7evkMDMZGBSQ2VlVf2M/edit','2026-07-26 13:52:59.000000','2026-08-01 16:31:35.000000',4),(8,'2026-07-31 23:03:11.000000','https://docs.google.com/spreadsheets/d/1HSRpFWq5u2wTpqq8RfPDpD9shksahQwBGIp0PQg9FuU/edit','2026-07-26 13:53:00.000000','2026-08-01 16:31:42.000000',5),(9,'2026-08-24 14:59:31.000000',NULL,'2026-08-24 14:59:28.000000',NULL,3),(10,'2026-08-27 15:29:56.000000',NULL,'2026-08-27 15:29:52.000000',NULL,3),(11,NULL,'https://docs.google.com/spreadsheets/d/1bB3SeSkU7zA3D3qOBBeK7upEBexN2Ro6BzuCXZ71ULc/edit','2026-08-27 15:33:02.000000','2026-08-28 17:35:25.000000',3),(12,'2026-08-28 15:01:56.000000',NULL,'2026-08-28 15:01:51.000000',NULL,5),(13,'2026-08-28 15:03:03.000000',NULL,'2026-08-28 15:02:59.000000',NULL,5),(14,NULL,NULL,'2026-08-28 15:23:25.000000',NULL,5);
/*!40000 ALTER TABLE `committee_call` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `committee_form_question`
--

DROP TABLE IF EXISTS `committee_form_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `committee_form_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_required` bit(1) NOT NULL,
  `question_text` text NOT NULL,
  `question_type` enum('CHECKBOX','MULTIPLE_CHOICE','TEXT') NOT NULL,
  `committee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK32twwxpgaibkx1kko1lqs7w7h` (`committee_id`),
  CONSTRAINT `FK32twwxpgaibkx1kko1lqs7w7h` FOREIGN KEY (`committee_id`) REFERENCES `committee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `committee_form_question`
--

LOCK TABLES `committee_form_question` WRITE;
/*!40000 ALTER TABLE `committee_form_question` DISABLE KEYS */;
INSERT INTO `committee_form_question` VALUES (4,'','Which committee did you belong to during the previous season?','MULTIPLE_CHOICE',1),(5,'','Which committee did you belong to during the previous season?','MULTIPLE_CHOICE',2),(7,'','Which committee did you belong to during the previous season?','MULTIPLE_CHOICE',4),(9,'','Why are you joining the media committee?','MULTIPLE_CHOICE',3),(10,'\0','Link to your portfolio if exists.','TEXT',3),(11,'\0','Second Option Committee','MULTIPLE_CHOICE',3),(12,'','Where did you hear about ACM?','TEXT',3),(13,'','How did you hear about ACM?','TEXT',5),(14,'','Why do you want to join HR?','TEXT',5);
/*!40000 ALTER TABLE `committee_form_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `committee_question_options`
--

DROP TABLE IF EXISTS `committee_question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `committee_question_options` (
  `question_id` bigint(20) NOT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  KEY `FKc5jjpv0dlwxaet9djqbmq9inw` (`question_id`),
  CONSTRAINT `FKc5jjpv0dlwxaet9djqbmq9inw` FOREIGN KEY (`question_id`) REFERENCES `committee_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `committee_question_options`
--

LOCK TABLES `committee_question_options` WRITE;
/*!40000 ALTER TABLE `committee_question_options` DISABLE KEYS */;
INSERT INTO `committee_question_options` VALUES (4,'Activities'),(4,'OC'),(4,'HR'),(4,'PR'),(4,'Media'),(5,'Activities'),(5,'OC'),(5,'HR'),(5,'PR'),(5,'Media'),(7,'Activities'),(7,'OC'),(7,'HR'),(7,'PR'),(7,'Media'),(9,'Graphic Design'),(9,'Video Editing'),(9,'Photography/Videography'),(9,'Content Creation'),(11,'HR'),(11,'OC'),(11,'PR'),(11,'Training and Development');
/*!40000 ALTER TABLE `committee_question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `event_time` datetime(6) DEFAULT NULL,
  `google_sheet_url` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `sheet_last_updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmt8ulcc4k7fnc56rxaeu1sa33` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'SSP Building A4','Join us for an electrifying event as we roll out the red carpet and welcome the newest members of Computer and Systems Engineering Department (CSED)!\nHere\'s what you can expect:\nPower-Up Sessions: Dive into interactive orientation sessions led by our brilliant faculty, staff and senior students. They will guide you through the labyrinth of academic programs, unveiling the secrets of our cutting-edge curriculum and revealing the keys to unlocking your full potential. 🗝📚\nChoose Your Quest: Unearth the fantastic array of courses and specialized tracks available to you in the world of computer and systems engineering. With countless quests to embark upon, you\'ll discover the areas where your talents truly shine. 🔍\nGame On: Prepare for an epic networking experience! Engage in thrilling conversations, team up with current students, and forge connections that will level up your university life. You\'ll be part of a vibrant community where collaboration and camaraderie reign supreme. 💬\nRemember, this is just the beginning of your extraordinary adventure at CSED. LEVEL UP will guide you through the first steps of your journey, ensuring a smooth transition and providing you with the support you need to thrive.\nSo, mark your calendars, charge up your enthusiasm, and get ready to LEVEL UP! We can\'t wait to welcome you into our extraordinary CSED family. 🎓💻','2025-09-10 00:00:00.000000',NULL,'https://res.cloudinary.com/ckraglw8/image/upload/v1787587360/hq10jgqfexo3021u9ilu.jpg','Level Up \'25',NULL),(2,'Creetiva- ITI','Attention CSED & CCE ! 📢\nEver wished someone could tell you how they handled a tricky situation before you face it yourself?\nNeed advice on a specific course, an upcoming internship, or just curious about different career paths ?\nThat’s exactly why We are excited to announce ACM Exchange ! 🎉\nA space where all your questions, whether about courses, internships, or future fields. Get real answers from people who’ve been in your shoes and are ready to share their experience.\n','2025-10-02 10:00:00.000000',NULL,'https://res.cloudinary.com/ckraglw8/image/upload/v1787586113/u0gz4teng2bpkwvfexkr.jpg','ACM Exchange',NULL),(3,'Wajan Workingspace','🚀🌌 Future youth… are you ready to explore CS Week? 🌌🚀\nPrepare to launch into a world where Computer Engineering comes to life.\nCS Week is a journey designed for future engineers who build systems, shape technology, and push boundaries, guided by the adventurous spirit we grew up with.\nThis is not just talks and games.\nIt’s an exploration of how engineering thinking meets the future.\n🔧 What Awaits You\n🗄️ Databases\nDive into the core of system design, where data, structure, and problem-solving come together through interactive challenges.\n🤖 Artificial Intelligence: Threat or Tool?\nAn honest discussion about AI in the engineering world, will it replace us, or amplify what we build?\n🧠 AI Agents\nExplore how intelligent agents work, communicate, and make decisions, and why they matter to modern software and systems engineering.\n🛰️ Git\nLearn how real engineers manage code, teams, and large-scale projects using version control.\n☁️ Cloud Computing\nLearn how modern applications are deployed, scaled, and managed in the cloud, and why every engineer needs this skill.\n🎮 Games & Challenges\nBecause every mission needs strategy, teamwork, and a bit of fun.\n📣 Stay Tuned for Updates!\n- We’ll soon reveal:\n- Venue details\n- Event schedules\n- Distinguished speakers\n📌 Mark your calendars and keep an eye out for announcements!\nReady to be part of this unforgettable journey?','2026-02-09 09:00:00.000000',NULL,'https://res.cloudinary.com/ckraglw8/image/upload/v1787586582/ohbgczooje5m5poqfxst.jpg','CS Week - Sixth Edition',NULL),(4,'SSP Building A4 ','Every great graduation project starts with the right direction. 🎓\nIf you’re about to begin your journey, this is your chance to understand what really matters, from choosing the right idea, to building, managing, and delivering a project you’ll actually be proud of.','2026-04-30 08:30:00.000000',NULL,'https://res.cloudinary.com/ckraglw8/image/upload/v1787586809/frvfrdtssqiyejpzbso7.jpg','Graduation Project \'26 Info Session',NULL);
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_form_question`
--

DROP TABLE IF EXISTS `event_form_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_form_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_required` bit(1) NOT NULL,
  `question_text` text NOT NULL,
  `question_type` enum('CHECKBOX','MULTIPLE_CHOICE','TEXT') NOT NULL,
  `event_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKxtvjh3c198i4ms2f0it1962n` (`event_id`),
  CONSTRAINT `FKxtvjh3c198i4ms2f0it1962n` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_form_question`
--

LOCK TABLES `event_form_question` WRITE;
/*!40000 ALTER TABLE `event_form_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_form_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_question_options`
--

DROP TABLE IF EXISTS `event_question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_question_options` (
  `question_id` bigint(20) NOT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  KEY `FKkehf1f37augy12u13dvnlnku0` (`question_id`),
  CONSTRAINT `FKkehf1f37augy12u13dvnlnku0` FOREIGN KEY (`question_id`) REFERENCES `event_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_question_options`
--

LOCK TABLES `event_question_options` WRITE;
/*!40000 ALTER TABLE `event_question_options` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gallery_image`
--

DROP TABLE IF EXISTS `gallery_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gallery_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `caption` varchar(255) DEFAULT NULL,
  `image_url` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gallery_image`
--

LOCK TABLES `gallery_image` WRITE;
/*!40000 ALTER TABLE `gallery_image` DISABLE KEYS */;
INSERT INTO `gallery_image` VALUES (1,'','https://res.cloudinary.com/ckraglw8/image/upload/v1787587986/yuc2o2lgrqs2uutk6ern.jpg'),(2,'','https://res.cloudinary.com/ckraglw8/image/upload/v1787588224/bo3hxq4dcbcifpbllqcr.jpg'),(3,'','https://res.cloudinary.com/ckraglw8/image/upload/v1787588362/zk9mry5eawtpkzzhnewx.jpg'),(4,'','https://res.cloudinary.com/ckraglw8/image/upload/v1787588511/th2dd8tybgkvdreaco7q.jpg');
/*!40000 ALTER TABLE `gallery_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `high_board`
--

DROP TABLE IF EXISTS `high_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `high_board` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) DEFAULT NULL,
  `linkedin_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `order` int(11) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `high_board`
--

LOCK TABLES `high_board` WRITE;
/*!40000 ALTER TABLE `high_board` DISABLE KEYS */;
INSERT INTO `high_board` VALUES (1,'https://res.cloudinary.com/ckraglw8/image/upload/v1787575824/scqtwowiuzusvnbdimfh.jpg','https://www.linkedin.com/in/michaelmagdy23','Michael Magdy',1,'Chairman'),(2,'https://res.cloudinary.com/ckraglw8/image/upload/v1787575973/oq2rrodgonvbgjxzuavv.jpg','https://www.linkedin.com/in/mohammed-atef-70270a24b','Mohamed Atef',2,'Vice-Chairman'),(3,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576035/xzjlbxucgzmaww9vuhwg.jpg','https://www.linkedin.com/in/nada-gamal-b85b4923b','Nada Gamal',3,'Secretary'),(4,'https://res.cloudinary.com/ckraglw8/image/upload/v1787576128/rkavtulrkupqwsiqync7.jpg','https://www.linkedin.com/in/mohamed-moeen-9a379930a/','Mohamed Moeen',4,'Treasurer');
/*!40000 ALTER TABLE `high_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `body` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,'This is a Committe call','2026-07-22 13:34:34.000000','Committe Call '),(2,'This is call for activities committee','2026-07-25 17:12:53.000000','Activities Committee Call'),(3,'Ready to learn, grow, and make an impact? Join T&D Committee and be part of an amazing team.','2026-07-25 17:52:59.000000','T&D Committee Call'),(4,'Ready to learn, grow, and make an impact? Join T&D Committee and be part of an amazing team.','2026-07-25 17:53:09.000000','T&D Committee Call'),(5,'Ready to learn, grow, and make an impact? Join T&D Committee and be part of an amazing team.','2026-07-25 17:53:30.000000','T&D Committee Call'),(6,'Ready to learn, grow, and make an impact? Join OC Committee and be part of an amazing team.','2026-07-25 17:53:52.000000','OC Committee Call'),(7,'Ready to learn, grow, and make an impact? Join Media Committee and be part of an amazing team.','2026-07-25 17:54:11.000000','Media Committee Call'),(8,'Ready to learn, grow, and make an impact? Join PR Committee and be part of an amazing team.','2026-07-25 17:54:32.000000','PR Committee Call'),(9,'Ready to learn, grow, and make an impact? Join HR Committee and be part of an amazing team.','2026-07-25 17:55:27.000000','HR Committee Call'),(10,'Ready to learn, grow, and make an impact? Join T&D Committee and be part of an amazing team.','2026-07-26 11:26:17.000000','T&D Committee Call'),(11,'Ready to learn, grow, and make an impact? Join OC Committee and be part of an amazing team.','2026-07-26 11:27:07.000000','OC Committee Call'),(12,'Ready to learn, grow, and make an impact? Join Media Committee and be part of an amazing team.','2026-07-26 11:27:21.000000','Media Committee Call'),(13,'Ready to learn, grow, and make an impact? Join PR Committee and be part of an amazing team.','2026-07-26 11:27:38.000000','PR Committee Call'),(14,'Ready to learn, grow, and make an impact? Join HR Committee and be part of an amazing team.','2026-07-26 11:27:49.000000','HR Committee Call'),(21,'Ready to learn, grow, and make an impact? Join PR Committee and be part of an amazing team','2026-08-24 14:59:12.000000','PR Committee Call'),(22,'Ready to learn, grow, and make an impact? Join PR Committee and be part of an amazing team.','2026-08-24 14:59:17.000000','PR Committee Call'),(23,'Ready to learn, grow, and make an impact? Join HR Committee and be part of an amazing team.','2026-08-28 15:02:43.000000','HR Committee Call'),(24,'Ready to empower others and build a stronger chapter? Join the HR Committee and shape our success!','2026-08-28 15:16:55.000000','HR Committee Call');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `partner`
--

DROP TABLE IF EXISTS `partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `partner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKa7jsvq2r4k841xht6cos347uc` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `partner`
--

LOCK TABLES `partner` WRITE;
/*!40000 ALTER TABLE `partner` DISABLE KEYS */;
INSERT INTO `partner` VALUES (1,'https://res.cloudinary.com/ckraglw8/image/upload/v1787571930/ylebfrh5c72og6k5an6w.webp','Techne','https://technesummit.com/2026');
/*!40000 ALTER TABLE `partner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program`
--

DROP TABLE IF EXISTS `program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `google_sheet_url` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `registration_open` bit(1) NOT NULL,
  `sheet_last_updated_at` datetime(6) DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKha1ojetw3fv9tfdrrvfy99yuf` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program`
--

LOCK TABLES `program` WRITE;
/*!40000 ALTER TABLE `program` DISABLE KEYS */;
INSERT INTO `program` VALUES (1,'Ready to sharpen your problem-solving skills and dive into the world of Competitive Programming?\nJoin ACM Alexandria\'s Competitive Programming Summer Training, where you\'ll learn the fundamentals of algorithms and data structures, practice solving real programming challenges, and build the mindset needed for programming contests and technical interviews.\nWhether you\'re just getting started or already have some experience, our training is designed with two levels to help you progress at your own pace.\nWhat to expect\n💡 Interactive sessions \n🏆 A reward system to keep you motivated\n🧩 Weekly problem-solving practice\n🤝 A supportive community to learn and grow with\n','2026-09-05 10:00:00.000000',NULL,'https://res.cloudinary.com/ckraglw8/image/upload/v1787587825/kckxgyvzayzj9bhsgjef.jpg','CP Summer Training \'26','\0',NULL,'2026-07-04 07:00:00.000000','Every Saturday 1:00 PM');
/*!40000 ALTER TABLE `program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program_form_question`
--

DROP TABLE IF EXISTS `program_form_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program_form_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_required` bit(1) NOT NULL,
  `question_text` text NOT NULL,
  `question_type` enum('CHECKBOX','MULTIPLE_CHOICE','TEXT') NOT NULL,
  `program_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsbwmwkw6b360ggl3gtbom4bi` (`program_id`),
  CONSTRAINT `FKsbwmwkw6b360ggl3gtbom4bi` FOREIGN KEY (`program_id`) REFERENCES `program` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program_form_question`
--

LOCK TABLES `program_form_question` WRITE;
/*!40000 ALTER TABLE `program_form_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `program_form_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program_question_options`
--

DROP TABLE IF EXISTS `program_question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program_question_options` (
  `question_id` bigint(20) NOT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  KEY `FKgqrm88w7exlt3lsxog2vwccn7` (`question_id`),
  CONSTRAINT `FKgqrm88w7exlt3lsxog2vwccn7` FOREIGN KEY (`question_id`) REFERENCES `program_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program_question_options`
--

LOCK TABLES `program_question_options` WRITE;
/*!40000 ALTER TABLE `program_question_options` DISABLE KEYS */;
/*!40000 ALTER TABLE `program_question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `radio_episode`
--

DROP TABLE IF EXISTS `radio_episode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `radio_episode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `episode_number` int(11) NOT NULL,
  `guest` varchar(255) DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `radio_season_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbpy8q3nha14ex56ihh505o0pg` (`radio_season_id`),
  CONSTRAINT `FKbpy8q3nha14ex56ihh505o0pg` FOREIGN KEY (`radio_season_id`) REFERENCES `radio_season` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radio_episode`
--

LOCK TABLES `radio_episode` WRITE;
/*!40000 ALTER TABLE `radio_episode` DISABLE KEYS */;
INSERT INTO `radio_episode` VALUES (1,1,'Essam Fahmy','Veronica','https://res.cloudinary.com/ckraglw8/image/upload/v1784752096/zu9m1uzihxaf2cdaykpg.png','A CS grad\'s rise to a Software Archetict','https://www.facebook.com/photo.php?fbid=799684245536374&set=pb.100064844162335.-2207520000&type=3',1),(2,2,'Abdelrahman Abo Samra','Ahmed Elsaeed','https://res.cloudinary.com/ckraglw8/image/upload/v1784752112/smympm3g0wwqvwprel4b.png','A peek into world of DevOps','https://www.facebook.com/photo.php?fbid=804236235081175&set=pb.100064844162335.-2207520000&type=3',1),(3,3,'Abdelrahman Elbakri','Meniem Hany','https://res.cloudinary.com/ckraglw8/image/upload/v1784752125/cqwclrazngtavjqj1gle.png','A peek into Security Engineering','https://www.facebook.com/photo?fbid=819157370255728&set=pb.100064844162335.-2207520000',1),(4,4,'Mahmoud Gamal','Ahmed Adel','https://res.cloudinary.com/ckraglw8/image/upload/v1784752136/rkqobl5yuf9iu5wtus65.png','How to survive as Softwate Engineer in the Army','https://www.facebook.com/photo?fbid=826552072849591&set=pb.100064844162335.-2207520000',1),(5,1,'Hamdy Khalil','Abdallah Adel','https://res.cloudinary.com/ckraglw8/image/upload/v1784753454/mlibnikrs3zaaiwjt1yh.png','Security Engineering','https://www.facebook.com/photo.php?fbid=940503921454405&set=pb.100064844162335.-2207520000&type=3',2),(6,2,'Amr Elhelw','Mohamed Sherif','https://res.cloudinary.com/ckraglw8/image/upload/v1784753724/cigttydxdy2bjqcdlvwj.png','Software Mastery','https://www.facebook.com/photo.php?fbid=945470384291092&set=pb.100064844162335.-2207520000&type=3',2),(7,3,'Mostafa Galal','Walid Ammar','https://res.cloudinary.com/ckraglw8/image/upload/v1784753776/jtgka1ym0jbgdcewteht.png','Intelegent Horizons','https://www.facebook.com/photo?fbid=955987299906067&set=pb.100064844162335.-2207520000',2),(8,4,'Nermeen Abbas','Mohamed Sherif','https://res.cloudinary.com/ckraglw8/image/upload/v1784753832/b7hne3jgedm7nhupre9l.png','Empowering Talent','https://www.facebook.com/photo?fbid=997814002390063&set=pb.100064844162335.-2207520000',2),(9,5,'Eman Diab','Mariam Gerges','https://res.cloudinary.com/ckraglw8/image/upload/v1784753908/ufonfqozqhoie6dcsysv.png','The Hidden Challenge','https://www.facebook.com/photo?fbid=1041167594721370&set=pb.100064844162335.-2207520000',2),(10,6,'Mostafa Youssef','Mohamed Sherif','https://res.cloudinary.com/ckraglw8/image/upload/v1784753961/enzzcndjq6jdq63yevnr.png','Research Field','https://www.facebook.com/photo.php?fbid=1088031123368350&set=pb.100064844162335.-2207520000&type=3',2),(11,1,'Hatem Elattar','Mazen Azhary','https://res.cloudinary.com/ckraglw8/image/upload/v1784819274/pkxl4ceosykstfqhnfrc.avif','What is the Trend?','https://www.youtube.com/live/ZxLuBA_OI1s?si=k5dztdyCQccaWOzp',3),(12,2,'Ahmed Elemam','Malak Asaal','https://res.cloudinary.com/ckraglw8/image/upload/v1784819388/axzzb3beckampdb2gjaw.jpg','Software Everywhere','https://www.youtube.com/live/B4Hq8Xge3wk?si=3NI3SvmnE5ukau_p',3),(13,3,'Amr Magdy','Abdelrahman Sakr','https://res.cloudinary.com/ckraglw8/image/upload/v1784819628/qo36tckykxtfhqxqqwjy.jpg','Quantum in queue','https://www.youtube.com/live/7HfcScV2Cos?si=GEk6RV2WVGpk5N4s',3),(14,4,'Roberto Joseph','Malak Assal','https://res.cloudinary.com/ckraglw8/image/upload/v1784819696/q3s9noslxspohp2nlxra.jpg','Did you say Mobile?','https://www.youtube.com/live/XBf8y37rh8Y?si=omiLZLWMz70xTKgf',3),(15,5,'Youssef Elmasry','Malak Assal','https://res.cloudinary.com/ckraglw8/image/upload/v1784819809/miiq51nw48x8dgv0plda.jpg','Behind the Backdoors','https://www.youtube.com/live/fHaoaqBB4k0?si=_B7p5dZ4jnQESoD5',3),(16,6,'Pola Kamel','Nour El Atawy','https://res.cloudinary.com/ckraglw8/image/upload/v1784819939/dx2zeuvl5cpaledfli1w.avif','Press Start','https://www.youtube.com/live/2FvI4iYi-oY?si=9j-xqZ2cjBZD94yM',3),(17,7,'Eslam Adham','Abdelrahman Sakr','https://res.cloudinary.com/ckraglw8/image/upload/v1784820133/ux6fmj9coasuq0cijskk.jpg','Through the Wires','https://www.youtube.com/live/Nkpcf8wmn_s?si=dzwqSwCbBjIjpg8J',3),(18,8,'Abdelrahman Eissa','Malak Assal','https://res.cloudinary.com/ckraglw8/image/upload/v1784820246/fvqdhh9z5ggleyoczgzp.jpg','A whale in the clouds','https://www.youtube.com/live/TWa8cwUKlYo?si=FD4QDEl0_OMXC7sZ',3),(19,9,'Mohamed Ebdelhamid','Malak Assal','https://res.cloudinary.com/ckraglw8/image/upload/v1784820316/sjovdijrfybqd4h4bp3i.jpg','From idea to impact','https://www.youtube.com/live/TYrNFGCnsIY?si=idUDZmkPu87r5Pbv',3),(20,10,'Ahmed Khaled','Abdelrahman Sakr','https://res.cloudinary.com/ckraglw8/image/upload/v1784829643/jtzf3fitjdtqnmpxj1pu.jpg','Vitae on the fly','https://www.youtube.com/live/iL5G5naxrZ4?si=jnhTag6CFU1m6vLh',3),(21,12,'Ahmed Elsaeed','Nour Al Atawy','https://res.cloudinary.com/ckraglw8/image/upload/v1784829779/hhg8v610iifxhx1posog.jpg','Continous Delivary','https://www.youtube.com/live/AwYhjAqdEyw?si=xY4TYIGWGD-R2Gjq',3),(22,13,'Amr Elhelw','Youssef Al Hazzawi','https://res.cloudinary.com/ckraglw8/image/upload/v1784829878/blrbyt2mvm1mzvp4ts9n.jpg','The Vault','https://www.youtube.com/live/X9eea82CUDI?si=Jx5ZfC64doMgpiUv',3),(23,14,'Amr Ahmed','Mohamed Mahfouz','https://res.cloudinary.com/ckraglw8/image/upload/v1784830415/clgskncsxcolojibxmxi.jpg','Bullseye','https://www.youtube.com/live/9bAztLra9C8?si=Ln-1k7GsRNLsDe_v',3),(24,15,'Heba Elayoty','Mohamed Atef','https://res.cloudinary.com/ckraglw8/image/upload/v1784830497/bmu0cp8jens2kkbioe6p.jpg','Captain Kube!','https://www.youtube.com/live/R3_7ad3Usnc?si=i0Q_wm94PooPw6gQ',3),(25,16,'Abdelaleem Ahmed','Mohamed Moeen','https://res.cloudinary.com/ckraglw8/image/upload/v1784830597/jtzwfjzlq2bbcwtfrmik.jpg','Accepted','https://www.youtube.com/live/5vZthcoawOw?si=Fm-m-MaXH-cpJhi9',3);
/*!40000 ALTER TABLE `radio_episode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `radio_season`
--

DROP TABLE IF EXISTS `radio_season`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `radio_season` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `season_number` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfbbh9saa434gym32yss3h81je` (`season_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `radio_season`
--

LOCK TABLES `radio_season` WRITE;
/*!40000 ALTER TABLE `radio_season` DISABLE KEYS */;
INSERT INTO `radio_season` VALUES (1,'2026-07-22 16:13:47.000000','https://res.cloudinary.com/ckraglw8/image/upload/v1784751224/kuz2lmfnqxm5teuj6xkh.jpg',1),(2,'2026-07-22 16:46:52.000000','https://res.cloudinary.com/ckraglw8/image/upload/v1784753210/ce2uajaqyj0sufskclpz.jpg',2),(3,'2026-07-23 11:06:14.000000','https://res.cloudinary.com/ckraglw8/image/upload/v1784819172/gupbbe2pkkqqppjokznr.jpg',3);
/*!40000 ALTER TABLE `radio_season` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_link`
--

DROP TABLE IF EXISTS `social_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `social_link` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `platform` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_link`
--

LOCK TABLES `social_link` WRITE;
/*!40000 ALTER TABLE `social_link` DISABLE KEYS */;
INSERT INTO `social_link` VALUES (1,'Facebook','https://www.facebook.com/acm.alexandria'),(2,'Instagram','https://www.instagram.com/acm.alexandria/'),(3,'LinkedIn','https://www.linkedin.com/company/alexacm/'),(4,'X','https://x.com/acm_alexandria');
/*!40000 ALTER TABLE `social_link` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-29  5:50:27
