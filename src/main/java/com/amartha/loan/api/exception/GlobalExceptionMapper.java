package com.amartha.loan.api.exception;

import com.amartha.loan.api.dto.response.ErrorResponse;
import com.amartha.loan.domain.exception.InvalidLoanStateException;
import com.amartha.loan.domain.exception.InvestmentExceedsPrincipalException;
import com.amartha.loan.domain.exception.LoanNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof LoanNotFoundException) {
            ErrorResponse error = new ErrorResponse("LOAN_NOT_FOUND", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        if (exception instanceof InvalidLoanStateException) {
            ErrorResponse error = new ErrorResponse("INVALID_LOAN_STATE", exception.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        if (exception instanceof InvestmentExceedsPrincipalException) {
            ErrorResponse error = new ErrorResponse("INVESTMENT_EXCEEDS_PRINCIPAL", exception.getMessage());
            return Response.status(422).entity(error).build();
        }
        if (exception instanceof IllegalArgumentException) {
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", exception.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (exception instanceof IllegalStateException) {
            ErrorResponse error = new ErrorResponse("ILLEGAL_STATE", exception.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
