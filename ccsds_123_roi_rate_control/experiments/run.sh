#!/bin/bash
infima="java -ea -Xmx16288m -classpath ../software/infima/dist/infima.jar infima.Infima"
emporda="java -Xmx24g -jar ../software/QEmpordaACROI/dist/emporda.jar"
ffc="../software/FormatConverter/ffc"
Gstat="../software/Gstat/Gstat"
Gcomp="../software/Gcomp/Gcomp"

sp="1" #sp = 1 --> CCSDS-123predictor    sp = no CCSDS-123 predictor
ecs="2"
#return bps for a file
trs="0.1 0.25 0.5 1 2 3 4 7"
trs="0.05 0.1 0.15 0.2 0.25 0.3 0.4 0.5 0.75 1 1.25 1.5 1.75 2 2.5 3 3.5 4"
rcs="3" #0.- Strategy 0 (Valsesia & Magli 2016), 1.- ROI coding, 2.- ROI coding (not coding first line), 3.- ROI coding losslessly. Rate is distributed for the BG
q="3"
trs="0.067 0.133 0.200 0.267 0.333 0.400 0.467 0.533 0.600 0.667 0.733 0.800 0.867 0.933 1.000 1.067 1.133 1.200 1.267 1.333 1.400 1.467 1.533 1.600 1.667 1.733 1.800 1.867 1.933 2.000"
function getbps {
    bytes=`ls -all $1`
    bytes=`echo $bytes |awk '{printf($5)}'`
    bps=`echo $bytes $zsize $ysize $xsize 8 |awk '{printf("%f",(($1*$5)/($2*$3*$4)))}'`
}

function getbitdepth {
    stats=`$infima -i $1 -ig $zsize $ysize $xsize $sampleType $signedType $bitdepth $byteOrder $dataOrder 0  -f 1`
    min=`echo $stats | cut -d " " -f 2`
    max=`echo $stats | cut -d " " -f 3`
    bitdepth=`echo $min $max |awk '{printf("%d",(log(sqrt($1*$1)+sqrt($2*$2)+1)/(log(2))+2))}'`
}
#rm -f  *.info *.metrics 2>/dev/null


rois="1"
qstepROIs="1"
#images="NCI01.3_1280_1280_1_0_8_0_0_1.raw NCI20.3_3840_1920_1_0_8_0_0_1.raw NCI26.3_4480_4480_1_0_8_0_0_1.raw"
images="NCI01.3_1280_1280_1_0_8_0_0_1.raw"
#trs="0.067 0.133 0.200 0.267 0.333 0.400 0.467 0.533 0.600 0.667 0.733 0.800 0.867 0.933 1.000 1.067 1.133 1.200 1.267 1.333 1.400 1.467 1.533 1.600 1.667 1.733 1.800 1.867 1.933 2.000 20"
trs="0.067 0.133"
for image in `echo $images`; do
for rc in `echo $rcs`; do
for roi in `echo $rois`; do
for ec in `echo $ecs`; do
    if [ "$ec" == 2 ]; then
        cms="1 12"
    else
        cms="1"
    fi
