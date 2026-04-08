package com.amartha.loan.api.resource;

import com.amartha.loan.api.dto.request.AddInvestmentRequest;
import com.amartha.loan.api.dto.response.InvestmentResponse;
import com.amartha.loan.domain.model.LoanInvestment;
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
import java.util.List;

@Path("/api/v1/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Investments", description = "Loan investment endpoints")
public class LoanInvestmentResource {

    @Inject
    LoanService loanService;

    @POST
    @RolesAllowed({"investor", "admin"})
    @Operation(summary = "Add investment to loan")
    public Uni<InvestmentResponse> addInvestment(@Valid AddInvestmentRequest request) {
        return loanService.addInvestmentReactive(
                request.loanId(),
                request.investorId(),
                request.amount())
                .map(this::toInvestmentResponse);
    }

    @GET
    @Path("/{loanId}")
    @Authenticated
    @Operation(summary = "List investments for loan")
    public Uni<List<InvestmentResponse>> listInvestments(@PathParam("loanId") Long loanId) {
        return loanService.getInvestmentsReactive(loanId)
                .map(investments ->
                    investments.stream()
                            .map(this::toInvestmentResponse)
                            .toList()
                );
    }

    private InvestmentResponse toInvestmentResponse(LoanInvestment investment) {
        return new InvestmentResponse(
            investment.id,
            investment.loanId,
            investment.investorId,
            investment.amount,
            investment.investedAt
        );
    }
}
