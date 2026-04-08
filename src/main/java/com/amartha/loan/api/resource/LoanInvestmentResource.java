package com.amartha.loan.api.resource;

import com.amartha.loan.api.dto.request.AddInvestmentRequest;
import com.amartha.loan.api.dto.response.InvestmentResponse;
import com.amartha.loan.domain.model.LoanInvestment;
import com.amartha.loan.domain.service.LoanService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/api/v1/loans/{id}/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Investments", description = "Loan investment endpoints")
public class LoanInvestmentResource {

    @Inject
    LoanService loanService;

    @POST
    @RolesAllowed({"investor", "admin"})
    @Operation(summary = "Add investment to loan")
    public Response addInvestment(
            @PathParam("id") Long loanId,
            @Valid AddInvestmentRequest request) {
        loanService.addInvestment(
                loanId,
                request.investorId,
                request.amount);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Authenticated
    @Operation(summary = "List investments for loan")
    public List<InvestmentResponse> listInvestments(@PathParam("id") Long loanId) {
        return loanService.getInvestments(loanId).stream()
                .map(this::toInvestmentResponse)
                .toList();
    }

    private InvestmentResponse toInvestmentResponse(LoanInvestment investment) {
        InvestmentResponse response = new InvestmentResponse();
        response.id = investment.id;
        response.loanId = investment.loanId;
        response.investorId = investment.investorId;
        response.amount = investment.amount;
        response.investedAt = investment.investedAt;
        return response;
    }
}
