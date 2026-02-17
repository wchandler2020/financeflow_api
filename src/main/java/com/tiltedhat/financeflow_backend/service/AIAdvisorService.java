package com.tiltedhat.financeflow_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.repository.*;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIAdvisorService {

    @Value("${openai.api-key}")
    private String apiKey;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getFinancialAdvice(User user, String userMessage) {
        try {
            // 1. Gather user's financial context
            String financialContext = buildFinancialContext(user);

            // 2. Build messages for ChatGPT
            String systemMessage = buildSystemPrompt();
            String userPrompt = financialContext + "\n\nUser Question: " + userMessage;

            // 3. Call OpenAI API
            String response = callOpenAIAPI(systemMessage, userPrompt);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "I apologize, but I'm having trouble analyzing your financial data right now. Please try again later.";
        }
    }

    private String buildFinancialContext(User user) {
        StringBuilder context = new StringBuilder();

        // Get current month/year
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 1. Account Summary
        var accounts = accountRepository.findByUser(user);
        BigDecimal totalBalance = accounts.stream()
                .map(a -> a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.append("ACCOUNT SUMMARY:\n");
        context.append("Total Balance: $").append(totalBalance).append("\n");
        accounts.forEach(account -> {
            context.append("- ").append(account.getName())
                    .append(" (").append(account.getType()).append("): $")
                    .append(account.getBalance()).append("\n");
        });
        context.append("\n");

        // 2. Current Month Spending by Category
        context.append("CURRENT MONTH SPENDING (").append(now.getMonth()).append(" ").append(currentYear).append("):\n");

        var categories = categoryRepository.findAll();
        Map<String, BigDecimal> categorySpending = new HashMap<>();

        categories.forEach(category -> {
            BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                    user, category.getId(), currentMonth, currentYear
            );
            if (spent != null && spent.compareTo(BigDecimal.ZERO) > 0) {
                categorySpending.put(category.getName(), spent);
            }
        });

        // Sort by amount descending
        categorySpending.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(entry -> {
                    context.append("- ").append(entry.getKey()).append(": $")
                            .append(entry.getValue()).append("\n");
                });
        context.append("\n");

        // 3. Budget Status
        var budgets = budgetRepository.findByUserAndMonthAndYear(user, currentMonth, currentYear);
        if (!budgets.isEmpty()) {
            context.append("BUDGET STATUS:\n");
            budgets.forEach(budget -> {
                BigDecimal spent = transactionRepository.calculateSpendingForCategoryInMonth(
                        user, budget.getCategory().getId(), currentMonth, currentYear
                );
                BigDecimal remaining = budget.getAmount().subtract(spent != null ? spent : BigDecimal.ZERO);
                double percentage = spent != null ?
                        (spent.doubleValue() / budget.getAmount().doubleValue()) * 100 : 0;

                context.append("- ").append(budget.getCategory().getName())
                        .append(": $").append(spent).append(" / $").append(budget.getAmount())
                        .append(" (").append(String.format("%.0f", percentage)).append("% used, $")
                        .append(remaining).append(" remaining)\n");
            });
            context.append("\n");
        }

        // 4. Recent Transactions (last 10)
        var recentTransactions = transactionRepository.findTop10ByUserOrderByTransactionDateDesc(user);
        if (!recentTransactions.isEmpty()) {
            context.append("RECENT TRANSACTIONS:\n");
            recentTransactions.forEach(txn -> {
                context.append("- ").append(txn.getDescription())
                        .append(": $").append(txn.getAmount())
                        .append(" (").append(txn.getCategory().getName()).append(") - ")
                        .append(txn.getTransactionDate()).append("\n");
            });
        }

        return context.toString();
    }

    private String buildSystemPrompt() {
        return "You are a knowledgeable and friendly personal finance advisor. " +
                "You help users understand their spending, create budgets, and make better financial decisions. " +
                "You analyze their actual financial data and provide personalized, actionable advice. " +
                "Be conversational but professional. Use specific numbers from their data in your responses. " +
                "If they're overspending, be honest but encouraging. " +
                "Always provide 2-3 concrete action items they can take. " +
                "Keep responses under 300 words.";
    }

    private String callOpenAIAPI(String systemMessage, String userMessage) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");  // GPT-4 Omni (latest, cheaper than gpt-4-turbo)
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemMessage),
                Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

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
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                System.err.println("OpenAI API error: " + response.code() + " - " + errorBody);
                throw new IOException("OpenAI API error: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }
}