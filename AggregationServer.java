public class AggregationServer {
    public static void main(String[] args) {
        String filePath = "./weather_info.txt"; // replace with the actual path to your file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print each line of the file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
