package com.example.resources;

import com.example.model.Book;
import com.example.service.BookService;
import com.mongodb.MongoTimeoutException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    @Inject
    BookService bookService;

    @GET
    public List<Book> findAll() {
        return bookService.findAll();
    }

    @GET
    @Path("/min-pages/{minPages}")
    public List<Book> findByMinPages(@PathParam("minPages") int minPages) {
        return bookService.findByMinPages(minPages);
    }

    @POST
    public Response create(Book book) {
        String id = bookService.create(book);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of("id", id))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Book book) {
        boolean updated = bookService.update(id, book);
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        boolean deleted = bookService.delete(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

}
