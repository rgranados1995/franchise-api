package co.com.franchise.usecase.renamebranch;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
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
class RenameBranchUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Long BRANCH_ID = 10L;
    private static final Branch BRANCH = Branch.builder().id(BRANCH_ID).franchiseId(FRANCHISE_ID).name("Downtown").build();

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private RenameBranchUseCase useCase;

    @Test
    void shouldSaveNewNameWhenNameIsAvailableInTheFranchise() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Uptown")).thenReturn(Mono.empty());
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectNext(BRANCH.toBuilder().name("Uptown").build())
                .verifyComplete();
    }

    @Test
    void shouldAllowRenamingWhenTheOnlyMatchIsTheSameBranch() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "DOWNTOWN")).thenReturn(Mono.just(BRANCH));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "DOWNTOWN"))
                .expectNext(BRANCH.toBuilder().name("DOWNTOWN").build())
                .verifyComplete();
    }

    @Test
    void shouldFailWithNameTakenWhenAnotherBranchOfTheFranchiseHasTheName() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Uptown"))
                .thenReturn(Mono.just(Branch.builder().id(11L).franchiseId(FRANCHISE_ID).name("Uptown").build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.BRANCH_NAME_TAKEN))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithNotFoundWhenBranchIsNotPartOfTheFranchise() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.BRANCH_NOT_FOUND))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingTheRepositoryWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, " "))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(branchRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
