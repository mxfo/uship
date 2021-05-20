package io.yupiik.uship.jsonrpc.core.servlet;

import io.yupiik.uship.jsonrpc.core.impl.JsonRpcHandler;
import io.yupiik.uship.jsonrpc.core.protocol.JsonRpcException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.json.JsonException;
import jakarta.json.JsonStructure;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JsonRpcServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private JsonRpcHandler handler;

    @Inject
    private Event<JsonRpcBeforeExecution> beforeExecutionEvent;

    @Inject
    private Jsonb jsonb;

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final JsonStructure request;
        try {
            request = handler.readRequest(req.getReader());
        } catch (final JsonbException | JsonException jsonbEx) {
            forwardResponse(handler.createResponse(null, -32700, jsonbEx.getMessage()), resp);
            return;
        }

        try {
            beforeExecutionEvent.fire(new JsonRpcBeforeExecution(request, req));
        } catch (final JsonRpcException re) {
            forwardResponse(handler.toErrorResponse(null, re, request), resp);
            return;
        } catch (final RuntimeException re) {
            forwardResponse(handler.createResponse(null, 100, re.getMessage()), resp);
            return;
        }

        final var ctx = req.startAsync();
        handler.execute(request, req, resp).whenComplete((value, error) -> {
            try {
                if (value != null) {
                    forwardResponse(value, resp);
                } else {
                    forwardResponse(handler.createResponse(null, -32603, error.getMessage()), resp);
                }
            } catch (final IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new IllegalStateException(e);
            } catch (final RuntimeException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            } finally {
                ctx.complete();
            }
        });
    }

    private void forwardResponse(final Object payload, final HttpServletResponse resp) throws IOException {
        resp.setStatus(200);
        resp.addHeader("content-type", "application/json;charset=utf-8");
        try (final var out = resp.getWriter()) {
            jsonb.toJson(payload, out);
        }
    }
}
