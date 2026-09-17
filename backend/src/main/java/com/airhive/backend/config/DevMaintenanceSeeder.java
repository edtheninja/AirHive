package com.airhive.backend.config;

import com.airhive.backend.entity.Maintenance;
import com.airhive.backend.repository.MaintenanceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevMaintenanceSeeder {

    @Bean
    CommandLineRunner seedMaintenance(MaintenanceRepository repository) {
        return args -> {
            seed(
                    repository,
                    "MX-4412",
                    "VT-DKS",
                    "A-Check",
                    "Routine",
                    "In progress",
                    62,
                    "T. Novak"
            );

            seed(
                    repository,
                    "MX-4413",
                    "VT-FTL",
                    "Engine Inspection",
                    "Critical",
                    "Overdue 2d",
                    24,
                    "M. Farouk"
            );

            seed(
                    repository,
                    "MX-4414",
                    "VT-CLM",
                    "Avionics Update",
                    "Routine",
                    "22 Aug",
                    0,
                    "S. Kapoor"
            );

            seed(
                    repository,
                    "MX-4415",
                    "VT-BQR",
                    "Landing Gear",
                    "Major",
                    "03 Sep",
                    8,
                    "L. Bianchi"
            );

            seed(
                    repository,
                    "MX-4416",
                    "VT-AXN",
                    "Cabin Refit",
                    "Minor",
                    "12 Aug",
                    88,
                    "R. Nair"
            );
        };
    }

    private void seed(
            MaintenanceRepository repository,
            String orderNumber,
            String aircraft,
            String type,
            String severity,
            String due,
            Integer progress,
            String engineer
    ) {
        if (repository.existsByOrderNumber(orderNumber)) {
            return;
        }

        Maintenance maintenance = new Maintenance();

        maintenance.setOrderNumber(orderNumber);
        maintenance.setAircraft(aircraft);
        maintenance.setType(type);
        maintenance.setSeverity(severity);
        maintenance.setDue(due);
        maintenance.setProgress(progress);
        maintenance.setEngineer(engineer);

        repository.save(maintenance);
    }
}
