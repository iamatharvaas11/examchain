package com.examchain.blockchain;

import com.examchain.blockchain.dto.BlockchainDtos.LedgerTransactionDto;
import com.examchain.blockchain.entity.LedgerTransactionEntity;
import com.examchain.blockchain.model.LedgerTransactionStatus;
import com.examchain.blockchain.repository.LedgerTransactionRepository;
import com.examchain.blockchain.service.DefaultFabricGatewayService;
import com.examchain.blockchain.service.FabricLedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FabricLedgerIntegrationTest {

    private LedgerTransactionRepository repository;
    private DefaultFabricGatewayService gatewayService;
    private FabricLedgerService ledgerService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(LedgerTransactionRepository.class);
        when(repository.save(any(LedgerTransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

        gatewayService = new DefaultFabricGatewayService(repository, false, "examchain-channel", "examchain-cc");
        ledgerService = new FabricLedgerService(gatewayService, repository);
    }

    @Test
    @DisplayName("RegisterQuestionPool creates ledger transaction with SHA-256 txId and COMMITTED status")
    void testRegisterQuestionPool() {
        LedgerTransactionDto tx = ledgerService.registerQuestionPool(
                "POOL-CS101", "CS101", 50, "hash-cs101-pool", "SETTER-001"
        );

        assertThat(tx.txId()).isNotNull();
        assertThat(tx.txId()).hasSize(64);
        assertThat(tx.status()).isEqualTo(LedgerTransactionStatus.COMMITTED);
        assertThat(tx.transactionName()).isEqualTo("RegisterQuestionPool");
        assertThat(tx.entityId()).isEqualTo("POOL-CS101");
        assertThat(tx.payloadJson()).contains("hash-cs101-pool");
    }

    @Test
    @DisplayName("RegisterPaper commits immutable paper cryptographic hash to ledger")
    void testRegisterPaper() {
        LedgerTransactionDto tx = ledgerService.registerPaper(
                "PAP-CS101-A", "EXAM-CS101", "SET_A", 100, "paper-sha256-hash", "KEY-VAULT-01"
        );

        assertThat(tx.txId()).hasSize(64);
        assertThat(tx.status()).isEqualTo(LedgerTransactionStatus.COMMITTED);
        assertThat(tx.transactionName()).isEqualTo("RegisterPaper");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("paper-sha256-hash");
    }

    @Test
    @DisplayName("RecordApproval registers cryptographic authority approval")
    void testRecordApproval() {
        LedgerTransactionDto tx = ledgerService.recordApproval(
                "PAP-CS101-A", "AUTH-01", "EXAM_AUTHORITY"
        );

        assertThat(tx.transactionName()).isEqualTo("RecordApproval");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("AUTH-01");
    }

    @Test
    @DisplayName("AuthorizeRelease records time-lock release event")
    void testAuthorizeRelease() {
        LedgerTransactionDto tx = ledgerService.authorizeRelease(
                "PAP-CS101-A", "CONTROLLER-CHIEF"
        );

        assertThat(tx.transactionName()).isEqualTo("AuthorizeRelease");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("CONTROLLER-CHIEF");
    }

    @Test
    @DisplayName("RecordAccess logs operator decryption and print event")
    void testRecordAccess() {
        LedgerTransactionDto tx = ledgerService.recordAccess(
                "PAP-CS101-A", "CENTRE-DELHI-01", "OP-101", "PRINT_DECRYPT", "SUCCESS"
        );

        assertThat(tx.transactionName()).isEqualTo("RecordAccess");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("PRINT_DECRYPT");
        assertThat(tx.payloadJson()).contains("CENTRE-DELHI-01");
    }

    @Test
    @DisplayName("QuarantineVariant flags variant on ledger during security incident")
    void testQuarantineVariant() {
        LedgerTransactionDto tx = ledgerService.quarantineVariant(
                "PAP-CS101-A", "INC-999", "Suspicious print attempt", "CONTROLLER-SEC"
        );

        assertThat(tx.transactionName()).isEqualTo("QuarantineVariant");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("INC-999");
        assertThat(tx.payloadJson()).contains("Suspicious print attempt");
    }

    @Test
    @DisplayName("ReplaceVariant links quarantined paper to reserve replacement on ledger")
    void testReplaceVariant() {
        LedgerTransactionDto tx = ledgerService.replaceVariant(
                "PAP-CS101-A", "PAP-CS101-RESERVE-B", "CONTROLLER-SEC"
        );

        assertThat(tx.transactionName()).isEqualTo("ReplaceVariant");
        assertThat(tx.entityId()).isEqualTo("PAP-CS101-A");
        assertThat(tx.payloadJson()).contains("PAP-CS101-RESERVE-B");
    }

    @Test
    @DisplayName("Query transactions for entity returns ordered transaction trail")
    void testQueryTransactionsForEntity() {
        LedgerTransactionEntity entity1 = new LedgerTransactionEntity(
                "tx-1", "examchain-channel", "examchain-cc", "RegisterPaper", "PAP-1", "{}", LedgerTransactionStatus.COMMITTED, 101L
        );
        LedgerTransactionEntity entity2 = new LedgerTransactionEntity(
                "tx-2", "examchain-channel", "examchain-cc", "RecordApproval", "PAP-1", "{}", LedgerTransactionStatus.COMMITTED, 102L
        );

        when(repository.findByEntityIdOrderByCreatedAtDesc("PAP-1")).thenReturn(List.of(entity2, entity1));

        List<LedgerTransactionDto> results = ledgerService.getTransactionsForEntity("PAP-1");
        assertThat(results).hasSize(2);
        assertThat(results.get(0).txId()).isEqualTo("tx-2");
        assertThat(results.get(1).txId()).isEqualTo("tx-1");
    }
}

