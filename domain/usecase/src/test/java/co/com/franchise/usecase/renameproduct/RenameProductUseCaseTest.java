package co.com.franchise.usecase.renameproduct;

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
class RenameProductUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Long BRANCH_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Product PRODUCT = Product.builder().id(PRODUCT_ID).branchId(BRANCH_ID).name("Espresso").stock(3).build();

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RenameProductUseCase useCase;

    @Test
    void shouldSaveOnlyTheNewNameWhenNameIsAvailableInTheBranch() {
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(PRODUCT));
        when(productRepository.findByBranchIdAndName(BRANCH_ID, "Double Espresso")).thenReturn(Mono.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Double Espresso"))
                .expectNext(PRODUCT.toBuilder().name("Double Espresso").build())
                .verifyComplete();
    }

    @Test
    void shouldAllowRenamingWhenTheOnlyMatchIsTheSameProduct() {
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(PRODUCT));
        when(productRepository.findByBranchIdAndName(BRANCH_ID, "espresso")).thenReturn(Mono.just(PRODUCT));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "espresso"))
                .expectNext(PRODUCT.toBuilder().name("espresso").build())
                .verifyComplete();
    }

    @Test
    void shouldFailWithNameTakenWhenAnotherProductOfTheBranchHasTheName() {
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(PRODUCT));
        when(productRepository.findByBranchIdAndName(BRANCH_ID, "Latte"))
                .thenReturn(Mono.just(Product.builder().id(101L).branchId(BRANCH_ID).name("Latte").stock(8).build()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Latte"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.PRODUCT_NAME_TAKEN))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithNotFoundWhenProductIsNotInTheBranch() {
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Latte"))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.PRODUCT_NOT_FOUND))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithInvalidNameWithoutQueryingTheRepositoryWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "  "))
                .expectErrorSatisfies(error -> assertBusinessError(error, BusinessError.INVALID_NAME))
                .verify();

        verifyNoInteractions(productRepository);
    }

    private static void assertBusinessError(Throwable error, BusinessError expected) {
        assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getError()).isEqualTo(expected));
    }
}
