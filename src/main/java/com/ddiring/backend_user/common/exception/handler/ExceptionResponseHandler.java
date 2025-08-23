package com.example.User_Service.common.exception.handler;


import com.example.User_Service.common.exception.CustomException;
import com.example.User_Service.common.exception.ErrorCode;
import com.example.User_Service.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionResponseHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException e) {
        ErrorCode error = e.getErrorCode();
        HttpStatus status = error.getStatus();
        String message = error.getMessage();
        String errorCode = error.getErrorCode();

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(message, errorCode));

    }


}
