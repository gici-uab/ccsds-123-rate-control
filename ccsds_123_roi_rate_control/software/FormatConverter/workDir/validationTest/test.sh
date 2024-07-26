#!/bin/bash

INGEO=( "1 1 24 0 0" "1 1 24 1 0" "1 1 12 2 0" "1 1 12 3 0" "1 1 6 4 0" "1 1 3 5 0" "1 1 6 6 0" "1 1 3 7 0" "1 1 24 0 1" "1 1 24 1 1" "1 1 12 2 1" "1 1 12 3 1" "1 1 6 4 1" "1 1 3 5 1" "1 1 6 6 1" "1 1 3 7 1");
OUTGEO=("0 0" "1 0" "2 0" "3 0" "4 0" "5 0" "6 0" "7 0" "0 1" "1 1" "2 1" "3 1" "4 1" "5 1" "6 1" "7 1");
COUNTSI=(24 24 12 12 6 3 6 3 24 24 12 12 6 3 6 3);
SIZEO=(1 1 2 2 4 8 4 8 1 1 2 2 4 8 4 8);

c=0;

for ((i=0; i < ${#INGEO[@]}; i++));
do
	for ((o=0; o < ${#OUTGEO[@]}; o++));
	do
		ci=${COUNTSI[i]};
		so=${SIZEO[$o]};

		#out=results/$c.raw;
		out=validate.raw;

		java -Xms2m -jar ../../dist/ffc.jar -i test.raw -o $out -ig ${INGEO[$i]} 0 -og 1 1 $ci ${OUTGEO[$o]} 0
	
		#cp $out validate.raw

		size=$(wc -c < $out);

		if (( size != ci * so ));
		then
			echo "File mush have" $(( ci * so )) "bytes"
			exit 1;
		fi;

		cmp results/$c.raw validate.raw || { echo "ERROR: invalid conversion"; exit 1; };

		# DUMP
		if [[ $(echo ${OUTGEO[$o]} | sed 's/^.* //') == 1 ]];
		then
			echo "little";
			cat validate.raw > swaped.raw
		else
			if [[ ${SIZEO[$o]} == 1 ]];
			then
				echo "big 1";
				cat validate.raw > swaped.raw
			elif [[ ${SIZEO[$o]} == 2 ]];
			then
				echo "big 2";
				./byteswap < validate.raw > swaped.raw
			elif [[ ${SIZEO[$o]} == 4 ]];
			then
				echo "big 4";
				./4rot < validate.raw > swaped.raw
			elif [[ ${SIZEO[$o]} == 8 ]];
			then
				echo "big 8";
				./8rot < validate.raw > swaped.raw
			else
				echo "NO MATCH";
				exit 1;
			fi
		fi

		echo "From $(echo ${INGEO[$i]} | sed 's/ [^ ]*$//' | sed 's/.* //') to $(echo ${OUTGEO[$o]} | sed 's/ .*$//')";

		if [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 0 ]];
		then
			hexdump -ve '1/1 "%u "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 1 ]];
		then
			hexdump -ve '1/1 "%u "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 2 ]];
		then
			hexdump -ve '1/2 "%u "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 3 ]];
		then
			hexdump -ve '1/2 "%d "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 4 ]];
		then
			hexdump -ve '1/4 "%d "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 5 ]];
		then
# Does not work	hexdump -ve '1/8 "%d "' < swaped.raw; echo
			echo -n
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 6 ]];
		then
			hexdump -ve '1/4 "%g "' < swaped.raw; echo
		elif [[ $(echo ${OUTGEO[$o]} | sed 's/ .*$//') == 7 ]];
		then
			hexdump -ve '1/8 "%g "' < swaped.raw; echo
		else
			echo "NO MATCH";
			exit 1;
		fi

		c=$((c+1));
	done
done

rm -f swaped.raw validate.raw;
