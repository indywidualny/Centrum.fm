#!/usr/bin/python

import sys
sys.path.append('/home/user/secret')
from connection import credentials
import pymysql.cursors
from datetime import datetime
import json


class DateTimeEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, datetime):
            return o.isoformat()

        return json.JSONEncoder.default(self, o)


# Get POST arguments
POST = {}
args = sys.stdin.read().split('&')

for arg in args:
    t = arg.split('=')
    if len(t) > 1: k, v = arg.split('='); POST[k] = v

date_from = POST.get('from')
date_to = POST.get('to')
limit = POST.get('limit')
skip = POST.get('skip')
popular = POST.get('popular')
count = POST.get('count')

if limit is None:
    limit = 10
elif int(limit) > 500:
    limit = 500

if skip is None:
    skip = 0

if count is None:
    count = 1

if date_from is None:
    date_from = "1000-01-01"
if date_to is None:
    date_to = "9999-12-31"

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
        if popular == "1":
            sql = "SELECT COUNT(*) AS `sum`, S.`title`, S.`artist`, S.`duration` FROM `centrum_log` L INNER JOIN " \
                  "`centrum_songs` S ON L.`track`=S.`id` WHERE L.`played` BETWEEN %s AND %s GROUP BY L.`track` " \
                  "HAVING COUNT(*) >= %s ORDER BY COUNT(*) DESC LIMIT %s, %s"
            cursor.execute(sql, (date_from, date_to, int(count), int(skip), int(limit),))
        else:
            sql = "SELECT * FROM `centrum_history` WHERE `played` BETWEEN %s AND %s ORDER BY `id` DESC LIMIT %s, %s"
            cursor.execute(sql, (date_from, date_to, int(skip), int(limit),))
        result = cursor.fetchall()

finally:
    connection.close()

print("Content-type: application/json")
print
print(json.dumps(result, cls=DateTimeEncoder))
