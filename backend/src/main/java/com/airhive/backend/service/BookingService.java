
package com.airhive.backend.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.BookingResponseDTO;
import com.airhive.backend.dto.CreateBookingRequestDTO;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Booking;
import com.airhive.backend.entity.Flight;
import com.airhive.backend.mapper.BookingMapper;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.BookingRepository;
import com.airhive.backend.repository.FlightRepository;

@Service
public class BookingService {

    private static final BigDecimal ECONOMY_FARE =
            new BigDecimal("1499.00");

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final AppUserRepository appUserRepository;

    // Preserve compatibility with existing BookingServiceTest.
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = null;
        this.appUserRepository = null;
    }

    // Constructor used by Spring for the actual application.
    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            FlightRepository flightRepository,
            AppUserRepository appUserRepository) {

        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsForUser(Long userId) {
        return bookingRepository.findByUser_Id(userId)
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponseDTO createBooking(
            CreateBookingRequestDTO request,
            Long userId) {

        // Lock the flight row to prevent concurrent capacity overbooking.
        Flight flight = flightRepository.findByIdForBooking(request.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight not found."));

        LocalDateTime departure = flight.getScheduledDeparture();

        if (departure == null || !departure.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Bookings are only allowed for future flights.");
        }

        String flightStatus = flight.getStatus() == null
                ? ""
                : flight.getStatus().toUpperCase(Locale.ROOT);

        if (!flightStatus.equals("SCHEDULED")
                && !flightStatus.equals("DELAYED")) {
            throw new IllegalArgumentException(
                    "This flight is not available for booking.");
        }

        if (flight.getAircraft() == null
                || flight.getAircraft().getAircraftType() == null) {
            throw new IllegalArgumentException(
                    "Flight aircraft capacity is unavailable.");
        }

        int capacity = flight.getAircraft()
                .getAircraftType()
                .getPassengerCapacity();

        long bookedSeats = bookingRepository
                .countByLinkedFlight_Id(flight.getId());

        if (bookedSeats >= capacity) {
            throw new IllegalArgumentException(
                    "This flight is fully booked.");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Passenger account not found."));

        Booking booking = new Booking();

        booking.setPnr(generatePnr());
        booking.setPassenger(request.getPassenger().trim());
        booking.setFlight(flight.getFlightNumber());

        booking.setRoute(
                flight.getDepartureAirport().getIataCode()
                        + "-"
                        + flight.getArrivalAirport().getIataCode());

        booking.setCabin("ECONOMY");

        // Sequential economy seat assignment.
        booking.setSeat(
                "E" + String.format("%03d", bookedSeats + 1));

        booking.setStatus("CONFIRMED");
        booking.setAmount(ECONOMY_FARE);
        booking.setTravelDate(departure.toLocalDate());

        booking.setUser(user);
        booking.setLinkedFlight(flight);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toResponse(savedBooking);
    }

    private String generatePnr() {
        String pnr;

        do {
            int randomNumber = SECURE_RANDOM.nextInt(100_000_000);
            pnr = "AH" + String.format("%08d", randomNumber);
        } while (bookingRepository.existsByPnr(pnr));

        return pnr;
    }
}
