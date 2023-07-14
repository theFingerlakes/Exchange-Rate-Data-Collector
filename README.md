# Exchange Rate Data Collector

> This program is a daily exchange rate data collector using Bank of Canada Valet API

## Build With

- Language: Java 17
- Technologies used: Jackson Json, Java I/O, Java.net

## Main Features

- Get data for supported series from Bank of Canada Valet API.
- Write the data in a CSV file with the following headers: Date, Series Name, Label, Description, Value.
- Accept command line parameters to only collect data for a specific date range and a maximum of four supported Series Name.
- If no parameters are provided, the default behavior will be to collect for the past week and the following series name, FXCADUSD and FXAUDCAD.
- The data in the CSV file is grouped by Series Name and sorted by Date.
