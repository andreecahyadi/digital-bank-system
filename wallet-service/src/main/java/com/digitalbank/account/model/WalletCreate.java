package com.digitalbank.account.model;

import lombok.Data;

@Data
public class WalletCreate {
    private Long userId;
    private String currency;
}