<?php

$csv = file_get_contents("teraz.txt");
$csv .= "\n";
$csv .= file_get_contents("zaraz.txt");

$rows = explode("\n", $csv);

foreach($rows as $row) {
	$row = str_getcsv($row, "|");
	$json[] = Array(
  		"id" => $row[0],
  		"key" => $row[1],
  		"title" => $row[2],
  		"artist" => $row[3],
  		"time" => $row[4],
  		"total" => $row[5],
	);
}

header('Content-Type: application/json');
echo json_encode($json);

?>
