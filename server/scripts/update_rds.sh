#!/bin/bash
rm /home/user/indywidualni.org/centrum/teraz.txt
rm /home/user/indywidualni.org/centrum/zaraz.txt

wget -q http://centrum.fm/radio/rds/teraz.txt -O /home/user/indywidualni.org/centrum/teraz.txt
wget -q http://centrum.fm/radio/rds/zaraz.txt -O /home/user/indywidualni.org/centrum/zaraz.txt &
python /home/user/indywidualni.org/centrum/scripts/save_song.py &
