import java.io.*;
import java.net.*;
import java.util.Base64;

public class SpotifyAuthManual {

    // 🔹 Put your own Client ID and Secret here
    private static final String CLIENT_ID = "22d881d6a91944dca1583bed76adcc14";
    private static final String CLIENT_SECRET = "7458f332b1a74e9d9255f3fcbf2abc84";

    public static void main(String[] args) {
        try {
            // 1. Encode ClientID:ClientSecret to Base64
            String auth = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            // 2. Create POST request
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setDoOutput(true);

            // 3. Send request body
            String body = "grant_type=client_credentials";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            // 4. Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 5. Extract access_token
            String json = response.toString();
            String accessToken = json.split("\"access_token\":\"")[1].split("\"")[0];

            // 6. Save token to file
            try (FileWriter writer = new FileWriter("token.txt")) {
                writer.write(accessToken);
            }

            System.out.println("✅ Access token saved to token.txt!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

