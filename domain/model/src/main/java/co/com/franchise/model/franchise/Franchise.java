package co.com.franchise.model.franchise;

import lombok.Builder;
import lombok.Value;

/**
 * Commercial brand that groups branches. Its name is unique across all franchises, ignoring case.
 * Branches reference their franchise by id instead of being nested here, so they can be streamed
 * on demand rather than loaded as a whole tree.
 */
@Value
@Builder(toBuilder = true)
public class Franchise {
    Long id;
    String name;
}
