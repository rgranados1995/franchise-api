package co.com.franchise.config;

import co.com.franchise.model.branch.gateways.BranchRepository;
import co.com.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.franchise.model.product.gateways.ProductRepository;
import co.com.franchise.usecase.addbranch.AddBranchUseCase;
import co.com.franchise.usecase.addproduct.AddProductUseCase;
import co.com.franchise.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.franchise.usecase.gettopstockproducts.GetTopStockProductsUseCase;
import co.com.franchise.usecase.removeproduct.RemoveProductUseCase;
import co.com.franchise.usecase.renamebranch.RenameBranchUseCase;
import co.com.franchise.usecase.renamefranchise.RenameFranchiseUseCase;
import co.com.franchise.usecase.renameproduct.RenameProductUseCase;
import co.com.franchise.usecase.updateproductstock.UpdateProductStockUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(UseCasesConfig.class)
            .withBean(FranchiseRepository.class, () -> mock(FranchiseRepository.class))
            .withBean(BranchRepository.class, () -> mock(BranchRepository.class))
            .withBean(ProductRepository.class, () -> mock(ProductRepository.class));

    @Test
    void shouldRegisterEveryUseCaseWhenTheirGatewaysAreAvailable() {
        contextRunner.run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(CreateFranchiseUseCase.class)
                .hasSingleBean(RenameFranchiseUseCase.class)
                .hasSingleBean(AddBranchUseCase.class)
                .hasSingleBean(RenameBranchUseCase.class)
                .hasSingleBean(AddProductUseCase.class)
                .hasSingleBean(RemoveProductUseCase.class)
                .hasSingleBean(UpdateProductStockUseCase.class)
                .hasSingleBean(RenameProductUseCase.class)
                .hasSingleBean(GetTopStockProductsUseCase.class));
    }
}