for cm in `echo $cms`; do
    cd ../images
    rm * 2> /dev/null
    ln -s ../experiments/emporda-settings.pl . 2> /dev/null
    ln -s ../experiments/buildOptionsFileRC.sh . 2> /dev/null
    cp originals/$image .
    imagename=`echo $image | cut -d "." -f 1`
    zsize=`echo $image | cut -d "." -f 2 | cut -d "_" -f 1`
    ysize=`echo $image | cut -d "." -f 2 | cut -d "_" -f 2`
    xsize=`echo $image | cut -d "." -f 2 | cut -d "_" -f 3`
    sampleType=`echo $image | cut -d "." -f 2 | cut -d "_" -f 4`
    signedType=`echo $image | cut -d "." -f 2 | cut -d "_" -f 5`  
    bitdepth=`echo $image | cut -d "." -f 2 | cut -d "_" -f 6` 
    byteOrder=`echo $image | cut -d "." -f 2 | cut -d "_" -f 7`
    dataOrder=`echo $image | cut -d "." -f 2 | cut -d "_" -f 8`
    componentsRGB=`echo $image | cut -d "." -f 2 | cut -d "_" -f 9`
    
    ###########################
    #TRANSFORM IMAGE TO 2 BYTES PER SAMPLE
    ###########################
    orginalSampleType=`echo $sampleType`
    $ffc -i $image -ig $zsize $ysize $xsize $sampleType $byteOrder 0 -o $imagename"."$zsize"_"$ysize"_"$xsize"_2_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -og $zsize $ysize $xsize 2 0 0
    sampleType=2
    byteOrder=0

    newimage=$imagename"."$zsize"_"$ysize"_"$xsize"_"$sampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw"
    
    for qstepROI in `echo $qstepROIs`; do
    echo ""
    echo $image"_rc_"$rc"_roi_"$roi"_qstepROI_"$qstepROI"_ccsds123_roicoding_ec_"$ec"_cm_"$cm".results"
    rm -f ../experiments/results/$image"_rc_"$rc"_roi_"$roi"_qstepROI_"$qstepROI"_ccsds123_roicoding_ec_"$ec"_cm_"$cm".results"
    
    getbitdepth $newimage
    roisamplenumber=`$Gstat -i masks/ROImask1-$image -ig 1 $ysize $xsize 1 0 -s 5 -v 255 -t 0 -f 1 | head -n 1 | cut -d "-" -f 2 | cut -d " " -f 2`

    for tr in `echo $trs`; do
        #trc=`echo $tr`
        #tr=`echo $tr $zsize |awk '{printf("%f",(($1*$2)))}'`
        rm options.txt 2> /dev/null
        sh buildOptionsFileRC.sh $image
        echo "# MAX BIT SIZE FOR SAMPLE 4 bits (range = [1, 16])" >> options.txt
        echo "DYNAMIC_RANGE="$bitdepth >> options.txt
        
        ###########################
        #PLAIN CODING AND DECODING
        ###########################
        $emporda -c -i $newimage -o coded -ig $zsize $ysize $xsize $sampleType $byteOrder 0 -qm 1 -q $q -qs $qstepROI -ec $ec -cm $cm -pm 0 -qlut 0 -wp 4096 -up 2 -sp $sp -rcs $rc -f options.txt -tr $tr -mk "masks/ROImask"$roi"-"$image > out.txt
        tail -n 1 out.txt > tmp.txt
        bps=`cat tmp.txt | cut -d " " -f 1`
        bpsROI=`cat tmp.txt | cut -d " " -f 2`
        bpsBG=`cat tmp.txt | cut -d " " -f 3`
        bpsROIc=`echo $bpsROI $zsize |awk '{printf("%f",(($1/$2)))}'`
        bpsBGc=`echo $bpsBG $zsize |awk '{printf("%f",(($1/$2)))}'`
        getbps coded 
        bytes=`ls -all coded`
        bytes=`echo $bytes |awk '{printf($5)}'`
        
        $emporda -d -i coded  -o decoded.raw -ig $zsize $ysize $xsize $sampleType $byteOrder 0 -qm 1 -q $q -qs $qstepROI -ec $ec -cm $cm -pm 0 -qlut 0 -wp 4096 -up 2 -sp $sp  -rcs $rc -f options.txt -tr $tr -mk "masks/ROImask"$roi"-"$image > out.txt    
        ###########################
        #TO ORIGINAL FORMAT DATA 
        ###########################
        $ffc -i decoded.raw -ig $zsize $ysize $xsize $sampleType $byteOrder 0 -o "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -og $zsize $ysize $xsize $orginalSampleType 0 0 2> /dev/null

        ###########################
        #COMPARE IMAGES
        ###########################
        plainpae=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -t 2 -f 1 -m 2`
        roipae=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -mk "masks/ROImask"$roi"-"$image -t 2 -f 1 -m 2 -inv 0`
        bgpae=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -mk "masks/ROImask"$roi"-"$image -t 2 -f 1 -m 2 -inv 1`
        plainpsnr=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -t 2 -f 1 -m 7`
        roipsnr=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -mk "masks/ROImask"$roi"-"$image -t 2 -f 1 -m 7 -inv 0`
        bgpsnr=`$Gcomp -i1 $image -ig1 $zsize $ysize $xsize $orginalSampleType $byteOrder -i2 "decoded."$zsize"_"$ysize"_"$xsize"_"$orginalSampleType"_"$signedType"_"$bitdepth"_0_"$dataOrder"_"$componentsRGB".raw" -ig2 $zsize $ysize $xsize $orginalSampleType $byteOrder -mk "masks/ROImask"$roi"-"$image -t 2 -f 1 -m 7 -inv 1`
        
        ###########################
        #REPORT RESULTS
        ###########################
        #echo $tr":"$trc":"$bytes":"$bps":"$bpsROI":"$bpsROIc":"$bpsBG":"$bpsBGc":"$plainpae":"$roipae":"$bgpae":"$plainpsnr":"$roipsnr":"$bgpsnr
        #echo $tr":"$trc":"$bytes":"$bps":"$bpsROI":"$bpsROIc":"$bpsBG":"$bpsBGc":"$plainpae":"$roipae":"$bgpae":"$plainpsnr":"$roipsnr":"$bgpsnr
        echo $tr":"$bps":"$bpsROI":"$bpsBG":"$plainpae":"$roipae":"$bgpae":"$plainpsnr":"$roipsnr":"$bgpsnr":"$roisamplenumber
        echo $tr":"$bps":"$bpsROI":"$bpsBG":"$plainpae":"$roipae":"$bgpae":"$plainpsnr":"$roipsnr":"$bgpsnr":"$roisamplenumber >> ../experiments/results/$image"_rc_"$rc"_roi_"$roi"_qstepROI_"$qstepROI"_ccsds123_roicoding_ec_"$ec"_cm_"$cm".results"
        rm -f coded decoded.raw qsteps.txt
    done
    echo ""
    done    
    rm -f $newimage
    cd ../experiments
done
done
done
done
done

rm -f ../images/* 2> /dev/null






