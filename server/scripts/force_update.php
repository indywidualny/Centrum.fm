<?php

// for testing, cron will be doing it anyway
shell_exec('./update_rds.sh');
shell_exec('./update_schedule.sh');

?>
