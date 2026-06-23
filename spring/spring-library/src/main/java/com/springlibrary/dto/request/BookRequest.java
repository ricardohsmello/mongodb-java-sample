package com.springlibrary.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record BookRequest(
        @NotBlank(message = "title is required")
        String title,

        @NotBlank(message = "author is required")
        String author,

        @NotBlank(message = "isbn is required")
        String isbn,

        @Min(value = 0, message = "publishedYear must not be negative")
        @Max(value = 2100, message = "publishedYear is not a valid year")
        Integer publishedYear,

        @Positive(message = "price must be greater than zero")
        Double price
) {
}