#!/usr/bin/php
<?php

# convert frap.log to a more friendly format and normalize to prebleach=1
# usage: frapseg.php frap.log

$names = array ();
$data = array ();
$cell = -1;


$lines = file ($argv [1]);


// collect post bleach background
$sumbg = 0;
$n = 0;
foreach ($lines as $line) {
	$seg = explode ("\t", $line);
	if (str_starts_with ($line, "post")) {
		$sumbg += trim ($seg [3]);
		$n++;
	}
}

$avgbg = 0;
if ($n > 0) {
	$avgbg = $sumbg / $n;
}
$avgbg = 102;


$preavg = array ();
foreach ($lines as $line) {
	$line = trim ($line);
	//echo $line."\n";
	if (str_starts_with ($line, "w")) {
		$cell++;
		$seg = explode (" ", $line);
		$name = trim ($seg [0]);
		$names [$cell] = $name;
		$d = array ();
		$cnt = 0;
		$presum = 0;
	}
	else if ((strlen ($line) > 2) && (!str_starts_with ($line, "step"))) {
		$seg = explode ("\t", $line);
		$measurement = new stdClass ();
		$measurement->time = intval (trim ($seg [1])) / 1;
		$measurement->val = trim ($seg [2]) - $avgbg;
		$d [] = $measurement;
		$data [$cell] = $d;
		
		if (str_starts_with ($line, "pre")) {
			$presum += $measurement->val;
			$cnt++;
		}
		
		if (str_starts_with ($line, "post0\t")) {
			$preavg [$cell] = $presum / $cnt;
		}
	}
}


//print_r ($preavg);

// normalize prebleach to 1
for ($i = 0; $i < sizeof ($data); $i++) {
	for ($t = 0; $t < sizeof ($data [$i]); $t++) {
		$data [$i][$t]->val /= $preavg [$i];
	}
}


//print_r ($names);
//print_r ($data);


echo "time";
for ($i = 0; $i < sizeof ($names); $i++) {
	echo "\t".$names [$i];
}
echo "\n";

for ($j = 0; $j < sizeof ($data [0]); $j++) {
	
	echo ($data [0][$j]->time) / 1000;
	
	for ($i = 0; $i < sizeof ($names); $i++) {
		echo "\t".round ($data [$i][$j]->val, 10);
	}
	echo "\n";
}



?>
