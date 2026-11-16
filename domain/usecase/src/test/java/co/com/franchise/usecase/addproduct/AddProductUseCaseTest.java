package co.com.franchise.usecase.addproduct;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
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
class AddProductUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Long BRANCH_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Branch BRANCH = Branch.builder().id(BRANCH_ID).franchiseId(FRANCHISE_ID).name("Downtown").build();

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AddProductUseCase useCase;

    @Test
    void shouldSaveOutOfStockProductWhenNameIsAvailableInTheBranch() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
        when(productRepository.findByBranchIdAndName(BRANCH_ID, "Espresso")).thenReturn(Mono.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.<Product>getArgument(0).toBuilder().id(PRODUCT_ID).build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, " Espresso ", 0))
                .expectNext(Product.builder().id(PRODUCT_ID).branchId(BRANCH_ID).name("Espresso").stock(0).build())
                .verifyComplete();
    }

    @Test
    void shouldFailWithNameTakenWhenTheBranchAlreadyHasTheProductName() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
        when(productRepository.findByBranchIdAndName(BRANCH_ID, "Espresso"))
                .thenReturn(Mono.just(Product.builder().id(PRODUCT_ID).branchId(BRANCH_ID).name("espresso").stock(4).build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Espresso", 10))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.PRODUCT_NAME_TAKEN))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithNotFoundWhenBranchIsNotPartOfTheFranchise() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Espresso", 10))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.BRANCH_NOT_FOUND))
                .verify();

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldFailWithInvalidStockWithoutQueryingRepositoriesWhenStockIsNegative() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Espresso", -1))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_STOCK))
                .verify();

        verifyNoInteractions(branchRepository, productRepository);
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingRepositoriesWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "", 10))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(branchRepository, productRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
