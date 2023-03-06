package volvocars.wearable_monitor.architecture

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(packages = ["com.volvocars.wearable_monitor"])
internal class DomainLayerTest {

    @ArchTest
    val `higher-level classes should not depend on the framework` =
        noClasses().that().resideInAnyPackage(
            "..domain.."
        ).should().dependOnClassesThat().resideInAnyPackage(
            "android..",
            "androidx..",
            "com.google.android..",
            "com.google.firebase.."
        )

    @ArchTest
    val `no one should ever name fields like this anymore` =
        noFields().should().haveNameMatching("m[A-Z]+.*")
}