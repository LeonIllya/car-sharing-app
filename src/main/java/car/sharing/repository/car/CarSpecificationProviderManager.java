package car.sharing.repository.car;

import car.sharing.model.Car;
import car.sharing.repository.SpecificationProvider;
import car.sharing.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CarSpecificationProviderManager implements SpecificationProviderManager<Car> {
    private final List<SpecificationProvider<Car>> carSpecificationProviders;

    @Override
    public SpecificationProvider<Car> getSpecificationProvider(String key) {
        return carSpecificationProviders.stream()
            .filter(provider -> provider.getKey().equals(key))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Can`t find correct specification provider for key: " + key));
    }
}
