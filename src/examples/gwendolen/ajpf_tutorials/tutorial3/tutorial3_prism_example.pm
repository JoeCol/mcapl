dtmc

 module jpfModel
state : [0 ..89] init 0;
bag1holdblock: bool init true;
[] state = 0 -> 0.5:(state'=1) & (bag1holdblock'= true) + 0.5:(state'=37) & (bag1holdblock'= true);
[] state = 1 -> 0.5:(state'=2) & (bag1holdblock'= true) + 0.5:(state'=24) & (bag1holdblock'= true);
[] state = 2 -> 0.5:(state'=3) & (bag1holdblock'= true) + 0.5:(state'=15) & (bag1holdblock'= true);
[] state = 3 -> 0.5:(state'=4) & (bag1holdblock'= true) + 0.5:(state'=10) & (bag1holdblock'= true);
[] state = 4 -> 0.5:(state'=5) & (bag1holdblock'= true) + 0.5:(state'=9) & (bag1holdblock'= true);
[] state = 5 -> 0.5:(state'=6) & (bag1holdblock'= false) + 0.5:(state'=8) & (bag1holdblock'= false);
[] state = 6 -> 1.0:(state'=89) & (bag1holdblock'= false);
[] state = 8 -> 1.0:(state'=88) & (bag1holdblock'= false);
[] state = 9 -> 0.5:(state'=6) & (bag1holdblock'= false) + 0.5:(state'=8) & (bag1holdblock'= false);
[] state = 10 -> 0.5:(state'=11) & (bag1holdblock'= true) + 0.5:(state'=14) & (bag1holdblock'= true);
[] state = 11 -> 0.5:(state'=12) & (bag1holdblock'= false) + 0.5:(state'=13) & (bag1holdblock'= false);
[] state = 12 -> 1.0:(state'=87) & (bag1holdblock'= false);
[] state = 13 -> 1.0:(state'=86) & (bag1holdblock'= false);
[] state = 14 -> 0.5:(state'=12) & (bag1holdblock'= false) + 0.5:(state'=13) & (bag1holdblock'= false);
[] state = 15 -> 0.5:(state'=16) & (bag1holdblock'= true) + 0.5:(state'=21) & (bag1holdblock'= true);
[] state = 16 -> 0.5:(state'=17) & (bag1holdblock'= true) + 0.5:(state'=20) & (bag1holdblock'= true);
[] state = 17 -> 0.5:(state'=18) & (bag1holdblock'= false) + 0.5:(state'=19) & (bag1holdblock'= false);
[] state = 18 -> 1.0:(state'=85) & (bag1holdblock'= false);
[] state = 19 -> 1.0:(state'=84) & (bag1holdblock'= false);
[] state = 20 -> 0.5:(state'=18) & (bag1holdblock'= false) + 0.5:(state'=19) & (bag1holdblock'= false);
[] state = 21 -> 0.5:(state'=22) & (bag1holdblock'= false) + 0.5:(state'=23) & (bag1holdblock'= false);
[] state = 22 -> 1.0:(state'=83) & (bag1holdblock'= false);
[] state = 23 -> 1.0:(state'=82) & (bag1holdblock'= false);
[] state = 24 -> 0.5:(state'=25) & (bag1holdblock'= true) + 0.5:(state'=31) & (bag1holdblock'= false);
[] state = 25 -> 0.5:(state'=26) & (bag1holdblock'= true) + 0.5:(state'=29) & (bag1holdblock'= true);
[] state = 26 -> 0.5:(state'=27) & (bag1holdblock'= true) + 0.5:(state'=28) & (bag1holdblock'= true);
[] state = 27 -> 0.5:(state'=6) & (bag1holdblock'= false) + 0.5:(state'=8) & (bag1holdblock'= false);
[] state = 28 -> 0.5:(state'=12) & (bag1holdblock'= false) + 0.5:(state'=13) & (bag1holdblock'= false);
[] state = 29 -> 0.5:(state'=22) & (bag1holdblock'= false) + 0.5:(state'=30) & (bag1holdblock'= true);
[] state = 30 -> 0.5:(state'=18) & (bag1holdblock'= false) + 0.5:(state'=19) & (bag1holdblock'= false);
[] state = 31 -> 0.5:(state'=32) & (bag1holdblock'= false) + 0.5:(state'=35) & (bag1holdblock'= false);
[] state = 32 -> 0.5:(state'=33) & (bag1holdblock'= false) + 0.5:(state'=34) & (bag1holdblock'= false);
[] state = 33 -> 0.5:(state'=6) & (bag1holdblock'= false) + 0.5:(state'=8) & (bag1holdblock'= false);
[] state = 34 -> 0.5:(state'=12) & (bag1holdblock'= false) + 0.5:(state'=13) & (bag1holdblock'= false);
[] state = 35 -> 0.5:(state'=36) & (bag1holdblock'= false) + 0.5:(state'=23) & (bag1holdblock'= false);
[] state = 36 -> 0.5:(state'=18) & (bag1holdblock'= false) + 0.5:(state'=19) & (bag1holdblock'= false);
[] state = 37 -> 0.5:(state'=67) & (bag1holdblock'= false) + 0.5:(state'=38) & (bag1holdblock'= true);
[] state = 38 -> 0.5:(state'=39) & (bag1holdblock'= true) + 0.5:(state'=60) & (bag1holdblock'= false);
[] state = 39 -> 0.5:(state'=53) & (bag1holdblock'= false) + 0.5:(state'=40) & (bag1holdblock'= true);
[] state = 40 -> 0.5:(state'=48) & (bag1holdblock'= true) + 0.5:(state'=41) & (bag1holdblock'= true);
[] state = 41 -> 0.5:(state'=42) & (bag1holdblock'= true) + 0.5:(state'=45) & (bag1holdblock'= true);
[] state = 42 -> 0.5:(state'=43) & (bag1holdblock'= false) + 0.5:(state'=44) & (bag1holdblock'= false);
[] state = 43 -> 1.0:(state'=81) & (bag1holdblock'= false);
[] state = 44 -> 1.0:(state'=80) & (bag1holdblock'= false);
[] state = 45 -> 0.5:(state'=46) & (bag1holdblock'= false) + 0.5:(state'=47) & (bag1holdblock'= false);
[] state = 46 -> 1.0:(state'=79) & (bag1holdblock'= false);
[] state = 47 -> 1.0:(state'=78) & (bag1holdblock'= false);
[] state = 48 -> 0.5:(state'=49) & (bag1holdblock'= true) + 0.5:(state'=52) & (bag1holdblock'= false);
[] state = 49 -> 0.5:(state'=50) & (bag1holdblock'= false) + 0.5:(state'=51) & (bag1holdblock'= false);
[] state = 50 -> 1.0:(state'=77) & (bag1holdblock'= false);
[] state = 51 -> 1.0:(state'=76) & (bag1holdblock'= false);
[] state = 52 -> 1.0:(state'=75) & (bag1holdblock'= false);
[] state = 53 -> 0.5:(state'=54) & (bag1holdblock'= false) + 0.5:(state'=57) & (bag1holdblock'= false);
[] state = 54 -> 0.5:(state'=55) & (bag1holdblock'= false) + 0.5:(state'=56) & (bag1holdblock'= false);
[] state = 55 -> 0.5:(state'=43) & (bag1holdblock'= false) + 0.5:(state'=44) & (bag1holdblock'= false);
[] state = 56 -> 0.5:(state'=46) & (bag1holdblock'= false) + 0.5:(state'=47) & (bag1holdblock'= false);
[] state = 57 -> 0.5:(state'=58) & (bag1holdblock'= false) + 0.5:(state'=59) & (bag1holdblock'= false);
[] state = 58 -> 0.5:(state'=50) & (bag1holdblock'= false) + 0.5:(state'=51) & (bag1holdblock'= false);
[] state = 59 -> 1.0:(state'=74) & (bag1holdblock'= false);
[] state = 60 -> 0.5:(state'=53) & (bag1holdblock'= false) + 0.5:(state'=61) & (bag1holdblock'= false);
[] state = 61 -> 0.5:(state'=65) & (bag1holdblock'= false) + 0.5:(state'=62) & (bag1holdblock'= false);
[] state = 62 -> 0.5:(state'=64) & (bag1holdblock'= false) + 0.5:(state'=63) & (bag1holdblock'= false);
[] state = 63 -> 0.5:(state'=43) & (bag1holdblock'= false) + 0.5:(state'=44) & (bag1holdblock'= false);
[] state = 64 -> 0.5:(state'=46) & (bag1holdblock'= false) + 0.5:(state'=47) & (bag1holdblock'= false);
[] state = 65 -> 0.5:(state'=66) & (bag1holdblock'= false) + 0.5:(state'=52) & (bag1holdblock'= false);
[] state = 66 -> 0.5:(state'=50) & (bag1holdblock'= false) + 0.5:(state'=51) & (bag1holdblock'= false);
[] state = 67 -> 0.5:(state'=68) & (bag1holdblock'= false) + 0.5:(state'=60) & (bag1holdblock'= false);
[] state = 68 -> 0.5:(state'=69) & (bag1holdblock'= false) + 0.5:(state'=53) & (bag1holdblock'= false);
[] state = 69 -> 0.5:(state'=70) & (bag1holdblock'= false) + 0.5:(state'=73) & (bag1holdblock'= false);
[] state = 70 -> 0.5:(state'=71) & (bag1holdblock'= false) + 0.5:(state'=72) & (bag1holdblock'= false);
[] state = 71 -> 0.5:(state'=43) & (bag1holdblock'= false) + 0.5:(state'=44) & (bag1holdblock'= false);
[] state = 72 -> 0.5:(state'=46) & (bag1holdblock'= false) + 0.5:(state'=47) & (bag1holdblock'= false);
[] state = 73 -> 0.5:(state'=52) & (bag1holdblock'= false) + 0.5:(state'=74) & (bag1holdblock'= false);
[] state = 74 -> 0.5:(state'=50) & (bag1holdblock'= false) + 0.5:(state'=51) & (bag1holdblock'= false);
endmodule