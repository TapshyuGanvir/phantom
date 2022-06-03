package com.sixsimplex.treasurehunt.Phantom1.direction;

public class dResponse {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        void onSuccess(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the provided error code and optional
         * user-readable message.
         */
        void onErrorResponse(String error);
    }
}
