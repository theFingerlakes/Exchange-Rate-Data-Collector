import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataCollector {
    private static final String BASE_URL = "https://www.bankofcanada.ca/valet/observations/";
    private static final String[] DEFAULT_SERIES_NAME = {"FXCADUSD", "FXAUDCAD"};
    private static final String DEFAULT_START_DATE = LocalDate.now().minusDays(7).toString();
    private static final String DEFAULT_END_DATE = LocalDate.now().toString();
    private static final String  CSV_FILE_NAME = "exchange_rate_data.csv";

    public static void main(String[] args) throws IOException, InterruptedException {
        String startDate = DEFAULT_START_DATE;
        String endDate = DEFAULT_END_DATE;
        String[] seriesName = DEFAULT_SERIES_NAME;
        String defaultURL = String.format("%s%s?start_date=%s&end_date=%s",
                BASE_URL, String.join(",", seriesName), startDate, endDate);

        // Check User's Command Line
        if (args.length >= 1 && args.length <= 5) {
            if (args.length == 1) {
                startDate = args[0];
                String url1 = String.format("%s%s?start_date=%s",
                        BASE_URL, String.join(",", seriesName), startDate);
                defaultURL = url1;
            }
            if (args.length == 2) {
                startDate = args[0];
                endDate = args[1];
                String url2 = String.format("%s%s?start_date=%s&end_date=%s",
                        BASE_URL, String.join(",", seriesName), startDate, endDate);
                defaultURL = url2;
            }
            if (args.length > 2) {
                seriesName = Arrays.copyOfRange(args, 2, args.length);
                startDate = args[0];
                endDate = args[1];
                String url3 = String.format("%s%s?start_date=%s&end_date=%s",
                        BASE_URL, String.join(",", seriesName), startDate, endDate);
                defaultURL = url3;
            }
        } else if (args.length > 5) {
            System.out.println("Invalid number of arguments. Usage: java DataCollector [start_date] [end_date] [series_name1] [series_name2] [series_name3] [series_name4]");
            return;
        }

        List<ExchangeRateData> test = getExchangeDataRate(defaultURL, seriesName);
        writeListToCSV(test, CSV_FILE_NAME);

    }

    private static List<ExchangeRateData> getExchangeDataRate(String myUrl, String[] newSeriesName) throws IOException, InterruptedException {
        // Get JSON Response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(myUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();

        ObjectMapper mapper = new ObjectMapper();

        // Parse JSON
        JsonNode rootNode = mapper.readTree(jsonResponse);

        // Create list to hold CurrencyData objects
        List<ExchangeRateData> dataList = new ArrayList<>();

        // Iterate over observations array
        for (String s : newSeriesName) {
            // Access seriesDetail node
            JsonNode tempSeriesDetailNode = rootNode.path("seriesDetail").path(s);
            for (JsonNode observationNode : rootNode.path("observations")) {
                ExchangeRateData data = new ExchangeRateData();

                // Set Date
                data.setDate(observationNode.path("d").asText());

                // Set SeriesName
                data.setSeriesName(s);

                // Set Label
                data.setLabel(tempSeriesDetailNode.path("label").asText());

                // Set Description
                data.setDescription(tempSeriesDetailNode.path("description").asText());

                // Set Value
                data.setValue(observationNode.path(s).path("v").asText());

                // add data object to list
                dataList.add(data);
            }
        }

        return  dataList;
    }


    private static void writeListToCSV(List<ExchangeRateData> dataList, String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            // Sort and group the data
            List<ExchangeRateData> sortedGroupedList = dataList.stream()
                    .sorted(Comparator.comparing(ExchangeRateData::getLabel)
                            .thenComparing(ExchangeRateData::getDate).reversed())
                    .collect(Collectors.toList());

            // Write CSV file headers
            writer.println("Date,Series Name,Label,Description,Value");

            // Write data
            for (ExchangeRateData data : sortedGroupedList) {
                writer.println(String.format("%s,%s,%s,%s,%s",
                        data.getDate(),
                        data.getSeriesName(),
                        data.getLabel(),
                        data.getDescription(),
                        data.getValue()));
            }
        } catch (Exception e) {
            System.out.println("An error occurred while writing the CSV file:" + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("The CSV file was successfully written!");

    }



}
