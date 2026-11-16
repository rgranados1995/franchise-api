package co.com.franchise.usecase.removeproduct;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveProductUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Long BRANCH_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RemoveProductUseCase useCase;

    @Test
    void shouldDeleteProductWhenItBelongsToTheBranchAndFranchise() {
        Product product = Product.builder().id(PRODUCT_ID).branchId(BRANCH_ID).name("Espresso").stock(3).build();
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(product));
        when(productRepository.deleteById(PRODUCT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .verifyComplete();

        verify(productRepository).deleteById(PRODUCT_ID);
    }

    @Test
    void shouldFailWithNotFoundAndDeleteNothingWhenProductIsNotInTheBranch() {
        when(productRepository.findByIdAndBranchIdAndFranchiseId(PRODUCT_ID, BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getError()).isEqualTo(BusinessError.PRODUCT_NOT_FOUND)))
                .verify();

        verify(productRepository, never()).deleteById(any());
    }
}
