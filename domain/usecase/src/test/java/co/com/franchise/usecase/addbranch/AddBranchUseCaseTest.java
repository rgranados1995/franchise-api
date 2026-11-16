package co.com.franchise.usecase.addbranch;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
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
class AddBranchUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Long BRANCH_ID = 10L;
    private static final Franchise FRANCHISE = Franchise.builder().id(FRANCHISE_ID).name("Coffee House").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private AddBranchUseCase useCase;

    @Test
    void shouldSaveBranchUnderTheFranchiseWhenNameIsAvailable() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Downtown")).thenReturn(Mono.empty());
        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.<Branch>getArgument(0).toBuilder().id(BRANCH_ID).build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, " Downtown "))
                .expectNext(Branch.builder().id(BRANCH_ID).franchiseId(FRANCHISE_ID).name("Downtown").build())
                .verifyComplete();
    }

    @Test
    void shouldFailWithNameTakenWhenTheFranchiseAlreadyHasTheBranchName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Downtown"))
                .thenReturn(Mono.just(Branch.builder().id(BRANCH_ID).franchiseId(FRANCHISE_ID).name("downtown").build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Downtown"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.BRANCH_NAME_TAKEN))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithNotFoundWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Downtown"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.FRANCHISE_NOT_FOUND))
                .verify();

        verifyNoInteractions(branchRepository);
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingRepositoriesWhenNameIsMissing() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, null))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(franchiseRepository, branchRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
