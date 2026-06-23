package com.springlibrary.dto.response;

public record BookResponse(
        String id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        Double price
) {
}
