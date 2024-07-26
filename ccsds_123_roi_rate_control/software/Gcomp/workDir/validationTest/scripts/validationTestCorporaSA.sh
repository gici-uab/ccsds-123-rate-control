#!/bin/bash

# Filename: validationTestSA.sh
#
# Author: jgonzalez@deic.uab.es
# Date:   Wednesday Jan 23, 2008
# Last modification: 
#
# Description: script for validate the implementation of the Gcomp application in the shape adaptive version

echo " "
echo "Validating Gcomp implementation for the shape adaptive version"
#echo "The default language for the bash is $LANG."

LANG=en_GB
#echo "Language changed to $LANG for real numbers."
echo " ";

#Variables
ERROR="0";

#Directories
corporaDirectory="../corpora";
scriptsDirectory=".";

#Applications
compareReference="${scriptsDirectory}/GcompReference";
compareCurrent="${scriptsDirectory}/GcompCurrent";

#Corpora
imageCorpora=`ls ${corporaDirectory}`;

for corpus in ${imageCorpora}; do
	currentCorpusRoute="${corporaDirectory}/${corpus}";
	currentCorpusMasksRoute="${currentCorpusRoute}/masks";
	imageSet=`ls ${currentCorpusRoute}/*.*`;
	
	for image in ${imageSet}; do
		imageFile=`echo ${image} | awk -F '/' '{printf $NF}'`;
		imageName=`echo ${imageFile} | awk -F '.' '{printf $1}'`;
		imageExtension=`echo ${imageFile} | awk -F '.' '{printf $2}'`;
		
		maskFile=`ls ${currentCorpusMasksRoute}/${imageName}*`;
		recoveredImage=`ls ${currentCorpusRoute}/recovered/*${imageName}*_SArecovered*`;
		
		echo "${compareReference} -i1 ${image} -i2 ${recoveredImage} -mk ${maskFile} -f 1";
		resultsReference=`${compareReference} -i1 ${image} -i2 ${recoveredImage} -mk ${maskFile} -f 1`;
		
		echo "${compareCurrent} -i1 ${image} -i2 ${recoveredImage} -mk ${maskFile} -f 1";
		resultsCurrent=`${compareCurrent} -i1 ${image} -i2 ${recoveredImage} -mk ${maskFile} -f 1`;

		echo " ";
		
		if [[ ${resultsReference} != ${resultsCurrent} ]]; then
			echo " ";
			echo "Difference detected for image ${imageFile}.";
			echo "Reference results: ${resultsReference}";
			echo "Current results: ${resultsCurrent}";
			echo " ";
			ERROR=`echo ${ERROR} | awk '{print $1 + 1}'`;
		fi
	done
done

if [[ ${ERROR} == 0 ]]; then
	echo "NO ERRORS FOUND. IMPLEMENTATION VALIDATED";
else
	echo "$ERROR error(s) found!!";
fi
