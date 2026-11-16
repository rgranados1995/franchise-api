package co.com.franchise.model.branch;

import lombok.Builder;
import lombok.Value;

/**
 * Point of sale that belongs to exactly one franchise. Its name is unique within that franchise,
 * ignoring case.
 */
@Value
@Builder(toBuilder = true)
public class Branch {
    Long id;
    Long franchiseId;
    String name;
}
