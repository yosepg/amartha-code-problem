package com.amartha.loan.infrastructure.email;

import com.amartha.loan.domain.event.LoanFullyFundedEvent;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoanMailerService {

    @Inject
    Mailer mailer;

    public void onLoanFullyFunded(@Observes LoanFullyFundedEvent event) {
        // This service can be extended to send emails to investors
        // For now, it logs the event
        String subject = "Loan " + event.getLoanId() + " is now fully funded!";
        String htmlBody = buildInvestorNotificationEmail(event.getLoanId().toString(), event.getBorrowerId().toString());
        
        // TODO: Fetch investor emails from member profiles and send emails
        // This requires a repository injection to query member emails by investor IDs
    }

    private String buildInvestorNotificationEmail(String loanId, String borrowerId) {
        return "<html><body>" +
                "<h2>Loan Fully Funded</h2>" +
                "<p>Loan " + loanId + " for borrower " + borrowerId + " has been fully funded by all investors.</p>" +
                "<p>The loan will proceed to disbursement shortly.</p>" +
                "</body></html>";
    }
}
