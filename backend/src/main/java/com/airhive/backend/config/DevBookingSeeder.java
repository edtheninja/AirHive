package com.airhive.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.airhive.backend.entity.Booking;
import com.airhive.backend.repository.BookingRepository;

@Configuration
@Profile("dev")
public class DevBookingSeeder {

    @Bean
    CommandLineRunner seedBookings(BookingRepository bookingRepository) {
        return args -> {
            List<BookingSeed> bookings = List.of(
                    new BookingSeed("BK-10248", "Aarav Sharma", "AI300", "DEL → BOM", "Economy", "1A", "Confirmed", 210, 1),
                    new BookingSeed("BK-10255", "Ananya Verma", "AI313", "BOM → BLR", "Premium", "2B", "Checked In", 347, 2),
                    new BookingSeed("BK-10262", "Rohan Gupta", "AI326", "BLR → HYD", "Business", "3C", "Pending", 484, 3),
                    new BookingSeed("BK-10269", "Priya Singh", "AI339", "HYD → MAA", "First", "4D", "Confirmed", 621, 4),
                    new BookingSeed("BK-10276", "Aditya Kumar", "AI352", "MAA → DEL", "Economy", "5E", "Checked In", 758, 5),
                    new BookingSeed("BK-10283", "Sneha Patel", "AI365", "DEL → CCU", "Premium", "6F", "Pending", 895, 6),
                    new BookingSeed("BK-10290", "Vikram Joshi", "AI378", "CCU → DEL", "Business", "7A", "Confirmed", 1032, 7),
                    new BookingSeed("BK-10297", "Meera Kapoor", "AI391", "DEL → BOM", "First", "8B", "Cancelled", 1169, 8),
                    new BookingSeed("BK-10304", "Kabir Malhotra", "AI404", "BOM → BLR", "Economy", "9C", "Confirmed", 1306, 9),
                    new BookingSeed("BK-10311", "Ishita Mehta", "AI417", "BLR → HYD", "Premium", "10D", "Checked In", 1443, 10),
                    new BookingSeed("BK-10318", "Arjun Rao", "AI430", "HYD → MAA", "Business", "11E", "Pending", 1580, 11),
                    new BookingSeed("BK-10325", "Diya Nair", "AI443", "MAA → DEL", "First", "12F", "Confirmed", 1717, 12),
                    new BookingSeed("BK-10332", "Rahul Bansal", "AI456", "DEL → CCU", "Economy", "13A", "Checked In", 1854, 13),
                    new BookingSeed("BK-10339", "Kavya Iyer", "AI469", "CCU → DEL", "Premium", "14B", "Pending", 1991, 14),
                    new BookingSeed("BK-10346", "Nikhil Sethi", "AI482", "DEL → BOM", "Business", "15C", "Confirmed", 2128, 15),
                    new BookingSeed("BK-10353", "Pooja Das", "AI495", "BOM → BLR", "First", "16D", "Cancelled", 2265, 16),
                    new BookingSeed("BK-10360", "Yash Agarwal", "AI508", "BLR → HYD", "Economy", "17E", "Confirmed", 402, 17),
                    new BookingSeed("BK-10367", "Riya Chawla", "AI521", "HYD → MAA", "Premium", "18F", "Checked In", 539, 18),
                    new BookingSeed("BK-10374", "Siddharth Jain", "AI534", "MAA → DEL", "Business", "19A", "Pending", 676, 19),
                    new BookingSeed("BK-10381", "Neha Arora", "AI547", "DEL → CCU", "First", "20B", "Confirmed", 813, 20),
                    new BookingSeed("BK-10388", "Karan Khanna", "AI560", "CCU → DEL", "Economy", "21C", "Checked In", 950, 21),
                    new BookingSeed("BK-10395", "Simran Kaur", "AI573", "DEL → BOM", "Premium", "22D", "Pending", 1087, 22),
                    new BookingSeed("BK-10402", "Manav Tandon", "AI586", "BOM → BLR", "Business", "23E", "Confirmed", 1224, 23),
                    new BookingSeed("BK-10409", "Tanya Roy", "AI599", "BLR → HYD", "First", "24F", "Cancelled", 1361, 24),
                    new BookingSeed("BK-10416", "Aman Mishra", "AI312", "HYD → MAA", "Economy", "25A", "Confirmed", 1498, 25),
                    new BookingSeed("BK-10423", "Nandini Shah", "AI325", "MAA → DEL", "Premium", "26B", "Checked In", 1635, 26),
                    new BookingSeed("BK-10430", "Varun Saxena", "AI338", "DEL → CCU", "Business", "27C", "Pending", 1772, 27),
                    new BookingSeed("BK-10437", "Aditi Sinha", "AI351", "CCU → DEL", "First", "28D", "Confirmed", 1909, 28),
                    new BookingSeed("BK-10444", "Dev Kapoor", "AI364", "DEL → BOM", "Economy", "29E", "Checked In", 2046, 1),
                    new BookingSeed("BK-10451", "Manya Gupta", "AI377", "BOM → BLR", "Premium", "30F", "Pending", 2183, 2),
                    new BookingSeed("BK-10458", "Ritesh Yadav", "AI390", "BLR → HYD", "Business", "31A", "Confirmed", 2320, 3),
                    new BookingSeed("BK-10465", "Shreya Kapoor", "AI403", "HYD → MAA", "First", "32B", "Cancelled", 2457, 4),
                    new BookingSeed("BK-10472", "Harsh Vardhan", "AI416", "MAA → DEL", "Economy", "1C", "Confirmed", 594, 5),
                    new BookingSeed("BK-10479", "Sakshi Jain", "AI429", "DEL → CCU", "Premium", "2D", "Checked In", 731, 6),
                    new BookingSeed("BK-10486", "Abhishek Roy", "AI442", "CCU → DEL", "Business", "3E", "Pending", 868, 7),
                    new BookingSeed("BK-10493", "Muskan Ali", "AI455", "DEL → BOM", "First", "4F", "Confirmed", 1005, 8),
                    new BookingSeed("BK-10500", "Varun Mehta", "AI468", "BOM → BLR", "Economy", "5A", "Checked In", 1142, 9),
                    new BookingSeed("BK-10507", "Pallavi Joshi", "AI481", "BLR → HYD", "Premium", "6B", "Pending", 1279, 10),
                    new BookingSeed("BK-10514", "Mohit Bhatia", "AI494", "HYD → MAA", "Business", "7C", "Confirmed", 1416, 11),
                    new BookingSeed("BK-10521", "Nisha Reddy", "AI507", "MAA → DEL", "First", "8D", "Cancelled", 1553, 12),
                    new BookingSeed("BK-10528", "Saurabh Tiwari", "AI520", "DEL → CCU", "Economy", "9E", "Confirmed", 1690, 13),
                    new BookingSeed("BK-10535", "Alisha Khan", "AI533", "CCU → DEL", "Premium", "10F", "Checked In", 1827, 14),
                    new BookingSeed("BK-10542", "Vivek Sharma", "AI546", "DEL → BOM", "Business", "11A", "Pending", 1964, 15),
                    new BookingSeed("BK-10549", "Rashmi Verma", "AI559", "BOM → BLR", "First", "12B", "Confirmed", 2101, 16),
                    new BookingSeed("BK-10556", "Ankit Gupta", "AI572", "BLR → HYD", "Economy", "13C", "Checked In", 2238, 17),
                    new BookingSeed("BK-10563", "Nikita Singh", "AI585", "HYD → MAA", "Premium", "14D", "Pending", 2375, 18),
                    new BookingSeed("BK-10570", "Rajiv Malhotra", "AI598", "MAA → DEL", "Business", "15E", "Confirmed", 512, 19),
                    new BookingSeed("BK-10577", "Shalini Rao", "AI311", "DEL → CCU", "First", "16F", "Cancelled", 649, 20),
                    new BookingSeed("BK-10584", "Kunal Sethi", "AI324", "CCU → DEL", "Economy", "17A", "Confirmed", 786, 21),
                    new BookingSeed("BK-10591", "Komal Agarwal", "AI337", "DEL → BOM", "Premium", "18B", "Checked In", 923, 22)
            );

            for (BookingSeed seed : bookings) {
                if (!bookingRepository.existsByPnr(seed.pnr())) {
                    Booking booking = new Booking();
                    booking.setPnr(seed.pnr());
                    booking.setPassenger(seed.passenger());
                    booking.setFlight(seed.flight());
                    booking.setRoute(seed.route());
                    booking.setCabin(seed.cabin());
                    booking.setSeat(seed.seat());
                    booking.setStatus(seed.status());
                    booking.setAmount(BigDecimal.valueOf(seed.amount()));
                    booking.setTravelDate(LocalDate.of(2026, 8, seed.day()));
                    bookingRepository.save(booking);
                }
            }
        };
    }

    private record BookingSeed(
            String pnr,
            String passenger,
            String flight,
            String route,
            String cabin,
            String seat,
            String status,
            int amount,
            int day
    ) {
    }
}
