package starter;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Kiểm định quy tắc phụ thuộc Clean Architecture bằng ArchUnit.
 * Đảm bảo các tầng bên trong không bao giờ phụ thuộc các tầng bên ngoài.
 */
@AnalyzeClasses(packages = "starter", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule domainMustNotDependOnOuterLayers =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("..domain..", "java..");

    @ArchTest
    public static final ArchRule useCasesMustOnlyDependOnDomainAndUsecase =
            classes().that().resideInAnyPackage("..dathang.usecase..", "..thanhtoan.usecase..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("..domain..", "..dathang.usecase..", "..thanhtoan.usecase..", "java..");

    @ArchTest
    public static final ArchRule adaptersMustNotDependOnInfrastructureOrApp =
            noClasses().that().resideInAnyPackage("..dathang.adapter..", "..thanhtoan.adapter..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "..app..");

    @ArchTest
    public static final ArchRule cleanArchitectureLayersRule =
            layeredArchitecture().consideringAllDependencies()
                    .layer("Domain").definedBy("starter.domain..")
                    .layer("UseCase").definedBy("starter.dathang.usecase..", "starter.thanhtoan.usecase..")
                    .layer("Adapter").definedBy("starter.dathang.adapter..", "starter.thanhtoan.adapter..")
                    .layer("Infrastructure").definedBy("starter.infrastructure..")
                    .layer("App").definedBy("starter.app..")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("UseCase", "Adapter", "Infrastructure", "App")
                    .whereLayer("UseCase").mayOnlyBeAccessedByLayers("Adapter", "Infrastructure", "App")
                    .whereLayer("Adapter").mayOnlyBeAccessedByLayers("App");
}
