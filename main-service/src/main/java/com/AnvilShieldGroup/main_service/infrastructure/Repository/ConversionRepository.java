package com.AnvilShieldGroup.main_service.infrastructure.Repository;

import com.AnvilShieldGroup.main_service.infrastructure.Repository.entity.ConversionsTbl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionRepository extends CrudRepository<ConversionsTbl,String> {
}
