package com.examchain.blockchain.service;

import com.examchain.blockchain.entity.LedgerTransactionEntity;

public interface FabricGatewayService {
    LedgerTransactionEntity submitTransaction(String transactionName, String entityId, String payloadJson);
    String queryState(String key);
}
