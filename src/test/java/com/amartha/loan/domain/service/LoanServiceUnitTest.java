package com.amartha.loan.domain.service;

import com.amartha.loan.domain.event.LoanFullyFundedEvent;
import com.amartha.loan.domain.exception.InvalidLoanStateException;
import com.amartha.loan.domain.exception.InvestmentExceedsPrincipalException;
import com.amartha.loan.domain.exception.LoanNotFoundException;
import com.amartha.loan.domain.model.*;
import com.amartha.loan.domain.repository.*;
import com.amartha.loan.domain.validator.LoanStateValidator;
import com.amartha.loan.infrastructure.pdf.AgreementLetterGenerator;
import com.amartha.loan.testdata.LoanTestDataBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceUnitTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInvestmentRepository loanInvestmentRepository;

    @Mock
    private LoanApprovalRepository loanApprovalRepository;

    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;

    @Mock
    private LoanStateValidator loanStateValidator;

    @Mock
    private AgreementLetterGenerator agreementLetterGenerator;

    @Mock
    private Event<LoanFullyFundedEvent> loanFullyFundedEvent;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
       
    }

    // ==================== Create Loan Tests ====================

    @Test
    void createLoanReactive_validData_returnsLoanWithProposedStatus() {

        Integer borrowerId = 1;
        BigDecimal principalAmount = new BigDecimal("5000000.00");
        BigDecimal rate = new BigDecimal("5.5");
        BigDecimal roi = new BigDecimal("12.0");

        Loan capturedLoan = new Loan();
        when(loanRepository.persist(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            capturedLoan.id = loan.id;
            capturedLoan.borrowerId = loan.borrowerId;
            capturedLoan.principalAmount = loan.principalAmount;
            capturedLoan.rate = loan.rate;
            capturedLoan.roi = loan.roi;
            capturedLoan.status = loan.status;
            return Uni.createFrom().voidItem();
        });

        
        Uni<Loan> result = loanService.createLoanReactive(borrowerId, principalAmount, rate, roi);
        Loan loan = result.await().indefinitely();

        
        assertNotNull(loan);
        assertEquals(borrowerId, loan.borrowerId);
        assertEquals(principalAmount, loan.principalAmount);
        assertEquals(rate, loan.rate);
        assertEquals(roi, loan.roi);
        assertEquals(LoanStatus.PROPOSED, loan.status);
        verify(loanRepository).persist(any(Loan.class));
    }

    // ==================== Get Loan Tests ====================

    @Test
    void getLoan_existingLoan_returnsLoan() {
        
        Long loanId = 1L;
        Loan expectedLoan = LoanTestDataBuilder.proposedLoan();
        when(loanRepository.findByIdSync(loanId)).thenReturn(expectedLoan);

        
        Loan result = loanService.getLoan(loanId);

        
        assertNotNull(result);
        assertEquals(expectedLoan.id, result.id);
        assertEquals(expectedLoan.status, result.status);
        verify(loanRepository).findByIdSync(loanId);
    }

    @Test
    void getLoan_nonExistentLoan_throwsNotFoundException() {
        
        Long loanId = 999L;
        when(loanRepository.findByIdSync(loanId)).thenReturn(null);

         & Assert
        assertThrows(LoanNotFoundException.class, () -> loanService.getLoan(loanId));
        verify(loanRepository).findByIdSync(loanId);
    }

    // ==================== Approve Loan Tests ====================

    @Test
    void approveLoan_proposedLoan_updatesStatusAndGeneratesAgreement() throws IOException {
        
        Long loanId = 1L;
        Integer employeeId = 1;
        String photoProofPath = "/path/to/photo.jpg";
        LocalDate approvalDate = LocalDate.now();
        String agreementPath = "/path/to/agreement.pdf";

        Loan proposedLoan = LoanTestDataBuilder.proposedLoan();
        when(loanRepository.findByIdSync(loanId)).thenReturn(proposedLoan);
        when(agreementLetterGenerator.generateAgreementLetter(anyString(), anyString(), anyString()))
                .thenReturn(agreementPath);
        doNothing().when(loanStateValidator).validateTransitionToApproved(LoanStatus.PROPOSED);

        
        Loan result = loanService.approveLoan(loanId, employeeId, photoProofPath, approvalDate);

        
        assertNotNull(result);
        assertEquals(LoanStatus.APPROVED, result.status);
        assertEquals(agreementPath, result.agreementLetterUrl);
        verify(loanStateValidator).validateTransitionToApproved(LoanStatus.PROPOSED);
        verify(agreementLetterGenerator).generateAgreementLetter(anyString(), anyString(), anyString());
        verify(loanRepository).persist(result);
        verify(loanApprovalRepository).persist(any(LoanApproval.class));
    }

    @Test
    void approveLoan_alreadyApprovedLoan_throwsException() throws IOException {
        
        Long loanId = 2L;
        Loan approvedLoan = LoanTestDataBuilder.approvedLoan();
        when(loanRepository.findByIdSync(loanId)).thenReturn(approvedLoan);
        doThrow(new InvalidLoanStateException("Cannot approve loan in APPROVED state"))
                .when(loanStateValidator).validateTransitionToApproved(LoanStatus.APPROVED);

         & Assert
        assertThrows(InvalidLoanStateException.class, 
                () -> loanService.approveLoan(loanId, 1, "/path", LocalDate.now()));
        verify(loanStateValidator).validateTransitionToApproved(LoanStatus.APPROVED);
        verify(loanRepository, never()).persist(any(Loan.class));
    }

    // ==================== Add Investment Tests ====================

    @Test
    void addInvestment_partialFunding_maintainsApprovedStatus() {
        
        Long loanId = 2L;
        Integer investorId = 101;
        BigDecimal investmentAmount = new BigDecimal("1000000.00");
        
        Loan approvedLoan = LoanTestDataBuilder.approvedLoan();
        approvedLoan.principalAmount = new BigDecimal("5000000.00");
        
        when(loanRepository.findByIdSync(loanId)).thenReturn(approvedLoan);
        when(loanInvestmentRepository.sumAmountByLoanId(loanId)).thenReturn(BigDecimal.ZERO);
        doNothing().when(loanStateValidator).validateTransitionToInvested(LoanStatus.APPROVED);
        doNothing().when(loanStateValidator).validateInvestmentAmount(any(), any(), any());

        
        Loan result = loanService.addInvestment(loanId, investorId, investmentAmount);

        
        assertNotNull(result);
        assertEquals(LoanStatus.APPROVED, result.status); // Still APPROVED, not fully funded
        verify(loanInvestmentRepository).persist(any(LoanInvestment.class));
        verify(loanFullyFundedEvent, never()).fire(any());
    }

    @Test
    void addInvestment_exactFunding_transitionsToInvestedAndFiresEvent() {
        
        Long loanId = 2L;
        Integer investorId = 101;
        BigDecimal principalAmount = new BigDecimal("5000000.00");
        BigDecimal existingInvestment = new BigDecimal("3000000.00");
        BigDecimal newInvestment = new BigDecimal("2000000.00");
        
        Loan approvedLoan = LoanTestDataBuilder.approvedLoan();
        approvedLoan.principalAmount = principalAmount;
        
        LoanInvestment investment1 = LoanTestDataBuilder.investment(loanId, existingInvestment);
        LoanInvestment investment2 = LoanTestDataBuilder.investment(loanId, newInvestment);
        
        when(loanRepository.findByIdSync(loanId)).thenReturn(approvedLoan);
        when(loanInvestmentRepository.sumAmountByLoanId(loanId)).thenReturn(existingInvestment);
        when(loanInvestmentRepository.findByLoanId(loanId)).thenReturn(List.of(investment1, investment2));
        doNothing().when(loanStateValidator).validateTransitionToInvested(LoanStatus.APPROVED);
        doNothing().when(loanStateValidator).validateInvestmentAmount(any(), any(), any());

        
        Loan result = loanService.addInvestment(loanId, investorId, newInvestment);

        
        assertNotNull(result);
        assertEquals(LoanStatus.INVESTED, result.status);
        verify(loanInvestmentRepository).persist(any(LoanInvestment.class));
        verify(loanRepository).persist(result);
        
        ArgumentCaptor<LoanFullyFundedEvent> eventCaptor = ArgumentCaptor.forClass(LoanFullyFundedEvent.class);
        verify(loanFullyFundedEvent).fire(eventCaptor.capture());
        
        LoanFullyFundedEvent firedEvent = eventCaptor.getValue();
        assertEquals(loanId, firedEvent.getLoanId());
        assertEquals(approvedLoan.borrowerId, firedEvent.getBorrowerId());
    }

    @Test
    void addInvestment_exceedsPrincipal_throwsException() {
        
        Long loanId = 2L;
        Integer investorId = 101;
        BigDecimal principalAmount = new BigDecimal("5000000.00");
        BigDecimal existingInvestment = new BigDecimal("4000000.00");
        BigDecimal newInvestment = new BigDecimal("2000000.00"); // Would exceed principal
        
        Loan approvedLoan = LoanTestDataBuilder.approvedLoan();
        approvedLoan.principalAmount = principalAmount;
        
        when(loanRepository.findByIdSync(loanId)).thenReturn(approvedLoan);
        when(loanInvestmentRepository.sumAmountByLoanId(loanId)).thenReturn(existingInvestment);
        doNothing().when(loanStateValidator).validateTransitionToInvested(LoanStatus.APPROVED);
        doThrow(new InvestmentExceedsPrincipalException(principalAmount, existingInvestment, newInvestment))
                .when(loanStateValidator).validateInvestmentAmount(principalAmount, existingInvestment, newInvestment);

         & Assert
        assertThrows(InvestmentExceedsPrincipalException.class, 
                () -> loanService.addInvestment(loanId, investorId, newInvestment));
        verify(loanInvestmentRepository, never()).persist(any(LoanInvestment.class));
    }

    @Test
    void addInvestment_proposedLoan_throwsException() {
        
        Long loanId = 1L;
        Loan proposedLoan = LoanTestDataBuilder.proposedLoan();
        
        when(loanRepository.findByIdSync(loanId)).thenReturn(proposedLoan);
        doThrow(new InvalidLoanStateException("Cannot invest in PROPOSED loan"))
                .when(loanStateValidator).validateTransitionToInvested(LoanStatus.PROPOSED);

         & Assert
        assertThrows(InvalidLoanStateException.class, 
                () -> loanService.addInvestment(loanId, 101, new BigDecimal("1000000.00")));
        verify(loanInvestmentRepository, never()).persist(any(LoanInvestment.class));
    }

    // ==================== Disburse Loan Tests ====================

    @Test
    void disburseLoan_investedLoan_updatesStatusAndSavesDisbursement() {
        
        Long loanId = 3L;
        Integer employeeId = 2;
        String signedAgreementPath = "/path/to/signed.pdf";
        LocalDate disbursementDate = LocalDate.now();
        
        Loan investedLoan = LoanTestDataBuilder.investedLoan();
        when(loanRepository.findByIdSync(loanId)).thenReturn(investedLoan);
        doNothing().when(loanStateValidator).validateTransitionToDisbursed(LoanStatus.INVESTED);

        
        Loan result = loanService.disburseLoan(loanId, employeeId, signedAgreementPath, disbursementDate);

        
        assertNotNull(result);
        assertEquals(LoanStatus.DISBURSED, result.status);
        verify(loanStateValidator).validateTransitionToDisbursed(LoanStatus.INVESTED);
        verify(loanRepository).persist(result);
        verify(loanDisbursementRepository).persist(any(LoanDisbursement.class));
    }

    @Test
    void disburseLoan_approvedLoan_throwsException() {
        
        Long loanId = 2L;
        Loan approvedLoan = LoanTestDataBuilder.approvedLoan();
        
        when(loanRepository.findByIdSync(loanId)).thenReturn(approvedLoan);
        doThrow(new InvalidLoanStateException("Cannot disburse loan in APPROVED state"))
                .when(loanStateValidator).validateTransitionToDisbursed(LoanStatus.APPROVED);

         & Assert
        assertThrows(InvalidLoanStateException.class, 
                () -> loanService.disburseLoan(loanId, 2, "/path", LocalDate.now()));
        verify(loanStateValidator).validateTransitionToDisbursed(LoanStatus.APPROVED);
        verify(loanRepository, never()).persist(any(Loan.class));
    }

    // ==================== Get Investments Tests ====================

    @Test
    void getInvestments_existingLoan_returnsAllInvestments() {
        
        Long loanId = 2L;
        List<LoanInvestment> expectedInvestments = List.of(
                LoanTestDataBuilder.investment(loanId, new BigDecimal("1000000.00")),
                LoanTestDataBuilder.investment(loanId, new BigDecimal("2000000.00"))
        );
        
        when(loanInvestmentRepository.findByLoanId(loanId)).thenReturn(expectedInvestments);

        
        List<LoanInvestment> result = loanService.getInvestments(loanId);

        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanInvestmentRepository).findByLoanId(loanId);
    }

    @Test
    void getInvestments_noInvestments_returnsEmptyList() {
        
        Long loanId = 1L;
        when(loanInvestmentRepository.findByLoanId(loanId)).thenReturn(List.of());

        
        List<LoanInvestment> result = loanService.getInvestments(loanId);

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanInvestmentRepository).findByLoanId(loanId);
    }
}
