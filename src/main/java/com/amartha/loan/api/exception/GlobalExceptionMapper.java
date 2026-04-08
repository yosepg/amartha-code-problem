package com.amartha.loan.api.exception;

import com.amartha.loan.api.dto.response.ErrorResponse;
import com.amartha.loan.domain.exception.InvalidLoanStateException;
import com.amartha.loan.domain.exception.InvestmentExceedsPrincipalException;
import com.amartha.loan.domain.exception.LoanNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof LoanNotFoundException) {
            log.warn("Business error - Loan not found: {}", exception.getMessage(), exception);
            ErrorResponse error = new ErrorResponse("LOAN_NOT_FOUND", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        if (exception instanceof InvalidLoanStateException) {
            log.warn("Business error - Invalid loan state transition: {}", exception.getMessage(), exception);
            ErrorResponse error = new ErrorResponse("INVALID_LOAN_STATE", exception.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        if (exception instanceof InvestmentExceedsPrincipalException) {
            log.warn("Business error - Investment exceeds principal: {}", exception.getMessage(), exception);
            ErrorResponse error = new ErrorResponse("INVESTMENT_EXCEEDS_PRINCIPAL", exception.getMessage());
            return Response.status(422).entity(error).build();
        }
        if (exception instanceof IllegalArgumentException) {
            log.warn("Validation error: {}", exception.getMessage(), exception);
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", exception.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (exception instanceof IllegalStateException) {
            log.error("Illegal state error: {}", exception.getMessage(), exception);
            ErrorResponse error = new ErrorResponse("ILLEGAL_STATE", exception.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        log.error("Unexpected error occurred", exception);
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
