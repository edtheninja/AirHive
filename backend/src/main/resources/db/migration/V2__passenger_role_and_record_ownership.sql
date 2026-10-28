-- Introduce passenger accounts without deleting or rewriting business records.
-- Existing VIEWER accounts were created through public signup in the previous
-- application version; reclassify them as PASSENGER. Verified staff can be
-- restored to VIEWER by an administrator after deployment.
ALTER TABLE public.app_users
    DROP CONSTRAINT IF EXISTS app_users_role_check;

UPDATE public.app_users
SET role = 'PASSENGER'
WHERE role = 'VIEWER';

ALTER TABLE public.app_users
    ADD CONSTRAINT app_users_role_check
    CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER', 'PASSENGER'));

-- Nullable ownership keeps historical rows intact. Unassigned legacy records
-- remain visible to operational/read-only staff but are never exposed to a
-- passenger account.
ALTER TABLE public.bookings
    ADD COLUMN user_id bigint;

ALTER TABLE public.passengers
    ADD COLUMN user_id bigint;

ALTER TABLE public.bookings
    ADD CONSTRAINT fk_bookings_user
    FOREIGN KEY (user_id) REFERENCES public.app_users(id);

ALTER TABLE public.passengers
    ADD CONSTRAINT fk_passengers_user
    FOREIGN KEY (user_id) REFERENCES public.app_users(id);

CREATE INDEX idx_bookings_user_id ON public.bookings(user_id);
CREATE INDEX idx_passengers_user_id ON public.passengers(user_id);
