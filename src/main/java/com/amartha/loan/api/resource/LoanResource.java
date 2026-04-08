package com.amartha.loan.api.resource;

import com.amartha.loan.api.dto.request.ApproveRequest;
import com.amartha.loan.api.dto.request.CreateLoanRequest;
import com.amartha.loan.api.dto.request.DisburseRequest;
import com.amartha.loan.api.dto.response.LoanResponse;
import com.amartha.loan.domain.model.Loan;
import com.amartha.loan.domain.model.LoanApproval;
import com.amartha.loan.domain.model.LoanDisbursement;
import com.amartha.loan.domain.model.LoanStatus;
import com.amartha.loan.domain.repository.LoanApprovalRepository;
import com.amartha.loan.domain.repository.LoanDisbursementRepository;
import com.amartha.loan.domain.repository.LoanInvestmentRepository;
import com.amartha.loan.domain.service.LoanService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Path("/api/v1/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Loans", description = "Loan management endpoints")
public class LoanResource {

    @Inject
    LoanService loanService;

    @Inject
    LoanInvestmentRepository loanInvestmentRepository;

    @Inject
    LoanApprovalRepository loanApprovalRepository;

    @Inject
    LoanDisbursementRepository loanDisbursementRepository;

    @POST
    @RolesAllowed({"staff", "admin"})
    @Operation(summary = "Create a new loan")
    public Uni<Response> createLoan(@Valid CreateLoanRequest request) {
        return loanService.createLoanReactive(
                request.borrowerId(),
                request.principalAmount(),
                request.rate(),
                request.roi())
                .map(loan -> {
                    LoanResponse response = toLoanResponse(loan, BigDecimal.ZERO, null, null);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                });
    }

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Get loan details")
    public Uni<LoanResponse> getLoan(@PathParam("id") Long id) {
        return loanService.getLoanDetailsReactive(id);
    }

    @GET
    @Path("/{id}/agreement-letter")
    @Authenticated
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download agreement letter PDF")
    public Response downloadAgreementLetter(@PathParam("id") Long id) throws IOException {
        Loan loan = loanService.getLoan(id);
        if (loan.agreementLetterUrl == null || loan.agreementLetterUrl.isBlank()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Agreement letter not yet generated").build();
        }
        File file = new File(loan.agreementLetterUrl);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Agreement letter file not found").build();
        }
        return Response.ok(file)
                .header("Content-Disposition", "attachment; filename=\"" + id + "_agreement.pdf\"")
                .type("application/pdf")
                .build();
    }

    @GET
    @Authenticated
    @Operation(summary = "List loans")
    public Uni<List<LoanResponse>> listLoans(@QueryParam("status") String status) {
        LoanStatus loanStatus = status != null ? LoanStatus.valueOf(status) : null;
        return loanService.listLoansWithDetails(loanStatus);
    }

    @PUT
    @Path("/{id}/approve")
    @RolesAllowed({"staff", "admin"})
    @Operation(summary = "Approve a loan")
    public Uni<LoanResponse> approveLoan(
            @PathParam("id") Long id,
            @Valid ApproveRequest request) throws IOException {
        return loanService.approveLoanAndFetchDetails(id, request.employeeId, request.photoProofPath, request.approvalDate);
    }

    @PUT
    @Path("/{id}/disburse")
    @RolesAllowed({"field_officer", "admin"})
    @Operation(summary = "Disburse a loan")
    public Uni<LoanResponse> disburseLoan(
            @PathParam("id") Long id,
            @Valid DisburseRequest request) {
        return loanService.disburseLoanAndFetchDetails(id, request.employeeId, request.signedAgreementLetterPath, request.disbursementDate);
    }

    private LoanResponse toLoanResponse(Loan loan, BigDecimal totalInvested, LoanApproval approval, LoanDisbursement disbursement) {
        BigDecimal invested = totalInvested != null ? totalInvested : BigDecimal.ZERO;
        
        LoanResponse.ApprovalInfo approvalInfo = null;
        if (approval != null) {
            approvalInfo = new LoanResponse.ApprovalInfo(
                approval.fieldValidatorEmployeeId,
                approval.approvalDate.toString()
            );
        }

        LoanResponse.DisbursementInfo disbursementInfo = null;
        if (disbursement != null) {
            disbursementInfo = new LoanResponse.DisbursementInfo(
                disbursement.fieldOfficerEmployeeId,
                disbursement.disbursementDate.toString()
            );
        }

        return new LoanResponse(
            loan.id,
            loan.borrowerId,
            loan.principalAmount,
            loan.rate,
            loan.roi,
            loan.status,
            invested,
            loan.principalAmount.subtract(invested),
            loan.agreementLetterUrl,
            approvalInfo,
            disbursementInfo,
            loan.createdAt
        );
    }
}
