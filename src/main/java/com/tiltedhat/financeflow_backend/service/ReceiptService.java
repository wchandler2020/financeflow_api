package com.tiltedhat.financeflow_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiltedhat.financeflow_backend.dto.ReceiptDataResponse;
import com.tiltedhat.financeflow_backend.entity.Category;
import com.tiltedhat.financeflow_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    @Value("${openai.api-key}")
    private String apiKey;

    private final S3Service s3Service;
    private final CategoryRepository categoryRepository;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReceiptDataResponse processReceipt(MultipartFile file, Long userId) throws IOException {
        // 1. Upload to S3
        String receiptUrl = s3Service.uploadFile(file, userId);

        // 2. Convert image to base64
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String mimeType = file.getContentType();

        // 3. Extract data using OpenAI Vision
        String extractedData = extractReceiptData(base64Image, mimeType);

        // 4. Parse the response
        ReceiptDataResponse receiptData = parseReceiptData(extractedData);
        receiptData.setReceiptUrl(receiptUrl);

        return receiptData;
    }

    private String extractReceiptData(String base64Image, String mimeType) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("max_tokens", 500);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        List<Map<String, Object>> content = new ArrayList<>();

        // Text part
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text",
                "Extract the following information from this receipt:\n" +
                        "1. Merchant/Store name\n" +
                        "2. Total amount (just the number)\n" +
                        "3. Date (in YYYY-MM-DD format)\n" +
                        "4. Category (choose from: Groceries, Dining, Transportation, Shopping, Entertainment, Healthcare, Utilities, Other)\n" +
                        "\n" +
                        "Respond ONLY in this exact format:\n" +
                        "MERCHANT: [name]\n" +
                        "AMOUNT: [number]\n" +
                        "DATE: [YYYY-MM-DD]\n" +
                        "CATEGORY: [category]");
        content.add(textPart);

        // Image part
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
        imagePart.put("image_url", imageUrl);
        content.add(imagePart);

        message.put("content", content);
        messages.add(message);
        requestBody.put("messages", messages);

        String json = objectMapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI Vision API error: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }

    private ReceiptDataResponse parseReceiptData(String extractedText) {
        System.out.println("Extracted text: " + extractedText);

        String merchant = extractField(extractedText, "MERCHANT:");
        String amountStr = extractField(extractedText, "AMOUNT:");
        String dateStr = extractField(extractedText, "DATE:");
        String categoryStr = extractField(extractedText, "CATEGORY:");

        // Parse amount
        BigDecimal amount = BigDecimal.ZERO;
        try {
            // Remove any currency symbols and parse
            amountStr = amountStr.replaceAll("[^0-9.]", "");
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            System.err.println("Error parsing amount: " + amountStr);
        }

        // Parse date
        LocalDate date = LocalDate.now();
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
        }

        // Find matching category
        String finalCategoryStr = categoryStr;
        Category category = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(finalCategoryStr))
                .findFirst()
                .orElse(null);

        return ReceiptDataResponse.builder()
                .merchantName(merchant)
                .amount(amount)
                .date(date)
                .category(category != null ? category.getName() : "Other")
                .description("Receipt from " + merchant)
                .build();
    }

    private String extractField(String text, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
