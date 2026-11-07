-- Associate new bookings with their corresponding flight.
-- Existing bookings remain valid with a NULL flight_id.

ALTER TABLE public.bookings
    ADD COLUMN flight_id BIGINT;

ALTER TABLE public.bookings
    ADD CONSTRAINT fk_bookings_flight
    FOREIGN KEY (flight_id)
    REFERENCES public.flight(id);

CREATE INDEX idx_bookings_flight_id
    ON public.bookings(flight_id);