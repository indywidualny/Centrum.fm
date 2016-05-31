Server
==============

cron jobs
--------------

    0 */6 * * * /home/user/indywidualni.org/centrum/scripts/update_schedule.sh
    * * * * * /home/user/indywidualni.org/centrum/scripts/update_rds.sh

Songs API
--------------

    pip install --user PyMySQL
    chmod +x songs.py
    
/home/user/secret/connection.py
--------------

    #!/usr/bin/python
    
    
    def credentials():
        return 'localhost', 'user', 'password', 'database', 'utf8mb4'

Database for Songs API
--------------

    -- phpMyAdmin SQL Dump
    -- version 4.6.0
    -- http://www.phpmyadmin.net
    --
    -- Generation Time: May 23, 2016 at 11:23 PM
    -- Server version: 5.6.25-log
    -- PHP Version: 7.0.6

    SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
    SET time_zone = "+00:00";

    -- --------------------------------------------------------

    --
    -- Table structure for table `centrum_artists`
    --

    CREATE TABLE `centrum_artists` (
    `id` int(11) NOT NULL,
    `name` varchar(100) COLLATE utf8_bin NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

    -- --------------------------------------------------------

    --
    -- Table structure for table `centrum_log`
    --

    CREATE TABLE `centrum_log` (
    `id` int(11) NOT NULL,
    `played` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `track` int(11) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

    -- --------------------------------------------------------

    --
    -- Table structure for table `centrum_tracks`
    --

    CREATE TABLE `centrum_tracks` (
    `id` int(11) NOT NULL,
    `title` varchar(100) COLLATE utf8_bin NOT NULL,
    `duration` varchar(6) COLLATE utf8_bin DEFAULT NULL,
    `artist` int(11) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

    --
    -- Indexes for table `centrum_artists`
    --
    ALTER TABLE `centrum_artists`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `name` (`name`);

    --
    -- Indexes for table `centrum_log`
    --
    ALTER TABLE `centrum_log`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `played` (`played`),
    ADD KEY `track` (`track`);

    --
    -- Indexes for table `centrum_tracks`
    --
    ALTER TABLE `centrum_tracks`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `unique_index` (`title`,`duration`,`artist`),
    ADD KEY `artist` (`artist`);

    --
    -- AUTO_INCREMENT for table `centrum_artists`
    --
    ALTER TABLE `centrum_artists`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
    --
    -- AUTO_INCREMENT for table `centrum_log`
    --
    ALTER TABLE `centrum_log`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
    --
    -- AUTO_INCREMENT for table `centrum_tracks`
    --
    ALTER TABLE `centrum_tracks`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

    --
    -- Constraints for table `centrum_log`
    --
    ALTER TABLE `centrum_log`
    ADD CONSTRAINT `song` FOREIGN KEY (`track`) REFERENCES `centrum_tracks` (`id`) ON DELETE CASCADE;

    --
    -- Constraints for table `centrum_tracks`
    --
    ALTER TABLE `centrum_tracks`
    ADD CONSTRAINT `performer` FOREIGN KEY (`artist`) REFERENCES `centrum_artists` (`id`) ON DELETE CASCADE;

    --
    -- View `centrum_songs`
    --
    CREATE VIEW centrum_songs
    AS
    SELECT  T.id id,
            T.title title,
            T.duration duration,
            A.name artist
    FROM    centrum_tracks T
            INNER JOIN centrum_artists A
                ON T.artist = A.id

    --
    -- View `centrum_history`
    --
    CREATE VIEW centrum_history
    AS
    SELECT  L.id id,
            L.played played,
            S.title title,
            S.artist artist,
            S.duration duration
    FROM    centrum_log L
            INNER JOIN centrum_songs S
                ON L.track = S.id

Acknowledgements
--------------

PHP scripts by CzarodziejskiMirek
