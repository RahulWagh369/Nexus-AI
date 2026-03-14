package com.example.demo; // Make sure this matches your actual package name!

import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class ChatController {

    // Your Groq API Key is now securely in the backend
	private static final String API_KEY = System.getenv("GROQ_API_KEY");
	private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @PostMapping(value = "/chat", produces = "application/json")
    public String handleChat(@RequestBody ChatRequest request) {
        
        String userMessage = request.getPrompt();
        System.out.println("Message received from terminal: " + userMessage);

        // Escape quotes so the JSON doesn't break if the user types a quote
        String safeMessage = userMessage.replace("\"", "\\\"");

        // 1. Prepare the JSON payload exactly how the AI expects it
        // We are using the extremely fast Llama 3 model!
        String requestBody = """
                {
                  "model": "llama-3.1-8b-instant",
                  "messages": [{"role": "user", "content": "%s"}]
                }
                """.formatted(safeMessage);

        try {
            // 2. Build the outgoing HTTP Request to Groq
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 3. Send the request and wait for the AI to reply
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 4. Return the AI's response directly to your terminal
            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to connect to AI\"}";
        }
    }
}

class ChatRequest {
    private String prompt;
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}