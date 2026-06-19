package com.example.resources;

import com.example.model.AverageBookRating;
import com.example.model.Book;
import com.example.model.Review;
import com.example.service.ReviewService;
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

@Path("/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {

    @Inject
    ReviewService reviewService;

    @GET
    public List<Review> findAll() {
        return reviewService.findAll();
    }

    @GET
    @Path("/avgRating/{bookId}")
    public AverageBookRating getAverageBookRating(@PathParam("bookId") String bookId) {
        return reviewService.getAverageBookRating(bookId);
    }

    @POST
    public Response create(Review review) {
        String id = reviewService.create(review);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of("id", id))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Review review) {
        boolean updated = reviewService.update(id, review);
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        boolean deleted = reviewService.delete(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

}
