package main

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// ExamchainContract manages paper lifecycle and provenance on Hyperledger Fabric
type ExamchainContract struct {
	contractapi.Contract
}

// ApprovalRecord stores multi-authority endorsement
type ApprovalRecord struct {
	AuthorityID string `json:"authorityId"`
	Role        string `json:"role"`
	Timestamp   string `json:"timestamp"`
}

// AccessLogEntry records centre decryption and printing events
type AccessLogEntry struct {
	CentreID   string `json:"centreId"`
	OperatorID string `json:"operatorId"`
	Action     string `json:"action"`
	Outcome    string `json:"outcome"`
	Timestamp  string `json:"timestamp"`
}

// QuestionPoolRecord represents an approved question pool committed to ledger
type QuestionPoolRecord struct {
	PoolID        string `json:"poolId"`
	SubjectCode   string `json:"subjectCode"`
	QuestionCount int    `json:"questionCount"`
	PoolHash      string `json:"poolHash"`
	SetterID      string `json:"setterId"`
	Status        string `json:"status"`
	CreatedAt     string `json:"createdAt"`
}

// PaperRecord represents an immutable confidential paper record on ledger
type PaperRecord struct {
	PaperID             string           `json:"paperId"`
	ExamCode            string           `json:"examCode"`
	SetCode             string           `json:"setCode"`
	TotalMarks          int              `json:"totalMarks"`
	PaperHash           string           `json:"paperHash"`
	EnclaveKeyID        string           `json:"enclaveKeyId"`
	Status              string           `json:"status"` // GENERATED, APPROVED, RELEASED, QUARANTINED, REPLACED
	Approvals           []ApprovalRecord `json:"approvals"`
	ReleaseAuthorizedAt string           `json:"releaseAuthorizedAt"`
	AuthorizedBy        string           `json:"authorizedBy"`
	AccessLogs          []AccessLogEntry `json:"accessLogs"`
	QuarantinedReason   string           `json:"quarantinedReason"`
	ReplacementPaperID  string           `json:"replacementPaperId"`
	CreatedAt           string           `json:"createdAt"`
	UpdatedAt           string           `json:"updatedAt"`
}

// InitLedger initializes chaincode
func (c *ExamchainContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("EXAMCHAIN Ledger Initialized")
	return nil
}

// RegisterQuestionPool records an approved question pool hash to the ledger
func (c *ExamchainContract) RegisterQuestionPool(
	ctx contractapi.TransactionContextInterface,
	poolID string,
	subjectCode string,
	questionCount int,
	poolHash string,
	setterID string,
) error {
	exists, err := ctx.GetStub().GetState("POOL_" + poolID)
	if err != nil {
		return fmt.Errorf("failed to read from world state: %v", err)
	}
	if exists != nil {
		return fmt.Errorf("question pool %s already registered on ledger", poolID)
	}

	record := QuestionPoolRecord{
		PoolID:        poolID,
		SubjectCode:   subjectCode,
		QuestionCount: questionCount,
		PoolHash:      poolHash,
		SetterID:      setterID,
		Status:        "APPROVED",
		CreatedAt:     time.Now().UTC().Format(time.RFC3339),
	}

	recordJSON, err := json.Marshal(record)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("POOL_"+poolID, recordJSON)
}

// RegisterPaper commits a generated paper cryptographic hash to the ledger
func (c *ExamchainContract) RegisterPaper(
	ctx contractapi.TransactionContextInterface,
	paperID string,
	examCode string,
	setCode string,
	totalMarks int,
	paperHash string,
	enclaveKeyID string,
) error {
	exists, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return fmt.Errorf("failed to read world state: %v", err)
	}
	if exists != nil {
		return fmt.Errorf("paper %s already exists on ledger", paperID)
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper := PaperRecord{
		PaperID:      paperID,
		ExamCode:     examCode,
		SetCode:      setCode,
		TotalMarks:   totalMarks,
		PaperHash:    paperHash,
		EnclaveKeyID: enclaveKeyID,
		Status:       "GENERATED",
		Approvals:    []ApprovalRecord{},
		AccessLogs:   []AccessLogEntry{},
		CreatedAt:    now,
		UpdatedAt:    now,
	}

	paperJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+paperID, paperJSON)
}

// RecordApproval logs a cryptographic approval from an examination authority
func (c *ExamchainContract) RecordApproval(
	ctx contractapi.TransactionContextInterface,
	paperID string,
	authorityID string,
	authorityRole string,
) error {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return err
	}
	if paperJSON == nil {
		return fmt.Errorf("paper %s does not exist", paperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return err
	}

	if paper.Status == "QUARANTINED" {
		return fmt.Errorf("cannot approve quarantined paper %s", paperID)
	}

	// Check if already approved by this authority
	for _, a := range paper.Approvals {
		if a.AuthorityID == authorityID {
			return fmt.Errorf("authority %s has already approved paper %s", authorityID, paperID)
		}
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper.Approvals = append(paper.Approvals, ApprovalRecord{
		AuthorityID: authorityID,
		Role:        authorityRole,
		Timestamp:   now,
	})
	paper.UpdatedAt = now

	// If quorum reached (e.g. 2 or more approvals), update status
	if len(paper.Approvals) >= 2 {
		paper.Status = "APPROVED"
	}

	updatedJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+paperID, updatedJSON)
}

