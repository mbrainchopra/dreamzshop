package org.example.dreamzshop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // ILLEGAL ARGUMENT
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "status",
                400
        );

        model.addAttribute(
                "title",
                "Bad Request"
        );

        model.addAttribute(
                "message",
                exception.getMessage() != null
                        ? exception.getMessage()
                        : "The request could not be processed."
        );

        model.addAttribute(
                "path",
                request.getRequestURI()
        );

        return "error/400";
    }


    // =========================================================
    // 404
    // =========================================================

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(
            NoHandlerFoundException exception,
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "status",
                404
        );

        model.addAttribute(
                "title",
                "Page Not Found"
        );

        model.addAttribute(
                "message",
                "The page you are looking for does not exist."
        );

        model.addAttribute(
                "path",
                request.getRequestURI()
        );

        return "error/404";
    }


    // =========================================================
    // GENERAL EXCEPTION
    // =========================================================

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception exception,
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "status",
                500
        );

        model.addAttribute(
                "title",
                "Something Went Wrong"
        );

        model.addAttribute(
                "message",
                "We are unable to process your request right now. Please try again."
        );

        model.addAttribute(
                "path",
                request.getRequestURI()
        );

        return "error/500";
    }
}