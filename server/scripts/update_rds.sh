#!/bin/bash
rm /home/kgrabowski_h/indywidualni.org/centrum/teraz.txt
rm /home/kgrabowski_h/indywidualni.org/centrum/zaraz.txt

wget -q http://centrum.fm/radio/rds/teraz.txt -O /home/kgrabowski_h/indywidualni.org/centrum/teraz.txt
wget -q http://centrum.fm/radio/rds/zaraz.txt -O /home/kgrabowski_h/indywidualni.org/centrum/zaraz.txt &
python /home/kgrabowski_h/indywidualni.org/centrum/scripts/save_song.py &
