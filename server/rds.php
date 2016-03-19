<?php

$csv = file_get_contents("teraz.txt");
$csv .= "\n";
$csv .= file_get_contents("zaraz.txt");

function fix($s) {
	$t = str_ireplace(Array("x",":"), "", $s);
 	return $t ? $s : "";
}

$rows = explode("\n", $csv);

foreach($rows as $row) {
	$row = str_getcsv($row, "|");
	$json[] = Array(
  		"id" => fix($row[0]),
  		"key" => fix($row[1]),
  		"title" => fix($row[2]),
  		"artist" => fix($row[3]),
  		"time" => fix($row[4]),
  		"total" => fix($row[5]),  
	);
}

header('Content-Type: application/json');
echo json_encode($json);

?>
