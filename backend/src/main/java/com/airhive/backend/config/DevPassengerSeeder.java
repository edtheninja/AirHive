package com.airhive.backend.config;

import com.airhive.backend.entity.Passenger;
import com.airhive.backend.repository.PassengerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevPassengerSeeder {

    @Bean
    CommandLineRunner seedPassengers(
            PassengerRepository passengerRepository
    ) {
        return args -> {
            seed(passengerRepository, "PX-9000", "Aarav Sharma", "Blue", "AI302", "DEL → BOM", "1A", 0, true);
            seed(passengerRepository, "PX-9001", "Lena Fischer", "Silver", "AI914", "FRA → DEL", "12B", 1, true);
            seed(passengerRepository, "PX-9002", "Rahul Nair", "Gold", "AI458", "BOM → BLR", "4C", 0, false);
            seed(passengerRepository, "PX-9003", "Sofia Alvarez", "Platinum", "AI770", "DXB → DEL", "2D", 2, true);
            seed(passengerRepository, "PX-9004", "Chen Wei", "Blue", "AI221", "SIN → DEL", "18F", 1, true);
            seed(passengerRepository, "PX-9005", "Priya Sharma", "Silver", "AI305", "DEL → HYD", "8A", 0, false);
            seed(passengerRepository, "PX-9006", "Tomas Novak", "Gold", "AI611", "LHR → DEL", "6B", 2, true);
            seed(passengerRepository, "PX-9007", "Aisha Rahman", "Platinum", "AI402", "DOH → BOM", "3C", 1, true);
            seed(passengerRepository, "PX-9008", "Kabir Mehta", "Blue", "AI508", "DEL → CCU", "21D", 0, false);
            seed(passengerRepository, "PX-9009", "Emma Wilson", "Silver", "AI731", "BOM → DEL", "14A", 1, true);
            seed(passengerRepository, "PX-9010", "Vikram Singh", "Gold", "AI845", "DEL → MAA", "5F", 2, true);
            seed(passengerRepository, "PX-9011", "Maya Kapoor", "Platinum", "AI916", "DEL → GOI", "1C", 0, true);
            seed(passengerRepository, "PX-9012", "Noah Williams", "Blue", "AI302", "DEL → BOM", "24B", 1, false);
            seed(passengerRepository, "PX-9013", "Ananya Gupta", "Silver", "AI914", "FRA → DEL", "9D", 0, true);
            seed(passengerRepository, "PX-9014", "Daniel Kim", "Gold", "AI458", "BOM → BLR", "7A", 2, true);
            seed(passengerRepository, "PX-9015", "Isabella Rossi", "Platinum", "AI770", "DXB → DEL", "2F", 1, false);
            seed(passengerRepository, "PX-9016", "Rohan Malhotra", "Blue", "AI221", "SIN → DEL", "19C", 0, true);
            seed(passengerRepository, "PX-9017", "Nisha Verma", "Silver", "AI305", "DEL → HYD", "11E", 1, true);
            seed(passengerRepository, "PX-9018", "Oliver Brown", "Gold", "AI611", "LHR → DEL", "5B", 2, false);
            seed(passengerRepository, "PX-9019", "Meera Joshi", "Platinum", "AI402", "DOH → BOM", "3A", 0, true);
            seed(passengerRepository, "PX-9020", "Ethan Miller", "Blue", "AI508", "DEL → CCU", "22F", 1, true);
            seed(passengerRepository, "PX-9021", "Simran Kaur", "Silver", "AI731", "BOM → DEL", "15C", 0, false);
            seed(passengerRepository, "PX-9022", "Lucas Martin", "Gold", "AI845", "DEL → MAA", "6D", 2, true);
            seed(passengerRepository, "PX-9023", "Ishita Rao", "Platinum", "AI916", "DEL → GOI", "1F", 1, true);
        };
    }

    private void seed(
            PassengerRepository repository,
            String passengerCode,
            String name,
            String tier,
            String flight,
            String route,
            String seat,
            int bags,
            boolean checkedIn
    ) {
        if (repository.existsByPassengerCode(passengerCode)) {
            return;
        }

        Passenger passenger = new Passenger();

        passenger.setPassengerCode(passengerCode);
        passenger.setName(name);
        passenger.setTier(tier);
        passenger.setFlight(flight);
        passenger.setRoute(route);
        passenger.setSeat(seat);
        passenger.setBags(bags);
        passenger.setCheckedIn(checkedIn);

        repository.save(passenger);
    }
}
