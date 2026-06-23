package com.springlibrary.service;

import com.springlibrary.dto.request.BookRequest;
import com.springlibrary.dto.response.BookResponse;
import com.springlibrary.dto.response.PageResponse;
import com.springlibrary.exception.ResourceNotFoundException;
import com.springlibrary.mapper.BookMapper;
import com.springlibrary.model.Book;
import com.springlibrary.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final String RESOURCE = "Book";

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public PageResponse<BookResponse> findAll(int page, int size) {
        Page<Book> result = bookRepository.findAll(PageRequest.of(page, size, Sort.by("title")));
        List<BookResponse> content = bookMapper.toResponseList(result.getContent());
        return PageResponse.of(content, page, size, result.getTotalElements());
    }

    public BookResponse findById(String id) {
        return bookMapper.toResponse(getExisting(id));
    }

    public List<BookResponse> findByAuthor(String author) {
        return bookMapper.toResponseList(bookRepository.findByAuthor(author));
    }

    public List<BookResponse> findByTitle(String title) {
        return bookMapper.toResponseList(bookRepository.findByTitle(title));
    }

    public BookResponse findByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, isbn));
        return bookMapper.toResponse(book);
    }

    public BookResponse create(BookRequest request) {
        Book book = bookMapper.toEntity(request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    public BookResponse update(String id, BookRequest request) {
        Book existing = getExisting(id);
        bookMapper.applyRequest(existing, request);
        return bookMapper.toResponse(bookRepository.save(existing));
    }

    public void delete(String id) {
        if (!bookRepository.existsById(id)) {
            throw ResourceNotFoundException.of(RESOURCE, id);
        }
        bookRepository.deleteById(id);
    }

    private Book getExisting(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, id));
    }
}
