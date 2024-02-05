import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String str = "https://iss.moex.com/iss/securities.csv?q=" + args[0];
        URI uri = URI.create(str);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
        Stream<String> data = response.body();
        var result = data
                .skip(2)
                .filter(x -> !x.isEmpty())
                .map(x -> x.split(";"))
                .filter(x -> x[6].equals("1") || x[6].equals("is_traded"))
                .map(strings -> new String[]{
                        strings[1],
                        strings[2],
                        strings[3],
                        strings[4],
                        strings[8],
                        strings[9],
                        strings[10]
                })
                .map(x -> String.join(";", x))
                .toList();
        result.forEach(System.out::println);

        Path path = Path.of(System.getProperty("user.home"), "MOEX securities", args[0] + ".csv");
        Path parentDir = path.getParent();
        if (!Files.exists(parentDir))
            Files.createDirectories(parentDir);
        Files.write(path, result, StandardCharsets.UTF_8);
    }
}
