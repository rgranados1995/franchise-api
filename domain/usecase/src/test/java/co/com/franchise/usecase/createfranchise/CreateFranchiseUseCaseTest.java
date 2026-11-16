package co.com.franchise.usecase.createfranchise;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private CreateFranchiseUseCase useCase;

    @Test
    void shouldSaveFranchiseWithNormalizedNameWhenNameIsAvailable() {
        when(franchiseRepository.findByName("Coffee House")).thenReturn(Mono.empty());
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.<Franchise>getArgument(0).toBuilder().id(FRANCHISE_ID).build()));

        StepVerifier.create(useCase.execute("  Coffee House "))
                .expectNext(Franchise.builder().id(FRANCHISE_ID).name("Coffee House").build())
                .verifyComplete();

        ArgumentCaptor<Franchise> saved = ArgumentCaptor.forClass(Franchise.class);
        verify(franchiseRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
    }

    @Test
    void shouldFailWithNameTakenWhenAnotherFranchiseHasTheName() {
        when(franchiseRepository.findByName("Coffee House"))
                .thenReturn(Mono.just(Franchise.builder().id(FRANCHISE_ID).name("coffee house").build()));

        StepVerifier.create(useCase.execute("Coffee House"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.FRANCHISE_NAME_TAKEN))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingTheRepositoryWhenNameIsBlank() {
        StepVerifier.create(useCase.execute("   "))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(franchiseRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
