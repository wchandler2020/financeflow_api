package com.tiltedhat.financeflow_backend.dto;


import com.tiltedhat.financeflow_backend.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {


    public Account toEntity(CreateAccountRequest request) {
        Account account = new Account();
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        account.setDescription(request.getDescription());

        // Don't set:
        // - id (auto-generated)
        // - user (set in service layer)
        // - createdAt/updatedAt (set by @PrePersist/@PreUpdate)

        return account;
    }

    /**
     * Convert Account Entity to AccountResponse DTO
     */
    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getCurrency(),
                account.getDescription(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
