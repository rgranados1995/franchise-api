package co.com.franchise.usecase.renamefranchise;

import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RenameFranchiseUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Franchise FRANCHISE = Franchise.builder().id(FRANCHISE_ID).name("Old Brand").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private RenameFranchiseUseCase useCase;

    @Test
    void shouldSaveNewNameWhenNameIsAvailable() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(franchiseRepository.findByName("New Brand")).thenReturn(Mono.empty());
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, " New Brand "))
                .expectNext(Franchise.builder().id(FRANCHISE_ID).name("New Brand").build())
                .verifyComplete();
    }

    @Test
    void shouldAllowRenamingWhenTheOnlyMatchIsTheSameFranchise() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(franchiseRepository.findByName("OLD BRAND")).thenReturn(Mono.just(FRANCHISE));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "OLD BRAND"))
                .expectNext(Franchise.builder().id(FRANCHISE_ID).name("OLD BRAND").build())
                .verifyComplete();
    }

    @Test
    void shouldFailWithNameTakenWhenAnotherFranchiseHasTheName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(franchiseRepository.findByName("Rival Brand"))
                .thenReturn(Mono.just(Franchise.builder().id(2L).name("Rival Brand").build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Rival Brand"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.FRANCHISE_NAME_TAKEN))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldFailWithNotFoundWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "New Brand"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.FRANCHISE_NOT_FOUND))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingTheRepositoryWhenNameIsTooLong() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, "a".repeat(101)))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(franchiseRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
