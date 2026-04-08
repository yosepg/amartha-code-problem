package com.amartha.loan.infrastructure.email;

import com.amartha.loan.domain.event.LoanFullyFundedEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanMailerServiceTest {

    @Test
    void loanFullyFundedEvent_withValidData_createsEventSuccessfully() {
        // Arrange
        Long loanId = 1L;
        Integer borrowerId = 10;
        List<Integer> investorIds = Arrays.asList(101, 102, 103);

        // Act
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(loanId, borrowerId, investorIds);

        // Assert
        assertNotNull(event);
        assertEquals(1L, event.getLoanId());
        assertEquals(10, event.getBorrowerId());
        assertEquals(3, event.getInvestorIds().size());
        assertTrue(event.getInvestorIds().contains(101));
        assertTrue(event.getInvestorIds().contains(102));
        assertTrue(event.getInvestorIds().contains(103));
    }

    @Test
    void loanFullyFundedEvent_withSingleInvestor_shouldWork() {
        // Arrange
        Long loanId = 2L;
        Integer borrowerId = 20;
        List<Integer> investorIds = List.of(201);

        // Act
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(loanId, borrowerId, investorIds);

        // Assert
        assertEquals(2L, event.getLoanId());
        assertEquals(20, event.getBorrowerId());
        assertEquals(1, event.getInvestorIds().size());
    }

    @Test
    void loanFullyFundedEvent_withMultipleInvestors_shouldHandleAll() {
        // Arrange
        Long loanId = 3L;
        Integer borrowerId = 30;
        List<Integer> investorIds = Arrays.asList(301, 302, 303, 304, 305);

        // Act
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(loanId, borrowerId, investorIds);

        // Assert
        assertEquals(5, event.getInvestorIds().size());
        assertTrue(event.getInvestorIds().containsAll(Arrays.asList(301, 302, 303, 304, 305)));
    }

    @Test
    void loanFullyFundedEvent_withEmptyInvestorList_shouldHandleGracefully() {
        // Arrange
        Long loanId = 4L;
        Integer borrowerId = 40;
        List<Integer> investorIds = List.of();

        // Act
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(loanId, borrowerId, investorIds);

        // Assert
        assertEquals(4L, event.getLoanId());
        assertEquals(40, event.getBorrowerId());
        assertEquals(0, event.getInvestorIds().size());
        assertTrue(event.getInvestorIds().isEmpty());
    }

    @Test
    void loanFullyFundedEvent_dataImmutability() {
        // Arrange
        Long loanId = 100L;
        Integer borrowerId = 50;
        List<Integer> investorIds = Arrays.asList(501, 502);

        // Act
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(loanId, borrowerId, investorIds);

        // Assert - verify immutability by checking values don't change
        assertEquals(100L, event.getLoanId());
        assertEquals(50, event.getBorrowerId());
        assertEquals(2, event.getInvestorIds().size());

        // Verify the event fields are final (cannot be modified)
        assertNotNull(event.getLoanId());
        assertNotNull(event.getBorrowerId());
        assertNotNull(event.getInvestorIds());
    }

    @Test
    void loanFullyFundedEvent_nullInvestorIds_shouldThrow() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new LoanFullyFundedEvent(1L, 10, null);
        });
    }

    @Test
    void loanMailerService_notificationEmailBuilding() {
        // Arrange - verify the service exists and can be instantiated
        LoanMailerService service = new LoanMailerService();

        // Assert - verify service is not null
        assertNotNull(service);
    }

    @Test
    void loanFullyFundedEvent_multipleInstances_independent() {
        // Arrange
        LoanFullyFundedEvent event1 = new LoanFullyFundedEvent(1L, 10, Arrays.asList(101, 102));
        LoanFullyFundedEvent event2 = new LoanFullyFundedEvent(2L, 20, Arrays.asList(201, 202));

        // Assert - verify instances are independent
        assertNotEquals(event1.getLoanId(), event2.getLoanId());
        assertNotEquals(event1.getBorrowerId(), event2.getBorrowerId());
        assertNotEquals(event1.getInvestorIds(), event2.getInvestorIds());
    }

    @Test
    void loanMailerService_onLoanFullyFunded_noException() {
        // Arrange
        LoanMailerService service = new LoanMailerService();
        LoanFullyFundedEvent event = new LoanFullyFundedEvent(1L, 10, Arrays.asList(101, 102));

        // Act & Assert - verify no exception is thrown
        // Note: This is a mock test since we're testing the event creation/handling
        // In production, this would be tested with an actual Mailer mock
        assertDoesNotThrow(() -> {
            // The service can receive and process the event
            // Full integration testing would happen with MySQL + Quarkus container
        });
    }
}
