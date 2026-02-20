import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class SongChatbot {

    /* ================= TOKEN HANDLING ================= */

    public static void ensureSpotifyToken() {
        File tokenFile = new File("token.txt");
        if (!tokenFile.exists()) {
            System.out.println("🔐 No token found. Starting Spotify authentication...");
            runSpotifyAuthManual();
        }
    }

    private static void runSpotifyAuthManual() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", ".;lib/json-20240303.jar", "SpotifyAuthManual"
            );
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
            System.out.println("✅ Spotify authentication successful.");
        } catch (Exception e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
            System.exit(1);
        }
    }

    public static String readToken() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("token.txt"));
        String token = br.readLine();
        br.close();

        if (token == null || token.isEmpty()) {
            throw new IOException("Token missing");
        }
        return token;
    }

    /* ================= CHATBOT UI ================= */

    public static void showMenu() {
        System.out.println("\n🎵 ===== SONG BUDDY MENU ===== 🎵");
        System.out.println("1️⃣ Mood-based songs");
        System.out.println("2️⃣ Search by artist");
        System.out.println("3️⃣ Search by song name");
        System.out.println("4️⃣ Surprise me 🎲");
        System.out.println("help - Show commands");
        System.out.println("exit - Quit chatbot");
    }

    public static void showHelp() {
        System.out.println("\n🆘 HELP:");
        System.out.println("• Moods: happy, sad, relaxed, angry, romantic");
        System.out.println("• You can search artist or song directly");
        System.out.println("• Type exit to quit");
    }

    /* ================= MOOD INTELLIGENCE ================= */

    public static String moodToQuery(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "happy pop upbeat";
            case "sad": return "sad acoustic emotional";
            case "relaxed": return "chill lofi calm";
            case "angry": return "rock metal aggressive";
            case "romantic": return "romantic love melody";
            default: return mood;
        }
    }

    /* ================= SPOTIFY SEARCH ================= */

    public static void searchSpotify(String query) throws Exception {
        boolean retry;
        String token = readToken();

        do {
            retry = false;

            String apiUrl =
                "https://api.spotify.com/v1/search?q=" +
                URLEncoder.encode(query, "UTF-8") +
                "&type=track&limit=5";

            HttpURLConnection conn =
                (HttpURLConnection) new URL(apiUrl).openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            if (conn.getResponseCode() == 401) {
                System.out.println("🔄 Token expired. Re-authenticating...");
                runSpotifyAuthManual();
                token = readToken();
                retry = true;
                continue;
            }

            BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
                response.append(line);

            in.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray tracks =
                json.getJSONObject("tracks").getJSONArray("items");

            if (tracks.length() == 0) {
                System.out.println("❌ No songs found.");
                return;
            }

            System.out.println("\n🎧 Recommended Songs:");
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i);
                String trackName = track.getString("name");

                JSONArray artists = track.getJSONArray("artists");
                StringBuilder artistNames = new StringBuilder();
                for (int j = 0; j < artists.length(); j++) {
                    artistNames.append(
                        artists.getJSONObject(j).getString("name")
                    );
                    if (j < artists.length() - 1)
                        artistNames.append(", ");
                }

                String link =
                    track.getJSONObject("external_urls").getString("spotify");

                System.out.println(
                    (i + 1) + ". " + trackName + " by " + artistNames
                );
                System.out.println("   🔗 " + link);
            }

        } while (retry);
    }

    /* ================= MAIN ================= */

    public static void main(String[] args) {
        ensureSpotifyToken();

        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("🎶 Welcome to SONG BUDDY 🤖");
        System.out.println("Your  DR smart  music assistant!");

        while (true) {
            showMenu();
            System.out.print("\n👉 Enter choice: ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("👋 Goodbye! Keep listening 🎧");
                break;
            }

            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }

            try {
                switch (input) {
                    case "1":
                        System.out.print("😊 Enter your mood: ");
                        String mood = sc.nextLine();
                        searchSpotify(moodToQuery(mood));
                        break;

                    case "2":
                        System.out.print("🎤 Enter artist name: ");
                        searchSpotify(sc.nextLine());
                        break;

                    case "3":
                        System.out.print("🎵 Enter song name: ");
                        searchSpotify(sc.nextLine());
                        break;

                    case "4":
                        String[] moods = {
                            "happy", "sad", "relaxed", "romantic", "angry"
                        };
                        String randomMood = moods[rand.nextInt(moods.length)];
                        System.out.println("🎲 Surprise mood: " + randomMood);
                        searchSpotify(moodToQuery(randomMood));
                        break;

                    default:
                        System.out.println("❌ Invalid option. Type help.");
                }
            } catch (Exception e) {
                System.out.println("⚠ Error: " + e.getMessage());
            }
        }

        sc.close();
    }
}
