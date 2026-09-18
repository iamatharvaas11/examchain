package com.examchain.blockchain.entity;

import com.examchain.blockchain.model.LedgerTransactionStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_transactions")
public class LedgerTransactionEntity {

    @Id
    @Column(name = "tx_id", nullable = false, length = 64)
    private String txId;

    @Column(name = "channel_id", nullable = false, length = 32)
    private String channelId;

    @Column(name = "chaincode_id", nullable = false, length = 32)
    private String chaincodeId;

    @Column(name = "transaction_name", nullable = false, length = 64)
    private String transactionName;

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private LedgerTransactionStatus status;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerTransactionEntity() {
        this.createdAt = Instant.now();
        this.channelId = "examchain-channel";
        this.chaincodeId = "examchain-cc";
        this.status = LedgerTransactionStatus.COMMITTED;
    }

    public LedgerTransactionEntity(
            String txId,
            String channelId,
            String chaincodeId,
            String transactionName,
            String entityId,
            String payloadJson,
            LedgerTransactionStatus status,
            Long blockNumber
    ) {
        this.txId = txId;
        this.channelId = channelId != null ? channelId : "examchain-channel";
        this.chaincodeId = chaincodeId != null ? chaincodeId : "examchain-cc";
        this.transactionName = transactionName;
        this.entityId = entityId;
        this.payloadJson = payloadJson;
        this.status = status;
        this.blockNumber = blockNumber;
        this.createdAt = Instant.now();
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChaincodeId() {
        return chaincodeId;
    }

    public void setChaincodeId(String chaincodeId) {
        this.chaincodeId = chaincodeId;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public LedgerTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(LedgerTransactionStatus status) {
        this.status = status;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
