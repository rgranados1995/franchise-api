package co.com.franchise.usecase.gettopstockproducts;

import co.com.franchise.model.branch.Branch;
import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.exception.BusinessError;
import co.com.franchise.model.exception.BusinessException;
import co.com.franchise.model.franchise.Franchise;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.product.BranchTopProduct;
import co.com.franchise.model.product.Product;
import co.com.franchise.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsUseCaseTest {

    private static final Long FRANCHISE_ID = 1L;
    private static final Franchise FRANCHISE = Franchise.builder().id(FRANCHISE_ID).name("Coffee House").build();
    private static final Branch DOWNTOWN = Branch.builder().id(10L).franchiseId(FRANCHISE_ID).name("Downtown").build();
    private static final Branch UPTOWN = Branch.builder().id(11L).franchiseId(FRANCHISE_ID).name("Uptown").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetTopStockProductsUseCase useCase;

    @Test
    void shouldReturnTheHighestStockProductOfEachBranch() {
        Product downtownTop = product(101L, DOWNTOWN, 20);
        Product uptownTop = product(201L, UPTOWN, 9);
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(DOWNTOWN, UPTOWN));
        when(productRepository.findByBranchId(DOWNTOWN.getId()))
                .thenReturn(Flux.just(product(100L, DOWNTOWN, 5), downtownTop, product(102L, DOWNTOWN, 7)));
        when(productRepository.findByBranchId(UPTOWN.getId())).thenReturn(Flux.just(product(200L, UPTOWN, 3), uptownTop));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(topProduct(DOWNTOWN, downtownTop))
                .expectNext(topProduct(UPTOWN, uptownTop))
                .verifyComplete();
    }

    @Test
    void shouldPickTheLowestProductIdWhenStocksAreTied() {
        Product oldestTied = product(103L, DOWNTOWN, 10);
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(DOWNTOWN));
        when(productRepository.findByBranchId(DOWNTOWN.getId()))
                .thenReturn(Flux.just(product(105L, DOWNTOWN, 10), oldestTied, product(108L, DOWNTOWN, 2)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(topProduct(DOWNTOWN, oldestTied))
                .verifyComplete();
    }

    @Test
    void shouldKeepBranchOrderWhenAnEarlierBranchRespondsLater() {
        Product downtownTop = product(101L, DOWNTOWN, 20);
        Product uptownTop = product(201L, UPTOWN, 9);
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(DOWNTOWN, UPTOWN));
        when(productRepository.findByBranchId(DOWNTOWN.getId()))
                .thenReturn(Flux.just(downtownTop).delayElements(Duration.ofMillis(100)));
        when(productRepository.findByBranchId(UPTOWN.getId())).thenReturn(Flux.just(uptownTop));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(topProduct(DOWNTOWN, downtownTop))
                .expectNext(topProduct(UPTOWN, uptownTop))
                .verifyComplete();
    }

    @Test
    void shouldLeaveOutBranchesWithoutProducts() {
        Product uptownTop = product(201L, UPTOWN, 9);
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(DOWNTOWN, UPTOWN));
        when(productRepository.findByBranchId(DOWNTOWN.getId())).thenReturn(Flux.empty());
        when(productRepository.findByBranchId(UPTOWN.getId())).thenReturn(Flux.just(uptownTop));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(topProduct(UPTOWN, uptownTop))
                .verifyComplete();
    }

    @Test
    void shouldCompleteEmptyWhenFranchiseHasNoBranches() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(FRANCHISE));
        when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldFailWithNotFoundWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getError()).isEqualTo(BusinessError.FRANCHISE_NOT_FOUND)))
                .verify();

        verifyNoInteractions(branchRepository, productRepository);
    }

    private static Product product(Long id, Branch branch, int stock) {
        return Product.builder().id(id).branchId(branch.getId()).name("Product " + id).stock(stock).build();
    }

    private static BranchTopProduct topProduct(Branch branch, Product product) {
        return BranchTopProduct.builder().branch(branch).product(product).build();
    }
}