// AuthorizeRelease authorizes the time-lock release of a paper
func (c *ExamchainContract) AuthorizeRelease(
	ctx contractapi.TransactionContextInterface,
	paperID string,
	authorizedBy string,
) error {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return err
	}
	if paperJSON == nil {
		return fmt.Errorf("paper %s does not exist", paperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return err
	}

	if paper.Status != "APPROVED" {
		return fmt.Errorf("paper %s must be in APPROVED state before release (current: %s)", paperID, paper.Status)
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper.Status = "RELEASED"
	paper.ReleaseAuthorizedAt = now
	paper.AuthorizedBy = authorizedBy
	paper.UpdatedAt = now

	updatedJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+paperID, updatedJSON)
}

// RecordAccess records a centre operator's decryption or print access
func (c *ExamchainContract) RecordAccess(
	ctx contractapi.TransactionContextInterface,
	paperID string,
	centreID string,
	operatorID string,
	action string,
	outcome string,
) error {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return err
	}
	if paperJSON == nil {
		return fmt.Errorf("paper %s does not exist", paperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return err
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper.AccessLogs = append(paper.AccessLogs, AccessLogEntry{
		CentreID:   centreID,
		OperatorID: operatorID,
		Action:     action,
		Outcome:    outcome,
		Timestamp:  now,
	})
	paper.UpdatedAt = now

	updatedJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+paperID, updatedJSON)
}

// QuarantineVariant flags a compromised paper variant and blocks further access
func (c *ExamchainContract) QuarantineVariant(
	ctx contractapi.TransactionContextInterface,
	paperID string,
	incidentID string,
	reason string,
	quarantinedBy string,
) error {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return err
	}
	if paperJSON == nil {
		return fmt.Errorf("paper %s does not exist", paperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return err
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper.Status = "QUARANTINED"
	paper.QuarantinedReason = fmt.Sprintf("Incident %s: %s (by %s)", incidentID, reason, quarantinedBy)
	paper.UpdatedAt = now

	updatedJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+paperID, updatedJSON)
}

// ReplaceVariant marks a quarantined paper as replaced by a reserve variant
func (c *ExamchainContract) ReplaceVariant(
	ctx contractapi.TransactionContextInterface,
	quarantinedPaperID string,
	replacementPaperID string,
	authorizedBy string,
) error {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + quarantinedPaperID)
	if err != nil {
		return err
	}
	if paperJSON == nil {
		return fmt.Errorf("paper %s does not exist", quarantinedPaperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return err
	}

	if paper.Status != "QUARANTINED" {
		return fmt.Errorf("only quarantined papers can be replaced (current status: %s)", paper.Status)
	}

	now := time.Now().UTC().Format(time.RFC3339)
	paper.Status = "REPLACED"
	paper.ReplacementPaperID = replacementPaperID
	paper.UpdatedAt = now

	updatedJSON, err := json.Marshal(paper)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState("PAPER_"+quarantinedPaperID, updatedJSON)
}

// QueryPaper returns the on-chain paper state
func (c *ExamchainContract) QueryPaper(ctx contractapi.TransactionContextInterface, paperID string) (*PaperRecord, error) {
	paperJSON, err := ctx.GetStub().GetState("PAPER_" + paperID)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if paperJSON == nil {
		return nil, fmt.Errorf("paper %s does not exist on ledger", paperID)
	}

	var paper PaperRecord
	if err := json.Unmarshal(paperJSON, &paper); err != nil {
		return nil, err
	}

	return &paper, nil
}

// QueryQuestionPool returns the on-chain question pool record
func (c *ExamchainContract) QueryQuestionPool(ctx contractapi.TransactionContextInterface, poolID string) (*QuestionPoolRecord, error) {
	poolJSON, err := ctx.GetStub().GetState("POOL_" + poolID)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if poolJSON == nil {
		return nil, fmt.Errorf("question pool %s does not exist on ledger", poolID)
	}

	var pool QuestionPoolRecord
	if err := json.Unmarshal(poolJSON, &pool); err != nil {
		return nil, err
	}

	return &pool, nil
}

func main() {
	cc, err := contractapi.NewChaincode(&ExamchainContract{})
	if err != nil {
		fmt.Printf("Error creating examchain chaincode: %s\n", err.Error())
		return
	}

	if err := cc.Start(); err != nil {
		fmt.Printf("Error starting examchain chaincode: %s\n", err.Error())
	}
}

