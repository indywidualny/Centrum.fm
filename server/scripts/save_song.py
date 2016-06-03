#!/usr/bin/python

import sys
sys.path.append('/home/user/secret')
from connection import credentials
import pymysql.cursors

now = open("/home/user/indywidualni.org/centrum/teraz.txt", "r")
line = now.read()
now.close()

array = (line.rstrip()).split("|")
ignore_artists = ['DOOKOLA SPORTU', 'Rockowa Komisja Wyborcza', 'CENTRUM W MROKU', 'KOKTAJL KULTURALNY']
ignore_titles = ['Podklad 2015', 'podklad']

if not array[2] or (array[2] in ignore_titles):
    array[2] = None
if not array[3] or (array[3] in ignore_artists):
    array[3] = None
if array[5] == "XX:XX":
    array[5] = None

# Connect to the database
secret = credentials()
connection = pymysql.connect(host=secret[0],
                             user=secret[1],
                             password=secret[2],
                             db=secret[3],
                             charset=secret[4],
                             cursorclass=pymysql.cursors.DictCursor)

try:
    with connection.cursor() as cursor:
        # Read a single record
        sql = "SELECT `id` FROM `centrum_history` WHERE `id` IN (SELECT MAX(`id`) " \
              "FROM `centrum_history`) AND `artist`=%s AND `title`=%s AND `duration`=%s"
        cursor.execute(sql, (array[3], array[2], array[5],))
        result = cursor.fetchone()

    if result is None:
        with connection.cursor() as cursor:
            # Create a new record
            sql = "SET time_zone = 'Europe/Warsaw'; INSERT IGNORE INTO `centrum_artists` " \
                  "(`name`) VALUES (%s); INSERT IGNORE INTO `centrum_tracks` (`title`, " \
                  "`duration`, `artist`) VALUES (%s, %s, (SELECT `id` FROM `centrum_artists` " \
                  "WHERE `name`=%s)); INSERT INTO `centrum_log` (`track`) VALUES ((SELECT `id` " \
                  "FROM `centrum_songs` WHERE `title`=%s AND `artist`=%s AND `duration`=%s))"
            cursor.execute(sql, (array[3], array[2], array[5], array[3], array[2], array[3], array[5],))

            # Commit to save
            connection.commit()

except pymysql.err.IntegrityError:
    print("Value cannot be None, skipping.")

finally:
    connection.close()
